package com.proper.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.proper.utils.NetUtils;
import com.proper.utils.WifiSignalLevelSorter;
import com.proper.warehousetools.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lebel on 12/08/2014.
 */
public class BinMoveService extends Service {
    private static final String TAG = BinMoveService.class.getSimpleName();
    private IBinder binder = null;
    private WifiLevelReceiver wifiReceiver;
    private HandlerThread mWorkerHandlerThread;
    private Handler handler;
    private WifiManager mainWifi;
    private static final long interval = 3600000 ; // 1 hour  -   120000000
    private Configurator configurator = null;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreated");
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);    //  --> initialise wifi <--
        configurator =  new Configurator(this);
        configurator.addPropertyChangeListener(new MyPropertyChangeListener());

        wifiReceiver = new WifiLevelReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(
                WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public synchronized void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (!configurator.isRunning) {
            configurator.setRunning(true);
        }

        Log.d(TAG, "onStarted");
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        long delay = 1; //the delay between the termination of one execution and the commencement of the next
        exec.scheduleWithFixedDelay(new doWorkInBackground(), 0, delay, TimeUnit.MINUTES);
    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {

        if (!configurator.isRunning) {
            configurator.setRunning(true);
        }
        Log.d(TAG, "onStartCommand");
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        long delay = 1; //the delay between the termination of one execution and the commencement of the next
        exec.scheduleWithFixedDelay(new doWorkInBackground(), 0, delay, TimeUnit.MINUTES);
        return START_NOT_STICKY;
    }

    @Override
    public synchronized void onDestroy() {
        super.onDestroy();
        if (configurator.isRunning) {
            configurator.setRunning(false);
        }
        unregisterReceiver(wifiReceiver);
        mWorkerHandlerThread.quit();
        mWorkerHandlerThread = null;
        handler = null;
        Log.d(TAG, "onDestroy");
    }

    private void doInBackground(Intent intent) {
        handler.postDelayed(new Runnable() {

            @Override
            public void run()
            {
                wifiReceiver = new WifiLevelReceiver();
                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                if (!mainWifi.isWifiEnabled()) {
                    mainWifi.setWifiEnabled(true);
                }
                mainWifi.startScan();
                //doInBackground();  //loop again?
            }
        }, interval);

    }

    public class MyPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equalsIgnoreCase("isRunning")) {
                boolean newVal = (Boolean) event.getNewValue();
                if (newVal) {
                    Toast.makeText(BinMoveService.this, "WifiReceiver is initialised", Toast.LENGTH_SHORT).show();
                }
            }
            if (event.getPropertyName().equalsIgnoreCase("endpointElect")) {
                ScanResult oldWifi = (ScanResult) event.getOldValue();
                ScanResult newWifi = (ScanResult) event.getNewValue();

                if (event.getOldValue() != null) {
                    if (!oldWifi.BSSID.equalsIgnoreCase(newWifi.BSSID)) {
                        Toast.makeText(BinMoveService.this, String.format("Switching to a stronger WIFI\nNow Connected to: %s\nOn channel: %s",
                                getEndPointLocation(BinMoveService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_SHORT).show();
                        UpdateNotifier updater = new UpdateNotifier(event);
                        updater.run();
                    }
                } else {
                    ScanResult wifiValue = (ScanResult) event.getNewValue();
                    Toast.makeText(BinMoveService.this, String.format("Switching to a stronger WIFI\nNow Connected to: %s\nOn channel: %s",
                            getEndPointLocation(BinMoveService.this, wifiValue.BSSID), getWifiChannel(wifiValue.frequency)), Toast.LENGTH_SHORT).show();
                    UpdateNotifier updater = new UpdateNotifier(event);
                    updater.run();
                }
            }
        }
    }

    class doWorkInBackground implements Runnable {

        @Override
        public void run() {
            if (!mainWifi.isWifiEnabled()) {
                mainWifi.setWifiEnabled(true);
            }
            mainWifi.startScan();
        }
    }

    class UpdateNotifier implements Runnable {
        private PropertyChangeEvent event;

        UpdateNotifier(PropertyChangeEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
            ScanResult newWifi = (ScanResult) event.getNewValue();
            boolean success = connectToAStrongerWIfi(newWifi);
            if (success) {
                Toast.makeText(BinMoveService.this, String.format("[[--> Success <--]]\nSwitching to a stronger WIFI\nConnecting to: %s\nOn channel: %s",
                        getEndPointLocation(BinMoveService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(BinMoveService.this, String.format("[[--> Error <--]]\nUnable to connect to: %s\nOn channel: %s",
                        getEndPointLocation(BinMoveService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean connectToAStrongerWIfi(ScanResult result) {
        // TODO - Handle Wifi Connectivity
        boolean success = false;
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }
        String pwd = "\"propermus1c\"";
        String ssid = "\"wifi_zz\"";
        if (removeAllSavedNetworks()) {
            // setup a wifi configuration to our chosen network
            WifiConfiguration wc = new WifiConfiguration();
            //wc.SSID = getResources().getString(R.string.ssid);
            //wc.preSharedKey = getResources().getString(R.string.password);
            wc.SSID = ssid;
            wc.BSSID = result.BSSID;
            wc.preSharedKey = pwd;
            wc.status = WifiConfiguration.Status.ENABLED;
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            // connect to and enable the connection
            int netId = mainWifi.addNetwork(wc);
            mainWifi.disconnect();   //disconnect ->>
            mainWifi.enableNetwork(netId, true);
            success = mainWifi.reconnect();
        }
        return success;
    }

    private boolean removeAllSavedNetworks() {
        boolean success = false;
        if (!mainWifi.isWifiEnabled()) {
            Log.d(TAG, "Enabled wifi before remove configured networks");
            mainWifi.setWifiEnabled(true);
        }
        List<WifiConfiguration> wifiConfigList = mainWifi.getConfiguredNetworks();
        if (wifiConfigList == null) {
            Log.d(TAG, "no configuration list is null");
            return true;
        }
        Log.d(TAG, "size of wifiConfigList: " + wifiConfigList.size());
        for (WifiConfiguration wifiConfig: wifiConfigList) {
            Log.d(TAG, "remove wifi configuration: " + wifiConfig.networkId);
            int netId = wifiConfig.networkId;
            mainWifi.removeNetwork(netId);
            mainWifi.saveConfiguration();
            success = true;
        }
        return success;
    }

    //Get Channel that the WiFi Endpoint is broadcasting at
    private static int getWifiChannel(int frequency) {
        final int[] channelsFrequency = {0,2412,2417,2422,2427,2432,2437,2442,2447,2452,2457,2462,2467,2472,2484};
        int channel = Arrays.binarySearch(channelsFrequency, frequency);
        return channel;
    }

    private static String getEndPointLocation(Context context, String BSSID) {
        final Resources res = context.getResources();
        String loc = "";

        if (BSSID.equalsIgnoreCase(res.getString(R.string.ENDPOINT_THE2S))) {
            loc = "ENDPOINT_THE2s";
        }else if (BSSID.equalsIgnoreCase(res.getString(R.string.ENDPOINT_AMAZONDISPATCH))) {
            loc = "ENDPOINT_AMAZONDISPATCH";
        }else if (BSSID.equalsIgnoreCase(res.getString(R.string.ENDPOINT_BACKSTOCK8))) {
            loc = "ENDPOINT_BACKSTOCK8";
        }else if (BSSID.equalsIgnoreCase(res.getString(R.string.ENDPOINT_EXPORT))) {
            loc = "ENDPOINT_EXPORT";
        } else {
            loc = "Undetermined";
        }
        return loc;
    }

    class Configurator {
        private PropertyChangeSupport pcs1;
        private boolean isRunning = false;
        private ScanResult endpointElect;

        Configurator(Context context) {
            this.pcs1 = new PropertyChangeSupport(context);
        }

        public boolean isRunning() {
            return isRunning;
        }

        public void setRunning(boolean isRunning) {
            this.pcs1.firePropertyChange("isRunning", this.isRunning, isRunning);
            this.isRunning = isRunning;
        }

        public ScanResult getEndpointElect() {
            return endpointElect;
        }

        public void setEndpointElect(ScanResult endpointElect) {
            this.pcs1.firePropertyChange("endpointElect", this.endpointElect, endpointElect);
            this.endpointElect = endpointElect;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.pcs1.addPropertyChangeListener(listener);
        }
    }

    class WifiLevelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            final Resources res = context.getResources();
            final ArrayList<ScanResult> connections = new ArrayList<ScanResult>();
            final List<ScanResult> wifiResults = mainWifi.getScanResults();
            final WifiSignalLevelSorter sorter = new WifiSignalLevelSorter();
            final NetUtils utils = new NetUtils();
            WifiInfo info = null;

            //Layout our accepted parameters
            String[] acceptedParam = {res.getString(R.string.ENDPOINT_THE2S), res.getString(R.string.ENDPOINT_AMAZONDISPATCH),
                    res.getString(R.string.ENDPOINT_BACKSTOCK8), res.getString(R.string.ENDPOINT_EXPORT)};

            if (wifiResults != null && !wifiResults.isEmpty()) {
                for (int i = 0; i < wifiResults.size(); i++) {

                    //If the current endpoint.SSID == to our main wifi then add to list
                    if (wifiResults.get(i).SSID.equalsIgnoreCase("wifi_zz")) {
                        connections.add(wifiResults.get(i));
                    }
                }
            }

            //Sort our list based on signal strength in ascending order
            Collections.sort(connections, sorter);
            //Collections.sort(wifiResults, sorter);

            //Get current wifi info
            if (networkInfo.isConnected()) info = mainWifi.getConnectionInfo();
            if (info  == null) {
                utils.connectToDefaultWifi(BinMoveService.this);
            }

            //If for some reason we're still not connected then simply connect
            if (info != null && !info.getBSSID().isEmpty()) {
                utils.connectToDefaultWifi(BinMoveService.this);
            } else {
                //loop through results, if instance.BSSID != info.getBSSID
                if (connections != null && !connections.isEmpty()) {
                    for (int x = connections.size() -1; x >= 0; x--) {
                        if (info != null && !info.getBSSID().isEmpty()) {
                            //if the current info is in our list of accepted endpoints then continue
                            if (!(Arrays.binarySearch(acceptedParam, info.getBSSID()) == -1)) {
                                //Make sure that it's not the the one we're currently connected to
                                if (!info.getBSSID().equalsIgnoreCase(connections.get(x).BSSID)) {
                                    //Compare the current signal level to our WiFi Elect level
                                    if (connections.get(x).level > info.getRssi()) {
                                        // Nominate a new wifi Elect -> this will in turn fire property changed -> update the UI -> connect to a new better connection
                                        configurator.setEndpointElect(connections.get(x));
                                        break;
                                    }
                                    break;
                                } else {
                                    continue;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
