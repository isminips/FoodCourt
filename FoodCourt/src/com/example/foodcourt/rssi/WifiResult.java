package com.example.foodcourt.rssi;

import com.example.foodcourt.particles.RoomInfo;

public class WifiResult {
    private String BSSID;
    private String SSID;
    private int level;
    private int channel;
    private long timestamp;

    private RoomInfo room;

    public WifiResult(String BSSID, String SSID, int level, int channel, long timestamp) {
        this.BSSID = BSSID;
        this.SSID = SSID;
        this.level = level;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public int getLevel() {
        return level;
    }

    public int getChannel() {
        return channel;
    }

    public String getBSSID() {
        return BSSID;
    }

    public String getSSID() {
        return SSID;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RoomInfo getRoom() {
        return room;
    }

    public void setRoom(RoomInfo room) {
        this.room = room;
    }

    public boolean hasRoom() {
        return room != null;
    }

    public String toString() {
        String result = BSSID + "," + SSID + "," + level + "," + channel;

        if (room != null) {
            result += "," + room.getName();
        }

        return result;
    }
}
