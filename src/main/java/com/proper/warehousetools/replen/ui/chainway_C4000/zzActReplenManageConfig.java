package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.chainway.deviceapi.Barcode1D;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.proper.data.binmove.BinResponse;
import com.proper.data.binmove.ProductResponse;
import com.proper.data.core.IViewPagerFragmentSwitcher;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.ResponseHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.ActMain;
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.fragments.zzReplenSelectBinFragment;
import com.proper.warehousetools.replen.fragments.zzReplenSelectDestinationFragment;
import com.proper.warehousetools.replen.fragments.zzReplenSelectProductFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lebel on 04/09/2014.
 */
public class zzActReplenManageConfig extends ActionBarActivity implements IViewPagerFragmentSwitcher {
    public AppContext appContext;
    public final String TAG = zzActReplenManageConfig.class.getSimpleName();
    public final int KEY_SCAN = 139; //  OK >>>>>>>>
    public int NAV_INSTRUCTION = 0;
    public int NAV_TURN = 0;
    public int fullTurnCount = 0;
    public int inputByHand = 0;
    public String deviceIMEI = "";
    public String deviceID = "";
    public static final String ApplicationID = "Replen";
    public Date utilDate = Calendar.getInstance().getTime();
    public java.sql.Timestamp today = null;

    public int readerStatus = 0;
    public boolean threadStop = true;
    public boolean isBarcodeOpened = false;
    public Barcode1D mInstance;
    public int fd;
    public Thread readThread;
    //protected Handler handler = null;
    //protected String scanInput;
    public int wsLineNumber = 0;
    public String originalEAN = "";
    public long startTime;
    public long elapseTime;
    public String backPressedParameter = "";
    public ResponseHelper responseHelper = new ResponseHelper();
    public UserLoginResponse currentUser = new UserLoginResponse();
    public UserAuthenticator authenticator = null;
    public DeviceUtils deviceUtils = null;
    public LogHelper logger = null;
    public com.proper.messagequeue.Message thisMessage = new com.proper.messagequeue.Message();
    public HttpMessageResolver resolver = null;
    public ShareActionProvider actionProvider = null;
    private ViewPager viewPager;
    public BinResponse thisBinResponse = new BinResponse();
    public ProductResponse thisProductResponse = new ProductResponse();
    public ActionBar actionBar;

    public zzActReplenManageConfig() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replenmanage);
        actionBar = getSupportActionBar();
        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // prepare for tabs

        viewPager = (ViewPager) this.findViewById(R.id.vp_replenManage);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                Log.d("zzActReplenManageConfig", String.format("onTabReselected at position: %s from: %s with number of pixels:", i, v, i2));
            }

            @Override
            public void onPageSelected(int i) {
                actionBar.setSelectedNavigationItem(i);     //sets selected page
                Log.d("zzActReplenManageConfig", "onPageSelected at position: " + i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.d("zzActReplenManageConfig", "onPageSelected at position: " + i);
            }
        });
        viewPager.setAdapter(new NavAdapter(getSupportFragmentManager()));

        ActionBar.Tab selBinTab = getSupportActionBar().newTab();
        selBinTab.setText("Bin");
        selBinTab.setTabListener(new MyTabsListener());
        getSupportActionBar().addTab(selBinTab);

        ActionBar.Tab selProdTab = getSupportActionBar().newTab();
        selProdTab.setText("Product");
        selProdTab.setTabListener(new MyTabsListener());
        getSupportActionBar().addTab(selProdTab);

        ActionBar.Tab selDstTab = getSupportActionBar().newTab();
        selDstTab.setText("Destination");
        selDstTab.setTabListener(new MyTabsListener());
        getSupportActionBar().addTab(selDstTab);

        appContext = (AppContext) getApplication();
        resolver = new HttpMessageResolver(appContext);
        authenticator = new UserAuthenticator(this);
        deviceUtils = new DeviceUtils(this);
        logger = new LogHelper();
        thisMessage = new com.proper.messagequeue.Message();
        deviceID = deviceUtils.getDeviceID();
        deviceIMEI = deviceUtils.getIMEI();
        currentUser = authenticator.getCurrentUser();



        try {
            mInstance = Barcode1D.getInstance();
            isBarcodeOpened = mInstance.open();
        } catch (SecurityException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "zzActReplenManageConfig - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "zzActReplenManageConfig - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
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
    public void switchFragment(int target) {
        viewPager.setCurrentItem(target);
    }

    class MyTabsListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            viewPager.setCurrentItem(tab.getPosition());
            Log.d("zzActReplenManageConfig", String.format("onTabReselected at position: %s, with name: %s ", tab.getPosition(), tab.getText()));
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Log.d("zzActReplenManageConfig", String.format("onTabReselected at position: %s, with name: %s ", tab.getPosition(), tab.getText()));
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Log.d("zzActReplenManageConfig", String.format("onTabReselected at position: %s, with name: %s ", tab.getPosition(), tab.getText()));
        }
    }

    class NavAdapter extends FragmentPagerAdapter {

        NavAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int pos) {
            Fragment fragment = null;
            if (pos == 0) {
                fragment = new zzReplenSelectBinFragment();
            }
            if (pos == 1) {
                fragment = new zzReplenSelectProductFragment();
            }
            if (pos == 2) {
                fragment = new zzReplenSelectDestinationFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.mnu_main, menu);
        // Find the share item
        MenuItem shareItem = menu.findItem(R.id.menu_logout);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:
                Intent i = new Intent(this, ActMain.class);
                i.putExtra("RESULT_EXTRA", 666);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(666, i);
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        threadStop = true;
        if (readThread != null && readThread.isInterrupted() == false) {
            readThread.interrupt();
        }
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isBarcodeOpened) {
            if (readThread != null && readThread.isInterrupted() == false) {
                readThread.interrupt();
            }
            //soundPool.release();
            mInstance.close();
        }
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