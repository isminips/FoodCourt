package com.example.foodcourt.particles;

import android.graphics.Rect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RoomInfo {

    public enum RoomType {
        Room,
        Aisle,
        Blocked
    }

    public static final String[] HEADINGS = {"Name", "Width", "Height", "Type", "GlobalX", "GlobalY"};
    protected String name;
    protected RoomType type;
    protected double x;  //offset to place the room on global co-ordinates, used to measure distance between two points
    protected double y;  //offset to place the room on global co-ordinates, used to measure distance between two points
    protected double width;
    protected double height;

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
        type = determineRoomType(parts[3]);

        x = Double.parseDouble(parts[4]);
        y = Double.parseDouble(parts[5]);
    }

    private RoomType determineRoomType(String type) {
        if(type.equals("ROOM")) {
            return RoomType.Room;
        }
        else if(type.equals("AISLE")) {
            return RoomType.Aisle;
        }
        else if(type.equals("BLOCKED")) {
            return RoomType.Blocked;
        }
        return null;
    }

    public void setDrawDimensions(android.graphics.Point screensize, Point totalDrawSize) {
        this.screenSize = screensize;
        this.totalDrawSize = totalDrawSize;
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
        Point start = locationToPixel(x, y);
        Point end = locationToPixel(x + width, y + height);

        return new Rect(
                start.getXint(),
                start.getYint(),
                end.getXint(),
                end.getYint()
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

    public double getArea() {
        return width * height;
    }

    public boolean containsLocation(Point point){
        double x = point.getX();
        double y = point.getY();

        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    /**
     * Whether the room represents an enclosed room.
     */
    public boolean isRoom() {
        return type.equals(RoomType.Room);
    }

    /**
     * Whether the room represents an aisle.
     */
    public boolean isAisle() {
        return type.equals(RoomType.Aisle);
    }

    /**
     * Whether the room represents a blocked area.
     */
    public boolean isBlocked() {
        return type.equals(RoomType.Blocked);
    }

    /**
     * Whether the room represents the entire aisle
     */
    public boolean isAislePlaceholder() {
        return isAisle() && name.startsWith("Aisle");
    }

    public List<Particle> fillWithParticles(double totalArea, int numParticles) {
        double allowedParticles = (getArea() / totalArea) * numParticles;

        List<Particle> particles = new ArrayList<Particle>();
        for(int i = 0; i < allowedParticles; i++) {
            double x = this.x + Math.random() * width;
            double y = this.y + Math.random() * height;

            Particle p = new Particle(x, y);
            particles.add(p);
        }

        return particles;
    }

    public boolean collidesWithWall(Point point) {
        if (name.equals("C17")) {
            return isRoom() && (point.getX() > x + width);
        } else if (name.equals("C18")) {
            return isRoom() && (point.getX() < x);
        } else {
            return isRoom() && (point.getX() < x || point.getX() > x + width);
        }
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

    public String toString() {
        return name;
    }
}
