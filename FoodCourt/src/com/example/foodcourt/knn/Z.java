package com.example.foodcourt.knn;

public class Z extends Feature {

	private double z;

	public Z() {
		super("Z");
	}

	public Z(double z) {
		super("Z");
		this.z = z;
	}
	
	public void setValue(double z) {
		this.z = z;
	}

	public double getValue() {
		return z;
	}

	public String toString() {
		return z + "";
	}
}
