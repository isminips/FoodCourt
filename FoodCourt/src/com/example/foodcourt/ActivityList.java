package com.example.foodcourt;

import java.util.ArrayList;

public class ActivityList {

	private ArrayList<Period> activityList;
	private int walkingPeriods = 0;
	private int standingPeriods = 0;

	public ActivityList() {
		this.activityList = new ArrayList<Period>();
	}

	public void add(Label.Activities type, int time) {
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
			if (p.getType() == Label.Activities.Walking) {
				time += p.getTime();
			}
		}
		return time;
	}

	public int totalStandingTime() {
		int time = 0;
		for (Period p : activityList) {
			if (p.getType() == Label.Activities.Standing) {
				time += p.getTime();
			}
		}
		return time;
	}

	public int totalQueueingTime() {
		int time = 0;
		for (Period p : activityList) {
			if (p.getType() == Label.Activities.Standing) {
				time += p.getTime();
			}
		}
		return time;
	}

	public double averageServiceTime() {
		return (double) totalStandingTime() / (double) standingPeriods;
	}

	public int getWalkingPeriods() {
		return walkingPeriods;
	}

	public int getStandingPeriods() {
		return standingPeriods;
	}

	public String toString() {
		String res = "Activity list: \n";
		for (Period p : activityList) {
			res += p.toString() + "\n";
		}
		return res;
	}
}
