package com.example.foodcourt.particles;

/**
 * Created with IntelliJ IDEA.
 * User: pierre
 * Date: 25/09/13
 * Time: 14:47
 * To change this template use File | Settings | File Templates.
 */
public class Particle extends Point {

    public Particle(double x, double y) {
        super(x, y);
    }

    public Point getPoint() {
        return new Point(x, y);
    }

    public void move(double xDisp, double yDisp) {
        x += xDisp;
        y += yDisp;
    }
}

