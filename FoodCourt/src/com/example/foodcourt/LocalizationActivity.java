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
import com.example.foodcourt.particles.Sensors;
import com.example.foodcourt.particles.Visualisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalizationActivity extends BaseActivity {

	private Sensors sensors;
	private final int NUMBER_PARTICLES = 5000;
	private final double CLOUD_RANGE = 72;
	private final double CLOUD_DISPLACEMENT = 0.2;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;

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
		sensors = new Sensors(this);
		sensors.execute();
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

	private void unregisterSensors() {
		sensors.cancel(false);
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

		visualisation.setParticles(particleCloud.getParticles());
	}

	public void updateParticleCloud(InertialPoint inertialPoint) {
		//log("Inertial point: "+inertialPoint.toString());
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
}
