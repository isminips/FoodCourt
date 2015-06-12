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
import com.example.foodcourt.particles.Movement;
import com.example.foodcourt.particles.Particle;
import com.example.foodcourt.particles.ParticleFilter;
import com.example.foodcourt.particles.Point;
import com.example.foodcourt.particles.RoomInfo;
import com.example.foodcourt.particles.Sensors;
import com.example.foodcourt.particles.Visualisation;
import com.example.foodcourt.particles.WifiScanReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
	private String wifiResults="";

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
		sensors = new Sensors(this);
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
		if(wifiReciever != null) {
			unregisterReceiver(wifiReciever);
		}
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

	public void updateParticleCloud(Movement movement) {
		log(movement);
		compassAngle = movement.getAngle();

		particleCloud = ParticleFilter.filter(particleCloud, movement, roomInfo);

		log(particleCloud.getEstimatedPosition().toString(3) + " " + particleCloud.getParticleCount());
	}

	public void updateBayes(ArrayList<String> results) {
		logCollection(Arrays.asList(results), "Wifi results obtained: " + results.size() + " results", "");
		if (particleCloud.calculateSpread() < CONVERGENCE_SIZE) {

			saveBayes(results, particleCloud.getEstimatedPosition());

		} else {
			wifiManager.startScan();
		}
	}

	private void saveBayes(ArrayList<String> results, Point estimatedPosition) {

		for (int i=0; i<results.size(); i++) {

			wifiResults+= results.get(i) + "," + getEstimatedRoom(estimatedPosition)  + "\n";
		}
		log("wifiresult: " +wifiResults);
		try {
			File myFile = new File("/sdcard/wifi.txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(wifiResults);
			myOutWriter.close();
			fOut.close();
			toast("Done writing file in SD");
			log(wifiResults);
		} catch (Exception e) {
			toast(e.getMessage());
		}

	}



	private void updateVisualization() {
		visualisation.setParticles(particleCloud.getParticles());
		visualisation.setEstimatedPoint(particleCloud.getEstimatedPosition());
		visualisation.setCompassAngle(compassAngle);
		visualisation.setEstimatedRoom(getEstimatedRoom(particleCloud.getEstimatedPosition()));
	}

	private String getEstimatedRoom(Point estimatedPoint) {
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
		wifiManager.startScan();
	}

	public void senseBayes(View view) {
		toast("TODO");
	}
}
