package com.example.foodcourt.knn;

import java.util.ArrayList;

public class Instance {
	private ArrayList<Feature> attributes;
	private Label.Activities label;

	public void setAttributes(ArrayList<Feature> attributes) {
		this.attributes = attributes;
	}

	public ArrayList<Feature> createAttributes(String label, double x, double y, double z, double magnitude, String time) {
		attributes = new ArrayList<Feature>();

		attributes.add(new Label(Label.determineActivity(label)));
		attributes.add(new X(x));
		attributes.add(new Y(y));
		attributes.add(new Z(z));
		attributes.add(new Magnitude(magnitude));
		attributes.add(new Time(Float.parseFloat(time)));

		return attributes;
	}

	public ArrayList<Feature> getAttributes() {
		return attributes;
	}

	public void setLabel(Label.Activities label) {
		this.label = label;
	}

	public Label.Activities getLabel() {
		return label;
	}

	public String toString() {
		String result = "";

		for (int i = 0; i < attributes.size(); i++) {
			result += attributes.get(i).toString();
			if (i < attributes.size() - 1) {
				result += ",";
			}
		}

		return result;
	}
}
