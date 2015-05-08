package com.example.foodcourt;

public class Time extends Feature {

	private int time;

	public Time() {
		super("Time");
	}

	public Time(int time) {
		super("Time");
		this.time = time;
	}
	
	public void setTime(int time) {
		this.time = time;
	}

	public int getTime() {
		return time;
	}

}
