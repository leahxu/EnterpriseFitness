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

public class RawDBWriterBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private OutputCollector _collector;

	@Override
	public void prepare(Map conf, TopologyContext context,
			OutputCollector collector) {
		_collector = collector;
	}

	@Override
	public void execute(Tuple tuple) {
		String deviceId = tuple.getString(0);
		String companyId = tuple.getString(1);
		String date = tuple.getString(2);
		String time = tuple.getString(3);
		double calorie = tuple.getDouble(4);
		double distance = tuple.getDouble(5);
		double runStep = tuple.getDouble(6);
		double totalStep = tuple.getDouble(7);
		double walkStep = tuple.getDouble(8);
		double deltaCalorie = tuple.getDouble(9);
		double deltaDistance = tuple.getDouble(10);
		double deltaRunStep = tuple.getDouble(11);
		double deltaTotalStep = tuple.getDouble(12);
		double deltaWalkStep = tuple.getDouble(13);
		
		String sqlQuery = "INSERT INTO [dbo].[Raw]"
				+ "([deviceId],[companyId],[timestamp],[calorie],[distance],"
				+ "[runStep],[totalStep],[walkStep],[deltaCalorie],"
				+ "[deltaDistance],[deltaRunStep],[deltaTotalStep],"
				+ "[deltaWalkStep]) VALUES ('"
				+ deviceId + "','"
				+ companyId + "','"
				+ date + " "
				+ time + "',"
				+ calorie + ","
				+ distance + ","
				+ runStep + ","
				+ totalStep + ","
				+ walkStep + ","
				+ deltaCalorie + ","
				+ deltaDistance + ","
				+ deltaRunStep + ","
				+ deltaTotalStep + "," + deltaWalkStep + ")";

		connectDB(sqlQuery);
		
		_collector.ack(tuple);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("DBWriter"));
	}

	public static void connectDB(String sqlQuery) {
		Properties prop = new Properties();
		try {
			prop.load(RawDBWriterBolt.class.getClassLoader().getResourceAsStream(
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
			//e.printStackTrace();
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
}