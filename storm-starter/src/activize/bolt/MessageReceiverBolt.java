package activize.bolt;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import activize.emulator.PedometerData;
import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;

public class MessageReceiverBolt extends BaseBasicBolt {
	private static final long serialVersionUID = 1L;

	@Override
	public void execute(Tuple tuple, BasicOutputCollector collector) {
		String input = tuple.getString(0);
		
		// cleans the output of the header AmqpValue{(java.lang.String) <message> }
		input = input.replace("AmqpValue{(java.lang.String)", "");
		input = input.substring(0, input.length() - 1);

		try {
			ObjectMapper mapper = new ObjectMapper();
			PedometerData data = mapper.readValue(input, PedometerData.class);
			collector.emit(new Values(data.getDeviceId(), data.getCompanyId(),
					data.getDate(), data.getTime(), data.getCalorie(), data
							.getDistance(), data.getRunStep(), data
							.getTotalStep(), data.getWalkStep(), data
							.getDeltaCalorie(), data.getDeltaDistance(), data
							.getDeltaRunStep(), data.getDeltaTotalStep(), data
							.getDeltaWalkStep()));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		declarer.declare(new Fields("deviceId", "companyId", "date", "time",
				"calorie", "distance", "runStep", "totalStep", "walkStep",
				"deltaCalorie", "deltaDistance", "deltaRunStep",
				"deltaTotalStep", "deltaWalkStep"));
	}
}