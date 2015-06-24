package com.example.foodcourt.knn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class FileReader {
	private InputStream stream;

	public FileReader(InputStream stream) {
		this.stream = stream;
	}

	public ArrayList<Instance> buildInstances() {
		BufferedReader reader;
		ArrayList<Instance> instances = new ArrayList<Instance>();

		try {
			reader = new BufferedReader(new InputStreamReader(stream));

			String line;
			int numa = 1;
			Instance instance;

			while ((line = reader.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line, ",");

				numa++;
				if (Knn.NUM_ATTRS != st.countTokens()) {
					System.out.println("LINE: " + numa--);
					throw new Exception("Unknown number of attributes!");
				}

				String label = st.nextToken();
				String meanMagnitude = st.nextToken();
				String maxMagnitude = st.nextToken();
				String varianceMagnitude = st.nextToken();
				String time = st.nextToken();

				instance = new Instance(
						label,
						Double.parseDouble(meanMagnitude),
						Double.parseDouble(maxMagnitude),
						Double.parseDouble(varianceMagnitude),
						Float.parseFloat(time)
				);

				instances.add(instance);
			}

		} catch (IOException e) {
			System.out.println("Uh oh, got an IOException error: "
					+ e.getMessage());
		} catch (Exception e) {
			System.out.println("Uh oh, got an Exception error: "
					+ e.getMessage());
		}

		return instances;
	}
}