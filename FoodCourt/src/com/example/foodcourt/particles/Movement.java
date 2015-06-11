package com.example.foodcourt.particles;

public class Movement {

    private double[] movement;
    private double angle;

    public Movement(double[] movement, double angle) {
        this.movement = movement;
        this.angle = angle;
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

    public String toString() {
        return "Movement: X:"+getX() + " Y:"+getY();
    }
}
