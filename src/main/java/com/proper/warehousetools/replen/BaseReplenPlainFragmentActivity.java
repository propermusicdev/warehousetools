package com.proper.warehousetools.replen;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.proper.data.helpers.ReplenResponseHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.*;
import com.proper.warehousetools.MockClass;

/**
 * Created by Lebel on 09/01/2015.
 */
public class BaseReplenPlainFragmentActivity extends FragmentActivity {
    protected final String ApplicationID = "Warehouse Tools"; //this.getPackageName(); //"BinMove";
    protected AppContext appContext;
    protected AppManager appManager;
    protected int screenSize;
    protected String deviceID = "";
    protected String deviceIMEI = "";
    protected java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    protected java.sql.Timestamp today = null;
    protected DeviceUtils device = null;
    protected HttpMessageResolver resolver = null;
    protected UserAuthenticator authenticator = null;
    protected UserLoginResponse currentUser = null;
    protected LogHelper logger = new LogHelper();
    protected com.proper.messagequeue.Message thisMessage = null;
    protected com.proper.warehousetools.MockClass testResolver;
    protected ReplenResponseHelper replenResponseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        replenResponseHelper = new ReplenResponseHelper();
        testResolver = new MockClass();
    }
}