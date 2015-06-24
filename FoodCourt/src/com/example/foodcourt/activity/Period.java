package com.example.foodcourt.activity;

import com.example.foodcourt.knn.Instance;

public class Period {

	private Instance.Activities type;
	private int start;
	private int end;

	public Period(Instance.Activities type) {
		this.type = type;
	}
	public Period(Instance.Activities type, int start) {
		this.type = type;
		setStart(start);
		setEnd(start);
	}

	public int getStart() { return this.start; }
	public int getEnd() { return this.end; }

	/**
	 * Start - end, inclusive (which explains +1)
	 * @return
	 */
	public int getTime() { return 1+this.end - this.start; }

	public void setStart(int start) {
		this.start = start;
	}
	public void setEnd(int end) {
		this.end = end;
	}

	public Instance.Activities getType() {
		return type;
	}

	public String toString() {
		return type.toString() + ": " + start + " - " + end + " ("+getTime()+"s)";
	}
}
