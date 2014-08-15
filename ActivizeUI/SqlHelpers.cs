// ---------------------------------------------------------------------------- 
// Copyright (c) Microsoft Corporation. All rights reserved.
// ---------------------------------------------------------------------------- 

using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Diagnostics.CodeAnalysis;
using System.Globalization;
using System.Threading;
using Newtonsoft.Json.Linq;
using System.Data;

namespace Microsoft.Azure.Zumo.Test.Helper
{
    // SqlErrorCode enum is duplicated/copied code from Resource provider as we removed the reference to that dll
    // This was done since the deployment tools need to be targetted to .NET4.0 and product code is already targetting .NET4.5 
    public enum SqlErrorCode
    {
        None = 0,
        DatabaseExists = 1801,
        UserExists = 15023,
        ServerPrincipalExists = 15025,
        [SuppressMessage("Microsoft.Naming", "CA1726:UsePreferredTerms", Justification = "Login is the right term here")]
        CannotDropLogin = 15151,
        ObjectExists = 2714,
        [SuppressMessage("Microsoft.Naming", "CA1726:UsePreferredTerms", Justification = "Login is the right term here")]
        LoginFailed = 4060,
        InvalidColumnNameErrorCode = 207,
        ObjectDoesNotExist = 208,
        CannotDropSinceObjectDoesNotExist = 3701,
        CannotDropColumnDoesNotExist = 4924,
        CannotDropColumnItHasDependencies = 5074,
        MissingColumn = 1911,
        IndexExists = 1913,
        CannotCreateIndexOnBitColumns = 1901,
        CannotCreateIndexOnTextColumns = 1919,
        StringOrBinaryDataWouldBeTruncated = 8152,
        MissingApplicationMetadata = 50001,
        MissingCollectionMetadata = 50002,
        UpdatedMultipleEntriesOnlyOneWasSupposedToBeUpdated = 50003,
        ApplicationScriptParentNotFound = 50004
    }

    public static class SqlHelpers
    {
        private const short MaxTries = 3;
        private const int SqlCommandTimeoutInSeconds = 90;
        /// <summary>
        /// Creates and opens a SQLConnection using the specified database domain, database name, user id, and user password.  
        /// Logs the connection string based on the specified flag.
        /// </summary>
        /// <param name="databaseDomain">The database domain/URI.</param>
        /// <param name="databaseName">The name of the database to connect to.</param>
        /// <param name="userId">The user to connect as.</param>
        /// <param name="userPassword">The password for the user.</param>
        /// <param name="logConnectionString">True to log the connection string to MDS.  False otherwise.</param>
        /// <returns>The opened SQL connection.</returns>
        public static SqlConnection CreateAndOpenConnection(string databaseDomain, string databaseName, string userId, string userPassword, bool logConnectionString = true, int timeout = 0)
        {
            return RetryExecute<SqlConnection>(() =>
            {
                var connectionString = GetConnectionString(databaseDomain, databaseName, userId, userPassword, timeout);

                if (logConnectionString)
                {
                    Console.WriteLine("CreateAndOpenConnection with {0}", connectionString);
                }
                else
                {
                    Console.WriteLine("CreateAndOpenConnection with connection string.");
                }

                var sqlConnection = new SqlConnection(connectionString);

                try
                {
                    sqlConnection.Open();

                    return sqlConnection;
                }
                catch
                {
                    if (sqlConnection != null)
                    {
                        sqlConnection.Dispose();
                    }

                    throw;
                }
            });
        }

        public static string GetConnectionString(string databaseDomain, string databaseName, string userId, string userPassword, int timeout = 0)
        {
            var sqlConnectionStringBuilder = new SqlConnectionStringBuilder();
            sqlConnectionStringBuilder.DataSource = databaseDomain;
            sqlConnectionStringBuilder.InitialCatalog = databaseName;
            sqlConnectionStringBuilder.AsynchronousProcessing = true;
            sqlConnectionStringBuilder.PersistSecurityInfo = true;
            sqlConnectionStringBuilder.UserID = userId;
            sqlConnectionStringBuilder.Password = userPassword;

            if (timeout > 0)
            {
                sqlConnectionStringBuilder.ConnectTimeout = timeout;
            }
            return sqlConnectionStringBuilder.ConnectionString;
        }

