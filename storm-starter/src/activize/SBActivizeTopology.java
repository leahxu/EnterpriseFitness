package activize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import activize.bolt.CompanyAggregatorBolt;
import activize.bolt.SBMessageReceiverBolt;
import activize.bolt.DBWriterBolt;
import activize.bolt.RawDBWriterBolt;
import activize.bolt.UserAggregatorBolt;
import activize.spout.ServiceBusQueueConnection;
import activize.spout.ServiceBusQueueSpout;
import activize.spout.interfaces.IServiceBusQueueDetail;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class SBActivizeTopology {

	public static void main(String[] args) throws Exception {

		Properties prop = new Properties();
		try {
			prop.load(EHActivizeTopology.class.getClassLoader()
					.getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String connectionString = prop.getProperty("SB_CONNECTION");
		String queueName = prop.getProperty("SB_QUEUE");
		String sbSpoutId = "SBSpout";

		IServiceBusQueueDetail connection = new ServiceBusQueueConnection(
				connectionString, queueName);
		TopologyBuilder builder = new TopologyBuilder();

		// sets spout, connects to ServiceBus Queue
		builder.setSpout(sbSpoutId, new ServiceBusQueueSpout(connection), 8);

		// sets bolts
		builder.setBolt("SBMessageReceiverBolt", new SBMessageReceiverBolt(), 8)
				.shuffleGrouping(sbSpoutId);

		builder.setBolt("UserAggregatorBolt", new UserAggregatorBolt(), 8)
				.fieldsGrouping("SBMessageReceiverBolt", new Fields("deviceId"));
		builder.setBolt("CompanyAggregatorBolt", new CompanyAggregatorBolt(), 8)
				.fieldsGrouping("SBMessageReceiverBolt", new Fields("companyId"));

		builder.setBolt("RawDBWriterBolt", new RawDBWriterBolt(), 8)
				.shuffleGrouping("SBMessageReceiverBolt");
		builder.setBolt("UserRTDatabaseBolt", new DBWriterBolt(), 8)
				.shuffleGrouping("UserAggregatorBolt");
		builder.setBolt("CompanyRTDatabaseBolt", new DBWriterBolt(), 8)
				.shuffleGrouping("CompanyAggregatorBolt");

		Config conf = new Config();
		conf.setDebug(false);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("ActivizeTopology", conf,
				builder.createTopology());

		// Waits for key press to stop program
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();

		cluster.shutdown();
	}
}
