package storm.starter.bolt;

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

public class DBWriterBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {

		// Define the SQL string.
		String sqlString = "INSERT INTO "
				+ "[ActivizeDB].[dbo].[Monthly]([Metric],[Avg Values],[Timestamp],[Company]) "
				+ "VALUES ('string',1234,'7/23/2014 11:04:00','Microsoft')";

		loadPropertiesDB(sqlString);

		collector.emit(new Values("DB"));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("DBWriter"));
	}

	public static void connectDB(String connectionString, String sqlString) {

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
			statement.executeUpdate(sqlString);

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
				if (null != resultSet)
					resultSet.close();
			} catch (SQLException sqlException) {
				// No additional action if close() statements fail.
			}
		}

	}

	public static void loadPropertiesDB(String sqlString) {
		Properties prop = new Properties();
		try {
			prop.load(DBWriterBolt.class.getClassLoader().getResourceAsStream(
					"config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String connectionString = prop.getProperty("SQL_CONNECTION");
		
		connectDB(connectionString, sqlString);
	}
}
