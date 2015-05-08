package com.example.foodcourt;

import java.util.ArrayList;

public class Instance {
	private ArrayList<Feature> attributes;
	private Label.Activities label;

	public void setAttributes(ArrayList<Feature> attributes) {
		this.attributes = attributes;
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
}
