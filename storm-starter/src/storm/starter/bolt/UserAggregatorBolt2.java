package storm.starter.bolt;

import java.util.HashMap;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class UserAggregatorBolt2 extends BaseBasicBolt {
	public static HashMap<String, HashMap<String, Double>> userData = new HashMap<String, HashMap<String, Double>>();

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

		if (!userData.containsKey(deviceId)) {
			userData.put(deviceId, new HashMap<String, Double>());
			userData.get(deviceId).put("calorie", calorie);
			userData.get(deviceId).put("distance", distance);
			userData.get(deviceId).put("runStep", runStep);
			userData.get(deviceId).put("speed", speed);
			userData.get(deviceId).put("totalStep", totalStep);
			userData.get(deviceId).put("walkStep", walkStep);
		} else {
			userData.get(deviceId).put("calorie",
					userData.get(deviceId).get("calorie") + calorie);
			userData.get(deviceId).put("distance",
					userData.get(deviceId).get("distance") + distance);
			userData.get(deviceId).put("runStep",
					userData.get(deviceId).get("runStep") + runStep);
			userData.get(deviceId).put("speed",
					userData.get(deviceId).get("speed") + speed);
			userData.get(deviceId).put("totalStep",
					userData.get(deviceId).get("totalStep") + totalStep);
			userData.get(deviceId).put("walkStep",
					userData.get(deviceId).get("walkStep") + walkStep);
		}

		calorie = userData.get(deviceId).get("calorie");
		distance = userData.get(deviceId).get("distance");
		runStep = userData.get(deviceId).get("runStep");
		speed = userData.get(deviceId).get("speed");
		totalStep = userData.get(deviceId).get("totalStep");
		walkStep = userData.get(deviceId).get("walkStep");

		collector.emit(new Values(deviceId, companyId, calorie, distance,
				runStep, speed, totalStep, walkStep));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("deviceId", "companyId", "calorie",
				"distance", "runStep", "speed", "totalStep", "walkStep"));
	}
}