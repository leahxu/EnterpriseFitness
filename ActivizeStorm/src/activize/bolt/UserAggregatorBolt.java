package activize.bolt;

import java.util.HashMap;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class UserAggregatorBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	public static HashMap<String, HashMap<String, Double>> _userData = new HashMap<String, HashMap<String, Double>>();
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

		if (!_userData.containsKey(deviceId)) {
			_userData.put(deviceId, new HashMap<String, Double>());
		}

		_userData.get(deviceId).put("calorie", calorie);
		_userData.get(deviceId).put("distance", distance);
		_userData.get(deviceId).put("runStep", runStep);
		_userData.get(deviceId).put("totalStep", totalStep);
		_userData.get(deviceId).put("walkStep", walkStep);

		_collector.emit(new Values("User", deviceId, companyId, date, time,
				calorie, distance, runStep, totalStep, walkStep));
		_collector.ack(tuple);
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("table", "deviceId", "companyId", "date",
				"time", "calorie", "distance", "runStep", "totalStep",
				"walkStep"));
	}
}