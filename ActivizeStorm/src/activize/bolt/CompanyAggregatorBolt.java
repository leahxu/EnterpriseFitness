package activize.bolt;

import java.util.Hashtable;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class CompanyAggregatorBolt extends BaseRichBolt {
	private static final long serialVersionUID = 1L;
	private static Hashtable<String, Hashtable<String, Double>> _companyData = new Hashtable<String, Hashtable<String, Double>>();
	private OutputCollector _collector; 
	
	@Override
	public void prepare(Map conf, TopologyContext context, OutputCollector collector) {
		_collector = collector; 
	}

	@Override
	public void execute(Tuple tuple) {
		String deviceId = tuple.getString(0);
		String companyId = tuple.getString(1);
		String date = tuple.getString(2);
		String time = tuple.getString(3);

		double deltaCalorie = tuple.getDouble(9);
		double deltaDistance = tuple.getDouble(10);
		double deltaRunStep = tuple.getDouble(11);
		double deltaTotalStep = tuple.getDouble(12);
		double deltaWalkStep = tuple.getDouble(13);

		// turns the String into a double
		String[] splitDate = date.split("/");
		String stringDate = splitDate[0] + splitDate[1] + splitDate[2];
		double doubleDate = Double.parseDouble(stringDate);

		if (!_companyData.containsKey(companyId)) {
			_companyData.put(companyId, new Hashtable<String, Double>());
			_companyData.get(companyId).put("date", doubleDate);
			_companyData.get(companyId).put("calorie", deltaCalorie);
			_companyData.get(companyId).put("distance", deltaDistance);
			_companyData.get(companyId).put("runStep", deltaRunStep);
			_companyData.get(companyId).put("totalStep", deltaTotalStep);
			_companyData.get(companyId).put("walkStep", deltaWalkStep);
		} else if (_companyData.get(companyId).get("date") != doubleDate) {
			// Resets metrics at the end of each day
			_companyData.get(companyId).put("date", doubleDate);
			_companyData.get(companyId).put("calorie", deltaCalorie);
			_companyData.get(companyId).put("distance", deltaDistance);
			_companyData.get(companyId).put("runStep", deltaRunStep);
			_companyData.get(companyId).put("totalStep", deltaTotalStep);
			_companyData.get(companyId).put("walkStep", deltaWalkStep);
		} else {
			_companyData.get(companyId).put("calorie",
					_companyData.get(companyId).get("calorie") + deltaCalorie);
			_companyData.get(companyId).put("distance",
					_companyData.get(companyId).get("distance") + deltaDistance);
			_companyData.get(companyId).put("runStep",
					_companyData.get(companyId).get("runStep") + deltaRunStep);
			_companyData.get(companyId).put("totalStep",
					_companyData.get(companyId).get("totalStep") + deltaTotalStep);
			_companyData.get(companyId).put("walkStep",
					_companyData.get(companyId).get("walkStep") + deltaWalkStep);
		}

		_collector.emit(new Values("Company", deviceId, companyId, date, time,
				_companyData.get(companyId).get("calorie"), _companyData.get(
						companyId).get("distance"), _companyData.get(companyId)
						.get("runStep"), _companyData.get(companyId).get(
						"totalStep"), _companyData.get(companyId)
						.get("walkStep")));
		
		_collector.ack(tuple);
		
	}
	
	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("table", "deviceId", "companyId", "date",
				"time", "calorie", "distance", "runStep", "totalStep",
				"walkStep"));
	}
}