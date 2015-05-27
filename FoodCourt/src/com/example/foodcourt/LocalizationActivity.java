package com.example.foodcourt;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.example.foodcourt.particles.Cloud;
import com.example.foodcourt.particles.InertialData;
import com.example.foodcourt.particles.InertialPoint;
import com.example.foodcourt.particles.Particle;
import com.example.foodcourt.particles.ParticleFilter;
import com.example.foodcourt.particles.Point;
import com.example.foodcourt.particles.RoomInfo;
import com.example.foodcourt.particles.Visualisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalizationActivity extends BaseActivity implements SensorEventListener {

	private static final boolean IS_ORIENTATION_MERGED = true;
	private static final int SPEEDBREAK = 40;
	private static final Double JITTER_OFFSET = 0.3;
	private static final Float[] ACCELERATION_OFFSET = new Float[]{0.005f, 0.03f, -0.17f};
	private static final Double BUILDING_ORIENTATION = -0.523598776;
	private final int NUMBER_PARTICLES = 5000;
	private final double CLOUD_RANGE = 72;
	private final double CLOUD_DISPLACEMENT = 0.2;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;

	private InertialPoint inertialPoint = null;
	private Integer deviceOrientation = null;      //Orientation direction for filtering offline map
	private SensorManager mSensorManager;
	private Sensor linearAcceleration;
	private Sensor magnetometer;
	private Sensor gravity;
	private float[] mLinearAcceleration = null;
	private float[] mGeomagnetic = null;
	private float[] mGravity = null;

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

		if (linearAcceleration != null) {
			mSensorManager.registerListener(this, linearAcceleration, SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			log("Error: Accelerometer not found");
		}
		if (magnetometer != null) {
			mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			log("Error: Magnetometer not found");
		}
		if (gravity != null) {
			mSensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_FASTEST);
		} else {
			log("Error: Gravity not found");
		}
	}

	private void unregisterSensors() {
		mSensorManager.unregisterListener(this, linearAcceleration);
		mSensorManager.unregisterListener(this, gravity);
		mSensorManager.unregisterListener(this, magnetometer);
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
		unregisterSensors();
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
		boolean success = false;
		if (mGravity != null && mGeomagnetic != null && mLinearAcceleration != null) {

			float R[] = new float[16];
			float I[] = new float[16];
			float iR[] = new float[16];
			success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				deviceOrientation = InertialData.getOrientation(IS_ORIENTATION_MERGED, orientation[0], BUILDING_ORIENTATION);

				boolean invert = android.opengl.Matrix.invertM(iR, 0, R, 0);
				if (invert) {

					InertialData results = InertialData.getDatas(iR, mLinearAcceleration, orientation, BUILDING_ORIENTATION, JITTER_OFFSET, ACCELERATION_OFFSET);
					inertialPoint = InertialPoint.move(inertialPoint, results, System.nanoTime(), SPEEDBREAK);

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
