package activize.emulator;

public class PedometerData {
	private String deviceId;
	private String companyId;
	private String date;
	private String time;
	private double calorie;
	private double distance;
	private double runStep;
	private double totalStep;
	private double walkStep;
	private double deltaCalorie;
	private double deltaDistance;
	private double deltaRunStep;
	private double deltaTotalStep;
	private double deltaWalkStep;
	
	public PedometerData(String deviceId, String companyId, String date, String time) {
		this.deviceId = deviceId; 
		this.companyId = companyId; 
		this.date = date; 
		this.time = time; 
		this.calorie = 0; 
		this.distance = 0; 
		this.runStep = 0; 
		this.totalStep = 0;
		this.walkStep = 0; 
		this.deltaCalorie = 0; 
		this.deltaDistance = 0; 
		this.deltaRunStep = 0; 
		this.deltaTotalStep = 0;
		this.deltaWalkStep = 0; 
	}
	
	public PedometerData() {
		this.deviceId = ""; 
		this.companyId = ""; 
		this.date = ""; 
		this.time = ""; 
		this.calorie = 0; 
		this.distance = 0; 
		this.runStep = 0; 
		this.totalStep = 0;
		this.walkStep = 0; 
		this.deltaCalorie = 0; 
		this.deltaDistance = 0; 
		this.deltaRunStep = 0; 
		this.deltaTotalStep = 0;
		this.deltaWalkStep = 0; 
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getCalorie() {
		return calorie;
	}

	public void setCalorie(double calorie) {
		this.calorie = calorie;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getRunStep() {
		return runStep;
	}

	public void setRunStep(double runStep) {
		this.runStep = runStep;
	}

	public double getTotalStep() {
		return totalStep;
	}

	public void setTotalStep(double totalStep) {
		this.totalStep = totalStep;
	}

	public double getWalkStep() {
		return walkStep;
	}

	public void setWalkStep(double walkStep) {
		this.walkStep = walkStep;
	}

	public double getDeltaCalorie() {
		return deltaCalorie;
	}

	public void setDeltaCalorie(double deltaCalorie) {
		this.deltaCalorie = deltaCalorie;
	}

	public double getDeltaDistance() {
		return deltaDistance;
	}

	public void setDeltaDistance(double deltaDistance) {
		this.deltaDistance = deltaDistance;
	}

	public double getDeltaRunStep() {
		return deltaRunStep;
	}

	public void setDeltaRunStep(double deltaRunStep) {
		this.deltaRunStep = deltaRunStep;
	}

	public double getDeltaTotalStep() {
		return deltaTotalStep;
	}

	public void setDeltaTotalStep(double deltaTotalStep) {
		this.deltaTotalStep = deltaTotalStep;
	}

	public double getDeltaWalkStep() {
		return deltaWalkStep;
	}

	public void setDeltaWalkStep(double deltaWalkStep) {
		this.deltaWalkStep = deltaWalkStep;
	}

}
