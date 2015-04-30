package com.proper.warehousetools.binmove;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.chainway.deviceapi.Barcode1D;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.BarcodeHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.*;

/**
 * Created by Lebel on 26/08/2014.
 */
public class BaseFragmentActivity extends FragmentActivity {
    protected AppContext appContext;
    protected int screenSize;
    public static final int KEY_SCAN = 139; //  OK >>>>>>>>
    protected static final String paramTaskCompleted = "TRANSACTION_COMPLETED";
    protected static final String paramTaskIncomplete = "TRANSACTION_INCOMPLETE";
    protected String backPressedParameter = "";
    protected String deviceID = "";
    protected String deviceIMEI = "";
    protected String ApplicationID = "BinMove";
    protected java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    protected java.sql.Timestamp today = null;
    protected LogHelper logger = null;
    protected UserLoginResponse currentUser = new UserLoginResponse();
    protected UserAuthenticator authenticator = null;
    protected DeviceUtils deviceUtils = null;
    protected BarcodeHelper barcodeHelper = null;
    protected com.proper.messagequeue.Message thisMessage = null;
    protected HttpMessageResolver resolver = null;
    protected int NAV_INSTRUCTION = 0;
    protected int NAV_TURN = 0;
    protected int instruction = 0;
    protected String scanInput;
    protected int inputByHand = 0;
    protected int fullTurnCount = 0;
    protected int readerStatus = 0;
    protected boolean threadStop = true;
    protected boolean isBarcodeOpened = false;
    protected Barcode1D mInstance;
    public int fd;
    protected Thread readThread;
    protected Handler handler = null, taskErrorHandler = null;
    protected MockClass testResolver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.appContext = (AppContext) getApplication();
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        logger = new LogHelper();
        resolver = new HttpMessageResolver(appContext);
        thisMessage = new com.proper.messagequeue.Message();
        deviceUtils = new DeviceUtils(this);
        authenticator = new UserAuthenticator(this);
        currentUser = authenticator.getCurrentUser();
        deviceID = deviceUtils.getDeviceID();
        deviceIMEI = deviceUtils.getIMEI();
        barcodeHelper = new BarcodeHelper();
        testResolver =  new MockClass();

        try {
            mInstance = Barcode1D.getInstance();
            isBarcodeOpened = mInstance.open();
        } catch (SecurityException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "BaseEmptyActivity - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "BaseEmptyActivity - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
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
        // Inflate the menu
        getMenuInflater().inflate(R.menu.mnu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_logout);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //startActivity(intent);
                setResult(RESULT_OK, intent);
                finish();
                break;
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
