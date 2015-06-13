package com.example.foodcourt.rssi;

import com.example.foodcourt.LocalizationActivity;
import com.example.foodcourt.particles.Cloud;
import com.example.foodcourt.particles.Movement;
import com.example.foodcourt.particles.Point;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RSSIDatabase {

    private TreeMap<String, TreeMap<String, List<WifiResult>>> database;
    private long lastMappedTime;

    public RSSIDatabase() {
        database = new TreeMap<String, TreeMap<String, List<WifiResult>>>();
        lastMappedTime = System.currentTimeMillis();
    }

    public TreeMap<String, TreeMap<String, List<WifiResult>>> getDatabase() {
        return database;
    }

    public int size() {
        return database.size();
    }

    public void updateTime() {
        lastMappedTime = System.currentTimeMillis();
    }

    public void add(WifiResult wifiResult) {
        add(wifiResult.getRoom().getName(), wifiResult.getBSSID(), wifiResult);
    }

    public void add(String room, String mac, WifiResult result) {
        if (!database.containsKey(room)) {
            database.put(room, new TreeMap<String, List<WifiResult>>());
        }
        TreeMap<String, List<WifiResult>> roomInfo = database.get(room);
        if (!roomInfo.containsKey(mac)) {
            roomInfo.put(mac, new ArrayList<WifiResult>());
        }
        List<WifiResult> roomMacInfo = roomInfo.get(mac);
        roomMacInfo.add(result);
    }

    /**
     * Trace back steps from wifiScanData and movementData to create rssiDatabase
     *
     * NOTE: should only be called when particle cloud has converged enough.
     */
    public void createRSSIdatabase(LocalizationActivity activity, Cloud particleCloud, TreeMap<Long, List<WifiResult>> wifiScanData, TreeMap<Long, Movement> movementData) {
        if (wifiScanData.size() == 0 || movementData.size() == 0) return;

        // where am I now?
        Point currentPosition = particleCloud.getEstimatedPosition();

        Iterator<Long> wifiScanTimes = wifiScanData.descendingKeySet().iterator();
        long scanTime = wifiScanTimes.next();
        long mappedUntil = scanTime; // saved to use at the end of this method

        double[] movementBeforeLastScan = new double[]{0,0};
        for (long movementTimestamp : movementData.descendingKeySet()) {
            if (movementTimestamp > scanTime) {
                Movement m = movementData.get(movementTimestamp);
                movementBeforeLastScan[0] += m.getX();
                movementBeforeLastScan[1] += m.getY();
            }
        }

        // where was I at the time of the scan?
        Point scanPosition = new Point(currentPosition.getX() - movementBeforeLastScan[0], currentPosition.getY() - movementBeforeLastScan[1]);
        long previousScanTime;
        while (wifiScanTimes.hasNext()) {
            previousScanTime = wifiScanTimes.next();

            // don't do double work
            if (previousScanTime < lastMappedTime) break;

            double[] movement = new double[]{0,0};
            for (long movementTimestamp : movementData.descendingKeySet()) {
                if (movementTimestamp < scanTime && movementTimestamp > previousScanTime) {
                    Movement m = movementData.get(movementTimestamp);
                    movement[0] += m.getX();
                    movement[1] += m.getY();
                } else if (movementTimestamp < previousScanTime) {
                    break;
                }
            }

            //System.out.println("Movement between " + scanTime + " and " + previousScanTime + " was: " + movement[0] + ", " + movement[1]);

            // we now know where we were supposed to be at each scan time, so add the estimated rooms!
            for (WifiResult scanResult : wifiScanData.get(scanTime)) {
                scanResult.setRoom(activity.getEstimatedRoom(scanPosition));

                // only save to database if useful
                if (scanResult.hasRoom()) {
                    add(scanResult);
                }
            }

            // where was I at the time of the previous scan?
            scanPosition  = new Point(scanPosition.getX() - movement[0], scanPosition.getY() - movement[1]);
            scanTime = previousScanTime;
        }

        // our database is now completed until here
        lastMappedTime = mappedUntil;
    }

    public void writeToSD() {
        if (database.size() == 0) return;

        String write = "";
        for (TreeMap<String, List<WifiResult>> ssids : database.values()) {
            for (List<WifiResult> list : ssids.values()) {
                for (WifiResult result : list) {
                    write += result + "\n";
                }
            }
        }

        try {
            File myFile = new File("/sdcard/rssiDatabase.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(write);
            myOutWriter.close();
            fOut.close();
            System.out.println("Saved RSSI database on SD card");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private class Neighbor implements Comparable<Neighbor> {
        public double distance;
        public String room;

        public Neighbor(double distance, String room) {
            this.distance = distance;
            this.room = room;
        }

        public String toString() {
            return room + ": " + distance;
        }

        @Override
        public int compareTo(Neighbor another) {
            int BEFORE = 1;
            int AFTER = -1;
            int EQUAL = 0;

            if (another == null) return BEFORE;

            if (distance > another.distance)
                return BEFORE;
            else if (distance < another.distance)
                return AFTER;
            else
                return EQUAL;
        }
    }

    public String determineRoom(List<WifiResult> scanResult) {
        List<Neighbor> distances = new ArrayList<Neighbor>();

        for (Map.Entry<String, TreeMap<String, List<WifiResult>>> entry : database.entrySet()) {
            TreeMap<String, List<WifiResult>> roomInfo = entry.getValue();
            String room = entry.getKey();

            double distance = 0;

            for (WifiResult result: scanResult) {
                List<WifiResult> compList = roomInfo.get(result.getBSSID());
                if (compList == null || compList.size() == 0) continue;

                int level = result.getLevel();
                int compLevel = 0;

                for (WifiResult compare: compList) {
                    compLevel += compare.getLevel();
                }

                distance += Math.abs((level - compLevel) / compList.size());
            }

            distances.add(new Neighbor(distance, room));
        }

        Collections.sort(distances);

        System.out.println("Sorted list of distances:");
        for (Neighbor n : distances) {
            System.out.println(n.toString());
        }

        return distances.get(0).room;
    }
}
