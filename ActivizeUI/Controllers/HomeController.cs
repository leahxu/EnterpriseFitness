using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.Entity.Core.Metadata.Edm;
using System.Globalization;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Data;
using System.Data.SqlClient;


namespace Bootstrap.Controllers
{
    public class HomeController : Controller
    {
        public static string[,] leaderboard = new string[20,6];

        public ActionResult Index()
        {
            initializeLeaderboard();
            return View();
        }

        public void initializeLeaderboard()
        {
            for (int i = 0; i < 20; i++)
            {
                for (int j = 0; j < 6; j++)
                {
                    leaderboard[i, j] = "";
                }
            }
        }

        public ActionResult About()
        {
            ViewBag.Message = "Your application description page.";

            return View();
        }

        public ActionResult Contact()
        {
            ViewBag.Message = "Your contact page.";

            return View();
        }

        public ActionResult UserInfo()
        {
            return View("~/Views/User/User.cshtml");
        }

        public static void getLeaderboard()
        {
            deleteAverages(); //make sure table is cleared.

            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);
            sqlConnection.Open();
            string query = String.Format("SELECT [timestamp], [companyId], [totalStep], [runStep], [walkStep], [calories], [distance] " +
                                         "FROM [dbo].[Leaderboard] ORDER BY [timestamp] DESC, [totalStep] DESC");
            SqlCommand queryCommand = new SqlCommand(query, sqlConnection);

            SqlDataReader queryReader = queryCommand.ExecuteReader();
            DataTable dataTable = new DataTable();
            dataTable.Load(queryReader);

            string date = "";
            bool sameDate = true;
            double runStep=0, walkStep=0, totalStep=0, calories=0, distance=0;
            for (int i = 0; i < 20; i++)
            {
                int count = 0;
                string company = "";

                try
                {
                    foreach (DataColumn col in dataTable.Columns)
                    {
                        if (count == 0)
                        {
                            if (i == 0)
                            {
                                date += dataTable.Rows[i][col.ColumnName];
                            }
                            else
                            {
                                sameDate = (date == "" + dataTable.Rows[i][col.ColumnName]);
                            }
                        }
                        else if (sameDate)
                        {
                            switch (count)
                            {
                                case 1:
                                    company += dataTable.Rows[i][col.ColumnName];
                                    break;
                                case 2:
                                    totalStep =
                                        Math.Round(
                                            Double.Parse("" + dataTable.Rows[i][col.ColumnName])/
                                            CompanyController.teamNumber(company), 2);
                                    break;
                                case 3:
                                    runStep =
                                        Math.Round(
                                            Double.Parse("" + dataTable.Rows[i][col.ColumnName])/
                                            CompanyController.teamNumber(company), 2);
                                    break;
                                case 4:
                                    walkStep =
                                        Math.Round(
                                            Double.Parse("" + dataTable.Rows[i][col.ColumnName])/
                                            CompanyController.teamNumber(company), 2);
                                    break;
                                case 5:
                                    calories =
                                        Math.Round(
                                            Double.Parse("" + dataTable.Rows[i][col.ColumnName])/
                                            CompanyController.teamNumber(company), 2);
                                    break;
                                case 6:
                                    distance =
                                        Math.Round(
                                            Double.Parse("" + dataTable.Rows[i][col.ColumnName])/
                                            CompanyController.teamNumber(company), 2);
                                    break;
                            }
                        }
                        count++;
                    }
                }
                catch 
                {
                    company = "";
                    totalStep = 0;
                    runStep = 0;
                    walkStep = 0;
                    calories = 0;
                    distance = 0;
                }
                
                if (sameDate)
                {
                    SqlConnection sqlConnection2 = new SqlConnection(conString);
                    sqlConnection2.Open();
                    SqlCommand query2 = sqlConnection2.CreateCommand();
                    query2.CommandText =
                        String.Format(
                            "INSERT INTO [dbo].[Averages] ([companyId], [totalStep], [runStep], [walkStep], [calories], [distance]) " +
                            "VALUES ('{0}', {1}, {2}, {3}, {4}, {5})", company, totalStep, runStep, walkStep, calories, distance);
                    query2.ExecuteNonQuery();
                }
                
            }
            sqlConnection.Close();

            averages();
        }

        public static void deleteAverages()
        {
            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection3 = new SqlConnection(conString);
            sqlConnection3.Open();
            SqlCommand query3 = sqlConnection3.CreateCommand();
            query3.CommandText = "DELETE FROM [dbo].[Averages]";
            query3.ExecuteNonQuery();
        }

        public static void averages()
        {
            ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
            if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
            {
                throw new Exception("Fatal error: missing connection string in web.config file");
            }
            string conString = mySetting.ConnectionString;

            SqlConnection sqlConnection = new SqlConnection(conString);
            sqlConnection.Open();
            string counting = "SELECT COUNT(*) AS \"Number\" FROM [dbo].[Averages]";
            SqlCommand queryCounting = new SqlCommand(counting, sqlConnection);
            SqlDataReader queryCounter = queryCounting.ExecuteReader();
            DataTable counted = new DataTable();
            counted.Load(queryCounter);
            int length = int.Parse("" + counted.Rows[0][counted.Columns[0].ColumnName]);
            sqlConnection.Close();

            sqlConnection.Open();
            string query =
                String.Format("SELECT [companyId], [totalStep], [runStep], [walkStep], [calories], [distance] " +
                              "FROM [dbo].[Averages] ORDER BY [totalStep] DESC");
            SqlCommand queryCommand = new SqlCommand(query, sqlConnection);
            SqlDataReader queryReader = queryCommand.ExecuteReader();
            DataTable dataTable = new DataTable();
            dataTable.Load(queryReader);
            
            for (int i = 0; i < length; i++)
            {
                int count = 0;
                foreach (DataColumn col in dataTable.Columns)
                {
                    try
                    {
                        if (count == 0)
                        {
                            leaderboard[i, count] += dataTable.Rows[i][col.ColumnName];
                        }
                        else if (count == 4 || count == 5)
                        {
                            leaderboard[i, count] += Math.Round(Double.Parse("" + dataTable.Rows[i][col.ColumnName]), 2);
                        }
                        else
                        {
                            leaderboard[i, count] += Math.Round(Double.Parse("" + dataTable.Rows[i][col.ColumnName]), 0);
                        }
                    }
                    catch
                    {
                        leaderboard[i, count] += "";
                    }
                    count++;
                }
            }
            sqlConnection.Close();
        }
    }
}