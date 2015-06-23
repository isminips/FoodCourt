package com.example.foodcourt.knn;

public class Envelope extends Feature {

	private double envelope;

	public Envelope() {
		super("Envelope");
	}

	public Envelope(double envelope) {
		super("Envelope");
		this.envelope = envelope;
	}
	
	public void setEnvelope(double envelope) {
		this.envelope = envelope;
	}

	public double getEnvelope() {
		return envelope;
	}

	public double getEnvelopeDiffFromMean() {
		return Math.abs(envelope - 9.8);
	}

}
