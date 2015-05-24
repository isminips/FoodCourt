package com.example.foodcourt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.foodcourt.knn.FileReader;
import com.example.foodcourt.knn.Instance;
import com.example.foodcourt.knn.Knn;
import com.example.foodcourt.knn.Label;
import com.example.foodcourt.knn.Neighbor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class ActivityMonitoringActivity extends BaseActivity implements SensorEventListener {

    private final int TIER_1_SAMPLING_TIME = 1000;
    private final int TIER_2_SAMPLING_TIME = 2;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private TextView currentActivityLabel;
	private String data;
	private float starttime;
	private ActivityList activityList;
	private Label.Activities trainingStatus = Label.Activities.Standing;
    ArrayList<Instance> trainingSet = null;
    ArrayList<Label.Activities> tier1 = null;
    ArrayList<Label.Activities> tier2 = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeViews();


        try {
            initializeAcc();
        } catch (Exception e) {
            e.printStackTrace();
        }

		try {
            trainingSet = loadTrainingSet("trainingSet.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}

		reset();
	}

	private ArrayList<Instance> loadTrainingSet(String filename) throws IOException {
        InputStream trainStream = getAssets().open(filename);
        FileReader trainReader = new FileReader(trainStream);
        return trainReader.buildInstances();
    }

    public void initializeViews() {
		setContentView(R.layout.activity_activity_monitoring);
		currentActivityLabel = (TextView) findViewById(R.id.currentActivityLabel);
	}

	private void reset() {
		data = "";
		starttime = 0;
		activityList = new ActivityList();
		tier1 = new ArrayList<Label.Activities>();
		tier2 = new ArrayList<Label.Activities>();
	}

	// onResume() register the accelerometer for listening the events
	protected void onResume() {
		super.onResume();
		startAcc();
	}

	// onPause() unregister the accelerometer for stop listening the events
	protected void onPause() {
		super.onPause();
		stopAcc();
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
		data += trainingStatus.toString() + "," + magnitude + "," + time + "\n";

		if (time > TIER_1_SAMPLING_TIME) {
			tier1.add(classify());
			data = "";
		}
	}

	// BUTTONS
	public void start(View view) {
		Button btn = (Button) findViewById(R.id.startstop);
		btn.setText("Stop");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stop(v);
			}
		});

        startAcc();
	}

	public void stop(View view) {
		Button btn = (Button) findViewById(R.id.startstop);
		btn.setText("Start");
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				start(v);
			}
		});

		stopAcc();

		calculateTimes();
	}

	public void clear(View view) {
		reset();
	}

	// ACCELEROMETER
	private void initializeAcc() throws Exception {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			// success! we have an accelerometer

			accelerometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_NORMAL);

		} else {
			throw new Exception("No accelerometer found");
		}
	}

	public void startAcc() {
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void stopAcc() {
		sensorManager.unregisterListener(this);
	}

	// CLASSIFICATION
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

			distances = Knn.calculateDistances(trainingSet, classificationInstance);
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

		log("Current activity: " + currentActivity.toString());

		return currentActivity;
	}

	private void calculateTimes() {
		int walking = 0, standing = 0;
		int time = 0;
		int processing = 0;
		activityList = new ActivityList();
		for (Label.Activities tier1activity : tier1) {
			time++;
			processing++;
			activityList.add(tier1activity, time);
			switch(tier1activity) {
				case Walking:
					walking++;
					break;
				case Standing:
					standing++;
					break;
				default: throw new IllegalArgumentException("UNKNOWN classification");
			}
			if (processing >= TIER_2_SAMPLING_TIME) {
				Label.Activities tier2Activity = walking >= standing ? Label.Activities.Walking : Label.Activities.Standing;
				tier2.add(tier2Activity);
				processing = 0; walking = 0; standing = 0;
			}
		}

		log(activityList.toString());
		int totalQueueingTime = activityList.totalQueueingTime();
		double averageServiceTime = activityList.averageServiceTime();
		if (totalQueueingTime > 0 && averageServiceTime > 0) {
			showInfo("Queueing information",
					"Total queueing time: " + activityList.totalQueueingTime() + "\n" +
							"Average service time: " + activityList.averageServiceTime());
		} else {
			showInfo("Queueing not finished", "Please try again");
		}
	}

	// CREATE TRAINING DATA
	public void save(View view) {
		try {
			File myFile = new File("/sdcard/mysdfile.txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(data);
			myOutWriter.close();
			fOut.close();
			toast("Done writing file in SD");
			log(data);
		} catch (Exception e) {
			toast(e.getMessage());
		}

	}

	public void changeLoggingActivity(View view) {
		if (trainingStatus == Label.Activities.Standing) {
			trainingStatus = Label.Activities.Walking;
		} else {
			trainingStatus = Label.Activities.Walking;
		}

		Button p1_button = (Button) findViewById(R.id.change);
		p1_button.setText(trainingStatus.toString());
	}

}
