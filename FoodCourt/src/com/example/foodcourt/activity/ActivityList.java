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
	public void add(Instance.Activities type, float time) {
		if(activityList.size() > 0 && activityList.get(activityList.size()-1).getType() == type) {
			activityList.get(activityList.size()-1).setEnd(time);
		} else {
			float starttime = 0;
			if (activityList.size() > 0) {
				starttime = activityList.get(activityList.size()-1).getEnd();
			}
			Period newPeriod = new Period(type, starttime);
			newPeriod.setEnd(time);
			activityList.add(newPeriod);

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

	public float totalQueueingTime() {
		float queueStart = getQueueingStartTime();
		float queueEnd = getQueueingEndTime();

		if(queueEnd < 0)
			return -1;

		return queueEnd - queueStart;
	}

	public double averageServiceTime() {
		double totalServiceTime = 0;
		int services = 0;
		float queueStart = getQueueingStartTime();
		float queueEnd = getQueueingEndTime();

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

	public float getQueueingStartTime() {
		for (int i = 0; i < activityList.size(); i++) {
			Period p = activityList.get(i);
			if (p.getType() == Instance.Activities.Walking) {
				return activityList.get(i+1).getStart();
			}
		}
		return -1;
	}

	public float getQueueingEndTime() {
		for (int i = activityList.size()-1; i >= 0; i--) {
			Period p = activityList.get(i);
			if (p.getType() == Instance.Activities.Walking && p.getTime() > 10000) {
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
