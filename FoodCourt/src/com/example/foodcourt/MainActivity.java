package com.example.foodcourt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	public void startWifi(View view) {
		Intent intent = new Intent(this, WifiActivity.class);
		startActivity(intent);
	}

	public void startActivityMonitoring(View view) {
		Intent intent = new Intent(this, ActivityMonitoringActivity.class);
		startActivity(intent);
	}

	public void startLocalization(View view) {
		Intent intent = new Intent(this, LocalizationActivity.class);
		startActivity(intent);
	}

}
