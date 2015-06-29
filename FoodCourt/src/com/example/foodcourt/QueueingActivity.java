package com.example.foodcourt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.foodcourt.activity.ActivityList;
import com.example.foodcourt.knn.FileReader;
import com.example.foodcourt.knn.Instance;
import com.example.foodcourt.knn.Knn;
import com.example.foodcourt.activity.Measurement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class QueueingActivity extends BaseActivity implements SensorEventListener {

	public static final int TIER_1_SAMPLING = 10;
	public static final int TIER_2_SAMPLING = 4;

	private SensorManager sensorManager;
	private Sensor accelerometer;
	private TextView currentActivityLabel;
	private TextView currentActivityTimeLabel;
	private Button clearButton;
	private Instance.Activities tier2CurrentActivity;
	private long tier2CurrentActivityStartTime;
	private int tier2ActivitySwitches;
	private ArrayList<Measurement> data;
	private String saving_data;
	private long startTime;
	private ActivityList activityList;
	private Instance.Activities trainingStatus = Instance.Activities.Standing;
	ArrayList<Instance> trainingSet = null;
	ArrayList<Instance> tier1 = null;
	ArrayList<Instance> tier2 = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		try {
			Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

				@Override
				public void uncaughtException(Thread t, Throwable e) {
					log("Uncaught Exception detected in thread {}" + t);
					e.printStackTrace();
				}
			});
		} catch (SecurityException e) {
			log("Could not set the Default Uncaught Exception Handler");
			e.printStackTrace();
		}

		initializeViews();
		initializeAcc();

		try {
			trainingSet = loadTrainingSet("trainingSet9.csv");
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
		setContentView(R.layout.activity_queueing);
		currentActivityLabel = (TextView) findViewById(R.id.currentActivityLabel);
		currentActivityTimeLabel = (TextView) findViewById(R.id.currentActivityTimeLabel);
		clearButton = (Button) findViewById(R.id.clear);
	}

	private void reset() {
		data = new ArrayList<Measurement>();
		saving_data = "";
		tier2ActivitySwitches = 0;
		tier2CurrentActivityStartTime = 0;
		tier2CurrentActivity = null;
		currentActivityLabel.setText("");
		currentActivityTimeLabel.setText("");
		startTime = System.currentTimeMillis();
		activityList = new ActivityList();
		tier1 = new ArrayList<Instance>();
		tier2 = new ArrayList<Instance>();
		clearButton.setText("Clear");
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
		long time = System.currentTimeMillis() - startTime;

		Measurement measurement = new Measurement(x, y, z, time);

		data.add(measurement);
		//log("Acc data [" + data.size() + "]:" + measurement);

		if (data.size() >= TIER_1_SAMPLING) {
			tier1.add(tier1classify(data));
			data.clear();

			if (tier1.size() % TIER_2_SAMPLING == 0) {
				tier2.add(tier2classify(tier1.subList(tier1.size() - TIER_2_SAMPLING, tier1.size() - 1)));
				clearButton.setText("Clear ("+tier2.size()+")");

				if (tier2CurrentActivity == Instance.Activities.Walking && tier2ActivitySwitches > 2 && (tier2.get(tier2.size() - 1).getTime() - tier2CurrentActivityStartTime) > 10000) {
					stop(null);
				}
			}
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
	private void initializeAcc() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		startAcc();
	}

	public void startAcc() {
		if (accelerometer != null) {
			sensorManager.registerListener(this, accelerometer,	SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			log("Error: No accelerometer found");
		}
	}

	public void stopAcc() {
		sensorManager.unregisterListener(this);
	}

	// CLASSIFICATION
	public Instance tier1classify(ArrayList<Measurement> data) {
		Instance classificationInstance = Knn.createInstanceFromMeasurements(data, trainingStatus.toString());

		// For creating a training set, keep this before the setting of the classified label!
		saving_data += classificationInstance + "\n";

		Instance.Activities currentActivity = Knn.classify(classificationInstance, trainingSet);
		classificationInstance.setLabel(currentActivity);

		currentActivityLabel.setText(currentActivity.toString());
		log("Tier 1: " + classificationInstance);

		return classificationInstance;
	}

	public Instance tier2classify(List<Instance> tier1data) {
		int walking = 0, standing = 0;
		long time = 0;

		for (Instance tier1activity : tier1data) {
			time = tier1activity.getTime();

			switch (tier1activity.getLabel()) {
				case Walking:
					walking++;
					break;
				case Standing:
					standing++;
					break;
				default:
					throw new IllegalArgumentException("UNKNOWN classification");
			}
		}

		Instance.Activities label = Instance.determineActivity(standing, walking);
		Instance tier2Activity = new Instance();
		tier2Activity.setLabel(label);
		tier2Activity.setTime(time);

		log("Tier 2: " + label + " - time: " + time);

		if (tier2CurrentActivity != label) {
			tier2ActivitySwitches++;
			tier2CurrentActivity = label;

			if (tier2.isEmpty())
				tier2CurrentActivityStartTime = 0;
			else
				tier2CurrentActivityStartTime = tier2.get(tier2.size() - 1).getTime();
		}
		currentActivityTimeLabel.setText(tier2CurrentActivity + " " + msToS(time - tier2CurrentActivityStartTime) + "s");

		return tier2Activity;
	}

	private void calculateTimes() {
		activityList = new ActivityList();

		for (Instance tier2activity : tier2) {
			activityList.add(tier2activity.getLabel(), tier2activity.getTime());
		}

		log(activityList.toString());
		long totalQueueingTime = activityList.totalQueueingTime();
		long averageServiceTime = activityList.averageServiceTime();
		if (totalQueueingTime > 0 && averageServiceTime > 0) {
			showInfo("Queueing information",
					"Total queueing time: " + msToS(totalQueueingTime) + "\n" + "Average service time: " + msToS(averageServiceTime) + "\n" + "People in front: " + (activityList.getServices()-1));
		} else {
			showInfo("Queueing not finished", "Please try again");
		}
	}

	private double msToS(long ms) {
		return (double) Math.round(ms/100) / 10;
	}

	// CREATE TRAINING DATA
	public void save(View view) {
		try {
			File myFile = new File("/sdcard/acceleration_"+ startTime +".txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(saving_data);
			myOutWriter.close();
			fOut.close();
			toast("Done writing file in SD");
			log(saving_data);
		} catch (Exception e) {
			toast(e.getMessage());
		}
	}

	public void changeLoggingActivity(View view) {
		if (trainingStatus == Instance.Activities.Standing) {
			trainingStatus = Instance.Activities.Walking;
		} else {
			trainingStatus = Instance.Activities.Standing;
		}

		Button p1_button = (Button) findViewById(R.id.change);
		p1_button.setText(trainingStatus.toString());
	}

}