        public static void CreateDatabase(string databaseDomain, string databaseName, string userId, string userPassword)
        {
            Console.WriteLine("Creating database: {0}, {1}, {2}", databaseDomain, databaseName, userId);

            RetryExecute<object>(() =>
            {
                using (SqlConnection sqlConnection = CreateAndOpenConnection(databaseDomain, "master", userId, userPassword))
                {
                    try
                    {
                        ExecuteSqlCommand(sqlConnection, "CREATE DATABASE [{0}];", databaseName);
                        return null;
                    }
                    catch (SqlException ex)
                    {
                        if (ex.Number == (int)SqlErrorCode.DatabaseExists)
                        {
                            // Ignore an error where the database already exists.
                            return null;
                        }

                        throw;
                    }
                }
            });
        }

        public static void ExecuteSqlCommand(SqlConnection sqlConnection, string sqlCommandFormat, params object[] parameters)
        {
            string sqlCommand = string.Format(CultureInfo.InvariantCulture, sqlCommandFormat, parameters);

            Console.WriteLine("Executing {0}", sqlCommand);
            using (SqlCommand myCommand = new SqlCommand(sqlCommand, sqlConnection))
            {
                myCommand.CommandTimeout = SqlCommandTimeoutInSeconds;
                myCommand.ExecuteNonQuery();
            }
        }

        public static JArray ExecuteSqlCommandWithResults(SqlConnection sqlConnection, string sqlCommandFormat, params object[] parameters)
        {
            string sqlCommand = string.Format(CultureInfo.InvariantCulture, sqlCommandFormat, parameters);

            return ExecuteSqlCommandWithResults(sqlConnection, sqlCommand, null);
        }

        public static JArray ExecuteSqlCommandWithResults(SqlConnection sqlConnection, string sqlCommand, params SqlParameter[] parameters)
        {
            if (sqlConnection.State != ConnectionState.Open)
            {
                sqlConnection.Open();
            }

            Console.WriteLine("Executing {0}", sqlCommand);

            using (SqlCommand myCommand = new SqlCommand(sqlCommand, sqlConnection))
            {
                myCommand.CommandTimeout = SqlCommandTimeoutInSeconds;
                if (parameters != null && parameters.Length > 0)
                {
                    myCommand.Parameters.AddRange(parameters);
                }

                using (SqlDataReader reader = myCommand.ExecuteReader())
                {
                    JArray results = new JArray();
                    while (reader.Read())
                    {
                        JObject item = new JObject();
                        for (int i = 0; i < reader.FieldCount; i++)
                        {
                            item.Add(reader.GetName(i), new JValue(reader.GetValue(i)));
                        }
                        results.Add(item);
                    }

                    return results;
                }
            }
        }

        public static List<TResult> ExecuteSqlCommandWithResults<TResult>(Func<SqlDataReader, TResult> initializer, SqlConnection sqlConnection, string sqlCommandFormat, params object[] parameters)
        {
            string sqlCommand = string.Format(CultureInfo.InvariantCulture, sqlCommandFormat, parameters);
            return ExecuteSqlCommandWithResults<TResult>(initializer, sqlConnection, sqlCommand, null);
        }

        public static List<TResult> ExecuteSqlCommandWithResults<TResult>(Func<SqlDataReader, TResult> initializer, SqlConnection sqlConnection, string sqlCommand, params SqlParameter[] parameters)
        {
            if (sqlConnection.State != ConnectionState.Open)
            {
                sqlConnection.Open();
            }

            Console.WriteLine("Executing {0}", sqlCommand);

            using (SqlCommand myCommand = new SqlCommand(sqlCommand, sqlConnection))
            {
                myCommand.CommandTimeout = SqlCommandTimeoutInSeconds;
                if (parameters != null && parameters.Length > 0)
                {
                    myCommand.Parameters.AddRange(parameters);
                }

                using (SqlDataReader reader = myCommand.ExecuteReader())
                {
                    List<TResult> results = new List<TResult>();
                    while (reader.Read())
                    {
                        TResult item = initializer(reader);
                        results.Add(item);
                    }

                    return results;
                }
            }
        }

        public static DataSet ExecuteQuery(string connectionString, string commandText, SqlParameter[] parameters = null)
        {
            using (SqlConnection conn = new SqlConnection(connectionString))
            {
                using (SqlDataAdapter adapter = new SqlDataAdapter(commandText, conn))
                {
                    adapter.SelectCommand.CommandTimeout = SqlCommandTimeoutInSeconds;
                    if (parameters != null)
                    {
                        adapter.SelectCommand.Parameters.AddRange(parameters);
                    }
                    DataSet ds = new DataSet();
                    adapter.Fill(ds);
                    return ds;
                }
            }
        }

