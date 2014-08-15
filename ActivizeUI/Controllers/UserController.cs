using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Threading;
using System.Threading.Tasks;
using System.Web.Mvc;
using Microsoft.Azure.Zumo.Test.Helper;
using Newtonsoft.Json.Linq;

namespace Bootstrap.Controllers
{
    public class UserController : Controller
    {
        public static string currentUserID;
        public static string currentUserName;
        private const string SQLsForCustomerQuery = @"
            SELECT
            *
            FROM [dbo].[DailyUser]
            where [timestamp] >= @start AND [timestamp] <= @end
            and [deviceId] = @namespace
            and [metric] = @metric
            order by [timestamp]";

        //
        // GET: /User/
        public new ActionResult User()
        {
            return View();
        }

        public ActionResult DailyTotalSteps()
        {
            ViewBag.Message = "Graph of your daily total steps.";
            return View();
        }

        public ActionResult DailyRunSteps()
        {
            ViewBag.Message = "Graph of your daily steps run.";
            return View();
        }

        public ActionResult DailyCalories()
        {
            ViewBag.Message = "Graph of your daily calories burned.";
            return View();
        }

        public ActionResult DailyWalkSteps()
        {
            ViewBag.Message = "Graph of your daily steps walked.";
            return View();
        }

        public ActionResult DailyDistance()
        {
            ViewBag.Message = "Graph of your daily distance.";
            return View();
        }

        public ActionResult ViewGoals()
        {
            ViewBag.Message = "Your Goals.";

            return View("~/Views/Goal/ViewGoals.cshtml");
        }

        public async Task<string> recurringEvent()
        {
            return "";
        }

        public static string queryField(string name, string field)
        {
            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);
            sqlConnection.Open();
            string query =
                String.Format(
                    "SELECT [value] FROM [dbo].[RTUser] WHERE " + 
                    "[metric] = '{0}' AND [deviceId] = '{1}'", field, getId(name));
            string cols = string.Empty;
            SqlCommand queryCommand = new SqlCommand(query, sqlConnection);
            try
            {
                SqlDataReader queryReader = queryCommand.ExecuteReader();
                DataTable dataTable = new DataTable();
                dataTable.Load(queryReader);

                cols += dataTable.Rows[0][dataTable.Columns[0].ColumnName];
            }
            catch
            {
                cols += "0.00";
            }
            double average = Double.Parse(cols);
            if (field == "Calories" || field == "Distance")
            {
                average = Math.Round(average, 2);
            }
            else
            {
                average = Math.Round(average, 0);
            }
            cols = "" + average;
            
            return cols;
        }

        public static string setUserID(string username)
        {
            currentUserName = username;
            currentUserID = getId(username);
            return "";
        }

        public static string getId(string user)
        {
            string conString;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);
            sqlConnection.Open();
            string query = String.Format("SELECT [deviceId] FROM [dbo].[UserInfo] WHERE [username] = '{0}'", user);
            string cols = string.Empty;
            SqlCommand queryCommand = new SqlCommand(query, sqlConnection);
            try
            {
                SqlDataReader queryReader = queryCommand.ExecuteReader();
                DataTable dataTable = new DataTable();
                dataTable.Load(queryReader);

                cols += dataTable.Rows[0][dataTable.Columns[0].ColumnName];
            }
            catch
            {
                cols += "Not Found";
            }
            finally
            {
                sqlConnection.Close();
            }

