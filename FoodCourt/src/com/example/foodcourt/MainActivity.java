package com.example.foodcourt;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void startQueueing(View view) {
		Intent intent = new Intent(this, QueueingActivity.class);
		startActivity(intent);
	}

	public void startLocalization(View view) {
		Intent intent = new Intent(this, LocalizationActivity.class);
		startActivity(intent);
	}

}
