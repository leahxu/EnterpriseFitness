package activize.bolt;

import java.util.Hashtable;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class CompanyAggregatorBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;
	private static Hashtable<String, Hashtable<String, Double>> companyData = new Hashtable<String, Hashtable<String, Double>>();
	private static double numPeople = 0; 

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String deviceId = tuple.getString(0);
		String companyId = tuple.getString(1);
		String date = tuple.getString(2);
		String time = tuple.getString(3);
		
		double deltaCalorie = tuple.getDouble(9);
		double deltaDistance = tuple.getDouble(10);
		double deltaRunStep = tuple.getDouble(11);
		double deltaTotalStep = tuple.getDouble(12);
		double deltaWalkStep = tuple.getDouble(13);
		numPeople += 1; 

		// turns the String into a double
		String[] splitDate = date.split("/");
		String stringDate = splitDate[0] + splitDate[1] + splitDate[2];
		double doubleDate = Double.parseDouble(stringDate);

		if (!companyData.containsKey(companyId)) {
			companyData.put(companyId, new Hashtable<String, Double>());
			companyData.get(companyId).put("date", doubleDate);
			companyData.get(companyId).put("people", numPeople);
			companyData.get(companyId).put("calorie", deltaCalorie);
			companyData.get(companyId).put("distance", deltaDistance);
			companyData.get(companyId).put("runStep", deltaRunStep);
			companyData.get(companyId).put("totalStep", deltaTotalStep);
			companyData.get(companyId).put("walkStep", deltaWalkStep);
		} else if (companyData.get(companyId).get("date") != doubleDate) {
			companyData.get(companyId).put("date", doubleDate);
			companyData.get(companyId).put("people", numPeople);
			companyData.get(companyId).put("calorie", deltaCalorie);
			companyData.get(companyId).put("distance", deltaDistance);
			companyData.get(companyId).put("runStep", deltaRunStep);
			companyData.get(companyId).put("totalStep", deltaTotalStep);
			companyData.get(companyId).put("walkStep", deltaWalkStep);
		} else {
			double currCalorie = companyData.get(companyId).get("calorie")
					+ deltaCalorie;
			double currDistance = companyData.get(companyId).get("distance")
					+ deltaDistance;
			double currRunStep = companyData.get(companyId).get("runStep")
					+ deltaRunStep;
			double currWalkStep = companyData.get(companyId).get("walkStep")
					+ deltaWalkStep;
			double currTotalStep = companyData.get(companyId).get("totalStep")
					+ deltaTotalStep;

			companyData.get(companyId).put("people", numPeople);
			companyData.get(companyId).put("calorie", currCalorie);
			companyData.get(companyId).put("distance", currDistance);
			companyData.get(companyId).put("runStep", currRunStep);
			companyData.get(companyId).put("totalStep", currTotalStep);
			companyData.get(companyId).put("walkStep", currWalkStep);
		}

		collector.emit(new Values("Company", deviceId, companyId, date, time,
				companyData.get(companyId).get("calorie"), companyData.get(
						companyId).get("distance"), companyData.get(companyId)
						.get("runStep"), companyData.get(companyId).get(
						"totalStep"), companyData.get(companyId)
						.get("walkStep")));
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("table", "deviceId", "companyId", "date",
				"time", "calorie", "distance", "runStep", "totalStep",
				"walkStep"));
	}
}