            return cols;
        }

        public string getUserName(string id)
        {
            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);

            sqlConnection.Open();
            string query = String.Format("SELECT [username] FROM [dbo].[UserInfo] WHERE [deviceId] = {0}", id);
            string cols = string.Empty;
            SqlCommand queryCommand = new SqlCommand(query, sqlConnection);

            SqlDataReader queryReader = queryCommand.ExecuteReader();
            DataTable dataTable = new DataTable();
            dataTable.Load(queryReader);

            cols += dataTable.Rows[0][dataTable.Columns[0].ColumnName];

            return cols;
        }

        //using string to determine metric yields Internal Server Error. Sticking with separate methods for now.
        public string getStepHistory()
        {
            DateTime date = DateTime.Now;
            TimeSpan ts = new TimeSpan(0, 0, 0, 0);
            DateTime end = date.Date + ts;
            DateTime start = date.AddDays(-40).Date + ts;


            string conString;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            conString = mySetting.ConnectionString;            

            List<NamespaceSQL> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceSQL>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceSQL.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentUserID),
                        new SqlParameter("metric", "TotalStep"));
                });
                sqlConn.Close();
            }

            JArray stepNamespaceArray = new JArray();

            foreach (NamespaceSQL kpi in sqlResult)
            {
                
                JArray stepRecord = new JArray(DateTimeToUnixTimestamp(kpi.timestamp), kpi.TotalStep);

                stepNamespaceArray.Add(stepRecord);
            }

            JArray StepsForNameSpace = new JArray();

            JObject stepNamespaces = new JObject();
            stepNamespaces["name"] = "TotalStep";
            stepNamespaces["data"] = stepNamespaceArray;
            StepsForNameSpace.Add(stepNamespaces);

            JObject stepResult = new JObject();
            stepResult["NamespaceName"] = currentUserName;
            stepResult["ScaleUnit"] = "TotalStep";
            stepResult["NamespaceSQLs"] = StepsForNameSpace;

            return stepResult.ToString();
        }

        public string getCalHistory()
        {
            DateTime date = DateTime.Now;
            TimeSpan ts = new TimeSpan(0, 0, 0, 0);
            DateTime end = date.Date + ts;
            DateTime start = date.AddDays(-40).Date + ts;


            string conString;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            conString = mySetting.ConnectionString;

            List<NamespaceSQL> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceSQL>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceSQL.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentUserID),
                        new SqlParameter("metric", "Calories"));
                });
                sqlConn.Close();
            }

            JArray calNamespaceArray = new JArray();

            foreach (NamespaceSQL kpi in sqlResult)
            {
                JArray calRecord = new JArray(DateTimeToUnixTimestamp(kpi.timestamp), kpi.Calories);

                calNamespaceArray.Add(calRecord);
            }

            JArray CalsForNameSpace = new JArray();

            JObject calNamespaces = new JObject();
            calNamespaces["name"] = "Calories";
            calNamespaces["data"] = calNamespaceArray;
            CalsForNameSpace.Add(calNamespaces);

            JObject calResult = new JObject();
            calResult["NamespaceName"] = currentUserName;
            calResult["ScaleUnit"] = "Calories";
            calResult["NamespaceSQLs"] = CalsForNameSpace;

            return calResult.ToString();
        }

        public string getRunStepHistory()
        {
            DateTime date = DateTime.Now;
            TimeSpan ts = new TimeSpan(0, 0, 0, 0);
            DateTime end = date.Date + ts;
            DateTime start = date.AddDays(-40).Date + ts;


            string conString;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            conString = mySetting.ConnectionString;

            List<NamespaceSQL> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceSQL>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceSQL.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentUserID),
                        new SqlParameter("metric", "RunStep"));
                });
                sqlConn.Close();
            }

            JArray rStepNamespaceArray = new JArray();

            foreach (NamespaceSQL kpi in sqlResult)
            {
                JArray rStepRecord = new JArray(DateTimeToUnixTimestamp(kpi.timestamp), kpi.RunStep);

                rStepNamespaceArray.Add(rStepRecord);
            }

            JArray rStepForNameSpace = new JArray();

            JObject rStepNamespaces = new JObject();
            rStepNamespaces["name"] = "RunStep";
            rStepNamespaces["data"] = rStepNamespaceArray;
            rStepForNameSpace.Add(rStepNamespaces);

            JObject rStepResult = new JObject();
            rStepResult["NamespaceName"] = currentUserName;
            rStepResult["ScaleUnit"] = "RunStep";
            rStepResult["NamespaceSQLs"] = rStepForNameSpace;

            return rStepResult.ToString();
        }

        public string getWalkStepHistory()
        {
            DateTime date = DateTime.Now;
            TimeSpan ts = new TimeSpan(0, 0, 0, 0);
            DateTime end = date.Date + ts;
            DateTime start = date.AddDays(-40).Date + ts;


            string conString;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            conString = mySetting.ConnectionString;

            List<NamespaceSQL> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceSQL>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceSQL.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentUserID),
                        new SqlParameter("metric", "WalkStep"));
                });
                sqlConn.Close();
            }

            JArray wStepNamespaceArray = new JArray();

            foreach (NamespaceSQL kpi in sqlResult)
            {
                JArray wStepRecord = new JArray(DateTimeToUnixTimestamp(kpi.timestamp), kpi.WalkStep);

                wStepNamespaceArray.Add(wStepRecord);
            }

            JArray wStepForNameSpace = new JArray();

            JObject wStepNamespaces = new JObject();
            wStepNamespaces["name"] = "WalkStep";
            wStepNamespaces["data"] = wStepNamespaceArray;
            wStepForNameSpace.Add(wStepNamespaces);

            JObject wStepResult = new JObject();
            wStepResult["NamespaceName"] = currentUserName;
            wStepResult["ScaleUnit"] = "WalkStep";
            wStepResult["NamespaceSQLs"] = wStepForNameSpace;

            return wStepResult.ToString();
        }

        public string getDistHistory()
        {
            DateTime date = DateTime.Now;
            TimeSpan ts = new TimeSpan(0, 0, 0, 0);
            DateTime end = date.Date + ts;
            DateTime start = date.AddDays(-40).Date + ts;


            string conString;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            conString = mySetting.ConnectionString;

            List<NamespaceSQL> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceSQL>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceSQL.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentUserID),
                        new SqlParameter("metric", "Distance"));
                });
                sqlConn.Close();
            }

            JArray distNamespaceArray = new JArray();

            foreach (NamespaceSQL kpi in sqlResult)
            {
                JArray distRecord = new JArray(DateTimeToUnixTimestamp(kpi.timestamp), kpi.Distance);

                distNamespaceArray.Add(distRecord);
            }

            JArray DistanceForNameSpace = new JArray();

            JObject distNamespaces = new JObject();
            distNamespaces["name"] = "Distance";
            distNamespaces["data"] = distNamespaceArray;
            DistanceForNameSpace.Add(distNamespaces);

            JObject distResult = new JObject();
            distResult["NamespaceName"] = currentUserName;
            distResult["ScaleUnit"] = "Distance";
            distResult["NamespaceSQLs"] = DistanceForNameSpace;

            return distResult.ToString();
        }

        public static double DateTimeToUnixTimestamp(DateTime dateTime)
        {
            return (dateTime - new DateTime(1970, 1, 1).ToUniversalTime()).TotalMilliseconds;
        }


    }

    class NamespaceSQL
    {
        public DateTime timestamp { get; set; }
        public string deviceId { get; set; }
        public double RunStep { get; set; }
        public double WalkStep { get; set; }
        public double TotalStep { get; set; }
        public double Calories { get; set; }
        public double Distance { get; set; }

        public static NamespaceSQL Initializer(SqlDataReader reader)
        {
            NamespaceSQL namespaceSQL = new NamespaceSQL();

            for (int i = 0; i < reader.FieldCount; i++)
            {
                switch (reader.GetName(i))
                {
                    case "timestamp": namespaceSQL.timestamp = (DateTime)reader[i];
                        break;
                    case "deviceId": namespaceSQL.deviceId = reader[i].ToString();
                        break;
                    case "metric":
                        switch (reader[i].ToString())
                        {
                            case "Calories": namespaceSQL.Calories = Convert.ToDouble(reader[++i]);
                                break;
                            case "Distance": namespaceSQL.Distance = Convert.ToDouble(reader[++i]);
                                break;
                            case "RunStep": namespaceSQL.RunStep = Convert.ToDouble(reader[++i]);
                                break;
                            case "TotalStep": namespaceSQL.TotalStep = Convert.ToDouble(reader[++i]);
                                break;
                            case "WalkStep": namespaceSQL.WalkStep = Convert.ToDouble(reader[++i]);
                                break;
                        }
                        break;
                }
            }

            return namespaceSQL;
        }
    }
}