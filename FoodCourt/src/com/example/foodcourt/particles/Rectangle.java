/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.foodcourt.particles;

import java.util.Objects;

/**
 * Implementation of Rectangle. Intended to be common to both Java and Android
 * use of the library. Native awt.Rectangle is not available in Android SDK.
 *
 * @author Greg Albiston
 */
public class Rectangle {

    public Double x;
    public Double y;
    public Double width;
    public Double height;

    /**
     * Constructor
     *
     * @param x Top left hand corner x-coordinate.
     * @param y Top left hand corner y-coordinate.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     */
    public Rectangle(Double x, Double y, Double width, Double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Constructor
     *
     * @param x Top left hand corner x-coordinate.
     * @param y Top left hand corner y-coordinate.
     * @param width Width of the rectangle.
     * @param height Height of the rectangle.
     */
    public Rectangle(int x, int y, int width, int height) {
        this.x = Double.valueOf(x);
        this.y = Double.valueOf(y);
        this.width = Double.valueOf(width);
        this.height = Double.valueOf(height);
    }

    /**
     * Constructor - zero values
     */
    public Rectangle() {
        this.x = 0.0;
        this.y = 0.0;
        this.width = 0.0;
        this.height = 0.0;
    }

    /**
     * True, if coordinates are in the rectangle.
     *
     * @param x x-coordinate to test.
     * @param y y-coordinate to test.
     * @return
     */
    public boolean contains(Double x, Double y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    /**
     * True, if coordinates are in the rectangle.
     *
     * @param point point to test.
     * @return
     */
    public boolean contains(Point point) {
        return contains(point.getX(), point.getY());
    }
    
    @Override
    public boolean equals(Object o){
        boolean result = false;
        
        
        if(o instanceof Rectangle){
            Rectangle rect = (Rectangle) o;
            result = this.x.equals(rect.x) && this.y.equals(rect.y) && this.width.equals(rect.width) && this.height.equals(rect.height);            
        }else{
            throw new AssertionError("Only comparison with Rectangle supported.");
        }
        return result;
    }  

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.x);
        hash = 17 * hash + Objects.hashCode(this.y);
        hash = 17 * hash + Objects.hashCode(this.width);
        hash = 17 * hash + Objects.hashCode(this.height);
        return hash;
    }
    
    @Override
    public String toString(){
        return String.format("x: %s, y: %s, w: %s, h: %s", x, y, width, height);
    }
}
