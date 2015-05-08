package com.example.foodcourt;

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

}
