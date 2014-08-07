package activize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import activize.bolt.CompanyAggregatorBolt;
import activize.bolt.MessageReceiverBolt;
import activize.bolt.DBWriterBolt;
import activize.bolt.RawDatabaseBolt;
import activize.bolt.UserAggregatorBolt;
import activize.spout.EventHubSpout;
import activize.spout.ServiceBusQueueConnection;
import activize.spout.ServiceBusQueueSpout;
import activize.spout.interfaces.IServiceBusQueueDetail;
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
		String queueName = prop.getProperty("SB_QUEUE");
		String spoutId = "Spout";

		IServiceBusQueueDetail connection = new ServiceBusQueueConnection(
				connectionString, queueName);
		TopologyBuilder builder = new TopologyBuilder();

		// sets spout, connects to ServiceBus Queue
		builder.setSpout(spoutId, new ServiceBusQueueSpout(connection), 8);
//		builder.setSpout(spoutId, new EventHubSpout(), 1); 

		// sets bolts
		builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 8)
				.shuffleGrouping(spoutId);

		builder.setBolt("RawDBWriterBolt", new RawDatabaseBolt(), 8)
				.shuffleGrouping("MessageReceiverBolt");
		builder.setBolt("UserAggregatorBolt", new UserAggregatorBolt(), 8)
				.fieldsGrouping("MessageReceiverBolt", new Fields("deviceId"));
		builder.setBolt("CompanyAggregatorBolt", new CompanyAggregatorBolt(), 8)
				.fieldsGrouping("MessageReceiverBolt", new Fields("companyId"));

		builder.setBolt("UserRTDatabaseBolt", new DBWriterBolt(), 8)
				.shuffleGrouping("UserAggregatorBolt");
		builder.setBolt("CompanyRTDatabaseBolt", new DBWriterBolt(), 8)
				.shuffleGrouping("CompanyAggregatorBolt");

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
