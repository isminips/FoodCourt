/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.foodcourt.particles;

import android.graphics.Rect;

import com.example.foodcourt.LocalizationActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Information about rooms within a floor.
 * TODO Adapt to having spacing different to 1m.
 *
 * @author Greg Albiston
 */
public class RoomInfo {

    public static final String[] HEADINGS = {"Name", "Width", "Height", "isRoom", "GlobalX", "GlobalY"};
    protected String name;
    protected double width;
    protected double height;
    protected double widthUnits;
    protected double heightUnits;
    protected Rectangle drawArea;
    protected boolean isRoom;
    protected double positionX;  //offset to place the room on global co-ordinates, used to measure distance between two points
    protected double positionY;  //offset to place the room on global co-ordinates, used to measure distance between two points
    protected Rectangle area;
    protected android.graphics.Point screenSize;
    protected Point totalDrawSize;
    /**
     * Constructor
     *
     * @param parts String array of elements to build class. See HEADINGS for
     * order.
     */
    public RoomInfo(final String[] parts) throws NumberFormatException {
        name = parts[0];
        width = Double.parseDouble(parts[1]);
        height = Double.parseDouble(parts[2]);
        isRoom = Boolean.parseBoolean(parts[3]);

        positionX = Double.parseDouble(parts[4]);
        positionY = Double.parseDouble(parts[5]);
        area = new Rectangle(positionX, positionY, width, height);
    }

    public Rectangle makeDrawArea(android.graphics.Point screensize, Point totalDrawSize) {
        this.screenSize = screensize;
        this.totalDrawSize = totalDrawSize;

        Point start = locationToPixel(positionX, positionY);
        Point end = locationToPixel(positionX + width, positionY + height);
        drawArea = new Rectangle(start.getX(), start.getY(), end.getX()-start.getX(), end.getY()-start.getY());
        widthUnits = Math.round(drawArea.width / width);
        heightUnits = Math.round(drawArea.height / height);

        return drawArea;
    }

    private Point locationToPixel(double x, double y) {
        return locationToPixel(new Point(x, y));
    }

    private Point locationToPixel(Point p) {
        if (screenSize == null || totalDrawSize == null)
            return p;

        return new Point(
                p.getX() * (screenSize.x / totalDrawSize.getX()),
                p.getY() * (screenSize.y / totalDrawSize.getY())
        );
    }

    /**
     * Rectangle used to draw the room.
     *
     * @return
     */
    public Rect getDrawArea() {
        return new Rect(
                drawArea.x.intValue(),
                drawArea.y.intValue(),
                drawArea.x.intValue()+drawArea.width.intValue(),
                drawArea.y.intValue()+drawArea.height.intValue()
        );
    }

    /**
     * Get name of the room.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Get physical width of the room.
     *
     * @return
     */
    public double getWidth() {
        return width;
    }

    /**
     * Get physical height of the room.
     *
     * @return
     */
    public double getHeight() {
        return height;
    }

    public boolean containsLocation(Point point){
        return area.contains(point);
    }

    public boolean containsPixel(Point point){
        return drawArea.contains(point);
    }

    /**
     * Whether the room represents an enclosed room or open ended corridor.
     * Affects whether the Android Detector starts at 0 (corridor) or 1 (room) for references.
     *
     * @return True, if a room. False, if a corridor.
     */
    public int isRoom() {
        return isRoom ? 1 : 0;
    }

    public double getArea() {
        return width * height;
    }

    public List<Particle> fillWithParticles(double totalArea, int numParticles) {
        double allowedParticles = (getArea() / totalArea) * numParticles;

        System.out.println(name + " " + allowedParticles);

        List<Particle> particles = new ArrayList<Particle>();
        for(int i = 0; i < allowedParticles; i++) {
            double x = positionX + Math.random() * width;
            double y = positionY + Math.random() * height;

            Particle p = new Particle(x, y);
            particles.add(p);
        }

        return particles;
    }

    /**
     * Checks whether the supplied array contains the expected headings for
     * room info data.
     *
     * @param parts String array to be checked.
     * @return
     */
    public static boolean headerCheck(String[] parts) {
        return Arrays.equals(HEADINGS, parts);
    }

    public static HashMap<String, RoomInfo> load(InputStream stream) throws IOException {
        HashMap<String, RoomInfo> roomInfo = new HashMap<String, RoomInfo>();

        String line;
        String[] parts;
        BufferedReader roomReader = new BufferedReader(new InputStreamReader(stream));
        line = roomReader.readLine();
        parts = line.split(",");
        if (RoomInfo.headerCheck(parts)) {
            while ((line = roomReader.readLine()) != null) {
                parts = line.split(",");
                roomInfo.put(parts[0], new RoomInfo(parts));
            }
        } else {
            System.out.println("RoomInfo headings not as expected.");
        }

        stream.close();

        return roomInfo;
    }

    public double getBorderProximity(Particle particle) {
        if (!isRoom) {
            return 1;
        }

        double maxDist, dist;
        if (width < height) {
            maxDist = width/2;
            dist = Math.min(particle.x - positionX, positionX+width - particle.x) ;
        } else {
            maxDist = height/2;
            dist = Math.min(particle.y - positionY, positionY+height - particle.y);
        }

//        double a = (dist/maxDist);
//        double b = a * LocalizationActivity.CLOUD_RANGE;
//
//        return Math.exp(b)/1.6;

//        double a = Math.pow((dist/maxDist),2);
//        double b = a * LocalizationActivity.CLOUD_RANGE;
//
//        return Math.exp(b);
        return dist/maxDist;
    }
}
