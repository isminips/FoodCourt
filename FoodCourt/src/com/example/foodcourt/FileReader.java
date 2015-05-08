package com.example.foodcourt;

import java.io.*;
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
           reader = new BufferedReader(new InputStreamReader(stream));;
           
           // read the first Instance of the file
           String line;
           int numa=1;
           Instance instance = null;
           ArrayList<Feature> attributes;
           while ((line = reader.readLine()) != null) {
              StringTokenizer st = new StringTokenizer(line, ",");
              attributes = new ArrayList<Feature>();
              instance = new Instance();
              
              numa++;
              if(Knn.NUM_ATTRS != st.countTokens()) {
            	  System.out.println("LINE: " + numa--);
            	  throw new Exception("Unknown number of attributes!");
              }

              String label = st.nextToken();
              String magnitude = st.nextToken();
              String time = st.nextToken();

              attributes.add(new Label(Label.determineActivity(label)));
			  attributes.add(new Magnitude(Double.parseDouble(magnitude)));
			  attributes.add(new Time(Integer.parseInt(time)));

               Label labelObj = (Label) attributes.get(Knn.LABEL_INDEX);
			  instance.setLabel(labelObj.getLabel());

			  instance.setAttributes(attributes);
			  instances.add(instance);
           }

        } 
        catch (IOException e) { 
           System.out.println("Uh oh, got an IOException error: " + e.getMessage()); 
        } 
        catch (Exception e) {
            System.out.println("Uh oh, got an Exception error: " + e.getMessage()); 
        }
        finally { 
           if (dis != null) {
              try {
                 dis.close();
              } catch (IOException ioe) {
                 System.out.println("IOException error trying to close the file: " + ioe.getMessage()); 
              }
           }
        }
        
		return instances;
	}
}