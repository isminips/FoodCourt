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
import com.example.foodcourt.particles.Thresholds;
import com.example.foodcourt.particles.Visualisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalizationActivity extends BaseActivity {

	private final int NUMBER_PARTICLES = 5000;
	private final double CLOUD_RANGE = 72;
	private final double CLOUD_DISPLACEMENT = 0.2;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;

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

		initializeParticleCloud();
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

		visualisation.setParticles(particleCloud.getParticles());

		stopParticleCloud();
		particleUpdater.postDelayed(particleUpdate, PARTICLE_UPDATE_DELAY);
	}

	private void stopParticleCloud() {
		particleUpdater.removeCallbacks(particleUpdate);
	}

	private void updateParticleCloud() {
		Point oldInerPoint = particleCloud.getInerPoint();

		// For now, move the cloud by +1X
		particleCloud = ParticleFilter.filter(particleCloud, particleCloud.getEstiPos(), new InertialPoint(new Point(oldInerPoint.getX() + 1, oldInerPoint.getY())), NUMBER_PARTICLES, CLOUD_RANGE, CLOUD_DISPLACEMENT, roomInfo);
		visualisation.setParticles(particleCloud.getParticles());

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
