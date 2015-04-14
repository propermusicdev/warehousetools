package com.proper.warehousetools;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.chainway.deviceapi.Barcode1D;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.BarcodeHelper;
import com.proper.data.helpers.ResponseHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lebel on 16/03/2015.
 */
public class BaseScanActivity extends ActionBarActivity {
    public AppContext appContext;
    protected int screenSize;
    public final String TAG = BaseScanActivity.class.getSimpleName();
    public final int KEY_SCAN = 139; //  OK >>>>>>>>
    public int NAV_INSTRUCTION = 0;
    public int NAV_TURN = 0;
    public int fullTurnCount = 0;
    public int inputByHand = 0;
    public String deviceIMEI = "";
    public String deviceID = "";
    public static final String ApplicationID = "";
    public Date utilDate = Calendar.getInstance().getTime();
    public java.sql.Timestamp today = null;
    public int readerStatus = 0;
    public boolean threadStop = true;
    public boolean isBarcodeOpened = false;
    public Barcode1D mInstance;
    public int fd;
    public Thread readThread;
    public Handler handler = null, taskErrorHandler = null;
    public String scanInput;
    public int wsLineNumber = 0;
    public String originalEAN = "";
    public long startTime;
    public long elapseTime;
    public String backPressedParameter = "";
    public String paramTaskCompleted = "COMPLETED";
    public String paramTaskIncomplete = "INCOMPLETE";
    public BarcodeHelper barcodeHelper = null;
    public ResponseHelper responseHelper = null;
    public UserLoginResponse currentUser = null;
    public UserAuthenticator authenticator = null;
    public DeviceUtils deviceUtils = null;
    public LogHelper logger = null;
    public com.proper.messagequeue.Message thisMessage = null;
    public HttpMessageResolver resolver = null;
    protected MockClass testResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appContext = (AppContext) getApplication();
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        authenticator = new UserAuthenticator(this);
        deviceUtils = new DeviceUtils(this);
        logger = new LogHelper();
        resolver = new HttpMessageResolver(appContext);
        responseHelper = new ResponseHelper();
        barcodeHelper = new BarcodeHelper();
        thisMessage = new com.proper.messagequeue.Message();
        deviceID = deviceUtils.getDeviceID();
        deviceIMEI = deviceUtils.getIMEI();
        currentUser = authenticator.getCurrentUser();
        testResolver = new MockClass();

        try {
            mInstance = Barcode1D.getInstance();
            isBarcodeOpened = mInstance.open();
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "BaseScannerActivity - onCreate", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            //logger.log(log);
            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_main, menu);
        MenuItem logoutItem = menu.findItem(R.id.menu_logout);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.menu_logout:
//                Intent i = new Intent();
//                setResult(666, i);
//                finish();
//                break;
//            default:
//                break;
//        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The ScrollView highly targeted based on internal controls in the end
     *
     * @param scroll
     * @param inner
     */
    public void scrollToBottom(final View scroll, final View inner) {

        Handler mHandler = new Handler();

        mHandler.post(new Runnable() {
            public void run() {
                if (scroll == null || inner == null) {
                    return;
                }
                int offset = inner.getMeasuredHeight() - scroll.getHeight();
                if (offset < 0) {
                    offset = 0;
                }

                scroll.scrollTo(0, offset);
            }
        });
    }
}
