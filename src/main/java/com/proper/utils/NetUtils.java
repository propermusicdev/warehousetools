package com.proper.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.*;
import android.os.AsyncTask;
import com.proper.warehousetools.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Lebel on 13/08/2014.
 */
public class NetUtils {
    private SupplicantState suppState;
    private WifiInfo wifiInfo;
    private String pwd = "\"God0fC0mm5\"";
    private String ssid = "\"Mercury\"";
    /**
     * Detecting whether the network is available
     *
     * @return
     */
    public static boolean isNetworkConnected(Activity act) {
        ConnectivityManager cm = (ConnectivityManager) act
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * Detecting whether the WIFI is turned On
     *
     * @return
     */
    public static boolean isWiFiSwitchedOn(Context context) {
        WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return mainWifi.isWifiEnabled();
    }

    /**
     * Turn WIFI - On
     *
     * @return
     */
    public void turnWifiOn(Context context) {
        WifiManager mainWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!mainWifi.isWifiEnabled()) mainWifi.setWifiEnabled(true);
    }

    /**
     * Get the current network type
     *
     * @return 0：No Network 1：WiFi Network 2：WAP Network 3：Ethernet Network
     */
    public static String getNetworkType(Activity act) {
        String netType = "";
        ConnectivityManager connectivityManager = (ConnectivityManager) act
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {

            String extraInfo = networkInfo.getExtraInfo();
            if (extraInfo != null) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = "CMNET";
                } else {
                    netType = "CMWAP";
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = "WIFI";
        }
        return netType;
    }

    public boolean isNetworkAvailable(Context context) {
        boolean success = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setConnectTimeout(3000);
                urlc.connect();
                if (urlc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    success = true;
                }
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        return success;
    }

    public boolean connectToDefaultWifi(Context context) {
        boolean success = false;
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo != null) {
            suppState = wifiInfo.getSupplicantState();
            if (suppState.name().equalsIgnoreCase("COMPLETED")) {
                if (wifiInfo.getSSID() != null && !wifiInfo.getSSID().toString().trim().equalsIgnoreCase("<none>") &&
                        !wifiInfo.getSSID().toString().trim().equalsIgnoreCase("")) {
                    if (!wifiInfo.getSSID().equalsIgnoreCase("Mercury")) {
                        success = connectToMainWIfiPartTwo(wifiManager);
                    } else {
                        success = true;
                    }
                } else {
                    success = connectToMainWIfiPartTwo(wifiManager);
                }
            } else {
                success = connectToMainWIfiPartTwo(wifiManager);
            }
        } else {
            success = connectToMainWIfiPartTwo(wifiManager);
        }
        return success;
    }

    public boolean connectToMainWIfiPartTwo(WifiManager wifiManager) {
        // TODO - Handle Wifi Connectivity
        boolean success = false;
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        //Remove saved associated networks
        List<WifiConfiguration> wifiConfigList = wifiManager.getConfiguredNetworks();
        if (wifiConfigList.size() == 0) {
            success = true;
        }
        for (WifiConfiguration wifiConfig: wifiConfigList) {
            int netId = wifiConfig.networkId;
            wifiManager.removeNetwork(netId);
        }

        //Create a new (netID) wifi network connection profile
        WifiConfiguration wc = new WifiConfiguration();
        wc.SSID = ssid;
        wc.preSharedKey = pwd;
        wc.status = WifiConfiguration.Status.ENABLED;
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        // Enable and Connect to the given connection
        int netId = wifiManager.addNetwork(wc);
        wifiManager.saveConfiguration();
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();
        try {
            Thread.sleep(2000); //2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (wc.status == WifiConfiguration.Status.CURRENT ) {
            //then we're connected
            success = true;
        } else {
            //try tio connect again, but this time disable wifi before we continue
            //Remove saved associated networks
            for (WifiConfiguration wifiConfig: wifiConfigList) {
                int netId1 = wifiConfig.networkId;
                wifiManager.removeNetwork(netId1);
            }

            // Once again, Enable and Connect to the given connection
            int sameNetId = wifiManager.addNetwork(wc);
            wifiManager.saveConfiguration();
            wifiManager.disconnect();
            if (wifiManager.isWifiEnabled()) {
                wifiManager.setWifiEnabled(false);
            }
            wifiManager.setWifiEnabled(true);
            wifiManager.enableNetwork(sameNetId, true);
            wifiManager.reconnect();
        }
        return success;
    }

    public boolean connectToMainWIfi(WifiManager wifiManager) {
        // TODO - Handle Wifi Connectivity
        boolean success = false;
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        if (removeAllSavedNetworks(wifiManager)) {
            // setup a wifi configuration to our chosen network
            WifiConfiguration wc = new WifiConfiguration();
            wc.SSID = ssid;
            wc.preSharedKey = pwd;
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            // connect to and enable the connection
            int netId = wifiManager.addNetwork(wc);
            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            success = wifiManager.reconnect();
        }

        return success;
    }

    private boolean removeAllSavedNetworks(WifiManager mainWifi) {
        boolean success = false;
        if (!mainWifi.isWifiEnabled()) {
            //log.d(TAG, "Enabled wifi before remove configured networks");
            mainWifi.setWifiEnabled(true);
        }
        List<WifiConfiguration> wifiConfigList = mainWifi.getConfiguredNetworks();
        if (wifiConfigList.size() == 0) {
            //log.d(TAG, "no configuration list is null");
            success = true;
        }
        //log.d(TAG, "size of wifiConfigList: " + wifiConfigList.size());
        for (WifiConfiguration wifiConfig: wifiConfigList) {
            //log.d(TAG, "remove wifi configuration: " + wifiConfig.networkId);
            int netId = wifiConfig.networkId;
            mainWifi.removeNetwork(netId);
            success = mainWifi.saveConfiguration();
        }
        return success;
    }

    private class checkConnectionAsync extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... contexts) {
            boolean ret = false;
            ret = isNetworkAvailable(contexts[0]);
            return ret;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    public class CheckConnectionCallable implements Callable<Boolean> {
        private Context context;
        public CheckConnectionCallable(Context context){
            this.context = context;
        }

        @Override
        public Boolean call() throws Exception {
            boolean ret = false;
            ret = isNetworkAvailable(this.context);
            return ret;
        }
    }

    /*
    * Disconnect from the current AP and remove configured networks.
    */
//    public boolean disconnectAP() {
//        // remove saved networks
//        if (!mWifiManager.isWifiEnabled()) {
//            log("Enabled wifi before remove configured networks");
//            mWifiManager.setWifiEnabled(true);
//            sleep(SHORT_TIMEOUT);
//        }
//        List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
//        if (wifiConfigList == null) {
//            log("no configuration list is null");
//            return true;
//        }
//        log("size of wifiConfigList: " + wifiConfigList.size());
//        for (WifiConfiguration wifiConfig: wifiConfigList) {
//            log("remove wifi configuration: " + wifiConfig.networkId);
//            int netId = wifiConfig.networkId;
//            mWifiManager.forget(netId, new WifiManager.ActionListener() {
//                public void onSuccess() {
//                }
//                public void onFailure(int reason) {
//                    log("Failed to forget " + reason);
//                }
//            });
//        }
//        return true;
//    }

}
