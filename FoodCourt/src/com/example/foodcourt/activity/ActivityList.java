package com.example.foodcourt.activity;

import com.example.foodcourt.knn.Instance;

import java.util.ArrayList;

public class ActivityList {

	private ArrayList<Period> activityList;

	/**
	 * Total amount of walking periods, not relevant for queueing
	 */
	private int walkingPeriods = 0;

	/**
	 * Total amount of standing periods, not relevant for queueing
	 */
	private int standingPeriods = 0;

	public ActivityList() {
		this.activityList = new ArrayList<Period>();
	}

	/**
	 * Add an activity type at a certain time.
	 * If this is supposed to extend a period, increase the end time of that period.
	 * If this creates a new period, create a new period.
	 *
	 * @param type
	 * @param time
	 */
	public void add(Instance.Activities type, int time) {
		if(activityList.size() > 0 && activityList.get(activityList.size()-1).getType() == type) {
			activityList.get(activityList.size()-1).setEnd(time);
		} else {
			activityList.add(new Period(type, time));
			switch(type) {
				case Walking:
					walkingPeriods++;
					break;
				case Standing:
					standingPeriods++;
					break;
			}

		}
	}

	public int totalWalkingTime() {
		int time = 0;
		for (Period p : activityList) {
			if (p.getType() == Instance.Activities.Walking) {
				time += p.getTime();
			}
		}
		return time;
	}

	public int totalStandingTime() {
		int time = 0;
		for (Period p : activityList) {
			if (p.getType() == Instance.Activities.Standing) {
				time += p.getTime();
			}
		}
		return time;
	}

	public int totalQueueingTime() {
		int queueStart = getQueueingStartTime();
		int queueEnd = getQueueingEndTime();

		if(queueEnd < 0)
			return -1;

		return queueEnd - queueStart + 1;
	}

	public double averageServiceTime() {
		double totalServiceTime = 0;
		int services = 0;
		int queueStart = getQueueingStartTime();
		int queueEnd = getQueueingEndTime();

		if(queueEnd < 0)
			return -1;

		for (Period p : activityList) {
			if (p.getType() == Instance.Activities.Standing && p.getStart() >= queueStart && p.getEnd() <= queueEnd) {
				totalServiceTime += p.getTime();
				services++;
			}
		}

		return totalServiceTime / (double) services;
	}

	public int getWalkingPeriods() {
		return walkingPeriods;
	}

	public int getStandingPeriods() {
		return standingPeriods;
	}

	public int getQueueingStartTime() {
		for (int i = 0; i < activityList.size(); i++) {
			Period p = activityList.get(i);
			if (p.getType() == Instance.Activities.Walking) {
				return activityList.get(i+1).getStart();
			}
		}
		return -1;
	}

	public int getQueueingEndTime() {
		for (int i = activityList.size()-1; i >= 0; i--) {
			Period p = activityList.get(i);
			if (p.getType() == Instance.Activities.Walking && p.getTime() > 10) {
				return activityList.get(i-1).getEnd();
			}
		}
		return -1;
	}

	public String toString() {
		String res = "Activity list: \n";
		res += "-----------------------\n";
		for (Period p : activityList) {
			res += p.toString() + "\n";
		}
		res += "-----------------------\n";
		return res;
	}
}
