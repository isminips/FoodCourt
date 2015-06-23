package com.example.foodcourt.knn;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

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
	public static final int NUM_RUNS = 1;
	public static double averageDistance = 0;


	public static Instance extractIndividualInstance(
			ArrayList<Instance> instances) {
		Random generator = new Random(new Date().getTime());
		int random = generator.nextInt(instances.size() - 1);

		Instance singleInstance = instances.get(random);
		instances.remove(random);

		return singleInstance;
	}

	public static void printClassificationInstance(
			Instance classificationInstance) {
		for (Feature f : classificationInstance.getAttributes()) {
			System.out.print(f.getName() + ": ");
			if (f instanceof Label) {
				System.out.println(((Label) f).getLabel().toString());
			} else if (f instanceof Magnitude) {
				System.out.println(((Magnitude) f).getMagnitude());
			}
			else if (f instanceof X) {
				System.out.println(((X) f).getX());
			}
			else if (f instanceof Y) {
				System.out.println(((Y) f).getY());
			}
			else if (f instanceof Z) {
				System.out.println(((Z) f).getZ());
			}else if (f instanceof Time) {
				System.out.println(((Time) f).getTime());
			}
		}
	}

	public static void printNeighbors(ArrayList<Neighbor> neighbors) {
		int i = 0;
		for (Neighbor neighbor : neighbors) {
			Instance instance = neighbor.getInstance();

			System.out.println("\nNeighbor " + (i + 1) + ", distance: "
					+ neighbor.getDistance());
			i++;
			for (Feature f : instance.getAttributes()) {
				System.out.print(f.getName() + ": ");
				if (f instanceof Label) {
					System.out.println(((Label) f).getLabel().toString());
				} else if (f instanceof Magnitude) {
					System.out.println(((Magnitude) f).getMagnitude());
				} else if (f instanceof Time) {
					System.out.println(((Time) f).getTime());
				}
			}
		}
	}

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
		ArrayList<Neighbor> distances;
		ArrayList<Neighbor> neighbors;
		Label.Activities classification;
		Instance classificationInstance;

		FileReader newReader = new FileReader(new ByteArrayInputStream(data.getBytes()));
		newInstances = newReader.buildInstances();

		int walking = 0, standing = 0;
		do {
			classificationInstance = newInstances.remove(0);

			distances = Knn.calculateDistances(trainingSet, classificationInstance);
			neighbors = Knn.getNearestNeighbors(distances);
			classification = Knn.determineMajority(neighbors);

			switch(classification) {
				case Walking: walking++; break;
				case Standing: standing++; break;
				default: throw new IllegalArgumentException("UNKNOWN classification");
			}
		} while (!newInstances.isEmpty());

		return walking >= standing ? Label.Activities.Walking : Label.Activities.Standing;
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
					double magnitude = ((Magnitude) f).getMagnitude();
					Magnitude singleInstanceMagnitude = (Magnitude) singleInstance.getAttributes().get(MAGNITUDE_INDEX);
					//distance += Math.pow((magnitude - singleInstanceMagnitude.getMagnitude()), 2);
					distance += Math.pow(((((Magnitude) f).getMagnitudeDiffFromMean() - singleInstanceMagnitude.getMagnitudeDiffFromMean())), 2);

				}

				else if (f instanceof X) {
					double x= ((X) f).getX();
					X singleInstanceX = (X) singleInstance
							.getAttributes().get(X_INDEX);
//					if (singleInstanceX.getX() >= maxX){
//						maxX=singleInstanceX.getX();
//						System.out.print("maxX is" +maxX);
//					}
//					if (singleInstanceX.getX() < minX){
//						minX=singleInstanceX.getX();
//						System.out.print("minX is" +maxX);
//					}
					//distance += Math.pow((x - singleInstanceX.getX()), 2);
//					distance += Math.abs(maxX - minX);
				}
				else if (f instanceof Y) {
					double y= ((Y) f).getY();
					Y singleInstanceY = (Y) singleInstance
							.getAttributes().get(Y_INDEX);
//					if (singleInstanceY.getY() >= maxY){
//						maxY=singleInstanceY.getY();
//						System.out.print("maxX is" +maxY);
//					}
//					if (singleInstanceY.getY() < minY){
//						minY=singleInstanceY.getY();
//						System.out.print("minX is" +minY);
//					}
				//distance += Math.pow((y - singleInstanceY.getY()), 2);
//					distance += Math.abs(maxY - minY);
				}
				else if (f instanceof Z) {
					double z= ((Z) f).getZ();
					Z singleInstanceZ = (Z) singleInstance
							.getAttributes().get(Z_INDEX);
					//distance += Math.pow((z - singleInstanceZ.getZ()), 2);
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