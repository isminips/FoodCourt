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
	
	public void setZ(double z) {
		this.z = z;
	}

	public double getZ() {
		return z;
	}

}
