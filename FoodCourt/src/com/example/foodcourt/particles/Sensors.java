package com.example.foodcourt.particles;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;

import com.example.foodcourt.LocalizationActivity;
import com.example.foodcourt.knn.FileReader;
import com.example.foodcourt.knn.Instance;
import com.example.foodcourt.knn.Knn;
import com.example.foodcourt.knn.Label;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Sensors extends AsyncTask<String, Movement, Void> implements SensorEventListener {

    private LocalizationActivity activity;

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private Sensor accelerometer;

    private float[] mGeomagnetic = null;
    private int mGeomagnetic_size = 0;
    private float[] mGravity = null;

    public static final Double BUILDING_ORIENTATION = -157.0;
    public double previousAngle = 0;

    private long measureStart = System.currentTimeMillis();
    private String movementData = "";
    private ArrayList<Instance> trainingSet;

    public Sensors(LocalizationActivity activity) {
        this.activity = activity;
    }

    @Override
    protected Void doInBackground(String... params) {
        initializeSensors();

        while (!isCancelled()) {
            if (countLines(movementData) > 10)
                processSensorValues();
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

    private int countLines(String str) {
        if(str == null || str.isEmpty()) {
            return 0;
        }
        int lines = 1;
        int pos = 0;
        while ((pos = str.indexOf("\n", pos) + 1) != 0) {
            lines++;
        }
        return lines;
    }

    private void initializeSensors() {
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        int SENSOR_SENSITIVITY = SensorManager.SENSOR_DELAY_NORMAL;

        if (magnetometer != null) {
            sensorManager.registerListener(this, magnetometer, SENSOR_SENSITIVITY);
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
        if (accelerometer != null) {
            sensorManager.unregisterListener(this, accelerometer);
        }
        if (magnetometer != null) {
            sensorManager.unregisterListener(this, magnetometer);
        }
    }

    /**
     * Handler for results of sensors.
     * Move inertial point after values for all three sets of data (gravity, geomagnetic and linear acceleration) have been received.
     */
    private boolean processSensorValues() {
        boolean success = false;

        if (mGravity != null && mGeomagnetic != null && movementData != null && movementData.length() != 0) {
            float R[] = new float[9];
            float I[] = new float[9];

            mGeomagnetic[0] /= mGeomagnetic_size;
            mGeomagnetic[1] /= mGeomagnetic_size;
            mGeomagnetic[2] /= mGeomagnetic_size;

            success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);

            mGravity = null;
            mGeomagnetic = null;
            mGeomagnetic_size = 0;

            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                double azimuth = orientation[0]; // orientation contains: azimuth, pitch and roll (in radians)

                if (trainingSet != null) {
                    Label.Activities activity = Knn.classify(movementData, trainingSet);

                    if (activity == Label.Activities.Walking) {
                        // TODO create proper angle
                        double deviceOrientationDegrees = Math.toDegrees(azimuth) + BUILDING_ORIENTATION;
                        deviceOrientationDegrees = deviceOrientationDegrees >= 0 ? deviceOrientationDegrees : deviceOrientationDegrees + 360;

                        // TODO determine best speed
                        double speed = 1; // speed in m/s

                        // Get elapsed time
                        String[] lines = movementData.split("\n");
                        String[] firstLine = lines[0].split(",");
                        String[] lastLine = lines[lines.length-1].split(",");
                        int elapsedMs = (int) (Double.parseDouble(lastLine[lastLine.length - 1]) - Double.parseDouble(firstLine[firstLine.length - 1]));

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

            movementData = "";
            measureStart = System.currentTimeMillis();
        }

        return success;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            if (mGeomagnetic == null) {
                mGeomagnetic = event.values;
            } else {
                mGeomagnetic[0] += event.values[0];
                mGeomagnetic[1] += event.values[1];
                mGeomagnetic[2] += event.values[2];
            }
            mGeomagnetic_size++;
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values;
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            float time = System.currentTimeMillis() - measureStart;

            double magnitude = Math.sqrt(x * x + y * y + z * z);
            movementData += "Undetermined," + magnitude + "," + time + "\n";
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
