package com.example.foodcourt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;

import com.example.foodcourt.particles.Cloud;
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
	public final static int NUMBER_PARTICLES = 2000;
	public final static double CLOUD_RANGE = 0.5;
	public final static double CLOUD_DISPLACEMENT = 0.1;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;

	private final Handler particleUpdater = new Handler();
	final int PARTICLE_UPDATE_DELAY = 2000; //milliseconds
	final Runnable particleUpdate = new Runnable() {
		public void run() {
			updateVisualization();
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

		initializeParticleCloud();
		initializeSensors();
	}

	private void initializeSensors() {
		sensors = new Sensors(this, particleCloud.getInerPoint());
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
		particleUpdater.removeCallbacks(particleUpdate);
		visualisation.clear();
	}

	private void unregisterSensors() {
		sensors.cancel(false);
	}

	public void initializeViews() {
		setContentView(R.layout.activity_localization);
		visualisation = (Visualisation) findViewById(R.id.view);
		particleUpdater.postDelayed(particleUpdate, PARTICLE_UPDATE_DELAY);
	}

	// PARTICLE CLOUD
	private void initializeParticleCloud() {
		Point mapCenter = new Point(36, 7.15);

		List<Particle> particles = new ArrayList<Particle>();
		for(RoomInfo room : roomInfo.values()) {
			particles.addAll(room.fillWithParticles(totalArea, NUMBER_PARTICLES));
		}
		particleCloud = new Cloud(mapCenter, particles);

		updateVisualization();
	}

	public void updateParticleCloud(InertialPoint inertialPoint) {
		if (inertialPoint.getPoint().equals(particleCloud.getInerPoint())) {
			return;
		}

		particleCloud = ParticleFilter.filter(particleCloud, particleCloud.getEstiPos(), inertialPoint, NUMBER_PARTICLES, CLOUD_RANGE, CLOUD_DISPLACEMENT, roomInfo);

		log(particleCloud.getEstiPos().toString(3) + " " + particleCloud.getParticleCount());
	}

	private void updateVisualization() {
		visualisation.setParticles(particleCloud.getParticles());
		visualisation.setEstimatedPoint(particleCloud.getEstiPos());
	}

	// BUTTONS
	public void initializePA(View view) {
		toast("Reset particle cloud");
		initializeParticleCloud();
	}

	public void initializeBayes(View view) {
		toast("TODO");
	}

	public void senseBayes(View view) {
		toast("TODO");
	}
}
