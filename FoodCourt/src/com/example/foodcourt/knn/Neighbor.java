package com.example.foodcourt.knn;

public class Neighbor {
	private Instance instance;
	private double distance;
	
	public Neighbor() {
		setInstance(new Instance());
		setDistance(0);
	}
	
	public Neighbor(Instance instance, double distance) {
		this.setInstance(instance);
		this.setDistance(distance);
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public double getDistance() {
		return distance;
	}

	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public Instance getInstance() {
		return instance;
	}
}
