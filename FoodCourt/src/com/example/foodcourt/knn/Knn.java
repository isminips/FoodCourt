package com.example.foodcourt.knn;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class Knn {
	public static final int NUM_ATTRS = 6;
	public static final int K = 5;

	public static final int LABEL_INDEX = 0;
	public static final int X_INDEX = 1;
	public static final int Y_INDEX = 2;
	public static final int Z_INDEX = 3;
	public static final int MAGNITUDE_INDEX = 4;
	//public static final int ENVELOPE_INDEX = 5;
	public static final int TIME_INDEX = 5;
	public static double averageDistance = 0;

	public static Label.Activities determineMajority(
			ArrayList<Neighbor> neighbors) {
		int walking = 0, standing = 0;

		for (int i = 0; i < neighbors.size(); i++) {
			Neighbor neighbor = neighbors.get(i);
			Instance instance = neighbor.getInstance();
			if (instance.getLabel() == Label.Activities.Walking) {
				walking++;

			} else {
				standing++;
			}
		}

		if (walking > standing) {
			return Label.Activities.Walking;
		} else {
			return Label.Activities.Standing;
		}
	}

	public static Label.Activities classify(String data, ArrayList<Instance> trainingSet) {
		ArrayList<Instance> newInstances;
		Label.Activities classification;
		Instance classificationInstance;

		FileReader newReader = new FileReader(new ByteArrayInputStream(data.getBytes()));
		newInstances = newReader.buildInstances();

		int walking = 0, standing = 0;
		do {
			classificationInstance = newInstances.remove(0);
			classification = Knn.classify(classificationInstance, trainingSet);

			switch(classification) {
				case Walking: walking++; break;
				case Standing: standing++; break;
				default: throw new IllegalArgumentException("UNKNOWN classification");
			}
		} while (!newInstances.isEmpty());

		return walking >= standing ? Label.Activities.Walking : Label.Activities.Standing;
	}

	public static Label.Activities classify(Instance instance, ArrayList<Instance> trainingSet) {
		ArrayList<Neighbor> distances = Knn.calculateDistances(trainingSet, instance);
		ArrayList<Neighbor> neighbors = Knn.getNearestNeighbors(distances);
		return Knn.determineMajority(neighbors);
	}

	public static ArrayList<Neighbor> getNearestNeighbors(
			ArrayList<Neighbor> distances) {
		ArrayList<Neighbor> neighbors = new ArrayList<Neighbor>();

		for (int i = 0; i < K; i++) {
			averageDistance += distances.get(i).getDistance();

			neighbors.add(distances.get(i));
		}

		return neighbors;
	}

	public static ArrayList<Neighbor> calculateDistances(
			ArrayList<Instance> instances, Instance singleInstance) {
		ArrayList<Neighbor> distances = new ArrayList<Neighbor>();
		Neighbor neighbor = null;
		int distance = 0;
		double maxM=9.8;
		double minM=9.8;

		for (int i = 0; i < instances.size(); i++) {
			Instance instance = instances.get(i);
			distance = 0;
			neighbor = new Neighbor();

			// for each feature, go through and calculate the "distance"
			for (Feature f : instance.getAttributes()) {
				if (f instanceof Magnitude) {
					double magnitude = ((Magnitude) f).getValue();
					Magnitude singleInstanceMagnitude = (Magnitude) singleInstance.getAttributes().get(MAGNITUDE_INDEX);
					//distance += Math.pow((magnitude - singleInstanceMagnitude.getValue()), 2);
					distance += Math.pow(((((Magnitude) f).getMagnitudeDiffFromMean() - singleInstanceMagnitude.getMagnitudeDiffFromMean())), 2);

				}

				else if (f instanceof X) {
					double x= ((X) f).getValue();
					X singleInstanceX = (X) singleInstance
							.getAttributes().get(X_INDEX);
//					if (singleInstanceX.getValue() >= maxX){
//						maxX=singleInstanceX.getValue();
//						System.out.print("maxX is" +maxX);
//					}
//					if (singleInstanceX.getValue() < minX){
//						minX=singleInstanceX.getValue();
//						System.out.print("minX is" +maxX);
//					}
					//distance += Math.pow((x - singleInstanceX.getValue()), 2);
//					distance += Math.abs(maxX - minX);
				}
				else if (f instanceof Y) {
					double y= ((Y) f).getValue();
					Y singleInstanceY = (Y) singleInstance
							.getAttributes().get(Y_INDEX);
//					if (singleInstanceY.getValue() >= maxY){
//						maxY=singleInstanceY.getValue();
//						System.out.print("maxX is" +maxY);
//					}
//					if (singleInstanceY.getValue() < minY){
//						minY=singleInstanceY.getValue();
//						System.out.print("minX is" +minY);
//					}
					//distance += Math.pow((y - singleInstanceY.getValue()), 2);
//					distance += Math.abs(maxY - minY);
				}
				else if (f instanceof Z) {
					double z= ((Z) f).getValue();
					Z singleInstanceZ = (Z) singleInstance
							.getAttributes().get(Z_INDEX);
					//distance += Math.pow((z - singleInstanceZ.getValue()), 2);
				}

				else if (f instanceof Time) {
					// Do nothing!
				} else if (f instanceof Label) {
					// Do nothing!
				} else {
					System.out
							.println("Unknown category in distance calculation.  Exiting for debug: "
									+ f);
					System.exit(1);
				}
			}
			neighbor.setDistance(distance);
			neighbor.setInstance(instance);

			distances.add(neighbor);
		}

		for (int i = 0; i < distances.size(); i++) {
			for (int j = 0; j < distances.size() - i - 1; j++) {
				if (distances.get(j).getDistance() > distances.get(j + 1)
						.getDistance()) {
					Neighbor tempNeighbor = distances.get(j);
					distances.set(j, distances.get(j + 1));
					distances.set(j + 1, tempNeighbor);
				}
			}
		}

		return distances;
	}

}