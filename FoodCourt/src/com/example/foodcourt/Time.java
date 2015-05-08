package com.example.foodcourt;

public class Time extends Feature {

	private float time;

	public Time() {
		super("Time");
	}

	public Time(float time) {
		super("Time");
		this.time = time;
	}

	public void setTime(float time) {
		this.time = time;
	}

	public float getTime() {
		return time;
	}

}
