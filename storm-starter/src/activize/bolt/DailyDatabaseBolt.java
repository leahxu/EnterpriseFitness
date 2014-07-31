package activize.bolt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class DailyDatabaseBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String table = tuple.getString(0);
		String deviceId = tuple.getString(1);
		String companyId = tuple.getString(2);
		String date = tuple.getString(3);
		String time = tuple.getString(4);
		double calorie = tuple.getDouble(5);
		double distance = tuple.getDouble(6);
		double runStep = tuple.getDouble(7);
		double totalStep = tuple.getDouble(8);
		double walkStep = tuple.getDouble(9);

		String[] metricName = { "Calories", "Distance", "RunStep", "TotalStep",
				"WalkStep" };
		double[] metricValue = { calorie, distance, runStep, totalStep,
				walkStep };

		if (table.equals("Company")) {
			for (int i = 0; i < metricName.length; i++) {
				writeCompanyQuery(companyId, date, time, metricName[i],
						metricValue[i]);
			}
		} else if (table.equals("User")) {
			for (int i = 0; i < metricName.length; i++) {
				writeUserQuery(deviceId, date, time, metricName[i],
						metricValue[i]);
			}
		} else {
			System.out.println("ERROR: This is not a valid table name!");
		}

		collector.emit(new Values("Done"));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("DailyDatabaseWriter"));
	}

	public static void connectDB(String sqlQuery) {

		Properties prop = new Properties();
		try {
			prop.load(DailyDatabaseBolt.class.getClassLoader().getResourceAsStream(
					"config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String connectionString = prop.getProperty("SQL_CONNECTION");

		Connection connection = null; // For making the connection
		Statement statement = null; // For the SQL statement
		ResultSet resultSet = null; // For the result set

		try {
			// Ensure the SQL Server driver class is available.
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");

			// Establish the connection.
			connection = DriverManager.getConnection(connectionString);

			// Use the connection to create the SQL statement.
			statement = connection.createStatement();

			// Execute the statement.
			statement.executeUpdate(sqlQuery);

		}
		// Exception handling
		catch (ClassNotFoundException cnfe) {
			System.out.println("ClassNotFoundException " + cnfe.getMessage());
		} catch (Exception e) {
			System.out.println("Exception " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				// Close resources.
				if (null != connection)
					connection.close();
				if (null != statement)
					statement.close();
				if (null != resultSet) {
					resultSet.close();
				}

			} catch (SQLException sqlException) {
				// No additional action if close() statements fail.
			}
		}
	}

	public static void writeUserQuery(String deviceId,
			String date, String time, String metricName, double metricValue) {
		String insert = "INSERT INTO [dbo].[DailyUser]"
				+ "([deviceId],[timestamp],[metric],[value]) VALUES ('"
				+ deviceId + "','" + date + " " + time + "','" + metricName
				+ "'," + metricValue + ")";

		connectDB(insert);
	}

	public static void writeCompanyQuery(String companyId,
			String date, String time, String metricName, double metricValue) {
		String sqlQuery = "INSERT INTO [dbo].[DailyCompany]"
				+ "([companyId],[timestamp],[metric],[value]) VALUES ('"
				+ companyId + "','" + date + " " + time + "','" + metricName
				+ "'," + metricValue + ")";

		connectDB(sqlQuery);
	}
}
