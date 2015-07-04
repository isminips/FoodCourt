package com.example.foodcourt;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


import com.example.foodcourt.particles.Cloud;
import com.example.foodcourt.particles.Compass;
import com.example.foodcourt.particles.Movement;
import com.example.foodcourt.particles.Particle;
import com.example.foodcourt.particles.ParticleFilter;
import com.example.foodcourt.particles.Point;
import com.example.foodcourt.particles.RoomInfo;
import com.example.foodcourt.particles.MotionModel;
import com.example.foodcourt.particles.Visualisation;
import com.example.foodcourt.rssi.RSSIDatabase;
import com.example.foodcourt.rssi.WifiResult;
import com.example.foodcourt.rssi.WifiScanReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TreeMap;

public class LocalizationActivity extends BaseActivity {

	private MotionModel motionModel;
	private WifiManager wifiManager;
	private WifiScanReceiver wifiReciever;
	public final static int NUMBER_PARTICLES = 1000;
	public final static int PARTICLE_PREVIOUS_MOVEMENTS = 20;
	public final static double CONVERGENCE_SIZE = 2;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private Compass compass;
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;
	private TreeMap<Long, List<WifiResult>> wifiScanData;
	private TreeMap<Long, Movement> movementData;
	private RSSIDatabase rssiDatabase;
	private Queue<Point> positions;

	private final Handler visualizationUpdater = new Handler();
	final int VISUALIZATION_UPDATE_DELAY = 2000; //milliseconds
	final Runnable visualizationUpdate = new Runnable() {
		public void run() {
			updateVisualization();
			visualizationUpdater.postDelayed(this, VISUALIZATION_UPDATE_DELAY);
		}
	};

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

		try {
			Drawable floorPlan = Drawable.createFromStream(getAssets().open("floorPlan.png"), null);
			visualisation.setFloorPlan(floorPlan);
			visualisation.setTotalDrawSize(TOTAL_DRAW_SIZE);

			roomInfo = RoomInfo.load(getAssets().open("RoomInfo.csv"));
			for(RoomInfo r : roomInfo.values()) {
				r.setDrawDimensions(visualisation.getScreenSize(), TOTAL_DRAW_SIZE);
				if (r.isRoom() || r.isAislePlaceholder()) {
					totalArea += r.getArea();
				}
			}

			visualisation.setRooms(roomInfo.values());
		} catch (IOException e) {
			e.printStackTrace();
		}

