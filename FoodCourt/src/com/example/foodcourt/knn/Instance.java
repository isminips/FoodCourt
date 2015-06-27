package com.example.foodcourt.knn;

public class Instance {

	public enum Activities {
		Standing,
		Walking
	}

	private Activities label;
	private double meanMagnitude;
	private double maxMagnitude;
	private double varianceMagnitude;
	private long time;

	public Instance() {
		// empty constructor
	}

	public Instance(String label, double meanMagnitude, double maxMagnitude, double varianceMagnitude, long time) {
		this.label = determineActivity(label);
		this.meanMagnitude = meanMagnitude;
		this.maxMagnitude = maxMagnitude;
		this.varianceMagnitude = varianceMagnitude;
		this.time = time;
	}

	public void setLabel(Activities label) {
		this.label = label;
	}

	public static Activities determineActivity(String activity) {
		if (activity.equals("Standing")) {
			return Activities.Standing;
		}
		else if (activity.equals("Walking")) {
			return Activities.Walking;
		}
		return null;
	}

	public static Activities determineActivity(int standing, int walking) {
		return walking > standing ? Instance.Activities.Walking : Instance.Activities.Standing;
	}

	public Activities getLabel() {
		return label;
	}

	public double getMeanMagnitude() {
		return meanMagnitude;
	}

	public void setMeanMagnitude(double meanMagnitude) {
		this.meanMagnitude = meanMagnitude;
	}

	public double getMaxMagnitude() {
		return maxMagnitude;
	}

	public void setMaxMagnitude(double maxMagnitude) {
		this.maxMagnitude = maxMagnitude;
	}

	public double getVarianceMagnitude() {
		return varianceMagnitude;
	}

	public void setVarianceMagnitude(double varianceMagnitude) {
		this.varianceMagnitude = varianceMagnitude;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String toString() {
		return label + "," + meanMagnitude + "," + maxMagnitude + "," + varianceMagnitude + "," + time;
	}
}
