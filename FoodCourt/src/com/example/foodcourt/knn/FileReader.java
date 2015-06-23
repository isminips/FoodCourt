package com.example.foodcourt.knn;

import java.io.BufferedReader;
import java.io.DataInputStream;
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
		BufferedReader reader = null;
		DataInputStream dis = null;
		ArrayList<Instance> instances = new ArrayList<Instance>();

		try {
			reader = new BufferedReader(new InputStreamReader(stream));



			// read the first Instance of the file
			String line;
			int numa = 1;
			Instance instance = null;
			ArrayList<Feature> attributes;

			while ((line = reader.readLine()) != null) {
				instance = new Instance();
				double ax = 0;
				double ay = 0;
				double az = 0;
				double amag = 0;
				attributes = new ArrayList<Feature>();
				String label = null;
				String time = null;
				for (int l = 0; l <= 10; l++) {
					line = reader.readLine();
					StringTokenizer st = new StringTokenizer(line, ",");


					numa++;
					if (Knn.NUM_ATTRS != st.countTokens()) {
						System.out.println("LINE: " + numa--);
						throw new Exception("Unknown number of attributes!");
					}

					label = st.nextToken();
					String x = st.nextToken();
					String y = st.nextToken();
					String z = st.nextToken();
					String magnitude = st.nextToken();
					time = st.nextToken();


					ax += Double.parseDouble(x) / l;
					ay += Double.parseDouble(y) / l;
					az += Double.parseDouble(z) / l;
					amag += Double.parseDouble(magnitude) / l;
					attributes.add(new Label(Label.determineActivity(label)));
					attributes.add(new X(ax));
					attributes.add(new Y(ay));
					attributes.add(new Z(az));
					attributes.add(new Magnitude(amag));
					attributes.add(new Time(Float.parseFloat(time)));


				}
				Label labelObj = (Label) attributes.get(Knn.LABEL_INDEX);
				instance.setLabel(labelObj.getLabel());

				instance.setAttributes(attributes);
				instances.add(instance);


			}

		} catch (IOException e) {
			System.out.println("Uh oh, got an IOException error: "
					+ e.getMessage());
		} catch (Exception e) {
			System.out.println("Uh oh, got an Exception error: "
					+ e.getMessage());
		} finally {
			if (dis != null) {
				try {
					dis.close();
				} catch (IOException ioe) {
					System.out
							.println("IOException error trying to close the file: "
									+ ioe.getMessage());
				}
			}
		}

		return instances;
	}
}