		initializeParticleCloud();
		initializeRSSI();
		resetRSSIdatabase();
		initializeMotionModel();
	}

	/*protected void onResume() {
		super.onResume();
		initializeMotionModel();
		initializeWifiSensors();
	}

	protected void onPause() {
		super.onPause();
		unregisterMotionModel();
		unregisterWifiSensors();
	}*/

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		stop();
	}

	private void initializeMotionModel() {
		motionModel = new MotionModel(this);
		motionModel.execute();
	}

	private void initializeWifiSensors() {
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiReciever = new WifiScanReceiver(this, wifiManager);

		registerReceiver(wifiReciever, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		wifiManager.startScan();
	}

	private void stop() {
		unregisterMotionModel();
		unregisterWifiSensors();
		visualizationUpdater.removeCallbacks(visualizationUpdate);
		saveRSSIdatabase();
	}

	private void unregisterMotionModel() {
		if (motionModel != null) {
			motionModel.cancel(true);
		}
	}

	private void unregisterWifiSensors() {
		if(wifiReciever != null) {
			try {
				unregisterReceiver(wifiReciever);
			} catch (IllegalArgumentException e) {
			}
		}
	}

	public void initializeViews() {
		setContentView(R.layout.activity_localization);
		visualisation = (Visualisation) findViewById(R.id.visualization);
		compass = (Compass) findViewById(R.id.compass);
		visualizationUpdater.postDelayed(visualizationUpdate, VISUALIZATION_UPDATE_DELAY);
	}

	// PARTICLE CLOUD
	private void initializeParticleCloud() {
		List<Particle> particles = new ArrayList<Particle>();
		for(RoomInfo room : roomInfo.values()) {
			if (room.isRoom() || room.isAislePlaceholder()) {
				particles.addAll(room.fillWithParticles(totalArea, NUMBER_PARTICLES));
			}
		}
		particleCloud = new Cloud(particles);
		positions = new LinkedList<Point>();

		movementData = new TreeMap<Long, Movement>();

		updateVisualization();
	}

	public void updateParticleCloud(Movement movement) {
		log(movement);

		movementData.put(System.currentTimeMillis(), movement);

		compass.setCompassAngle(movement.getAngle());

		particleCloud = ParticleFilter.filter(particleCloud, movement, roomInfo, positions);
		positions.add(particleCloud.getEstimatedPosition());
	}

	public RoomInfo getEstimatedRoom(Point estimatedPoint) {
		RoomInfo room = null;

		for(RoomInfo r : roomInfo.values()) {
			if ((r.isRoom() || (r.isAisle() && !r.isAislePlaceholder())) && r.containsLocation(estimatedPoint)) {
				return r;
			} else if (r.isAislePlaceholder() && r.containsLocation(estimatedPoint)) {
				room = r; // rooms can overlap with aisle placeholder
			}
		}

		return room;
	}

	// VISUALIZATION
	private void updateVisualization() {
		visualisation.setCloud(particleCloud);
		visualisation.setEstimatedRoom(getEstimatedRoom(particleCloud.getEstimatedPosition()));

		visualisation.update();
	}

	// RSSI
	private void initializeRSSI() {
		unregisterWifiSensors();
		initializeWifiSensors();
		resetRSSImeasurements();
	}

	public void updateRSSI(List<WifiResult> results) {
		log("Wifi results obtained: " + results.size() + " results");

		if (results.size() > 0) {
			wifiScanData.put(System.currentTimeMillis(), results);
		}

		wifiManager.startScan();
	}

	private void createRSSIdatabase() {
		if (rssiDatabase == null) resetRSSIdatabase();

		rssiDatabase.createRSSIdatabase(this, particleCloud, wifiScanData, movementData);

		Button resetButton = (Button) findViewById(R.id.initial_rssi);
		resetButton.setEnabled(rssiDatabase.size() > 0);
	}

	private void loadRSSIdatabase() {
		rssiDatabase = RSSIDatabase.loadFromSD();
	}

	private void resetRSSIdatabase() {
		rssiDatabase = new RSSIDatabase();
		Button resetButton = (Button) findViewById(R.id.initial_rssi);
		resetButton.setEnabled(false);
	}

	private void saveRSSIdatabase() {
		if (particleCloud.getSpread() < CONVERGENCE_SIZE) {
			createRSSIdatabase();
			rssiDatabase.writeToSD();
		}
	}

	private void resetRSSImeasurements() {
		wifiScanData = new TreeMap<Long, List<WifiResult>>();
	}

	// BUTTONS
	public void initializePA(View view) {
		toast("Reset particle cloud");
		createRSSIdatabase();
		initializeParticleCloud();
		resetRSSImeasurements();
	}

	public void initializeRSSI(View view) {
		toast("Initialized RSSI database");
		resetRSSIdatabase();
	}

	public void senseRSSI(View view) {
		visualisation.setEstimatedRoomRSSI(null);

		log("Movement data: " + movementData.size() + " measurements");
		log("RSSI data: " + wifiScanData.size() + " measurements");
		// If we don't have RSSI data, what are we doing here?
		if (wifiScanData.size() == 0) {
			wifiManager.startScan();
		}

		// Can we create a database?
		if (particleCloud.getSpread() < CONVERGENCE_SIZE) {
			createRSSIdatabase();
			log("RSSI database created: Your steps have been traced back to " + rssiDatabase.size() + " rooms");
		} else {
			log("Particles have not converged yet");
		}

		if (wifiScanData.size() > 0 && rssiDatabase != null && rssiDatabase.size() > 0) {
			toast("Estimating position based on RSSI...");

			List<WifiResult> lastScan = wifiScanData.lastEntry().getValue();

			String estimatedRoom = rssiDatabase.determineRoom(lastScan);

			visualisation.setEstimatedRoomRSSI(estimatedRoom);
			toast("Estimated room: " + estimatedRoom);
		} else {
			toast("Unable to estimate position based on RSSI");
			log("Database: "+(rssiDatabase == null ? 0 : rssiDatabase.size()) + " Scan data: "+wifiScanData.size());
		}
	}
}
