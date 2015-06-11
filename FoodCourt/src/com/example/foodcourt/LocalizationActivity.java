package com.example.foodcourt;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
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
import com.example.foodcourt.particles.WifiScanReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class LocalizationActivity extends BaseActivity {

	private Sensors sensors;
	private WifiManager wifiManager;
	private WifiScanReceiver wifiReciever;
	private String wifis[];
	public final static int NUMBER_PARTICLES = 1000;
	public final static double CLOUD_DISPLACEMENT = 0.2;
	public final static double CONVERGENCE_SIZE = 10;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private HashMap<String, RoomInfo> roomInfo;
	private double compassAngle = 0;
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

	private void initializeWifiSensors() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiReciever = new WifiScanReceiver(this, wifiManager);

		registerReceiver(wifiReciever, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
		unregisterWifiSensors();
		particleUpdater.removeCallbacks(particleUpdate);
		visualisation.clear();
	}

	private void unregisterSensors() {
		sensors.cancel(false);
	}

	private void unregisterWifiSensors() {
		unregisterReceiver(wifiReciever);
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

		compassAngle = inertialPoint.getInertialData().getAzimuth();

		particleCloud = ParticleFilter.filter(particleCloud, particleCloud.getEstiPos(), inertialPoint, roomInfo);

		log(particleCloud.getEstiPos().toString(3) + " " + particleCloud.getParticleCount());
	}

	public void updateBayes(String[] wifis) {
		logCollection(Arrays.asList(wifis), "Wifi results obtained: " + wifis.length + " results", "");
		if (ParticleFilter.calculateSpread(particleCloud.getParticles()) < CONVERGENCE_SIZE) {
			saveBayes(wifis, particleCloud.getEstiPos());
		} else {
			wifiManager.startScan();
		}
	}

	private void saveBayes(String[] wifis, Point estimatedPosition) {
		// TODO;
	}

	private void updateVisualization() {
		visualisation.setParticles(particleCloud.getParticles());
		visualisation.setEstimatedPoint(particleCloud.getEstiPos());
		visualisation.setCompassAngle(compassAngle);
		visualisation.setEstimatedRoom(getEstimatedRoom(particleCloud.getEstiPos()));
	}

	private String getEstimatedRoom(Point estimatedPoint) {
		log("Spread: "+ParticleFilter.calculateSpread(particleCloud.getParticles()));

		for(RoomInfo r : roomInfo.values()) {
			if (r.containsLocation(estimatedPoint)) {
				return r.getName();
			}
		}

		return "";
	}

	// BUTTONS
	public void initializePA(View view) {
		toast("Reset particle cloud");
		initializeParticleCloud();
	}

	public void initializeBayes(View view) {
		unregisterWifiSensors();
		toast("Initialized Bayes");
		initializeWifiSensors();
	}

	public void senseBayes(View view) {
		toast("TODO");
	}
}
