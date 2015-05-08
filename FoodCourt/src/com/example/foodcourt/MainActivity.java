package com.example.foodcourt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

	private float lastX, lastY, lastZ;

	private SensorManager sensorManager;
	private Sensor accelerometer;

	private float deltaXMax = 0;
	private float deltaYMax = 0;
	private float deltaZMax = 0;

	private float deltaX = 0;
	private float deltaY = 0;
	private float deltaZ = 0;
	private float[] preX = new float[100];
	private float[] preY = new float[100];
	private float[] preZ = new float[100];
	private int i = 0;
	private float[] points = new float[100];

	private float vibrateThreshold = 0;

	private TextView currentX, currentY, currentZ, maxX, maxY, maxZ, previousX,
			previousY, previousZ;
	private TextView test;
	private StringBuffer accel;
	private FileWriter writer;
	private String data;
	private float starttime = 0;
	private String status;

	public Vibrator v;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeViews();

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			// success! we have an accelerometer

			accelerometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);
			accel = new StringBuffer("\tx\ty\tz\n");
			vibrateThreshold = accelerometer.getMaximumRange() / 2;
		} else {
			// fai! we dont have an accelerometer!
		}

		// initialize vibration
		v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);

		final Button clear = (Button) findViewById(R.id.clear);
		clear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				test.setText("");
			}
		});
	}

	public void initializeViews() {
		currentX = (TextView) findViewById(R.id.currentX);
		currentY = (TextView) findViewById(R.id.currentY);
		currentZ = (TextView) findViewById(R.id.currentZ);
		test = (TextView) findViewById(R.id.test);

		maxX = (TextView) findViewById(R.id.maxX);
		maxY = (TextView) findViewById(R.id.maxY);
		maxZ = (TextView) findViewById(R.id.maxZ);
	}

	// onResume() register the accelerometer for listening the events
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		try {
			writer = new FileWriter("myfile.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// onPause() unregister the accelerometer for stop listening the events
	protected void onPause() {
		super.onPause();
		sensorManager.unregisterListener(this);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		// clean current values
		displayCleanValues();
		// display the current x,y,z accelerometer values
		displayCurrentValues();
		// display the max x,y,z accelerometer values
		displayMaxValues();

		// get the change of the x,y,z values of the accelerometer
		deltaX = Math.abs(lastX - event.values[0]);
		deltaY = Math.abs(lastY - event.values[1]);
		deltaZ = Math.abs(lastZ - event.values[2]);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		float timestamp;
		float now, time;
		if (starttime == 0) {
			starttime = event.timestamp;
		}
		now = event.timestamp;
		time = Math.round((now - starttime) / 1000000);
		// float time = lastupdate != 0 ? now - lastupdate : 0;
		// timestamp= System.currentTimeMillis();
		double magnitude = Math.sqrt(x * x + y * y + z * z);
		// test.append("X: " + x + ",Y:" + y + ",Z:" + z + "\n");
		test.append("Magnitude: " + magnitude + ",Time:" + time + ",Status:"
				+ status + "\n");
		data = test.getText().toString();

		// if the change is below 2, it is just plain noise
		if (deltaX < 2)
			deltaX = 0;
		if (deltaY < 2)
			deltaY = 0;
		if ((deltaZ > vibrateThreshold) || (deltaY > vibrateThreshold)
				|| (deltaZ > vibrateThreshold)) {
			v.vibrate(50);
		}

		test.setMovementMethod(new ScrollingMovementMethod());

	}

	public void displayCleanValues() {
		currentX.setText("0.0");
		currentY.setText("0.0");
		currentZ.setText("0.0");

	}

	// display the current x,y,z accelerometer values
	public void displayCurrentValues() {
		currentX.setText(Float.toString(deltaX));
		currentY.setText(Float.toString(deltaY));
		currentZ.setText(Float.toString(deltaZ));

	}

	// display the max x,y,z accelerometer values
	public void displayMaxValues() {

		if (deltaX > deltaXMax) {
			deltaXMax = deltaX;
			maxX.setText(Float.toString(deltaXMax));
		}
		if (deltaY > deltaYMax) {
			deltaYMax = deltaY;
			maxY.setText(Float.toString(deltaYMax));
		}
		if (deltaZ > deltaZMax) {
			deltaZMax = deltaZ;
			maxZ.setText(Float.toString(deltaZMax));
		}
	}

	public void startAcc(View view) {
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		starttime = 0;
	}

	public void clearText(View view) {
		System.out.println("Clear");
		test.setText("");

	}

	public void save(View view) {
		// try {
		// // accel = new StringBuffer("t\tx\ty\tz\n");
		//
		// String accel21 = test.toString();
		// // String accel21 = test.toString();
		// // String reps2= reps.toString();
		// OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
		// openFileOutput("accel.txt", Context.MODE_PRIVATE));
		// outputStreamWriter.write(accel21);
		// outputStreamWriter.close();
		// } catch (IOException e) {
		// Log.e("Exception", "File write failed: " + e.toString());
		// }

		try {
			File myFile = new File("/sdcard/mysdfile.txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(test.getText());
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getBaseContext(), "Done writing SD 'mysdfile.txt'",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}
	}// onClick

	public void change(View view) {
		// getApplicationContext().deleteFile("accel.txt");
		// accel = new StringBuffer("t\tx\ty\tz\n");
		if (status == "Standing") {
			status = "Walking";
		} else
			status = "Standing";

	}

	public void stopAcc(View view) throws IOException {
		sensorManager.unregisterListener(this);
		starttime = 0;

		System.out
				.println("KNN STUFF! ---------------------------------------");
		ArrayList<Instance> instances = null;
		ArrayList<Neighbor> distances = null;
		ArrayList<Neighbor> neighbors = null;
		Label.Activities classification = null;
		Instance classificationInstance = null;
		FileReader reader = null;
		int numRuns = 0, truePositives = 0, falsePositives = 0, falseNegatives = 0, trueNegatives = 0;
		double precision = 0, recall = 0, fMeasure = 0;

		falsePositives = 1;

		String fileName = "testing4.csv";
		String path = Environment.getExternalStorageDirectory() + "/"
				+ fileName;
		File file = new File(path);
		// FileInputStream fileInputStream = new FileInputStream(file);
		// InputStream stream = getAssets().open("testing4.csv");
		InputStream stream = new FileInputStream(file);

		reader = new FileReader(stream);
		instances = reader.buildInstances();

		do {
			classificationInstance = Knn.extractIndividualInstance(instances);

			distances = Knn.calculateDistances(instances,
					classificationInstance);
			neighbors = Knn.getNearestNeighbors(distances);
			classification = Knn.determineMajority(neighbors);

			System.out.println("Gathering " + Knn.K + " nearest neighbors to:");
			Knn.printClassificationInstance(classificationInstance);

			Knn.printNeighbors(neighbors);
			System.out.println("\nExpected situation result for instance: "
					+ classification.toString());

			if (classification.toString().equals(
					((Label) classificationInstance.getAttributes().get(
							Knn.LABEL_INDEX)).getLabel().toString())) {
				truePositives++;
			} else {
				falseNegatives++;
			}
			numRuns++;

			instances.add(classificationInstance);
		} while (numRuns < Knn.NUM_RUNS);

		precision = ((double) (truePositives / (double) (truePositives + falsePositives)));
		recall = ((double) (truePositives / (double) (truePositives + falseNegatives)));
		fMeasure = ((double) (precision * recall) / (double) (precision + recall));

		System.out.println("Precision: " + precision);
		System.out.println("Recall: " + recall);
		System.out.println("F-Measure: " + fMeasure);
		System.out
				.println("Average distance: "
						+ (double) (Knn.averageDistance / (double) (Knn.NUM_RUNS * Knn.K)));

	}

}
