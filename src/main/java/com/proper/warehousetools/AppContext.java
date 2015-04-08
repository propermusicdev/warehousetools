package com.proper.warehousetools;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import com.proper.services.BinMoveService;
import com.proper.services.WifiReportingService;
import com.proper.utils.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Created by Lebel on 12/08/2014.
 */
public class AppContext extends Application {

    private static final String TAG = AppContext.class.getSimpleName();
    private Intent serviceIntent = null;
    public static final int NETTYPE_WIFI = 0x01;
    public static final int NETTYPE_CMWAP = 0x02;
    public static final int NETTYPE_CMNET = 0x03;

    public static int PAGE_SIZE = 20;// The default page size
    private static final int CACHE_TIME = 60 * 60000;// Cache expiration time

    private String saveImagePath;// Save image path

    private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();

    HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private SoundPool soundPool;
    private float volumnRatio;
    private AudioManager am;

    // 一Dimensional bar code data queue
    public Queue<String> d1Queue = new LinkedList<String>();
    // 14443a Label scan queue
    public Queue<String> a14443Queue = new LinkedList<String>();
    // 15693 Label scan queue
    public Queue<String> r15693Queue = new LinkedList<String>();
    // UHF Label scan queue
    public Queue<String> uhfQueue = new LinkedList<String>();

    // Ping Queue
    public Queue<String> pingQueue = new LinkedList<String>();
    // Ping if The service is stopped
    public boolean pingStop = true;
    public String deviceID = "";

