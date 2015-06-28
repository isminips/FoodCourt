package com.example.foodcourt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

public class BaseActivity extends Activity {

	private Toast t;



	protected void toast(String message) {
		if (t == null)
			t = Toast.makeText(this, message, Toast.LENGTH_SHORT);
		t.setText(message);
		t.show();

		log(message);
	}

	protected void showInfo(String title, String message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		if (title != null && title.length() != 0)
			alertDialogBuilder.setTitle(title);

		// set dialog message
		if (message != null && message.length() != 0)
			alertDialogBuilder.setMessage(message);

		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("Close", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	protected void log(Object object) {
		log(object.toString());
	}

	protected void log(String message) {
		System.out.println(message);
	}

	protected void logCollection(Iterable collection, String title) {
		logCollection(collection, title, "");
	}

	protected void logCollection(Iterable collection, String title, String message) {
		System.out.println(title);
		for(Object o : collection) {
			System.out.println(message + o.toString());
		}
	}

	protected void close(View v) {
		this.finish();
	}
}
