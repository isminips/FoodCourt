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
    private final int TIER_2_SAMPLING_TIME = 2;
    private SensorManager sensorManager;
	private Sensor accelerometer;
	private TextView currentActivityLabel;
	private TextView averageServiceTimeLabel;
	private TextView totalQueueingTimeLabel;
	private String data = "";
	private float starttime = 0;
	private String trainingStatus = "Standing";
    ArrayList<Instance> trainingSet = null;
    ArrayList<Label.Activities> tier1 = null;
    ArrayList<Label.Activities> tier2 = null;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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

        tier1 = new ArrayList<Label.Activities>();
        tier2 = new ArrayList<Label.Activities>();
	}

    private ArrayList<Instance> loadTrainingSet(String filename) throws IOException {
        InputStream trainStream = getAssets().open(filename);
        FileReader trainReader = new FileReader(trainStream);
        return trainReader.buildInstances();
    }

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

    public void initializeViews() {
		currentActivityLabel = (TextView) findViewById(R.id.currentActivityLabel);
		totalQueueingTimeLabel = (TextView) findViewById(R.id.totalQueueingTimeLabel);
		averageServiceTimeLabel = (TextView) findViewById(R.id.averageServiceTimeLabel);
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
		data += trainingStatus + "," + magnitude + "," + time + "\n";

		if (time > TIER_1_SAMPLING_TIME) {
			tier1.add(classify());
			data = "";
		}
	}

    public void start(View view) {
        startAcc();
        starttime = 0;

        totalQueueingTimeLabel.setText("-");
        averageServiceTimeLabel.setText("-");
    }

	public void startAcc() {
		sensorManager.registerListener(this, accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
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
		if (trainingStatus.equals("Standing")) {
			trainingStatus = "Walking";
			Button p1_button = (Button) findViewById(R.id.change);
			p1_button.setText("Walking");
		} else {
			trainingStatus = "Standing";
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

    public void stop(View view) {
        stopAcc();

        int walking = 0, standing = 0;
        int time = 0;
        int processing = 0;
        ActivityList activityList = new ActivityList();
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

        System.out.println(activityList.toString());
        int totalQueueingTime = activityList.totalQueueingTime();
        double averageServiceTime = activityList.averageServiceTime();
        if (totalQueueingTime > 0) {
            totalQueueingTimeLabel.setText(activityList.totalQueueingTime() + "");
        } else {
            totalQueueingTimeLabel.setText("Queueing not finished");
        }
        if (averageServiceTime > 0) {
            averageServiceTimeLabel.setText(activityList.averageServiceTime() + "");
        } else {
            averageServiceTimeLabel.setText("Queueing not finished");
        }
    }

	public void stopAcc() {
		sensorManager.unregisterListener(this);
	}
}
