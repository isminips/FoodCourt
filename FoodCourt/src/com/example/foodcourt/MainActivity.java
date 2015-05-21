package com.example.foodcourt;

import java.io.ByteArrayInputStream;
import java.io.File;
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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    private final int TIER_1_SAMPLING_TIME = 1000;
    private SensorManager sensorManager;
	private Sensor accelerometer;
	private TextView currentActivityLabel;
	private String data = "";
	private float starttime = 0;
	private String status = "Standing";
    ArrayList<Instance> trainingSet = null;
    ArrayList<Label.Activities> currentSession = null;

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

        currentSession = new ArrayList<Label.Activities>();

		try {
			InputStream trainStream = getAssets().open("trainingSet.csv");
			FileReader trainReader = new FileReader(trainStream);
			trainingSet = trainReader.buildInstances();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void initializeViews() {
		currentActivityLabel = (TextView) findViewById(R.id.currentActivityLabel);
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
		float now, time;
		if (starttime == 0) {
			starttime = event.timestamp;
		}
		now = event.timestamp;
		time = Math.round((now - starttime) / 1000000);

		double magnitude = Math.sqrt(x * x + y * y + z * z);
		data += status + "," + magnitude + "," + time + "\n";

		if (time > TIER_1_SAMPLING_TIME) {
			currentSession.add(classify());
			data = "";
		}
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
			myOutWriter.append(data);
			myOutWriter.close();
			fOut.close();
			Toast.makeText(getBaseContext(), "Done writing file in SD",
					Toast.LENGTH_SHORT).show();
			System.out.println(data);
		} catch (Exception e) {
			Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

	}

	public void change(View view) {
		if (status.equals("Standing")) {
			status = "Walking";
			Button p1_button = (Button) findViewById(R.id.change);
			p1_button.setText("Walking");
		} else {
			status = "Standing";
			Button p1_button = (Button) findViewById(R.id.change);
			p1_button.setText("Standing");
		}
	}

	public Label.Activities classify() {
		starttime = 0;

		ArrayList<Instance> newInstances = null;
		ArrayList<Neighbor> distances = null;
		ArrayList<Neighbor> neighbors = null;
		Label.Activities classification = null;
		Instance classificationInstance = null;

		FileReader newReader = new FileReader(new ByteArrayInputStream(data.getBytes()));
		newInstances = newReader.buildInstances();

		int walking = 0, standing = 0;
		do {
			classificationInstance = newInstances.remove(0);

			distances = Knn.calculateDistances(trainingSet,	classificationInstance);
			neighbors = Knn.getNearestNeighbors(distances);
			classification = Knn.determineMajority(neighbors);

			switch(classification) {
				case Walking: walking++; break;
				case Standing: standing++; break;
				default: throw new IllegalArgumentException("UNKNOWN classification");
			}
		} while (!newInstances.isEmpty());

		Label.Activities currentActivity = walking >= standing ? Label.Activities.Walking : Label.Activities.Standing;
		currentActivityLabel.setText(currentActivity.toString());

        System.out.println("Current activity: "+currentActivity.toString());

        return currentActivity;
	}

	public void stopAcc(View view) {
		sensorManager.unregisterListener(this);
		starttime = 0;
        System.out.println("------LIST OF CLASSIFICATIONS------");
        for (Label.Activities label : currentSession) {
            System.out.println(label);
        }
	}
}
