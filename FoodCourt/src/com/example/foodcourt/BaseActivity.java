package com.example.foodcourt;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.foodcourt.knn.FileReader;
import com.example.foodcourt.knn.Instance;
import com.example.foodcourt.knn.Knn;
import com.example.foodcourt.knn.Label;
import com.example.foodcourt.knn.Neighbor;
import com.example.foodcourt.particles.Cloud;
import com.example.foodcourt.particles.InertialPoint;
import com.example.foodcourt.particles.Particle;
import com.example.foodcourt.particles.ParticleFilter;
import com.example.foodcourt.particles.Point;
import com.example.foodcourt.particles.Thresholds;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends Activity {

	private Toast t;

	protected void toast(String message) {
		if (t == null)
			t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		t.setText(message);
		t.show();
	}

	protected void log(String message) {
		System.out.println(message);
	}
}