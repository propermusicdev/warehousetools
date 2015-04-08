package com.proper.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.proper.data.diagnostics.LogEntry;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.utils.FileUtils;
import com.proper.utils.UpdaterFileSorter;
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.R;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPFile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Lebel on 10/11/2014.
 */
public class UpdaterService extends Service {
    private static final String TAG = UpdaterService.class.getSimpleName();
    private IBinder binder = null;
    private AppContext appContext;
    //private WifiLevelReceiver wifiReceiver;
    private HandlerThread mWorkerHandlerThread;
    private Handler handler;
    private WifiManager mainWifi;
    private static final long interval = 7200000 ; // every 2 hours
    private Configurator configurator = null;
    protected int screenSize;
    protected String deviceID = "";
    protected String deviceIMEI = "";
    protected Date utilDate = Calendar.getInstance().getTime();
    protected java.sql.Timestamp today = null;
    protected DeviceUtils device = null;
    protected HttpMessageResolver resolver = null;
    protected UserAuthenticator authenticator = null;
    protected UserLoginResponse currentUser = null;
    protected LogHelper logger = new LogHelper();
    protected com.proper.messagequeue.Message thisMessage = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = (AppContext) getApplication();
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        authenticator = new UserAuthenticator(this);
        device = new DeviceUtils(this);
        logger = new LogHelper();
        thisMessage = new com.proper.messagequeue.Message();
        deviceID = device.getDeviceID();
        deviceIMEI = device.getIMEI();
        currentUser = authenticator.getCurrentUser();
        resolver = new HttpMessageResolver(appContext);
    }

    @Override
    public void onStart(Intent intent, int startId) {
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        //return super.onStartCommand(intent, flags, startId);
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
    public void onDestroy() {
        super.onDestroy();
        if (configurator.isRunning) {
            configurator.setRunning(false);
        }
        //unregisterReceiver(wifiReceiver);
        mWorkerHandlerThread.quit();
        mWorkerHandlerThread = null;
        handler = null;
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
//    private void doInBackground(Intent intent) {
//        handler.postDelayed(new Runnable() {
//
//            @Override
//            public void run()
//            {
//                //wifiReceiver = new WifiLevelReceiver();
//                //registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
//                if (!mainWifi.isWifiEnabled()) {
//                    mainWifi.setWifiEnabled(true);
//                }
//                mainWifi.startScan();
//                //doInBackground();  //loop again?
//            }
//        }, interval);
//
//    }

    public class MyPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getPropertyName().equalsIgnoreCase("isRunning")) {
                boolean newVal = (Boolean) event.getNewValue();
                if (newVal) {
                    Toast.makeText(UpdaterService.this, "WifiReceiver is initialised", Toast.LENGTH_SHORT).show();
                }
            }
//            if (event.getPropertyName().equalsIgnoreCase("endpointElect")) {
//                ScanResult oldWifi = (ScanResult) event.getOldValue();
//                ScanResult newWifi = (ScanResult) event.getNewValue();
//
//                if (event.getOldValue() != null) {
//                    if (!oldWifi.BSSID.equalsIgnoreCase(newWifi.BSSID)) {
//                        Toast.makeText(BinMoveService.this, String.format("Switching to a stronger WIFI\nNow Connected to: %s\nOn channel: %s",
//                                getEndPointLocation(BinMoveService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_SHORT).show();
//                        UpdateNotifier updater = new UpdateNotifier(event);
//                        updater.run();
//                    }
//                } else {
//                    ScanResult wifiValue = (ScanResult) event.getNewValue();
//                    Toast.makeText(BinMoveService.this, String.format("Switching to a stronger WIFI\nNow Connected to: %s\nOn channel: %s",
//                            getEndPointLocation(BinMoveService.this, wifiValue.BSSID), getWifiChannel(wifiValue.frequency)), Toast.LENGTH_SHORT).show();
//                    UpdateNotifier updater = new UpdateNotifier(event);
//                    updater.run();
//                }
//            }
        }
    }

    class doWorkInBackground implements Runnable {

        @Override
        public void run() {
            // retrieve file
            AbstractMap.SimpleEntry<Boolean, String> result = downloadFile();

            //check before install
            if (result.getKey()) {
                if (isNewer()) {
                    //TODO - Install

                }
            }
            //Clean up - whether update happened or not
        }
    }

    private synchronized AbstractMap.SimpleEntry<Boolean, String> downloadFile() {
        AbstractMap.SimpleEntry<Boolean, String> ret = null;
        Resources res = appContext.getResources();
        final UpdaterFileSorter sorter = new UpdaterFileSorter();
        boolean success = false;

        try {
            if (!mainWifi.isWifiEnabled()) {
                mainWifi.setWifiEnabled(true);
            }
            //Give it some time to work
            Thread.sleep(3000);

            org.apache.commons.net.ftp.FTPClient ftp = new org.apache.commons.net.ftp.FTPClient();
            String host = res.getString(R.string.FTP_HOST_EXTERNAL);
            String user = res.getString(R.string.FTP_DEFAULTUSER);
            String pass = res.getString(R.string.FTP_PASSWORD);
            ftp.connect(host);
            ftp.login(user, pass);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.BINARY_FILE_TYPE); //new
            ftp.enterLocalPassiveMode();
            String updatesDir = "/WarehouseUpdates/";
            //change directory
            //ftp.changeWorkingDirectory(updatesDir);
            //get files
            FTPFile[] files = ftp.listFiles(updatesDir);
            List<FTPFile> fileList = new ArrayList<FTPFile>();
            if (files.length > 1) {
                for (int i = 0; i < files.length; i ++) {
                    fileList.add(files[i]);
                }
                //Sort our list based on signal strength in ascending order
                Collections.sort(fileList, sorter);
            }
            //get the most recent file
            FTPFile latestUpdateFile = fileList.get(fileList.size() - 1);

            //Download File
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String newFileName = FilenameUtils.concat(downloadDir.getAbsolutePath(), latestUpdateFile.getName());
            File recentFile = new File(newFileName);
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(recentFile));
            success = ftp.retrieveFile(updatesDir + latestUpdateFile.getName(), outputStream);
            outputStream.close();
            if (success) {
                ret = new AbstractMap.SimpleEntry<Boolean, String>(success, newFileName);
            } else {
                ret = new AbstractMap.SimpleEntry<Boolean, String>(success, "");
            }
            ftp.logout();
            ftp.disconnect();
        } catch(Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, TAG, "doWorkInBackground - FTP download", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            //logger.log(log);
        }
        return ret;
    }

    private synchronized boolean isNewer() {
        boolean aggreed = false;

        return aggreed;
    }

    class UpdateNotifier implements Runnable {
        private PropertyChangeEvent event;

        UpdateNotifier(PropertyChangeEvent event) {
            this.event = event;
        }

        @Override
        public void run() {
//            ScanResult newWifi = (ScanResult) event.getNewValue();
//            boolean success = connectToAStrongerWIfi(newWifi);
//            if (success) {
//                Toast.makeText(BinMoveService.this, String.format("[[--> Success <--]]\nSwitching to a stronger WIFI\nConnecting to: %s\nOn channel: %s",
//                        getEndPointLocation(BinMoveService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(BinMoveService.this, String.format("[[--> Error <--]]\nUnable to connect to: %s\nOn channel: %s",
//                        getEndPointLocation(BinMoveService.this, newWifi.BSSID), getWifiChannel(newWifi.frequency)), Toast.LENGTH_LONG).show();
//            }
        }
    }

    class Configurator {
        private PropertyChangeSupport pcs1;
        private boolean isRunning = false;
        //private ScanResult endpointElect;

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

//        public ScanResult getEndpointElect() {
//            return endpointElect;
//        }
//
//        public void setEndpointElect(ScanResult endpointElect) {
//            this.pcs1.firePropertyChange("endpointElect", this.endpointElect, endpointElect);
//            this.endpointElect = endpointElect;
//        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.pcs1.addPropertyChangeListener(listener);
        }
    }
}
