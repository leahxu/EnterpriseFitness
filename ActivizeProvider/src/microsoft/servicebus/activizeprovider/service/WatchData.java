package microsoft.servicebus.activizeprovider.service;

public class WatchData {
	private double calorie;
	private double distance;
	private double runStep;
	private double totalStep;
	private double walkStep;
	
		
	public WatchData() { 
			this.calorie = 0; 
			this.distance = 0; 
			this.runStep = 0; 
			this.totalStep = 0;
			this.walkStep = 0;  
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

}

