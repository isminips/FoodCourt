package com.example.foodcourt;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

public class LocalizationActivity extends BaseActivity {

	private MotionModel motionModel;
	private WifiManager wifiManager;
	private WifiScanReceiver wifiReciever;
	public final static int NUMBER_PARTICLES = 1000;
	public final static double CONVERGENCE_SIZE = 2;
	public final static Point TOTAL_DRAW_SIZE = new Point(72, 14.3);
	private Cloud particleCloud;
	private Visualisation visualisation;
	private Compass compass;
	private String angles = ""; // for debugging purposes
	private HashMap<String, RoomInfo> roomInfo;
	double totalArea = 0;
	private TreeMap<Long, List<WifiResult>> wifiScanData;
	private TreeMap<Long, Movement> movementData;
	private RSSIDatabase rssiDatabase;

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
		loadRSSIdatabase();
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

		movementData = new TreeMap<Long, Movement>();

		updateVisualization();

		saveAngles(); // TODO remove debug
	}

	/**
	 * For debugging purposes
	 */
	private void saveAngles() {
		if (angles.length() == 0) return;

		try {
			File myFile = new File("/sdcard/angles_"+System.currentTimeMillis()+".txt");
			myFile.createNewFile();
			FileOutputStream fOut = new FileOutputStream(myFile);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(angles);
			myOutWriter.close();
			fOut.close();
			toast("Done writing angles to SD");
			angles = "";
		} catch (Exception e) {
			toast(e.getMessage());
		}
	}

	public void updateParticleCloud(Movement movement) {
		log(movement);
		angles += movement.getAngle() + "\n"; // TODO remove debug

		movementData.put(System.currentTimeMillis(), movement);

		compass.setCompassAngle(movement.getAngle());

		particleCloud = ParticleFilter.filter(particleCloud, movement, roomInfo);
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
		saveRSSIdatabase();
		resetRSSImeasurements();
	}

	public void updateRSSI(List<WifiResult> results) {
		log("Wifi results obtained: " + results.size() + " results");

		if (results.size() > 0) {
			wifiScanData.put(results.get(0).getTimestamp(), results);
		}

		wifiManager.startScan();
	}

	private void createRSSIdatabase() {
		if (rssiDatabase == null) resetRSSIdatabase();

		rssiDatabase.createRSSIdatabase(this, particleCloud, wifiScanData, movementData);
	}

	private void loadRSSIdatabase() {
		rssiDatabase = RSSIDatabase.loadFromSD();
	}

	private void resetRSSIdatabase() {
		rssiDatabase = new RSSIDatabase();
	}

	private void saveRSSIdatabase() {
		if (particleCloud.getSpread() < CONVERGENCE_SIZE) {
			createRSSIdatabase();
			rssiDatabase.writeToSD();
		}
	}

	private void resetRSSImeasurements() {
		wifiScanData = new TreeMap<Long, List<WifiResult>>();
		if (rssiDatabase != null) {
			rssiDatabase.updateTime();
		}
	}

	// BUTTONS
	public void initializePA(View view) {
		toast("Reset particle cloud");
		initializeParticleCloud();
		resetRSSImeasurements();
	}

	public void initializeRSSI(View view) {
		toast("Initialized RSSI database");
		initializeRSSI();
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

		if (wifiScanData.size() > 0 && rssiDatabase.size() > 0) {
			toast("Estimating position based on RSSI...");

			List<WifiResult> lastScan = wifiScanData.pollLastEntry().getValue();

			String estimatedRoom = rssiDatabase.determineRoom(lastScan);

			visualisation.setEstimatedRoomRSSI(estimatedRoom);
			toast("Estimated room: " + estimatedRoom);
		} else {
			toast("Unable to estimate position based on RSSI");
		}
	}
}
