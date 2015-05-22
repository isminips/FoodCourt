package com.example.foodcourt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Display;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by isminipsychoula on 5/22/15.
 */
public class WifiActivity extends Activity {
    WifiManager mainWifiObj;
    WifiScanReceiver wifiReciever;
    ListView list;
    String wifis[];
    private Location scanLocation;
    //private final List<RSSIResult> rssiResults= new ArrayList<>();
    private ListView rssiListView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);

        rssiListView = (ListView) findViewById(R.id.listViewRSSI);
        //list = (ListView)findViewById(R.id.listView1);
        mainWifiObj = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiReciever = new WifiScanReceiver();
        mainWifiObj.startScan();
        this.scanLocation = scanLocation;
    }


    protected void onPause() {
        unregisterReceiver(wifiReciever);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(wifiReciever, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    class WifiScanReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {

            List<ScanResult> wifiScanList = mainWifiObj.getScanResults();
            wifis = new String[wifiScanList.size()];
            ArrayList<String> results = new ArrayList<>();

            ArrayList<String> screenResults = new ArrayList<>();
//                for(int i = 0; i < wifiScanList.size(); i++){
//                    wifis[i] = ((wifiScanList.get(i)).toString());
//
//                }

            for (ScanResult scan : wifiScanList) {

                int channel = ((scan.frequency - 2412) / 5) + 1;  //calculates accurately for channels 1-13. Channel 14 is not generally available.
                results.add( scan.BSSID + "," + scan.SSID + "," + scan.level + "," + channel);
                screenResults.add(  scan.BSSID + "," + scan.SSID + "," + scan.level + "," + channel);
            }

//                list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
//                        android.R.layout.simple_list_item_1,wifis));
//                list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),
//                        android.R.layout.simple_list_item_1, screenResults));

            Context context = rssiListView.getContext();
            ArrayAdapter resultsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, screenResults);
            rssiListView.setAdapter(resultsAdapter);
        }
    }

}


