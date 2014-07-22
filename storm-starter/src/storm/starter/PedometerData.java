package storm.starter;

public class PedometerData {
	private String deviceId;
	private String companyId;
	private double calorie;
	private double distance;
	private double runStep;
	private double speed;
	private double totalStep;
	private double walkStep;

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

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
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
}
