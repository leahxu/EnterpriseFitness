package storm.starter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import storm.starter.bolt.MessageReceiverBolt;
import storm.starter.bolt.CompanyAggregatorBolt;
import storm.starter.bolt.UserAggregatorBolt;
import storm.starter.spout.ServiceBusQueueConnection;
import storm.starter.spout.ServiceBusQueueSpout;
import storm.starter.spout.interfaces.IServiceBusQueueDetail;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class ActivizeTopology {

	public static File file;
	public static FileWriter fw;
	public static BufferedWriter bw;

	public static File file2;
	public static FileWriter fw2;
	public static BufferedWriter bw2;

	// writes to file deviceId.out
	public static class DisplayBoltDevice extends BaseBasicBolt {
		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String deviceId = tuple.getString(0);
			String companyId = tuple.getString(1);
			double calorie = tuple.getDouble(2);
			double distance = tuple.getDouble(3);
			double runStep = tuple.getDouble(4);
			double speed = tuple.getDouble(5);
			double totalStep = tuple.getDouble(6);
			double walkStep = tuple.getDouble(7);

			try {
				bw.write(deviceId + "|" + companyId + "|" + calorie + "|"
						+ distance + "|" + runStep + "|" + speed + "|"
						+ totalStep + "|" + walkStep + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}

			collector.emit(new Values("Done"));
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("display"));
		}
	}

	// writes to file company.out
	public static class DisplayBoltCompany extends BaseBasicBolt {
		@Override
		public void execute(Tuple tuple, BasicOutputCollector collector) {
			String deviceId = tuple.getString(0);
			String companyId = tuple.getString(1);
			double calorie = tuple.getDouble(2);
			double distance = tuple.getDouble(3);
			double runStep = tuple.getDouble(4);
			double speed = tuple.getDouble(5);
			double totalStep = tuple.getDouble(6);
			double walkStep = tuple.getDouble(7);

			try {
				bw2.write(deviceId + "|" + companyId + "|" + calorie + "|"
						+ distance + "|" + runStep + "|" + speed + "|"
						+ totalStep + "|" + walkStep + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}

			collector.emit(new Values("Done"));
		}

		@Override
		public void declareOutputFields(OutputFieldsDeclarer declarer) {
			declarer.declare(new Fields("display"));
		}
	}

	public static void main(String[] args) throws Exception {

		// Creating a file to write the data to
		// Temporary and for testing purposes
		try {
			file = new File("/home/leah/workspace/storm-starter/deviceId.out");

			if (!file.exists()) {
				file.createNewFile();
			}

			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);

		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			file2 = new File("/home/leah/workspace/storm-starter/company.out");

			if (!file2.exists()) {
				file2.createNewFile();
			}

			fw2 = new FileWriter(file2.getAbsoluteFile());
			bw2 = new BufferedWriter(fw2);

		} catch (IOException e) {
			e.printStackTrace();
		}

		Properties prop = new Properties();
		try {
			prop.load(ActivizeTopology.class.getClassLoader()
					.getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String connectionString = prop.getProperty("SB_CONNECTION");
		String queueName = "fitnessdata";
		String spoutId = "Spout";

		IServiceBusQueueDetail connection = new ServiceBusQueueConnection(
				connectionString, queueName);
		TopologyBuilder builder = new TopologyBuilder();

		builder.setSpout(spoutId, new ServiceBusQueueSpout(connection), 6);

		builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 3)
				.shuffleGrouping(spoutId);
		builder.setBolt("UserAggregatorBolt", new UserAggregatorBolt(), 3)
				.fieldsGrouping("MessageReceiverBolt", new Fields("deviceId"));
		builder.setBolt("CompanyAggregatorBolt", new CompanyAggregatorBolt(), 3)
				.fieldsGrouping("MessageReceiverBolt", new Fields("companyId"));
		builder.setBolt("DisplayBoltDevice", new DisplayBoltDevice(), 3)
				.shuffleGrouping("UserAggregatorBolt");
		builder.setBolt("DisplayBoltCompany", new DisplayBoltCompany(), 3)
				.shuffleGrouping("CompanyAggregatorBolt");

		Config conf = new Config();
		conf.setDebug(false);
		// conf.setMaxTaskParallelism(3);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("sbMessageReceiver", conf,
				builder.createTopology());

		// Waits for key press to stop program
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();

		cluster.shutdown();
		bw.close();
		bw2.close();
	}
}
