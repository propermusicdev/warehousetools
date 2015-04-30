package com.proper.warehousetools;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.proper.data.helpers.BarcodeHelper;
import com.proper.data.helpers.ResponseHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;

/**
 * Created by Lebel on 30/10/2014.
 */
public class PlainActivity extends ActionBarActivity {
    protected final String ApplicationID = "Warehouse Tools"; //this.getPackageName(); //"BinMove";
    protected AppContext appContext;
    protected AppManager appManager;
    protected int screenSize;
    protected String deviceID = "";
    protected String deviceIMEI = "";
    protected java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    protected java.sql.Timestamp today = null;
    protected DeviceUtils deviceUtils = null;
    protected ResponseHelper responseHelper = null;
    protected BarcodeHelper barcodeHelper = null;
    protected HttpMessageResolver resolver = null;
    protected UserAuthenticator authenticator = null;
    protected UserLoginResponse currentUser = null;
    protected LogHelper logger = new LogHelper();
    protected com.proper.messagequeue.Message thisMessage = null;
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.mnu_main, menu);

//        MenuItem searchItem = menu.findItem(R.id.action_search);
//        mSearchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        // Find the share item
        MenuItem logOut = menu.findItem(R.id.menu_logout);
        // Need to use MenuItemCompat to retrieve the Action Provider
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
}
