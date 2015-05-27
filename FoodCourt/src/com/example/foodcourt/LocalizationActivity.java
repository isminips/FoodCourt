package com.example.foodcourt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import com.example.foodcourt.particles.AppSettings;
import com.example.foodcourt.particles.Cloud;
import com.example.foodcourt.particles.InertialData;
import com.example.foodcourt.particles.InertialPoint;
import com.example.foodcourt.particles.Particle;
import com.example.foodcourt.particles.ParticleFilter;
import com.example.foodcourt.particles.Point;
import com.example.foodcourt.particles.RoomInfo;
import com.example.foodcourt.particles.Threshold;
import com.example.foodcourt.particles.Thresholds;
import com.example.foodcourt.particles.Visualisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalizationActivity extends BaseActivity implements SensorEventListener {

	private final int NUMBER_PARTICLES = 5000;
	private final double CLOUD_RANGE = 72;
	private final double CLOUD_DISPLACEMENT = 0.2;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;


	private final List<LocalizationActivity> initialPoints = new ArrayList<>();
	private InertialPoint inertialPoint = null;
	boolean isInitialising = true;
	private Integer deviceOrientation = null;      //Orientation direction for filtering offline map
	//private final HashMap<String, KNNFloorPoint> offlineMap;
	//private  HashMap<String, RoomInfo> roomInfo;
	private  SensorManager mSensorManager;
	private  Sensor linearAcceleration;
	private Sensor magnetometer;
	private Sensor gravity;
	//private Cloud cloud = null;
	private float[] mLinearAcceleration = null;
	private float[] mGeomagnetic = null;
	private float[] mGravity = null;

	private AppSettings appSettings;

	private final Handler particleUpdater = new Handler();
	final int PARTICLE_UPDATE_DELAY = 1000; //milliseconds
	final Runnable particleUpdate = new Runnable() {
		public void run() {
			updateParticleCloud();
			particleUpdater.postDelayed(this, PARTICLE_UPDATE_DELAY);
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initializeViews();

		try {
			Drawable floorPlan = Drawable.createFromStream(getAssets().open("floorPlan.png"), null);
			visualisation.setFloorPlan(floorPlan);
			visualisation.setTotalDrawSize(TOTAL_DRAW_SIZE);

			roomInfo = RoomInfo.load(getAssets().open("RoomInfo.csv"));
			for(RoomInfo r : roomInfo.values()) {
				r.makeDrawArea(visualisation.getScreenSize(), TOTAL_DRAW_SIZE);
				totalArea += r.getArea();
			}

			visualisation.setRooms(roomInfo.values());
		} catch (IOException e) {
			e.printStackTrace();
		}

		initializeSensors();
		initializeParticleCloud();
	}

	private void initializeSensors() {
		//wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		linearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

		//log(linearAcceleration.toString() + magnetometer.toString() + gravity.toString());

		if (linearAcceleration != null) {
			log("Accelerometer");
			mSensorManager.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
		}
		if (magnetometer != null) {
			log("Magnetometer");
			mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
		}
		if (gravity != null) {
			log("Gravity");
			mSensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		stop();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if ((keyCode == KeyEvent.KEYCODE_BACK))
		{
			stop();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void stop() {
		stopParticleCloud();
	}

	public void initializeViews() {
		setContentView(R.layout.activity_localization);
		visualisation = (Visualisation) findViewById(R.id.view);
	}

	// PARTICLE CLOUD
	private void initializeParticleCloud() {
		Point mapCenter = new Point(36,7.15);

		List<Particle> particles = new ArrayList<Particle>();
		for(RoomInfo room : roomInfo.values()) {
			particles.addAll(room.fillWithParticles(totalArea, NUMBER_PARTICLES));
		}
		particleCloud = new Cloud(mapCenter, particles);
		inertialPoint = new InertialPoint(particleCloud.getInerPoint());

		visualisation.setParticles(particleCloud.getParticles());

		//stopParticleCloud();
		//particleUpdater.postDelayed(particleUpdate, PARTICLE_UPDATE_DELAY);
	}

	private void stopParticleCloud() {
		particleUpdater.removeCallbacks(particleUpdate);
	}

	private void updateParticleCloud() {
		particleCloud = ParticleFilter.filter(particleCloud, particleCloud.getEstiPos(), inertialPoint, NUMBER_PARTICLES, CLOUD_RANGE, CLOUD_DISPLACEMENT, roomInfo);
		visualisation.setParticles(particleCloud.getParticles());

		// For now, move the cloud by +1X
		//Point oldInerPoint = particleCloud.getInerPoint();
		//particleCloud = ParticleFilter.filter(particleCloud, particleCloud.getEstiPos(), new InertialPoint(new Point(oldInerPoint.getX() + 1, oldInerPoint.getY())), NUMBER_PARTICLES, CLOUD_RANGE, CLOUD_DISPLACEMENT, roomInfo);
		//visualisation.setParticles(particleCloud.getParticles());

		log(particleCloud.getEstiPos().toString(3) + " " + particleCloud.getParticleCount());
	}

	// BUTTONS
	public void initializePA(View view) {
		initializeParticleCloud();
	}

	public void initializeBayes(View view) {

	}

	public void senseBayes(View view) {

	}

	/**
	 * Handler for results of sensors.
	 * Move inertial point after values for all three sets of data (gravity, geomagnetic and linear acceleration) have been received.
	 */
	private boolean processSensorValues() {
		log("processSensorValues");
		boolean success = false;
		if (mGravity != null && mGeomagnetic != null && mLinearAcceleration != null) {

			float R[] = new float[16];
			float I[] = new float[16];
			float iR[] = new float[16];
			success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
			log(success ? "Success" : "Failure");
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				deviceOrientation = InertialData.getOrientation(appSettings.isOrientationMerged(), orientation[0], appSettings.getBuildingOrientation());

				if (!isInitialising) {
					boolean invert = android.opengl.Matrix.invertM(iR, 0, R, 0);
					if (invert) {

						InertialData results = InertialData.getDatas(iR, mLinearAcceleration, orientation, appSettings.getBuildingOrientation(), appSettings.getJitterOffset(), appSettings.getAccelerationOffset());
						inertialPoint = InertialPoint.move(inertialPoint, results, System.nanoTime(), appSettings.getSpeedBreak());

					}
				}

			}
			mGravity = null;
			mGeomagnetic = null;
			mLinearAcceleration = null;
		}

		return success;
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		log("onSensorChanged");
		if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
			mGravity = event.values;
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagnetic = event.values;
		} else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
			mLinearAcceleration = event.values;
		}

		if(processSensorValues()) {
			updateParticleCloud();
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}
}
