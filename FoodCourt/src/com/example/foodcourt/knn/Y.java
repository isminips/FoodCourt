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
	
	public void setValue(double y) {
		this.y = y;
	}

	public double getValue() {
		return y;
	}

	public String toString() {
		return y + "";
	}
}
