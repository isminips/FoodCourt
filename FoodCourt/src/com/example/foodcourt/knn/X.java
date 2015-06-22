package com.example.foodcourt.knn;

public class X extends Feature {

	private double x;

	public X() {
		super("X");
	}

	public X(double x) {
		super("X");
		this.x = x;
	}
	
	public void setX(double x) {
		this.x = x;
	}

	public double getX() {
		return x;
	}

}
