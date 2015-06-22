package com.example.foodcourt.knn;

public class Magnitude extends Feature {

	private double magnitude;
	
	public Magnitude() {
		super("Magnitude");
	}
	
	public Magnitude(double magnitude) {
		super("Magnitude");
		this.magnitude = magnitude;
	}
	
	public void setMagnitude(double magnitude) {
		this.magnitude = magnitude;
	}

	public double getMagnitude() {
		return magnitude;
	}

	public double getMagnitudeDiffFromMean() {
		return Math.abs(magnitude - 9.8);
	}

}
