package storm.starter.bolt;

import java.util.HashMap;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class CompanyAggregatorBolt extends BaseBasicBolt {
	public static HashMap<String, HashMap<String, Double>> companyData = new HashMap<String, HashMap<String, Double>>();

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

		if (!companyData.containsKey(companyId)) {
			companyData.put(companyId, new HashMap<String, Double>());
		} 
		
		companyData.get(companyId).put("calorie", calorie);
		companyData.get(companyId).put("distance", distance);
		companyData.get(companyId).put("runStep", runStep);
		companyData.get(companyId).put("speed", speed);
		companyData.get(companyId).put("totalStep", totalStep);
		companyData.get(companyId).put("walkStep", walkStep);

		collector.emit(new Values(deviceId, companyId, calorie, distance,
				runStep, speed, totalStep, walkStep));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("deviceId", "companyId", "calorie",
				"distance", "runStep", "speed", "totalStep", "walkStep"));
	}
}