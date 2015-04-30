package com.proper.warehousetools.binmove;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.chainway.deviceapi.Barcode1D;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.BarcodeHelper;
import com.proper.data.helpers.ResponseHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.*;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lebel on 26/08/2014.
 */
public class BaseScannerActivity extends ActionBarActivity {
    public AppContext appContext;
    protected int screenSize;
    public final String TAG = BaseScannerActivity.class.getSimpleName();
    public final int KEY_SCAN = 139; //  OK >>>>>>>>
    public int NAV_INSTRUCTION = 0;
    public int NAV_TURN = 0;
    public int fullTurnCount = 0;
    public int inputByHand = 0;
    public String deviceIMEI = "";
    public String deviceID = "";
    public static final String ApplicationID = "BinMove";
    public static final String myMessageType = "BarcodeQuery";
    //public static final String TAG = "ActBinMain";
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
//    public String currentSourceBin = "";
//    public String currentDestinationBin = "";
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
    //public ShareActionProvider actionProvider = null;
    //public SearchView mSearchView;
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

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            getSupportActionBar().hide();
        }else{
            getSupportActionBar().setLogo(R.drawable.ic_launcher);
            getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.woodendrape_blue));
            //getSupportActionBar().setTitle(String.format("ver %s", appContext.getPackageInfo().versionName));
        }

        try {
            mInstance = Barcode1D.getInstance();
            isBarcodeOpened = mInstance.open();
        } catch (SecurityException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "BaseScannerActivity - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "BaseScannerActivity - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
            new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    finish();
                }
            }).show();
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mnu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_logout);
        //actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                Intent i = new Intent(this, ActLogin.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;
            default:
                break;
        }
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