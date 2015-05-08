package com.example.foodcourt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private TextView test;
	private String data;
	private float starttime = 0;
	private String status = "Standing";

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

		} else {
			// fail! we dont have an accelerometer!
		}

		final Button clear = (Button) findViewById(R.id.clear);
		clear.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Clear text view on click
				test.setText("");
			}
		});
	}

	public void initializeViews() {

		test = (TextView) findViewById(R.id.test);

	}

	// onResume() register the accelerometer for listening the events
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);

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

		double magnitude = Math.sqrt(x * x + y * y + z * z);
		test.append(status + "," + magnitude + "," + time + "\n");
		data = test.getText().toString();

		test.setMovementMethod(new ScrollingMovementMethod());

	}

	public void startAcc(View view) {
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
		starttime = 0;
	}

	public void save(View view) {

		try {
			File myFile = new File("/sdcard/mysdfile.txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(test.getText());
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getBaseContext(), "Done writing file in SD",
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	public void change(View view) {

		if (status == "Standing") {
			status = "Walking";
			Button p1_button = (Button) findViewById(R.id.change);
			p1_button.setText("Walking");
		} else {
			status = "Standing";
			Button p1_button = (Button) findViewById(R.id.change);
			p1_button.setText("Standing");
		}

	}

	public void readfile(View view) throws IOException {

		starttime = 0;

		System.out
				.println("KNN STUFF! ---------------------------------------");
		ArrayList<Instance> newInstances = null;
		ArrayList<Instance> trainInstances = null;
		ArrayList<Neighbor> distances = null;
		ArrayList<Neighbor> neighbors = null;
		Label.Activities classification = null;
		Instance classificationInstance = null;
		FileReader newReader = null;
		FileReader trainReader = null;

		File file = new File("/sdcard/mysdfile.txt");
		InputStream trainStream = getAssets().open("testing4.csv");
		FileInputStream newStream = new FileInputStream(file);
		Toast.makeText(getBaseContext(), "Reading File", Toast.LENGTH_SHORT)
				.show();
		newReader = new FileReader(newStream);
		trainReader = new FileReader(trainStream);
		newInstances = newReader.buildInstances();
		trainInstances = trainReader.buildInstances();

		do {
			classificationInstance = newInstances.remove(0);

			distances = Knn.calculateDistances(trainInstances,
					classificationInstance);
			neighbors = Knn.getNearestNeighbors(distances);
			classification = Knn.determineMajority(neighbors);

			// Knn.printNeighbors(neighbors);
			System.out.println("\n-----Instance-----: ");
			Knn.printClassificationInstance(classificationInstance);
			System.out.println("\nExpected situation result for instance: "
					+ classification.toString());
		} while (!newInstances.isEmpty());

	}

	public void stopAcc(View view) {
		sensorManager.unregisterListener(this);
		starttime = 0;
	}
}
