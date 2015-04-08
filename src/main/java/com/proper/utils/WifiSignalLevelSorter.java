package com.proper.utils;

import android.net.wifi.ScanResult;

import java.util.Comparator;

/**
 * Created by Lebel on 13/08/2014.
 */
public class WifiSignalLevelSorter implements Comparator<ScanResult> {
    @Override
    public int compare(ScanResult lhs, ScanResult rhs) {
        int ret = 0;

        if (lhs.level  < rhs.level) {
            ret = -1;
        }else if(lhs.level > rhs.level ) {
            ret = 1;
        }else if (lhs.level == rhs.level) {
            ret = 0;
        }
        return ret;
    }
}
