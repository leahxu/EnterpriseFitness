using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data;
using System.Data.SqlClient;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using Microsoft.Azure.Zumo.Test.Helper;
using Newtonsoft.Json.Linq;

namespace Bootstrap.Controllers
{
    public class CompanyController : Controller
    {
        public static string currentCompanyId;
        public static int currentTeamNumber;
        public static bool hasTeam;
        private const string SQLsForCustomerQuery = @"
            SELECT
            *
            FROM [dbo].[DailyCompany]
            where [timestamp] >= @start AND [timestamp] <= @end
            and [companyId] = @namespace
            and [metric] = @metric
            order by [timestamp]";


        //
        // GET: /Company/
        public ActionResult Company()
        {
            ViewBag.Message = "";
            return View();
        }

        public ActionResult DailyCompanyTotalSteps()
        {
            ViewBag.Message = "Graph of daily team average total steps.";
            return View();
        }

        public ActionResult DailyCompanyRunSteps()
        {
            ViewBag.Message = "Graph of daily team average steps run.";
            return View();
        }

        public ActionResult DailyCompanyCalories()
        {
            ViewBag.Message = "Graph of daily team average calories burned.";
            return View();
        }

        public ActionResult DailyCompanyWalkSteps()
        {
            ViewBag.Message = "Graph of daily team average steps walked.";
            return View();
        }

        public ActionResult DailyCompanyDistance()
        {
            ViewBag.Message = "Graph of daily team average distance.";
            return View();
        }

        public static string setCompanyID(string user)
        {
            currentCompanyId = getCompanyId(user);
            return currentCompanyId;
        }

        public static void setTeamNumber()
        {
            currentTeamNumber = teamNumber(currentCompanyId);
        }

        public static int teamNumber(string team)
        {
            int number;

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);
            sqlConnection.Open();
            string query = String.Format("SELECT COUNT(*) AS NumTeam FROM [dbo].[UserInfo] WHERE [companyId] = '{0}'", team);
            string cols = string.Empty;
            SqlCommand queryCommand = new SqlCommand(query, sqlConnection);
            try
            {
                SqlDataReader queryReader = queryCommand.ExecuteReader();
                DataTable dataTable = new DataTable();
                dataTable.Load(queryReader);

                cols += dataTable.Rows[0][dataTable.Columns[0].ColumnName];
                number = int.Parse(cols);
            }
            catch
            {
                number = 1;
            }
            finally
            {
                sqlConnection.Close();
            }
            if (number == 0)
            {
                number = 1;
            }

            return number;
        }

