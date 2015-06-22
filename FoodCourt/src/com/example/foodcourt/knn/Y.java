package com.example.foodcourt.knn;

public class Y extends Feature {

	private double y;

	public Y() {
		super("Y");
	}

	public Y(double y) {
		super("Y");
		this.y = y;
	}
	
	public void setY(double y) {
		this.y = y;
	}

	public double getY() {
		return y;
	}

}
