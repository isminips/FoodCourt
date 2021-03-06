package com.example.foodcourt.particles;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;

import com.example.foodcourt.QueueingActivity;
import com.example.foodcourt.LocalizationActivity;
import com.example.foodcourt.activity.Measurement;
import com.example.foodcourt.knn.FileReader;
import com.example.foodcourt.knn.Instance;
import com.example.foodcourt.knn.Knn;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MotionModel extends AsyncTask<String, Movement, Void> implements SensorEventListener {

    private LocalizationActivity activity;

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;

    private float[] mGeomagnetic = null;
    private float[] mGravity = null;

    public static final Double BUILDING_ORIENTATION = -157.0;
    public static final Double DEVICE_ORIENTATION = 90.0;

    private long measureStart = System.currentTimeMillis();
    private ArrayList<Measurement> movementData = new ArrayList<Measurement>();
    private ArrayList<Instance> trainingSet;
    static final float ALPHA = 0.25f; // if ALPHA = 1 OR 0, no filter applies.
    public MotionModel(LocalizationActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(String... params) {
        initializeSensors();

        while (!isCancelled()) {
            if (movementData.size() >= QueueingActivity.TIER_1_SAMPLING) {
                processSensorValues();
            }
        }

        unregisterSensors();

        return null;
    }

    /**
     * Android onProgressUpdate.
     */
    @Override
    protected void onProgressUpdate(Movement... movements) {
        activity.updateParticleCloud(movements[0]);
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        int SENSOR_SENSITIVITY = SensorManager.SENSOR_DELAY_NORMAL;

        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SENSOR_SENSITIVITY);
            System.out.println("Magnetome");
        } else {
            System.out.println("Error: Magnetometer not found");
        }
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SENSOR_SENSITIVITY);

            // Load KNN training set
            try {
                InputStream trainStream = activity.getAssets().open("trainingSet.csv");
                FileReader trainReader = new FileReader(trainStream);
                trainingSet = trainReader.buildInstances();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error: Accelerometer not found");
        }
    }

    private void unregisterSensors() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Handler for results of sensors.
     * Move inertial point after values for all three sets of data (gravity, geomagnetic and linear acceleration) have been received.
     */
    private boolean processSensorValues() {
        boolean success = false;

        if (mGravity != null && mGeomagnetic != null && movementData != null && movementData.size() > 0) {
            float R[] = new float[9];
            float I[] = new float[9];

            success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            mGravity = null;
            mGeomagnetic = null;

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                double azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll (in radians)

                if (trainingSet != null) {
                    ArrayList<Measurement> movementDataCopy = new ArrayList<Measurement>();
                    movementDataCopy.addAll(movementData);
                    Instance instance = Knn.createInstanceFromMeasurements(movementDataCopy, "Undefined");
                    Instance.Activities activity = Knn.classify(instance, trainingSet);

                    if (activity == Instance.Activities.Walking) {
                        double deviceOrientationDegrees = Math.toDegrees(azimuth) + BUILDING_ORIENTATION + DEVICE_ORIENTATION;
                        deviceOrientationDegrees = deviceOrientationDegrees >= 0 ? deviceOrientationDegrees : deviceOrientationDegrees + 360;
                        deviceOrientationDegrees = roundToNearestDegrees(deviceOrientationDegrees);

                        double speed = 1.6; // speed in m/s

                        // Get elapsed time
                        int elapsedMs = (int) (movementData.get(movementData.size()-1).getTime() - movementData.get(0).getTime());

                        // Calculate step size
                        double stepSize = speed * elapsedMs/1000;

                        // Add direction to movement
                        double[] movement = new double[]{
                                stepSize * Math.cos(Math.toRadians(deviceOrientationDegrees)),
                                stepSize * Math.sin(Math.toRadians(deviceOrientationDegrees))
                        };

                        publishProgress(new Movement(movement, deviceOrientationDegrees, elapsedMs));
                    }
                }
            }

            movementData.clear();
            measureStart = System.currentTimeMillis();
        }

        return success;
    }

    private double roundToNearestDegrees(double angle) {
        final double ROUND = 90;

        double upper = ROUND / 2;
        int pie = 0;
        while (upper < 360) {
            if (angle < upper)
                return pie*ROUND;

            upper += ROUND;
            pie++;
        }
        return 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            if (mGeomagnetic == null) {
                mGeomagnetic = event.values;
            } else {
                mGeomagnetic = lowPass(event.values.clone(), mGeomagnetic);
            }
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long time = System.currentTimeMillis() - measureStart;

            Measurement measurement = new Measurement(x, y, z, time);
            movementData.add(measurement);
            //System.out.println("Acc data [" + movementData.size() + "]:" + measurement);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
