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
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;
import com.proper.data.diagnostics.WifiLogEntry;
import com.proper.messagequeue.LogResolver;
import com.proper.utils.NetUtils;
import com.proper.utils.WifiSignalLevelSorter;
import com.proper.warehousetools.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lebel on 13/02/2015.
 */
public class WifiReportingService extends Service {
    private static final String TAG = WifiReportingService.class.getSimpleName();
    private IBinder binder = null;
    private WifiLevelReceiver wifiReceiver;
    private HandlerThread mWorkerHandlerThread;
    private Handler handler;
    private Handler updaterToastHandler;
    private WifiManager mainWifi;
    private static final long interval = 5000; // 1 hour  -   120000000
    private Configurator configurator = null;
    private LogResolver resolver = null;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreated");
        mainWifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);    //  --> initialise wifi <--
        resolver = new LogResolver(this);
        configurator =  new Configurator(this);
        configurator.addPropertyChangeListener(new MyPropertyChangeListener());

        updaterToastHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    /** Report Updater Event **/
                    AbstractMap.SimpleEntry<Boolean, PropertyChangeEvent> updateEvent = (AbstractMap.SimpleEntry<Boolean, PropertyChangeEvent>) msg.obj;
                    ScanResult newWifi = (ScanResult) updateEvent.getValue().getNewValue();
                    boolean result = updateEvent.getKey();
                    Toast toast = new Toast(WifiReportingService.this);
                    toast.setGravity(Gravity.BOTTOM| Gravity.CENTER, 0, 0);
                    if (result) {
                        toast.makeText(WifiReportingService.this, String.format("[[--> Success <--]]\nSwitching to a stronger WIFI\nConnecting to: %s\nOn channel: %s",
                                getEndPointLocation(WifiReportingService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
                    }else {
                        toast.makeText(WifiReportingService.this, String.format("[[--> Error <--]]\nUnable to connect to: %s\nOn channel: %s",
                                getEndPointLocation(WifiReportingService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
                    }
                }
            }
        };

        wifiReceiver = new WifiLevelReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

//    @Override
//    public synchronized void onStart(Intent intent, int startId) {
//        super.onStart(intent, startId);
//        if (!configurator.isRunning) {
//            configurator.setRunning(true);
//        }
//
//        Log.d(TAG, "onStarted");
//        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
//        long delay = 3; //the delay between the termination of one execution and the commencement of the next
//        exec.scheduleWithFixedDelay(new doWorkInBackground(), 0, delay, TimeUnit.SECONDS);
//    }

    @Override
    public synchronized int onStartCommand(Intent intent, int flags, int startId) {

        if (!configurator.isRunning) {
            configurator.setRunning(true);
        }
        Log.d(TAG, "onStartCommand");
        ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
        long delay = 3; //the delay between the termination of one execution and the commencement of the next
        exec.scheduleWithFixedDelay(new doWorkInBackground(), 0, delay, TimeUnit.SECONDS);
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
        private Thread updater = null;
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equalsIgnoreCase("isRunning")) {
                boolean newVal = (Boolean) event.getNewValue();
                if (newVal) {
                    Toast.makeText(WifiReportingService.this, "WifiReceiver is initialised", Toast.LENGTH_SHORT).show();
                }
            }
            if (event.getPropertyName().equalsIgnoreCase("endpointElect")) {
                ScanResult oldWifi = (ScanResult) event.getOldValue();
                ScanResult newWifi = (ScanResult) event.getNewValue();

                if (event.getOldValue() != null) {
                    if (!oldWifi.BSSID.equalsIgnoreCase(newWifi.BSSID)) {
                        configurator.setLogEntry(new WifiLogEntry(getEndPointLocation(getApplicationContext(), newWifi.BSSID),newWifi.BSSID,
                                oldWifi.BSSID, newWifi.level, getWifiChannel(newWifi.frequency), "", 300, 1000*15, new Date().getTime()));
                        Toast.makeText(WifiReportingService.this, String.format("Switching to a stronger WIFI\nNow Connected to: %s\nOn channel: %s",
                                getEndPointLocation(WifiReportingService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_SHORT).show();
                        //UpdateNotifier updater = new UpdateNotifier(event); updater.run();
                        if(updater != null) updater.interrupt();
                        updater = new Thread(new UpdateNotifier(event));
                        updater.setName("WiFiLoggerUpdater");
                        updater.start();
                    }
                } else {
                    configurator.setLogEntry(null);
                    ScanResult wifiValue = (ScanResult) event.getNewValue();
                    Toast.makeText(WifiReportingService.this, String.format("Switching to a stronger WIFI\nNow Connected to: %s\nOn channel: %s",
                            getEndPointLocation(getApplicationContext(), wifiValue.BSSID), getWifiChannel(wifiValue.frequency)), Toast.LENGTH_SHORT).show();
                    //UpdateNotifier updater = new UpdateNotifier(event); updater.run();  //subdued
                    if(updater != null) updater.interrupt();
                    updater = new Thread(new UpdateNotifier(event));
                    updater.setName("WiFiLoggerUpdater");
                    updater.start();
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
            //boolean success = connectToAStrongerWIfi(newWifi);
            boolean success = true;
            Message msg = new Message();
            AbstractMap.SimpleEntry<Boolean, PropertyChangeEvent> updateResult= null;
            if (success) {
                //TODO - ********************************  Log the change in the file  ************************************************
                WifiLogEntry entry = new WifiLogEntry();
                entry = configurator.getLogEntry() != null? configurator.getLogEntry() : new WifiLogEntry("Testing", "Test BSSID",
                        "Test OldAPName", -20, 9, "0XYZ0", 100, 100*60, new Date().getTime());
                //resolver.LogWifiReceiver(getApplicationContext(), entry);
                resolver.LogWifiReceiverByFTP(getApplicationContext(), entry);
                updateResult = new AbstractMap.SimpleEntry<Boolean, PropertyChangeEvent>(success, event);
                msg.obj = updateResult;
                msg.what = 1;
                updaterToastHandler.sendMessage(msg);
//                Toast.makeText(WifiReportingService.this, String.format("[[--> Success <--]]\nSwitching to a stronger WIFI\nConnecting to: %s\nOn channel: %s",
//                        getEndPointLocation(WifiReportingService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
            } else {
                updateResult = new AbstractMap.SimpleEntry<Boolean, PropertyChangeEvent>(success, event);
                msg.obj = updateResult;
                msg.what = 0;
                updaterToastHandler.sendMessage(msg);
//                Toast.makeText(WifiReportingService.this, String.format("[[--> Error <--]]\nUnable to connect to: %s\nOn channel: %s",
//                        getEndPointLocation(WifiReportingService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean connectToAStrongerWIfi(ScanResult result) {
        // TODO - Handle Wifi Connectivity
        boolean success = false;
        if (!mainWifi.isWifiEnabled()) {
            mainWifi.setWifiEnabled(true);
        }
        String pwd = "\"God0fC0mm5\"";
        String ssid = "\"Mercury\"";
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
//            int netId = mainWifi.addNetwork(wc);
//            mainWifi.disconnect();   //disconnect ->>
//            mainWifi.enableNetwork(netId, true);
            //success = mainWifi.reconnect();
            success = true;
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
        }else if (BSSID.equalsIgnoreCase(res.getString(R.string.ENDPOINT_THE6s))) {
            loc = "ENDPOINT_THE6s";
        }else if (BSSID.equalsIgnoreCase(res.getString(R.string.ENDPOINT_Ones1P))) {
            loc = "ENDPOINT_Ones1P";
        }else {
            loc = "Undetermined";
        }
        return loc;
    }

    class Configurator {
        private PropertyChangeSupport pcs1;
        private boolean isRunning = false;
        private ScanResult endpointElect;
        private WifiLogEntry logEntry;

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

        public WifiLogEntry getLogEntry() {
            return logEntry;
        }

        public void setLogEntry(@Nullable WifiLogEntry logEntry) {
            this.logEntry = logEntry;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.pcs1.addPropertyChangeListener(listener);
        }
    }

    public class WifiLevelReceiver extends BroadcastReceiver {

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
                    res.getString(R.string.ENDPOINT_BACKSTOCK8), res.getString(R.string.ENDPOINT_EXPORT),
                    res.getString(R.string.ENDPOINT_THE6s), res.getString(R.string.ENDPOINT_Ones1P)};

            if (wifiResults != null && !wifiResults.isEmpty()) {
                for (int i = 0; i < wifiResults.size(); i++) {

                    //If the current endpoint.SSID == to our main wifi then add to list
                    if (wifiResults.get(i).SSID.equalsIgnoreCase("Mercury")) {
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
                utils.connectToDefaultWifi(WifiReportingService.this);
            }

            //If for some reason we're still not connected then simply connect
            if (info != null && !info.getBSSID().isEmpty()) {
                boolean isConnected = utils.connectToDefaultWifi(WifiReportingService.this);
                if (isConnected) {
                    //TODO - **************************    Report !!!     **********************************
                    for (int x = connections.size() -1; x >= 0; x--) {
                        if (info != null && !info.getBSSID().isEmpty()) {
                            //if the current info is in our list of accepted endpoints then continue
                            if (!(Arrays.binarySearch(acceptedParam, info.getBSSID()) == -1)) {
                                //Make sure that it's not the the one we're currently connected to
                                if (!info.getBSSID().equalsIgnoreCase(connections.get(x).BSSID)) {
                                    configurator.setEndpointElect(connections.get(x));
                                    //Compare the current signal level to our WiFi Elect level
//                                    if (connections.get(x).level > info.getRssi()) {
//                                        // Nominate a new wifi Elect -> this will in turn fire property changed -> update the UI -> connect to a new better connection
//                                        configurator.setEndpointElect(connections.get(x));
//                                        break;
//                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
