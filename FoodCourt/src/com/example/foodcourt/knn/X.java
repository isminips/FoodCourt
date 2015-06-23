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
	
	public void setValue(double value) {
		this.x = value;
	}

	public double getValue() {
		return x;
	}

	public String toString() {
		return x + "";
	}
}
