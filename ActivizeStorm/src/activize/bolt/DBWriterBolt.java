package activize.bolt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class DBWriterBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private OutputCollector _collector;

	@Override
	public void prepare(Map conf, TopologyContext context,
			OutputCollector collector) {
		_collector = collector;
	}

	@Override
	public void execute(Tuple tuple) {
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
					
			leaderboardQuery(companyId, date, time, calorie, distance, runStep, walkStep, totalStep);
			
		} else if (table.equals("DailyCompany")) {
			String queryDaily = 
				"INSERT INTO [dbo].[DailyCompany]([companyId],[timestamp],[metric],[value]) " + 
				"VALUES ('" + companyId + "','" + date + " " + time
				+ "','" + metricName + "'," + metricValue + ")";
			
			connectDB(queryDaily);
		} else if (table.equals("User")) {
			for (int i = 0; i < metricName.length; i++) {
				writeUserQuery(deviceId, date, time, metricName[i],
						metricValue[i]);
			}
		} else {
			System.out.println("ERROR: This is not a valid table name!");
		}

		_collector.emit(new Values("Done"));
		_collector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("RTDatabaseWriter"));
	}

	public static void connectDB(String sqlQuery) {

		Properties prop = new Properties();
		try {
			prop.load(DBWriterBolt.class.getClassLoader()
					.getResourceAsStream("config.properties"));
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
			resultSet = statement.executeQuery(sqlQuery);
		}
		// Exception handling
		catch (ClassNotFoundException cnfe) {
			System.out.println("ClassNotFoundException: " + cnfe.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			//e.printStackTrace();
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

	public static void writeUserQuery(String deviceId, String date,
			String time, String metricName, double metricValue) {

		String queryRT = 
				"IF (NOT EXISTS(SELECT * FROM [dbo].[RTUser] WHERE [deviceId] = '"
					+ deviceId + "' AND [metric] = '" + metricName + "')) " + 
				"BEGIN " +
					"INSERT INTO [dbo].[RTUser]([deviceId],[timestamp],[metric],[value]) " + 
					"VALUES ('" + deviceId + "','" + date + " " + time
					+ "','" + metricName + "'," + metricValue + ") " +
				"END " +
				"ELSE " +
				"BEGIN " +
					"UPDATE [dbo].[RTUser] SET [timestamp] = '" + date + " "
					+ time + "', [value] = " + metricValue + "WHERE [deviceId] = '"
					+ deviceId + "' AND [metric] = '" + metricName + "' " + 
				"END";
		
		String queryDaily = 
			"IF (NOT EXISTS(SELECT * FROM [dbo].[DailyUser] WHERE [deviceId] = '"
				+ deviceId + "' AND [metric] = '" + metricName + "' AND [timestamp] = '" + date + "' )) " + 
			"BEGIN " +
				"INSERT INTO [dbo].[DailyUser]([deviceId],[timestamp],[metric],[value]) " + 
				"VALUES ('" + deviceId + "','" + date
				+ "','" + metricName + "'," + metricValue + ") " +
			"END " +
			"ELSE " +
			"BEGIN " +
				"UPDATE [dbo].[DailyUser] SET [value] = " + metricValue + "WHERE [deviceId] = '"
				+ deviceId + "' AND [metric] = '" + metricName + "' AND [timestamp] = '" 
				+ date + "' " +
			"END";

		connectDB(queryRT);
		connectDB(queryDaily);
	}

	public static void writeCompanyQuery(String companyId, String date,
			String time, String metricName, double metricValue) {
		String queryRT = 
			"IF (NOT EXISTS(SELECT * FROM [dbo].[RTCompany] WHERE [companyId] = '"
				+ companyId + "' AND [metric] = '" + metricName + "')) " + 
			"BEGIN " +
				"INSERT INTO [dbo].[RTCompany]([companyId],[timestamp],[metric],[value]) " + 
				"VALUES ('" + companyId + "','" + date + " " + time
				+ "','" + metricName + "'," + metricValue + ") " +
			"END " +
			"ELSE " +
			"BEGIN " +
				"UPDATE [dbo].[RTCompany] SET [timestamp] = '" + date + " "
				+ time + "', [value] = " + metricValue + "WHERE [companyId] = '"
				+ companyId + "' AND [metric] = '" + metricName + "' " + 
			"END";
		
		String queryDaily = 
			"IF (NOT EXISTS(SELECT * FROM [dbo].[DailyCompany] WHERE [companyId] = '"
				+ companyId + "' AND [metric] = '" + metricName + "' AND [timestamp] = '" + date + "' )) " + 
			"BEGIN " +
				"INSERT INTO [dbo].[DailyCompany]([companyId],[timestamp],[metric],[value]) " + 
				"VALUES ('" + companyId + "','" + date 
				+ "','" + metricName + "'," + metricValue + ") " +
			"END " +
			"ELSE " +
			"BEGIN " +
				"UPDATE [dbo].[DailyCompany] SET [value] = " + metricValue + "WHERE [companyId] = '"
				+ companyId + "' AND [metric] = '" + metricName + "' AND [timestamp] = '" 
				+ date + "' " +
			"END";

		connectDB(queryRT);
		connectDB(queryDaily);
	}
	
	public static void leaderboardQuery(String companyId, String date,
			String time, double calorie, double distance, double runStep, 
			double walkStep, double totalStep) {
		
		String query = 
			"IF (NOT EXISTS(SELECT * FROM [dbo].[Leaderboard] WHERE [companyId] = '"
					+ companyId + "')) " + 
			"BEGIN " +
				"INSERT INTO [dbo].[Leaderboard]"
				+ "([timestamp],[companyId],[totalStep],[runStep],[walkStep],[calories],[distance]) VALUES ('"
				+ date + " " + time + "','" + companyId + "'," + totalStep 
				+ "," + runStep + "," + walkStep + "," + calorie + "," + distance + ")" +
			"END " +
			"ELSE " +
			"BEGIN " +
				"UPDATE [dbo].[Leaderboard] SET [timestamp] = '" + date + " "
				+ time + "', [totalStep] = " + totalStep + ", [runStep] = " + runStep
				+ ", [walkStep] = " + walkStep + ", [calories] = " + calorie + ", [distance] = " 
				+ distance + "WHERE [companyId] = '" + companyId + "' " +
			"END";
		
		connectDB(query); 
	}
}