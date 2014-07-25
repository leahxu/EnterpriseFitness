package storm.starter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import storm.starter.bolt.CompanyAggregatorBolt;
import storm.starter.bolt.DBWriterBolt;
import storm.starter.bolt.FileWriterBolt;
import storm.starter.bolt.MessageReceiverBolt;
import storm.starter.bolt.UserAggregatorBolt;
import storm.starter.spout.ServiceBusQueueConnection;
import storm.starter.spout.ServiceBusQueueSpout;
import storm.starter.spout.interfaces.IServiceBusQueueDetail;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class ActivizeTopology {

	public static void main(String[] args) throws Exception {

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

		// sets spout, connects to ServiceBus Queue
		builder.setSpout(spoutId, new ServiceBusQueueSpout(connection), 6);

		// sets bolts
		builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 3)
				.shuffleGrouping(spoutId);
		builder.setBolt("UserAggregatorBolt", new UserAggregatorBolt(), 3)
				.fieldsGrouping("MessageReceiverBolt", new Fields("deviceId"));
		builder.setBolt("CompanyAggregatorBolt", new CompanyAggregatorBolt(), 3)
				.fieldsGrouping("MessageReceiverBolt", new Fields("companyId"));
		builder.setBolt("UserDisplayBolt", new FileWriterBolt(), 3)
				.shuffleGrouping("UserAggregatorBolt");
//		builder.setBolt("UserDBWriterBolt", new DBWriterBolt(), 1)
//				.shuffleGrouping("UserAggregatorBolt");
//		builder.setBolt("CompanyDBWriterBolt", new DBWriterBolt(), 1)
//				.shuffleGrouping("CompanyAggregateBolt");

		Config conf = new Config();
		conf.setDebug(false);
		// conf.setMaxTaskParallelism(3);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("ActivizeTopology", conf,
				builder.createTopology());

		// Waits for key press to stop program
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();

		cluster.shutdown();
	}
}