        public static TResult RetryExecute<TResult>(Func<TResult> func)
        {
            var tries = 0;
            while (tries < MaxTries)
            {
                tries++;
                try
                {
                    return func();
                }
                catch (SqlException ex)
                {
                    if (tries >= MaxTries)
                    {
                        Console.WriteLine("Max tries reached for error in ExecuteSqlCommand\nError: " + ex);
                        throw;
                    }

                    Console.WriteLine("Error in ExecuteSqlCommand on try number {0}\nError: {1}", tries, ex);

                    Thread.Sleep(5000);
                }
            }

            throw new InvalidOperationException("Unreachable code");
        }

        public static void DeleteDatabase(string databaseDomain, string databaseName, string userId, string userPassword)
        {
            Console.WriteLine("Deleting database: {0}, {1}, {2}", databaseDomain, databaseName, userId);

            RetryExecute<object>(() =>
            {
                try
                {
                    using (SqlConnection sqlConnection = CreateAndOpenConnection(databaseDomain, "master", userId, userPassword))
                    {
                        ExecuteSqlCommand(sqlConnection, "DROP DATABASE [{0}];", databaseName);
                    }
                }
                catch (SqlException ex)
                {
                    Console.WriteLine("Failed to delete the database: " + ex);
                }
                catch (InvalidOperationException ex)
                {
                    Console.WriteLine("Failed to delete the database: " + ex);
                }

                return null;
            });
        }

        public static void DeleteDatabaseByPrefix(string databaseNamePrefix, string databaseDomain, string userId, string userPassword)
        {
            JArray databases = GetDatabaseNamesByPrefix(databaseNamePrefix, databaseDomain, userId, userPassword);
            if (databases != null)
            {
                try
                {
                    foreach (var result in databases)
                    {
                        DeleteDatabase(databaseDomain, (string)result["name"], userId, userPassword);
                    }
                }
                catch (SqlException ex)
                {
                    Console.WriteLine("Failed to delete the database: " + ex);
                }
            }
        }

        public static JArray GetDatabaseNamesByPrefix(string databaseNamePrefix, string databaseDomain, string userId, string userPassword)
        {
            try
            {
                using (SqlConnection sqlConnection = CreateAndOpenConnection(databaseDomain, "master", userId, userPassword))
                {
                    JArray results = ExecuteSqlCommandWithResults(sqlConnection, "SELECT name FROM sys.databases WHERE name like '{0}%'", databaseNamePrefix);
                    return results;
                }
            }
            catch (SqlException ex)
            {
                Console.WriteLine("Failed to delete the database: " + ex);
                return null;
            }
        }

        public static void AddCustomColumnsToTable(string databaseDomain, string databaseName, string userId, string userPassword, string mobileServiceName, string tableName)
        {
            Console.WriteLine("Add custom columns to table: {0}, {1}, {2}, {3}", databaseDomain, databaseName, userId, tableName);

            RetryExecute<object>(() =>
            {
                using (SqlConnection sqlConnection = CreateAndOpenConnection(databaseDomain, databaseName, userId, userPassword))
                {
                    const string sqlQuery = @"
                        ALTER TABLE [{0}].[{1}] ADD Geog Geography;
                        ALTER TABLE [{0}].[{1}] ADD Geom Geometry;
                        ALTER TABLE [{0}].[{1}] ADD Img Image;
                        ALTER TABLE [{0}].[{1}] ADD SmallMon Smallmoney;
                        ALTER TABLE [{0}].[{1}] ADD Bin Varbinary;
                        ALTER TABLE [{0}].[{1}] ADD Xml Xml;
                        ALTER TABLE [{0}].[{1}] ADD Txt Text;
                        ALTER TABLE [{0}].[{1}] ADD UniqueId Uniqueidentifier;
                        ALTER TABLE [{0}].[{1}] ADD Num numeric;
                        ALTER TABLE [{0}].[{1}] ADD HierId hierarchyid;
                        ALTER TABLE [{0}].[{1}] ADD SqlVar sql_variant;
                    ";

                    ExecuteSqlCommand(sqlConnection, sqlQuery, SqlHelpers.GetSchemaName(mobileServiceName), tableName);
                    return null;
                }
            });
        }

        internal static string GetSchemaName(string appName)
        {
            return appName != null ? appName.Replace('-', '_') : null;
        }
    }
}
