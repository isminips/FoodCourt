package com.example.foodcourt.particles;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.example.foodcourt.LocalizationActivity;

import java.util.ArrayList;
import java.util.List;

public class WifiScanReceiver extends BroadcastReceiver {
    String wifis[];
    WifiManager wifiManager;

    private LocalizationActivity activity;

    public WifiScanReceiver(LocalizationActivity activity, WifiManager wifiManager) {
        super();
        this.activity = activity;
        this.wifiManager = wifiManager;
    }

    @SuppressLint("UseValueOf")
    public void onReceive(Context c, Intent intent) {
        List<ScanResult> wifiScanList = wifiManager.getScanResults();
        wifis = new String[wifiScanList.size()];
        ArrayList<String> results = new ArrayList<String>();

        for(int i = 0; i < wifiScanList.size(); i++){
            wifis[i] = ((wifiScanList.get(i)).toString());
        }

        for (ScanResult scan : wifiScanList) {
            int channel = ((scan.frequency - 2412) / 5) + 1;  //calculates accurately for channels 1-13. Channel 14 is not generally available.
            results.add(scan.BSSID + "," + scan.SSID + "," + scan.level + "," + channel);
        }

        activity.updateBayes(results);
    }


}
