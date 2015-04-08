package com.proper.warehousetools;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.Stack;

/**
 * Created by Lebel on 12/08/2014.
 */
public class AppManager {

    private static final String TAG = AppManager.class.getSimpleName();

    private static Stack<Activity> activityStack;
    private static AppManager instance;

    private AppManager() {
    }

    public static AppManager getAppManager() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<Activity>();
        }
        activityStack.add(activity);
    }

    public Activity currentActivity() {
        Activity activity = activityStack.lastElement();
        return activity;
    }

    public void finishActivity() {
        Activity activity = activityStack.lastElement();
        finishActivity(activity);
    }

    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public void finishActivity(Class<?> cls) {
        for (Activity activity : activityStack) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

//    public void finishAllActivity() {
//
//        for (int i = 0, size = activityStack.size(); i < size; i++) {
//            if (null != activityStack.get(i)) {
//
//                if (i == 0) {
//                    // 关闭服务
//                    Intent intent = new Intent(activityStack.get(i),
//                            FileService.class);
//                    activityStack.get(i).stopService(intent);
//
//                    Log.i(TAG, "close FileService");
//                }
//
//                activityStack.get(i).finish();
//            }
//        }
//        activityStack.clear();
//    }

    public int getStackSize() {
        return activityStack.size();
    }

    public void AppExit(Context context) {
        try {

            //finishAllActivity();

            Log.d(TAG, "AppExit size=" + activityStack.size());

            ActivityManager activityMgr = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);

            int sdk = Integer.valueOf(Build.VERSION.SDK).intValue();
            if (sdk < 8) {
                activityMgr.restartPackage(context.getPackageName());
            } else {
                activityMgr.killBackgroundProcesses(context.getPackageName());
            }
            android.os.Process.killProcess(android.os.Process.myPid()); //Added new line *********** Terminate if still running
            System.exit(0);
            android.os.Process.killProcess(android.os.Process.myPid()); //Added new line *********** Terminate if still running

        } catch (Exception e) {
            Log.d(TAG, "Application Manager was struggling to close app");
        }
    }
}
