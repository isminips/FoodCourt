package com.example.foodcourt.particles;

public class Particle extends Point {

    private boolean alive;

    public Particle(double x, double y) {
        super(x, y);
        alive = true;
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public Point beforeMoving(double[] movement) {
        return new Point(x - movement[0], y - movement[1]);
    }

    public Particle copy() {
        return new Particle(x, y);
    }

    public Particle move(double[] movement) {
        return move(movement[0], movement[1]);
    }

    public Particle move(double xDisp, double yDisp) {
        x += xDisp;
        y += yDisp;

        return this;
    }

    public void kill() {
        alive = false;
    }

    public boolean isAlive() {
        return alive;
    }
}

