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

public class ActivityMonitoringActivity extends BaseActivity implements SensorEventListener {

    public static final int TIER_1_SAMPLING = 10;
    public static final int TIER_2_SAMPLING = 5;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private TextView currentActivityLabel;
	private ArrayList<Measurement> data;
	private String saving_data="";
	private float starttime;
	private ActivityList activityList;
	private Instance.Activities trainingStatus = Instance.Activities.Standing;
    ArrayList<Instance> trainingSet = null;
    ArrayList<Instance> tier1 = null;
    ArrayList<Instance> tier2 = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initializeViews();

        try {
            initializeAcc();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
		setContentView(R.layout.activity_activity_monitoring);
		currentActivityLabel = (TextView) findViewById(R.id.currentActivityLabel);
	}

	private void reset() {
		data = new ArrayList<Measurement>();
		starttime = 0;
		activityList = new ActivityList();
		tier1 = new ArrayList<Instance>();
		tier2 = new ArrayList<Instance>();
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

		Measurement measurement = new Measurement(x, y, z, time);

		data.add(measurement);
		log("Acc data [" + data.size() + "]:" + measurement);

		if (data.size() >= TIER_1_SAMPLING) {
			tier1.add(classify());
			data.clear();
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
	public Instance classify() {
		Instance classificationInstance = Knn.createInstanceFromMeasurements(data, trainingStatus.toString());

		// For creating a training set, keep this before the setting of the classified label!
		saving_data += classificationInstance + "\n";

		Instance.Activities currentActivity = Knn.classify(classificationInstance, trainingSet);
		classificationInstance.setLabel(currentActivity);

		currentActivityLabel.setText(currentActivity.toString());
		log("Current activity: " + currentActivity.toString() + " - instance: " + classificationInstance);

		return classificationInstance;
	}

	private void calculateTimes() {
		int walking = 0, standing = 0;
		int processing = 0;
		activityList = new ActivityList();
		for (Instance tier1activity : tier1) {
			processing++;
			switch(tier1activity.getLabel()) {
				case Walking:
					walking++;
					break;
				case Standing:
					standing++;
					break;
				default: throw new IllegalArgumentException("UNKNOWN classification");
			}
			if (processing >= TIER_2_SAMPLING) {
				Instance tier2Activity = new Instance();
				Instance.Activities label = walking >= standing ? Instance.Activities.Walking : Instance.Activities.Standing;
				tier2Activity.setLabel(label);
				tier2Activity.setTime(tier1activity.getTime());
				tier2.add(tier2Activity);
				processing = 0; walking = 0; standing = 0;
			}
		}

		for (Instance tier2activity : tier2) {
			activityList.add(tier2activity.getLabel(), tier2activity.getTime());
		}

		log(activityList.toString());
		float totalQueueingTime = activityList.totalQueueingTime();
		double averageServiceTime = activityList.averageServiceTime();
		if (totalQueueingTime > 0 && averageServiceTime > 0) {
			showInfo("Queueing information",
					"Total queueing time: " + msToS(activityList.totalQueueingTime()) + "\n" +
							"Average service time: " + msToS(activityList.averageServiceTime()));
		} else {
			showInfo("Queueing not finished", "Please try again");
		}
	}

	private double msToS(double ms) {
		return (double) Math.round(ms/100) / 10;
	}

	// CREATE TRAINING DATA
	public void save(View view) {
		try {
			File myFile = new File("/sdcard/acceleration.txt");
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
