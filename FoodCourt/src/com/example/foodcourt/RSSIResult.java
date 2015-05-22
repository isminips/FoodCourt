package com.example.foodcourt;

import android.net.wifi.ScanResult;
import java.util.List;
/**
 * Created by isminipsychoula on 5/22/15.
 */


public class RSSIResult {

    protected long timestamp;
    protected List<ScanResult> results;

    public RSSIResult(long timestamp_arg, List<ScanResult> results_arg) {
        timestamp = timestamp_arg;
        results = results_arg;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<ScanResult> getResults() {
        return results;
    }
}