        public static string getCompanyId(string user)
        {
            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);
            sqlConnection.Open();
            string query = String.Format("SELECT [companyId] FROM [dbo].[UserInfo] WHERE [username] = '{0}'", user);
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
                cols += "";
            }
            finally
            {
                sqlConnection.Close();
            }

            return cols;
        }

        public static string queryCompanyByField(string user, string field)
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
                    "SELECT [value] FROM [dbo].[RTCompany] WHERE " + 
                    "[metric] = '{0}' AND [companyId] = '{1}'", field, getCompanyId(user));
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
                cols += "0.0";
            }

            double average = Double.Parse(cols);
            average = average/currentTeamNumber;

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
        public string getCompanyTotalStepHistory()
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

            List<NamespaceCompany> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceCompany>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceCompany.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentCompanyId),
                        new SqlParameter("metric", "TotalStep"));
                });
                sqlConn.Close();
            }

            JArray stepNamespaceArray = new JArray();

            foreach (NamespaceCompany kpi in sqlResult)
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
            stepResult["NamespaceName"] = currentCompanyId;
            stepResult["ScaleUnit"] = "TotalStep";
            stepResult["NamespaceCompanys"] = StepsForNameSpace;

            return stepResult.ToString();
        }

        public string getCompanyCalHistory()
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

            List<NamespaceCompany> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceCompany>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceCompany.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentCompanyId),
                        new SqlParameter("metric", "Calories"));
                });
                sqlConn.Close();
            }

            JArray calNamespaceArray = new JArray();

            foreach (NamespaceCompany kpi in sqlResult)
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
            calResult["NamespaceName"] = currentCompanyId;
            calResult["ScaleUnit"] = "Calories";
            calResult["NamespaceCompanys"] = CalsForNameSpace;

            return calResult.ToString();
        }

        public string getCompanyRunStepHistory()
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

            List<NamespaceCompany> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceCompany>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceCompany.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentCompanyId),
                        new SqlParameter("metric", "RunStep"));
                });
                sqlConn.Close();
            }

            JArray rStepNamespaceArray = new JArray();

            foreach (NamespaceCompany kpi in sqlResult)
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
            rStepResult["NamespaceName"] = currentCompanyId;
            rStepResult["ScaleUnit"] = "RunStep";
            rStepResult["NamespaceCompanys"] = rStepForNameSpace;

            return rStepResult.ToString();
        }

        public string getCompanyWalkStepHistory()
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

            List<NamespaceCompany> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceCompany>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceCompany.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentCompanyId),
                        new SqlParameter("metric", "WalkStep"));
                });
                sqlConn.Close();
            }

            JArray wStepNamespaceArray = new JArray();

            foreach (NamespaceCompany kpi in sqlResult)
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
            wStepResult["NamespaceName"] = currentCompanyId;
            wStepResult["ScaleUnit"] = "WalkStep";
            wStepResult["NamespaceCompanys"] = wStepForNameSpace;

            return wStepResult.ToString();
        }

        public string getCompanyDistHistory()
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

            List<NamespaceCompany> sqlResult;

            using (SqlConnection sqlConn = new SqlConnection(conString))
            {
                sqlResult = SqlHelpers.RetryExecute<List<NamespaceCompany>>(() =>
                {
                    return SqlHelpers.ExecuteSqlCommandWithResults(NamespaceCompany.Initializer,
                        sqlConn,
                        SQLsForCustomerQuery,
                        new SqlParameter("start", start),
                        new SqlParameter("end", end),
                        new SqlParameter("namespace", currentCompanyId),
                        new SqlParameter("metric", "Distance"));
                });
                sqlConn.Close();
            }

            JArray distNamespaceArray = new JArray();

            foreach (NamespaceCompany kpi in sqlResult)
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
            distResult["NamespaceName"] = currentCompanyId;
            distResult["ScaleUnit"] = "Distance";
            distResult["NamespaceCompanys"] = DistanceForNameSpace;

            return distResult.ToString();
        }

        public static double DateTimeToUnixTimestamp(DateTime dateTime)
        {
            return (dateTime - new DateTime(1970, 1, 1).ToUniversalTime()).TotalMilliseconds;
        }


    }

    class NamespaceCompany
    {
        public DateTime timestamp { get; set; }
        public string deviceId { get; set; }
        public double RunStep { get; set; }
        public double WalkStep { get; set; }
        public double TotalStep { get; set; }
        public double Calories { get; set; }
        public double Distance { get; set; }

        public static NamespaceCompany Initializer(SqlDataReader reader)
        {
            NamespaceCompany namespaceCompany = new NamespaceCompany();

            for (int i = 0; i < reader.FieldCount; i++)
            {
                switch (reader.GetName(i))
                {
                    case "timestamp": namespaceCompany.timestamp = (DateTime)reader[i];
                        break;
                    case "deviceId": namespaceCompany.deviceId = reader[i].ToString();
                        break;
                    case "metric":
                        switch (reader[i].ToString())
                        {
                            case "Calories": namespaceCompany.Calories = Convert.ToDouble(reader[++i]);
                                break;
                            case "Distance": namespaceCompany.Distance = Convert.ToDouble(reader[++i]);
                                break;
                            case "RunStep": namespaceCompany.RunStep = Convert.ToDouble(reader[++i]);
                                break;
                            case "TotalStep": namespaceCompany.TotalStep = Convert.ToDouble(reader[++i]);
                                break;
                            case "WalkStep": namespaceCompany.WalkStep = Convert.ToDouble(reader[++i]);
                                break;
                        }
                        break;
                }
            }

            return namespaceCompany;
        }
    }
}