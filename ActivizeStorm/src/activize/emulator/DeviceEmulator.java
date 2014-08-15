package activize.emulator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class DeviceEmulator {

	protected ArrayList<String> mockPedometer;
	final static int DEVICES = 40;
	final static int HOURS = 5;

	public void createDevices() {
		mockPedometer = new ArrayList<String>();
		Random random = new Random();

		String[] companies = { "Azure", "Xbox", "Word", "Excel", "PowerPoint",
				"Dynamics", "BizTalk", "DevDiv", "ServiceBus", "EventHubs",
				"Access", "Outlook", "SharePoint", "SQLServer",
				"Visual Studio", "Windows", "Surface", "Bing",
				"Internet Explorer", "Skype" };

		MockDevice[] devices = new MockDevice[DEVICES];

		for (int i = 0; i < DEVICES; i++) {
			devices[i] = new MockDevice();
		}

		Calendar prev = Calendar.getInstance();
		prev.set(2014, 8, 12, 21, 0, 0);

		// Time
		for (int i = 0; i < HOURS; i++) {
			Calendar curr = Calendar.getInstance();
			curr.set(prev.get(Calendar.YEAR), prev.get(Calendar.MONTH),
					prev.get(Calendar.DATE), prev.get(Calendar.HOUR_OF_DAY),
					prev.get(Calendar.MINUTE), prev.get(Calendar.SECOND));
			curr.add(Calendar.HOUR_OF_DAY, 1);

			String currDate = curr.get(Calendar.MONTH) + "/"
					+ curr.get(Calendar.DATE) + "/" + curr.get(Calendar.YEAR);
			String currTime = curr.get(Calendar.HOUR_OF_DAY) + ":"
					+ curr.get(Calendar.MINUTE) + ":"
					+ curr.get(Calendar.SECOND);

			// PedometerData Instance
			for (int j = 0; j < DEVICES; j++) {
				String deviceId = "TRIAL" + j;
				String companyId = companies[j % companies.length];

				PedometerData instance = new PedometerData(deviceId, companyId,
						currDate, currTime);

				instance.setDeltaCalorie(random.nextDouble() * 100.0);
				instance.setDeltaDistance(random.nextDouble() * 1.0);
				instance.setDeltaRunStep((int) (random.nextDouble() * 200.0));
				instance.setDeltaWalkStep((int) (random.nextDouble() * 300.0));
				instance.setDeltaTotalStep(instance.getDeltaWalkStep()
						+ instance.getDeltaRunStep());

				if (prev.get(Calendar.DATE) == curr.get(Calendar.DATE)) {
					// Resets each metric at the end of each day
					devices[j].calorie += instance.getDeltaCalorie();
					devices[j].distance += instance.getDeltaDistance();
					devices[j].runStep += instance.getDeltaRunStep();
					devices[j].totalStep += instance.getDeltaTotalStep();
					devices[j].walkStep += instance.getDeltaWalkStep();
				} else {
					devices[j].calorie = instance.getDeltaCalorie();
					devices[j].distance = instance.getDeltaDistance();
					devices[j].runStep = instance.getDeltaRunStep();
					devices[j].totalStep = instance.getDeltaTotalStep();
					devices[j].walkStep = instance.getDeltaWalkStep();
				}

				instance.setCalorie(devices[j].calorie);
				instance.setDistance(devices[j].distance);
				instance.setRunStep(devices[j].runStep);
				instance.setWalkStep(devices[j].walkStep);
				instance.setTotalStep(devices[j].totalStep);

				try {
					String serialized = new ObjectMapper()
							.writeValueAsString(instance);
					mockPedometer.add(serialized);
				} catch (JsonGenerationException e) {
					e.printStackTrace();
				} catch (JsonMappingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			prev = curr;
		}
	}

}
