using System;
using System.Collections.Generic;
using System.Data;
using System.Globalization;
using System.Data.SqlClient;
using System.Linq;
using System.Threading.Tasks;
using System.Web;
using System.Web.Mvc;
using Bootstrap.Models;
using Microsoft.AspNet.Identity;
using Microsoft.AspNet.Identity.EntityFramework;
using System.Configuration;

namespace Bootstrap.Controllers
{
    public class GoalController : Controller
    {
        //
        // GET: /Goal/
        public ActionResult Goals()
        {
            return View();
        }

        public ActionResult ViewGoals()
        {
            ViewBag.Message = "Your Goals:";
            return View();
        }

        public UserManager<ApplicationUser> UserManager { get; private set; }

        [Authorize]
        [HttpPost]
        public ActionResult Goals(GoalViewModel model)
        {
            if (ModelState.IsValid)
            {
                string conString;

                ActionResult action;

                ConnectionStringSettings mySetting = ConfigurationManager.ConnectionStrings["DefaultConnection"];
                if (mySetting == null || string.IsNullOrEmpty(mySetting.ConnectionString))
                {
                    throw new Exception("Fatal error: missing connection string in web.config file");
                }
                conString = mySetting.ConnectionString;

                SqlConnection sqlConnection = new SqlConnection(conString);
                
                
                String id = getID(User.Identity.GetUserName());
                Double stepGoal = model.TotalStepGoal;
                Double calGoal = model.CalGoal;
                Double walkGoal = model.WalkStepGoal;
                Double runGoal = model.RunStepGoal;
                Double distGoal = model.DistGoal;
                SqlCommand cmd = sqlConnection.CreateCommand();

                try
                {
                    sqlConnection.Open();
                    if (hasGoal(User.Identity.GetUserName()))
                    {
                        cmd.CommandText = String.Format(
                            "UPDATE [dbo].[UserGoals] " +
                            "SET [TotalStepGoal] = {0}, [CalGoal] = {1}, [WalkStepGoal] = {2}, [RunStepGoal] = {3}, [DistGoal] = {4} " +
                            "WHERE [deviceId] = '{5}'", stepGoal, calGoal, walkGoal, distGoal, id);
                    }
                    else
                    {
                        cmd.CommandText = String.Format(
                            "INSERT INTO [dbo].[UserGoals] " +
                            "(deviceId, TotalStepGoal, CalGoal, WalkStepGoal, RunStepGoal, DistGoal) " +
                            "VALUES ('{0}', {1}, {2}, {3}, {4}, {5})", id, stepGoal, calGoal, walkGoal, runGoal, distGoal);
                    }

                    cmd.ExecuteNonQuery();
                    action = RedirectToAction("ViewGoals", "Goal");
                }
                catch (Exception ex)
                {
                    Response.Write(ex.Message);
                    action = RedirectToAction("Index", "Home");
                }
                finally
                {
                    sqlConnection.Close();

                }

                return action;
            }

            // If we got this far, something failed, redisplay form
            return View(model);
        }

        public static string queryGoals(string id, string field)
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
            string query = String.Format("SELECT [{0}] FROM [dbo].[UserGoals] WHERE [deviceId] = '{1}'", field, id);
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
            finally
            {
                sqlConnection.Close();
            }

            double average = Double.Parse(cols);
            if (field == "CalGoal" || field == "DistGoal")
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

        public static string getID(string user)
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

        public static bool hasGoal(string user)
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
            string query = String.Format("SELECT * FROM [dbo].[UserGoals] WHERE [deviceId] = '{0}'", getID(user));
            string cols = String.Empty;
            try
            {
                SqlCommand queryCommand = new SqlCommand(query, sqlConnection);
                SqlDataReader queryReader = queryCommand.ExecuteReader();
                DataTable dataTable = new DataTable();
                dataTable.Load(queryReader);

                cols += dataTable.Rows[0][dataTable.Columns[0].ColumnName];
            }
            catch
            {
            }
            finally
            {
                sqlConnection.Close();
            }

            if (String.IsNullOrEmpty(cols))
            {
                return false;
            }
            else
            {
                return true;
            }
        }

	}
}