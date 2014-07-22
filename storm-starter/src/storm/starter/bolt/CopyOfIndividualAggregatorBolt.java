package storm.starter.bolt;

import java.util.HashMap;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class CopyOfIndividualAggregatorBolt extends BaseBasicBolt {
  public static HashMap<String, HashMap<String,Double>> individualTrackerData = new HashMap<String, HashMap<String,Double>>();

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

    if (!individualTrackerData.containsKey(deviceId)) {
      individualTrackerData.put(deviceId, new HashMap<String,Double>());
      individualTrackerData.get(deviceId).put("calorie", metrics[0]);
      individualTrackerData.get(deviceId).put("distance", metrics[1]);
      individualTrackerData.get(deviceId).put("runStep", metrics[2]);
      individualTrackerData.get(deviceId).put("speed", metrics[3]);
      individualTrackerData.get(deviceId).put("totalStep", metrics[4]);
      individualTrackerData.get(deviceId).put("walkStep", metrics[5]);
    } else {
      individualTrackerData.get(deviceId).put("calorie", individualTrackerData.get(deviceId).get("calorie") + metrics[0]);
      individualTrackerData.get(deviceId).put("distance", individualTrackerData.get(deviceId).get("distance") + metrics[1]);
      individualTrackerData.get(deviceId).put("runStep", individualTrackerData.get(deviceId).get("runStep") + metrics[2]);
      individualTrackerData.get(deviceId).put("speed", individualTrackerData.get(deviceId).get("speed") + metrics[3]);
      individualTrackerData.get(deviceId).put("totalStep", individualTrackerData.get(deviceId).get("totalStep") + metrics[4]);
      individualTrackerData.get(deviceId).put("walkStep", individualTrackerData.get(deviceId).get("walkStep") + metrics[5]);
    }

    double calorie = individualTrackerData.get(deviceId).get("calorie");
    double distance = individualTrackerData.get(deviceId).get("distance");
    double runStep = individualTrackerData.get(deviceId).get("runStep");
    double speed = individualTrackerData.get(deviceId).get("speed");
    double totalStep = individualTrackerData.get(deviceId).get("totalStep");
    double walkStep = individualTrackerData.get(deviceId).get("walkStep");

    String updatedMetrics = calorie + "|" + distance + "|" + runStep + "|" + speed + "|" + totalStep + "|" + walkStep;

    collector.emit(new Values(deviceId, company, updatedMetrics));
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("deviceId", "company", "trackerData"));
  }
}