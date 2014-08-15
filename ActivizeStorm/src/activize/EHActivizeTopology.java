package activize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import activize.bolt.CompanyAggregatorBolt;
import activize.bolt.EHMessageReceiverBolt;
import activize.bolt.DBWriterBolt;
import activize.bolt.RawDBWriterBolt;
import activize.bolt.UserAggregatorBolt;
import activize.spout.EventHubSpout;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;

public class EHActivizeTopology {

	public static void main(String[] args) throws Exception {

		Properties prop = new Properties();
		try {
			prop.load(EHActivizeTopology.class.getClassLoader().getResourceAsStream("config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		String connectionUri = prop.getProperty("URI");
		TopologyBuilder builder = new TopologyBuilder();

		for (int i = 0; i < 8; i++) {
			builder.setSpout("EHSpout" + i, new EventHubSpout(connectionUri,
					prop.getProperty("CONSUMER" + i)), 1);

			builder.setBolt("MessageReceiverBolt" + i,
					new EHMessageReceiverBolt(), 1)
					.shuffleGrouping("EHSpout" + i);
			builder.setBolt("RawDBWriterBolt" + i, new RawDBWriterBolt(), 8)
					.shuffleGrouping("MessageReceiverBolt" + i);
			builder.setBolt("UserAggregatorBolt" + i, new UserAggregatorBolt(), 8)
					.fieldsGrouping("MessageReceiverBolt" + i,
							new Fields("deviceId"));
			builder.setBolt("CompanyAggregatorBolt" + i,
					new CompanyAggregatorBolt(), 8).fieldsGrouping(
					"MessageReceiverBolt" + i, new Fields("companyId"));

			builder.setBolt("UserDatabaseBolt" + i, new DBWriterBolt(), 8)
					.shuffleGrouping("UserAggregatorBolt" + i);
			builder.setBolt("CompanyDatabaseBolt" + i, new DBWriterBolt(), 8)
					.shuffleGrouping("CompanyAggregatorBolt" + i);
		}

		Config conf = new Config();
		conf.setDebug(false);

		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("ActivizeTopology", conf,
				builder.createTopology());

		// Waits for <Enter> key press to stop program
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		br.readLine();

		cluster.shutdown();
	}
}
