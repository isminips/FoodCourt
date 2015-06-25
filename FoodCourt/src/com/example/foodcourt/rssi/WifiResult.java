package com.example.foodcourt.rssi;

public class WifiResult {
    private String BSSID;
    private String SSID;
    private int level;
    private int channel;
    private long timestamp;

    private String room;

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

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public boolean hasRoom() {
        return room != null && room.length() != 0;
    }

    public String toString() {
        String separator = RSSIDatabase.SEPARATOR;
        String result = BSSID + separator + SSID + separator + level + separator + channel + separator + timestamp;

        if (hasRoom()) {
            result += separator + room;
        }

        return result;
    }
}
