package com.example.foodcourt.knn;

import com.example.foodcourt.activity.Measurement;

import java.util.ArrayList;

public class Knn {
	public static final int NUM_ATTRS = 5;
	public static final int K = 5;

	public static Instance createInstanceFromMeasurements(ArrayList<Measurement> measurements, String trainingStatus) {
		int count = measurements.size();

		double sumMagnitude = 0;
		double maxMagnitude = 0;
		float time = 0;

		for (Measurement line : measurements) {
			sumMagnitude += line.getMagnitude();

			if (line.getMagnitude() > maxMagnitude) {
				maxMagnitude = line.getMagnitude();
			}

			time = line.getTime();
		}

		double meanMagnitude = sumMagnitude / count;

		double varianceMagnitude = 0;
		for (Measurement line : measurements) {
			varianceMagnitude += Math.pow(line.getMagnitude() - meanMagnitude, 2);
		}
		varianceMagnitude /= count;

		// HERE WE SHOULD CREATE FEATURES
		// like mean magnitude, std magnitude, mean x, mean y.. etc
		return new Instance(trainingStatus, meanMagnitude, maxMagnitude, varianceMagnitude, time);
	}

	public static Instance.Activities classify(Instance instance, ArrayList<Instance> trainingSet) {
		ArrayList<Neighbor> distances = Knn.calculateDistances(trainingSet, instance);
		ArrayList<Neighbor> neighbors = Knn.getNearestNeighbors(distances);
		return Knn.determineMajority(neighbors);
	}

	private static Instance.Activities determineMajority(ArrayList<Neighbor> neighbors) {
		int walking = 0, standing = 0;

		for (int i = 0; i < neighbors.size(); i++) {
			Neighbor neighbor = neighbors.get(i);
			Instance instance = neighbor.getInstance();
			if (instance.getLabel() == Instance.Activities.Walking) {
				walking++;
			} else if (instance.getLabel() == Instance.Activities.Standing) {
				standing++;
			}
		}

		if (walking > standing) {
			return Instance.Activities.Walking;
		} else {
			return Instance.Activities.Standing;
		}
	}

	private static ArrayList<Neighbor> getNearestNeighbors(ArrayList<Neighbor> distances) {
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();

		for (int i = 0; i < K; i++) {
			neighbors.add(distances.get(i));
		}

		return neighbors;
	}

	private static ArrayList<Neighbor> calculateDistances(ArrayList<Instance> instances, Instance instance) {
		ArrayList<Neighbor> distances = new ArrayList<Neighbor>();

		for (int i = 0; i < instances.size(); i++) {
			Instance other = instances.get(i);
			double distance = 0;

			// for each feature, go through and calculate the "distance"
			distance += Math.pow((other.getMeanMagnitude() - instance.getMeanMagnitude()), 2);
			distance += Math.pow((other.getMaxMagnitude() - instance.getMaxMagnitude()), 2);
			distance += Math.pow((other.getVarianceMagnitude() - instance.getVarianceMagnitude()), 2);

			Neighbor neighbor = new Neighbor(other, distance);
			distances.add(neighbor);
		}

		for (int i = 0; i < distances.size(); i++) {
			for (int j = 0; j < distances.size() - i - 1; j++) {
				if (distances.get(j).getDistance() > distances.get(j + 1).getDistance()) {
					Neighbor tempNeighbor = distances.get(j);
					distances.set(j, distances.get(j + 1));
					distances.set(j + 1, tempNeighbor);
				}
			}
		}

		return distances;
	}

}