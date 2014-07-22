package storm.starter.bolt;

import java.util.HashMap;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class CopyOfCompanyAggregatorBolt extends BaseBasicBolt {

  public static HashMap<String, HashMap<String,Double>> companyTrackerData = new HashMap<String, HashMap<String,Double>>();

  @Override
  public void execute(Tuple tuple, BasicOutputCollector collector) {
    String deviceId = tuple.getString(0);
    String company = tuple.getString(1);
    String metric = tuple.getString(2);

    String[] strMetrics = metric.split("\\|");
    Double[] metrics = new Double[strMetrics.length]; 

    for (int i = 0; i < strMetrics.length; i++) {
      metrics[i] = Double.valueOf(strMetrics[i]);
    }

    if (!companyTrackerData.containsKey(company)) {
      companyTrackerData.put(company, new HashMap<String,Double>());
      companyTrackerData.get(company).put("calorie", metrics[0]);
      companyTrackerData.get(company).put("distance", metrics[1]);
      companyTrackerData.get(company).put("runStep", metrics[2]);
      companyTrackerData.get(company).put("speed", metrics[3]);
      companyTrackerData.get(company).put("totalStep", metrics[4]);
      companyTrackerData.get(company).put("walkStep", metrics[5]);
    } else {
      companyTrackerData.get(company).put("calorie", companyTrackerData.get(company).get("calorie") + metrics[0]);
      companyTrackerData.get(company).put("distance", companyTrackerData.get(company).get("distance") + metrics[1]);
      companyTrackerData.get(company).put("runStep", companyTrackerData.get(company).get("runStep") + metrics[2]);
      companyTrackerData.get(company).put("speed", companyTrackerData.get(company).get("speed") + metrics[3]);
      companyTrackerData.get(company).put("totalStep", companyTrackerData.get(company).get("totalStep") + metrics[4]);
      companyTrackerData.get(company).put("walkStep", companyTrackerData.get(company).get("walkStep") + metrics[5]);
    }

    double calorie = companyTrackerData.get(company).get("calorie");
    double distance = companyTrackerData.get(company).get("distance");
    double runStep = companyTrackerData.get(company).get("runStep");
    double speed = companyTrackerData.get(company).get("speed");
    double totalStep = companyTrackerData.get(company).get("totalStep");
    double walkStep = companyTrackerData.get(company).get("walkStep");

    String updatedMetrics = calorie + "|" + distance + "|" + runStep + "|" + speed + "|" + totalStep + "|" + walkStep;

    collector.emit(new Values(deviceId, company, updatedMetrics));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("deviceId", "company", "trackerData"));
  }
}