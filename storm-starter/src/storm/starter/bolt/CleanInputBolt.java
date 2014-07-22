package storm.starter.bolt;

import java.util.Map;

import backtype.storm.task.ShellBolt;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;


public class CleanInputBolt extends ShellBolt implements IRichBolt {

  public CleanInputBolt() {
    super("python", "CleanInputBolt.py");
  }

  @Override
  public void declareOutputFields(OutputFieldsDeclarer declarer) {
    declarer.declare(new Fields("processed"));
  }

  @Override
  public Map<String, Object> getComponentConfiguration() {
    return null;
  }
}