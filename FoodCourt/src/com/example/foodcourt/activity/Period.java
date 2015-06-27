package com.example.foodcourt.activity;

import com.example.foodcourt.knn.Instance;

public class Period {

	private Instance.Activities type;
	private long start;
	private long end;

	public Period(Instance.Activities type) {
		this.type = type;
	}
	public Period(Instance.Activities type, long start) {
		this.type = type;
		setStart(start);
		setEnd(start);
	}

	public long getStart() { return this.start; }
	public long getEnd() { return this.end; }

	/**
	 * Start - end
	 * @return
	 */
	public long getTime() { return this.end - this.start; }

	public void setStart(long start) {
		this.start = start;
	}
	public void setEnd(long end) {
		this.end = end;
	}

	public Instance.Activities getType() {
		return type;
	}

	public String toString() {
		return type.toString() + ": " + start + " - " + end + " ("+((double) Math.round(getTime()/100)/10)+"s)";
	}
}
