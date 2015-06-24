package com.example.foodcourt.activity;

public class Measurement {
    private float x;
    private float y;
    private float z;
    private float time;

    public Measurement(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Measurement(float x, float y, float z, float time) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.time = time;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getTime() {
        return time;
    }

    public double getMagnitude() {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    public String toString() {
        return x + "," + y + "," + z + "," + time;
    }
}
