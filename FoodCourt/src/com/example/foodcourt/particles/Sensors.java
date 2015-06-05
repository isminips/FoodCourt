package com.example.foodcourt.particles;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import com.example.foodcourt.LocalizationActivity;

import java.util.HashMap;

/**
 * Created by Gebruiker on 27-5-2015.
 */
public class Sensors extends AsyncTask<String, InertialPoint, Void> implements SensorEventListener {

    private InertialPoint inertialPoint;
    private Integer deviceOrientation = null;      //Orientation direction for filtering offline map
    private SensorManager mSensorManager;
    private Sensor linearAcceleration;
    private Sensor magnetometer;
    private Sensor gravity;
    private float[] mLinearAcceleration = null;
    private float[] mGeomagnetic = null;
    private float[] mGravity = null;
    private LocalizationActivity activity;

    public static final boolean IS_ORIENTATION_MERGED = true;
    public static final int SPEEDBREAK = 40;
    public static final Double JITTER_OFFSET = 0.3;
    public static final Float[] ACCELERATION_OFFSET = new Float[]{0.05f, 0.3f, -0.17f};
    public static final Double BUILDING_ORIENTATION = 0.0;

    public Sensors(LocalizationActivity activity, Point initialInertialPoint) {
        this.activity = activity;
        this.inertialPoint = new InertialPoint(initialInertialPoint);
    }

    @Override
    protected Void doInBackground(String... params) {
        initializeSensors();

        while (!isCancelled()) {
            processSensorValues();
        }

        unregisterSensors();

        return null;
    }

    /**
     * Android onProgressUpdate.
     */
    @Override
    protected void onProgressUpdate(InertialPoint... points) {
        activity.updateParticleCloud(points[0]);
    }


    private void initializeSensors() {
        //wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        linearAcceleration = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);

        int SENSOR_SENSITIVITY = SensorManager.SENSOR_DELAY_FASTEST;

        if (linearAcceleration != null) {
            mSensorManager.registerListener(this, linearAcceleration, SENSOR_SENSITIVITY);
        } else {
            System.out.println("Error: Accelerometer not found");
        }
        if (magnetometer != null) {
            mSensorManager.registerListener(this, magnetometer, SENSOR_SENSITIVITY);
        } else {
            System.out.println("Error: Magnetometer not found");
        }
        if (gravity != null) {
            mSensorManager.registerListener(this, gravity, SENSOR_SENSITIVITY);
        } else {
            System.out.println("Error: Gravity not found");
        }
    }

    private void unregisterSensors() {
        mSensorManager.unregisterListener(this, linearAcceleration);
        mSensorManager.unregisterListener(this, gravity);
        mSensorManager.unregisterListener(this, magnetometer);
    }

    /**
     * Handler for results of sensors.
     * Move inertial point after values for all three sets of data (gravity, geomagnetic and linear acceleration) have been received.
     */
    private boolean processSensorValues() {
        boolean success = false;
        if (mGravity != null && mGeomagnetic != null && mLinearAcceleration != null) {

            float R[] = new float[16];
            float I[] = new float[16];
            float iR[] = new float[16];
            success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                deviceOrientation = InertialData.getOrientation(IS_ORIENTATION_MERGED, orientation[0], BUILDING_ORIENTATION);

                boolean invert = android.opengl.Matrix.invertM(iR, 0, R, 0);
                if (invert) {

                    InertialData results = InertialData.getDatas(iR, mLinearAcceleration, orientation, BUILDING_ORIENTATION, JITTER_OFFSET, ACCELERATION_OFFSET);
                    inertialPoint = InertialPoint.move(inertialPoint, results, System.nanoTime(), SPEEDBREAK);
                    publishProgress(inertialPoint);
                }

            }
            mGravity = null;
            mGeomagnetic = null;
            mLinearAcceleration = null;
        }

        return success;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            mGravity = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mGeomagnetic = event.values;
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            mLinearAcceleration = event.values;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
