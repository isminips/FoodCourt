package com.example.foodcourt;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;

import com.example.foodcourt.knn.FileReader;
import com.example.foodcourt.knn.Instance;
import com.example.foodcourt.particles.Cloud;
import com.example.foodcourt.particles.InertialPoint;
import com.example.foodcourt.particles.Particle;
import com.example.foodcourt.particles.ParticleFilter;
import com.example.foodcourt.particles.Point;
import com.example.foodcourt.particles.Thresholds;
import com.example.foodcourt.particles.Visualisation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class LocalizationActivity extends BaseActivity {

    private final int NUMBER_PARTICLES = 100;
	private final double CLOUD_RANGE = 0.5;
	private final double CLOUD_DISPLACEMENT = 1;
	Cloud particleCloud;
	private Visualisation visualisation;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initializeViews();

		try {
			Drawable floorPlan = Drawable.createFromStream(getAssets().open("floorPlan.png"), null);
			visualisation.setFloorPlan(floorPlan);
		} catch (IOException e) {
			e.printStackTrace();
		}

		initializeParticleCloud();
	}

	private ArrayList<Instance> loadTrainingSet(String filename) throws IOException {
        InputStream trainStream = getAssets().open(filename);
        FileReader trainReader = new FileReader(trainStream);
        return trainReader.buildInstances();
    }

    public void initializeViews() {
		setContentView(R.layout.activity_localization);
		visualisation = (Visualisation) findViewById(R.id.view);
	}

	// onResume() register the accelerometer for listening the events
	protected void onResume() {
		super.onResume();
	}

	// onPause() unregister the accelerometer for stop listening the events
	protected void onPause() {
		super.onPause();
	}

	// PARTICLE CLOUD
	private void initializeParticleCloud() {
		Point estimatedPosition = new Point(0,0);
		List<Particle> particles = ParticleFilter.createParticles(estimatedPosition, NUMBER_PARTICLES);
		particleCloud = new Cloud(estimatedPosition, particles);
	}

	private void updateParticleCloud() {
		Point oldInerPoint = particleCloud.getInerPoint();
		// For now, move the cloud by +1X
		particleCloud = ParticleFilter.filter(particleCloud, particleCloud.getEstiPos(), new InertialPoint(new Point(oldInerPoint.getX() + 1, oldInerPoint.getY())), NUMBER_PARTICLES, CLOUD_RANGE, CLOUD_DISPLACEMENT, Thresholds.boundaries(), Thresholds.particleCreation());
		/*for(Particle p : particleCloud.getParticles()) {
			log(p.toString());
		}*/
		log(particleCloud.getEstiPos().toString() + " (average over " + particleCloud.getParticles().size() + " particles)");
	}

	// BUTTONS
	public void initializePA(View view) {

	}

	public void initializeBayes(View view) {

	}

	public void senseBayes(View view) {

	}

}
