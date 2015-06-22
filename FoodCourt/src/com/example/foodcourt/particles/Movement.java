package com.example.foodcourt.particles;

public class Movement {

    private double[] movement;
    private double angle;
    private int timeMS;

    public Movement(double[] movement, double angle) {
        this.movement = movement;
        this.angle = angle;
        this.timeMS = 1000;
    }

    public Movement(double[] movement, double angle, int timeMS) {
        this.movement = movement;
        this.angle = angle;
        this.timeMS = timeMS;
    }

    public double[] getMovement() {
        return movement;
    }

    public double getX() {
        return movement[0];
    }

    public double getY() {
        return movement[1];
    }

    public double getAngle() {
        return angle;
    }

    public int getTimeMS() {
        return timeMS;
    }

    public String toString() {
        return "Movement: X:"+getX() + " Y:"+getY() + " A:"+getAngle() + " T:"+getTimeMS();
    }
}