    @Override
    public void onCreate() {

        super.onCreate();

        Log.i(TAG, "onCreate");

        // Register App to avoid abnormal collapse/crash in processor
        //Thread.setDefaultUncaughtExceptionHandler(AppException
                //.getAppExceptionHandler());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialization
     */
    private void init() throws Exception {
        // PAGE_SIZE= getResources().getInteger(R.integer.PAGE_SIZE);
        // Set the path to save the image
        // saveImagePath = getProperty(AppConfig.SAVE_IMAGE_PATH);
        // if (StringUtils.isEmpty(saveImagePath)) {
        // setProperty(AppConfig.SAVE_IMAGE_PATH,
        // AppConfig.DEFAULT_SAVE_IMAGE_PATH);
        // saveImagePath = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
        // }

        deviceID = getDeviceID();

        soundPool = new SoundPool(10, AudioManager.STREAM_RING, 5);
        if (deviceID.equalsIgnoreCase(getString(R.string.SmallDevice))) {
            soundMap.put(1, soundPool.load(getString(R.string.SOUND_SCAN), 1));
            soundMap.put(2, soundPool.load(getString(R.string.SOUND_ERROR), 1));
            soundMap.put(3, soundPool.load(this, R.raw.barcodebeep, 1));
            soundMap.put(4, soundPool.load(this, R.raw.serror, 1));
        }else {
            soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
            soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
        }
//        if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
//            soundMap.put(1, soundPool.load(this, R.raw.barcodebeep, 1));
//            soundMap.put(2, soundPool.load(this, R.raw.serror, 1));
//        }
        am = (AudioManager) this.getSystemService(AUDIO_SERVICE);//AudioManager object instantiation

        Log.i(TAG, "AppContext init");

        //Intent intent = new Intent(this, FileService.class);
        //startService(intent);
        //FileUtils.createPath(AppConfig.DEFAULT_SAVE_PATH);

        NetUtils utils = new NetUtils();
        boolean isWifiOn = utils.isWiFiSwitchedOn(this);
        if (!isWifiOn) {
            utils.turnWifiOn(this);
            utils.connectToDefaultWifi(this);
        }
//        utils.connectToDefaultWifi(this);
        //Start BinMoveService for larger devices

//        if (deviceID.equalsIgnoreCase(getString(R.string.LargeDevice))) {
//            Intent serviceIntent = new Intent(this, BinMoveService.class);
//            startService(serviceIntent);
//        }
        //TODO - Start updater service if not running
//        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//            if (serviceClass.getName().equals(service.service.getClassName())) {
//                //return true; then it's running so do nothing
//            } else {
//                Intent i = new Intent();
//                i.setComponent(new ComponentName("com.proper.warehouseupdater", "com.proper.warehouseupdater.services.UpdaterService"));
//                ComponentName c = this.startService(i);
//            }
//        }
//        Intent intent = new Intent(this, WifiReportingService.class);
//        startService(intent);
//        Intent i = new Intent();
//        i.setComponent(new ComponentName("com.proper.warehouseupdater", "com.proper.warehouseupdater.services.UpdaterService"));
//        ComponentName c = this.startService(i);
        launchApp("com.proper.warehouseupdater");
    }

    protected synchronized void launchApp(String packageName) {
        Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                packageName);
        if (mIntent != null) {
            try {
                startActivity(mIntent);
            } catch (ActivityNotFoundException err) {
                Toast t = Toast.makeText(getApplicationContext(), "Updater app Not Found !!!", Toast.LENGTH_LONG);
                t.show();
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get Device Manufacturer Identity
     *
     * @return
     */
    public String getDeviceID() {
        return Build.MANUFACTURER;
    }

    /**
     * Get Device Model
     *
     * @return
     */
    public String getDeviceNum() {
        return Build.MODEL;
    }

    /**
     * Get mac address of the device to ensure that this method requires WIFI since this power once opened, otherwise it will return null 。
     *
     * @return
     */
    public String getLocalMacAddress() {
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return (info == null) ? "NULL" : info.getMacAddress();
    }

    /**
     * Detecting whether the current system sound normal mode
     *
     * @return
     */
    public boolean isAudioNormal() {
        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    /**
     * Whether the application tone sounds
     *
     * @return
     */
    public boolean isAppSound() {
        return isAudioNormal() && isVoice();
    }

    /**
     * Whether a tone sounds
     *
     * @return
     */
    public boolean isVoice() {
        String perf_voice = getProperty(AppConfig.CONF_VOICE);
        // Is enabled by default voice prompts
        if (StringUtils.isEmpty(perf_voice))
            return true;
        else
            return StringUtils.toBool(perf_voice);
    }

    /**
     * Set whether a tone sounds
     *
     * @param b
     */
    public void setConfigVoice(boolean b) {
        setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
    }

    /**
     * Play tone
     *
     * @param id
     *            Success 1 or failure 2
     */
    public void playSound(int id) {

        float audioMaxVolumn = am.getStreamMaxVolume(AudioManager.STREAM_RING); // Returns the current value of the maximum volume AudioManager object
        float audioCurrentVolumn = am.getStreamVolume(AudioManager.STREAM_RING);// Returns the current volume value AudioManager object
        volumnRatio = audioCurrentVolumn / audioMaxVolumn;

        if (isAppSound()) {

            try {
                soundPool.play(soundMap.get(id), volumnRatio, // Left channel volume
                        volumnRatio, // Right channel volume
                        1, // Priority，0 is the lowest
                        0, // Cycles，0 all cycle, -1 without ever cycle
                        1 // Playback speed, the value between 0.5 and 2.0, a normal speed,
                );
            } catch (Exception e) {
                e.printStackTrace();

                UIHelper.ToastMessage(this, "playSound error");

            }

            Log.i(TAG, "playSound volumnRatio：" + volumnRatio
                    + " audioCurrentVolumn:" + audioCurrentVolumn
                    + " audioMaxVolumn:" + audioMaxVolumn + " soundMap:"
                    + soundMap.get(id));

        }
    }

    /**
     * Detecting whether the network is available
     *
     * @return
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }

    /**
     * Get the current network type
     *
     * @return 0：No network 1：WIFI Network 2：WAP Network 3：Ethernet cable network
     */
    public int getNetworkType() {
        int netType = 0;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return netType;
        }
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_MOBILE) {
            String extraInfo = networkInfo.getExtraInfo();
            if (!StringUtils.isEmpty(extraInfo)) {
                if (extraInfo.toLowerCase().equals("cmnet")) {
                    netType = NETTYPE_CMNET;
                } else {
                    netType = NETTYPE_CMWAP;
                }
            }
        } else if (nType == ConnectivityManager.TYPE_WIFI) {
            netType = NETTYPE_WIFI;
        }
        return netType;
    }

    /**
     * The current version of the method to determine whether the versions are compatible goals
     *
     * @param VersionCode
     * @return
     */
    public static boolean isMethodsCompat(int VersionCode) {
        int currentVersion = Build.VERSION.SDK_INT;
        return currentVersion >= VersionCode;
    }

    /**
     * Get App installation package information
     *
     * @return
     */
    public PackageInfo getPackageInfo() {
        PackageInfo info = null;
        try {
            info = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace(System.err);
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    /**
     * Get App uniquely identifies
     *
     * @return
     */
    public String getAppId() {
        String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
        if (StringUtils.isEmpty(uniqueID)) {
            uniqueID = UUID.randomUUID().toString();
            setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
        }
        return uniqueID;
    }

    /**
     * Show articles are loaded picture
     *
     * @return
     */
    public boolean isLoadImage() {
        String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
        // The default is loaded
        if (StringUtils.isEmpty(perf_loadimage))
            return true;
        else
            return StringUtils.toBool(perf_loadimage);
    }

    /**
     * Set is loaded articles Pictures
     *
     * @param b
     */
    public void setConfigLoadimage(boolean b) {
        setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
    }

    /**
     * Whether to start checking for updates
     *
     * @return
     */
    public boolean isCheckUp() {
        String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
        // Is enabled by default
        if (StringUtils.isEmpty(perf_checkup))
            return true;
        else
            return StringUtils.toBool(perf_checkup);
    }

    /**
     * Set to start checking for updates
     *
     * @param b
     */
    public void setConfigCheckUp(boolean b) {
        setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
    }

    /**
     * Clear saved cache
     */
    public void cleanCookie() {
        removeProperty(AppConfig.CONF_COOKIE);
    }

    /**
     * Determine whether the cache data readable
     *
     * @param cachefile
     * @return
     */
    private boolean isReadDataCache(String cachefile) {
        return readObject(cachefile) != null;
    }

    /**
     * Determine whether there is cache
     *
     * @param cachefile
     * @return
     */
    private boolean isExistDataCache(String cachefile) {
        boolean exist = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists())
            exist = true;
        return exist;
    }

    /**
     * Determine whether the failure of the cache
     *
     * @param cachefile
     * @return
     */
    public boolean isCacheDataFailure(String cachefile) {
        boolean failure = false;
        File data = getFileStreamPath(cachefile);
        if (data.exists()
                && (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
            failure = true;
        else if (!data.exists())
            failure = true;
        return failure;
    }

    /**
     * Clear app cache
     */
    public void clearAppCache() {
        // // Clear webview cache
        // File file = CacheManager.getCacheFileBaseDir();
        // if (file != null && file.exists() && file.isDirectory()) {
        // for (File item : file.listFiles()) {
        // item.delete();
        // }
        // file.delete();
        // }
        deleteDatabase("webview.db");
        deleteDatabase("webview.db-shm");
        deleteDatabase("webview.db-wal");
        deleteDatabase("webviewCache.db");
        deleteDatabase("webviewCache.db-shm");
        deleteDatabase("webviewCache.db-wal");
        // Clear data cache
        clearCacheFolder(getFilesDir(), System.currentTimeMillis());
        clearCacheFolder(getCacheDir(), System.currentTimeMillis());
        // 2.Two versions have to apply caching functions transferred to the sd card
        if (isMethodsCompat(Build.VERSION_CODES.FROYO)) {
            clearCacheFolder(MethodsCompact.getExternalCacheDir(this),
                    System.currentTimeMillis());
        }
        // Clear Temporary Content Editor saved
        Properties props = getProperties();
        for (Object key : props.keySet()) {
            String _key = key.toString();
            if (_key.startsWith("temp"))
                removeProperty(_key);
        }
    }

    private int clearCacheFolder(File dir, long curTime) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, curTime);
                    }
                    if (child.lastModified() < curTime) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    /**
     * Save the object into memory cache
     *
     * @param key
     * @param value
     */
    public void setMemCache(String key, Object value) {
        memCacheRegion.put(key, value);
    }

    /**
     * Gets an object from the cache memory
     *
     * @param key
     * @return
     */
    public Object getMemCache(String key) {
        return memCacheRegion.get(key);
    }

    /**
     * Save disk cache
     *
     * @param key
     * @param value
     * @throws java.io.IOException
     */
    public void setDiskCache(String key, String value) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
            fos.write(value.getBytes());
            fos.flush();
        } finally {
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Get the disk cache data
     *
     * @param key
     * @return
     * @throws java.io.IOException
     */
    public String getDiskCache(String key) throws IOException {
        FileInputStream fis = null;
        try {
            fis = openFileInput("cache_" + key + ".data");
            byte[] datas = new byte[fis.available()];
            fis.read(datas);
            return new String(datas);
        } finally {
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Save Object
     *
     * @param ser
     * @param file
     * @throws java.io.IOException
     */
    public boolean saveObject(Serializable ser, String file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = openFileOutput(file, MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Read the object
     *
     * @param file
     * @return
     * @throws java.io.IOException
     */
    public Serializable readObject(String file) {
        if (!isExistDataCache(file))
            return null;

        Log.i("MY", "readObject " + file);

        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = openFileInput(file);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = getFileStreamPath(file);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
            }
            try {
                fis.close();
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * Get the path to save the image in memory
     *
     * @return
     */
    public String getSaveImagePath() {
        return saveImagePath;
    }

    public boolean containsProperty(String key) {
        Properties props = getProperties();
        return props.containsKey(key);
    }

    public void setProperties(Properties ps) {
        AppConfig.getAppConfig(this).set(ps);
    }

    public Properties getProperties() {
        return AppConfig.getAppConfig(this).get();
    }

    public void setProperty(String key, String value) {
        AppConfig.getAppConfig(this).set(key, value);
    }

    public String getProperty(String key) {
        return AppConfig.getAppConfig(this).get(key);
    }

    public void removeProperty(String... key) {
        AppConfig.getAppConfig(this).remove(key);
    }

    public double getDensity(Activity act) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = act.getResources().getDisplayMetrics();
        return dm.density;
    }

    /**
     * Open the Input Method
     *
     * @param act
     */
    public void showInputMethod(final Activity act) {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) act
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }, 500);
    }

    /**
     * Close the input method
     *
     * @param act
     */
    public void closeInputMethod(final Activity act) {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) act
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                // if (imm.isActive()) { //无效
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                // }
            }
        }, 500);
    }

