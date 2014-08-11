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

		String connectionUri = prop.getProperty("URI");
		// String consumerGroup0 = prop.getProperty("CONSUMER0");
		// String consumerGroup1 = prop.getProperty("CONSUMER1");
		// String consumerGroup2 = prop.getProperty("CONSUMER2");
		// String consumerGroup3 = prop.getProperty("CONSUMER3");
		// String consumerGroup4 = prop.getProperty("CONSUMER4");
		// String consumerGroup5 = prop.getProperty("CONSUMER5");
		// String consumerGroup6 = prop.getProperty("CONSUMER6");
		// String consumerGroup7 = prop.getProperty("CONSUMER7");

		TopologyBuilder builder = new TopologyBuilder();

		for (int i = 0; i < 8; i++) {
			// sets spout, connects to ServiceBus Queue
			builder.setSpout("EHSpout" + i, new EventHubSpout(connectionUri,
					prop.getProperty("CONSUMER" + i)), 1);

			builder.setBolt("MessageReceiverBolt" + i,
					new MessageReceiverBolt(), 1)
					.shuffleGrouping("EHSpout" + i);
			builder.setBolt("RawDBWriterBolt" + i, new RawDatabaseBolt(), 8)
					.shuffleGrouping("MessageReceiverBolt" + i);
			builder.setBolt("UserAggregatorBolt" + i, new UserAggregatorBolt(), 8)
					.fieldsGrouping("MessageReceiverBolt" + i,
							new Fields("deviceId"));
			builder.setBolt("CompanyAggregatorBolt" + i,
					new CompanyAggregatorBolt(), 8).fieldsGrouping(
					"MessageReceiverBolt" + i, new Fields("companyId"));

			builder.setBolt("UserRTDatabaseBolt" + i, new DBWriterBolt(), 8)
					.shuffleGrouping("UserAggregatorBolt" + i);
			builder.setBolt("CompanyRTDatabaseBolt" + i, new DBWriterBolt(), 8)
					.shuffleGrouping("CompanyAggregatorBolt" + i);
		}

		// sets spout, connects to ServiceBus Queue
		// builder.setSpout("EHSpout0", new EventHubSpout(connectionUri,
		// consumerGroup0), 1);
		// builder.setSpout("EHSpout1", new EventHubSpout(connectionUri,
		// consumerGroup1), 1);
		// builder.setSpout("EHSpout2", new EventHubSpout(connectionUri,
		// consumerGroup2), 1);
		// builder.setSpout("EHSpout3", new EventHubSpout(connectionUri,
		// consumerGroup3), 1);
		// builder.setSpout("EHSpout4", new EventHubSpout(connectionUri,
		// consumerGroup4), 1);
		// builder.setSpout("EHSpout5", new EventHubSpout(connectionUri,
		// consumerGroup5), 1);
		// builder.setSpout("EHSpout6", new EventHubSpout(connectionUri,
		// consumerGroup6), 1);
		// builder.setSpout("EHSpout7", new EventHubSpout(connectionUri,
		// consumerGroup7), 1);

		// sets bolts
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout0");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout1");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout2");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout3");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout4");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout5");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout6");
		// builder.setBolt("MessageReceiverBolt", new MessageReceiverBolt(), 1)
		// .shuffleGrouping("EHSpout7");

		// builder.setBolt("RawDBWriterBolt", new RawDatabaseBolt(), 8)
		// .shuffleGrouping("MessageReceiverBolt");
		// builder.setBolt("UserAggregatorBolt", new UserAggregatorBolt(), 8)
		// .fieldsGrouping("MessageReceiverBolt", new Fields("deviceId"));
		// builder.setBolt("CompanyAggregatorBolt", new CompanyAggregatorBolt(),
		// 8)
		// .fieldsGrouping("MessageReceiverBolt", new Fields("companyId"));
		//
		// builder.setBolt("UserRTDatabaseBolt", new DBWriterBolt(), 8)
		// .shuffleGrouping("UserAggregatorBolt");
		// builder.setBolt("CompanyRTDatabaseBolt", new DBWriterBolt(), 8)
		// .shuffleGrouping("CompanyAggregatorBolt");

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
