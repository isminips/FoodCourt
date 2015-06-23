package com.example.foodcourt.knn;

public class Time extends Feature {

	private float time;

	public Time() {
		super("Time");
	}

	public Time(float time) {
		super("Time");
		this.time = time;
	}

	public void setValue(float time) {
		this.time = time;
	}

	public float getValue() {
		return time;
	}

	public String toString() {
		return time + "";
	}
}