    /**
     * Save the scanned data
     *
     * @param fileName
     * @param data
     */
    public void saveRecords(String fileName, String data) {
        if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(data)) {
            return;
        }

        if (!FileUtils.checkSaveLocationExists()) {
            return;
        }

        boolean isCreate = false;

        if (!TextUtils.isEmpty(getProperty(fileName))) {
            long between_days = (System.currentTimeMillis() - Long
                    .parseLong(getProperty(fileName))) / (1000 * 3600 * 24);

            if (between_days > 1) {
                isCreate = true;
            }

            Log.i(TAG, "saveRecords between_days:" + between_days);
        }

        setProperty(fileName, System.currentTimeMillis() + "");

        String path = AppConfig.DEFAULT_SAVE_PATH + "ScanLog/";

        if (!FileUtils.checkFilePathExists(path)) {
            FileUtils.createPath(path);
        }

        File logFile = new File(path + fileName);

        FileWriter fw = null;

        try {

            Log.i(TAG, "logFile.exists()=" + logFile.exists());

            if (!logFile.exists()) {

                logFile.createNewFile();

            }

            if (isCreate) {
                fw = new FileWriter(logFile, false);
            } else {
                fw = new FileWriter(logFile, true);
            }

            fw.append("\n******" + getLocalMacAddress() + "@"
                    + StringUtils.getTimeFormat(System.currentTimeMillis())
                    + "******\n" + data);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                }
            }
        }

    }

    /**
     * Used to determine whether the service is running.
     *
     * @param - context
     * @param className
     *            Judgment of the service name
     * @return true In the run false Not running
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {

            Log.i(TAG, "cls1=" + serviceList.get(i).service.getClassName()
                    + " cls2=" + className);

            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    @Override
    public void onTerminate() {
        //terminate the background service
        //stopService(serviceIntent);
        soundPool.release();
//        if (deviceID.equalsIgnoreCase(getResources().getString(R.string.LargeDevice))) {
//            stopService(serviceIntent);
//        }
        super.onTerminate();
    }

    public class CheckConnectionCallable implements Callable<Boolean> {
        private Context context;
        public CheckConnectionCallable(Context context){
            this.context = context;
        }

        @Override
        public Boolean call() throws Exception {
            boolean ret = false;
            NetUtils utils = new NetUtils();
            ret = utils.isNetworkAvailable(this.context);
            return ret;
        }
    }
}
