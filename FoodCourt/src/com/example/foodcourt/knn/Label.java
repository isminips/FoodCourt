package com.example.foodcourt.knn;

public class Label extends Feature {

	public enum Activities {
		Standing,
		Walking
	}
	
	private Activities label;

	public Label() {
		super("Label");
	}
	
	public Label(Activities label) {
		super("Label");
		this.label = label;
	}

	public void setLabel(Activities label) {
		this.label = label;
	}

	public Activities getLabel() {
		return label;
	}
	
	public static Activities determineActivity(String activity) {
		if(activity.equals("Standing")) {
			return Activities.Standing;
		}
		else if(activity.equals("Walking")) {
			return Activities.Walking;
		}
		return null;
	}

}
