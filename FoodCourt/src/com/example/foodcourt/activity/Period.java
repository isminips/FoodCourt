package com.example.foodcourt.activity;

import com.example.foodcourt.knn.Instance;

public class Period {

	private Instance.Activities type;
	private float start;
	private float end;

	public Period(Instance.Activities type) {
		this.type = type;
	}
	public Period(Instance.Activities type, float start) {
		this.type = type;
		setStart(start);
		setEnd(start);
	}

	public float getStart() { return this.start; }
	public float getEnd() { return this.end; }

	/**
	 * Start - end
	 * @return
	 */
	public float getTime() { return this.end - this.start; }

	public void setStart(float start) {
		this.start = start;
	}
	public void setEnd(float end) {
		this.end = end;
	}

	public Instance.Activities getType() {
		return type;
	}

	public String toString() {
		return type.toString() + ": " + start + " - " + end + " ("+((double) Math.round(getTime()/100)/10)+"s)";
	}
}
