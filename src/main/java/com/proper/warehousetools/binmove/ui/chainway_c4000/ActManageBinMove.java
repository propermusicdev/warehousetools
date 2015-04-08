package com.proper.warehousetools.binmove.ui.chainway_c4000;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.KeyEvent;
import com.chainway.deviceapi.Barcode1D;
import com.chainway.deviceapi.exception.ConfigurationException;
import com.proper.data.binmove.BinMove;
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
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.fragments.BinMoveConfirmationFragment;
import com.proper.warehousetools.binmove.fragments.BinMoveScanFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Lebel on 05/09/2014.
 */
//public class ActManageBinMove extends BaseScannerActivity implements IViewPagerFragmentSwitcher {
public class ActManageBinMove extends ActionBarActivity implements IViewPagerFragmentSwitcher {
    public AppContext appContext;
    public final String TAG = ActManageBinMove.class.getSimpleName();
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
    public Handler handler = null;
    //protected String scanInput;
    public int wsLineNumber = 0;
    public String originalEAN = "";
    public long startTime;
    public long elapseTime;
    public String backPressedParameter = "";
    public String paramTaskCompleted = "COMPLETED";
    public String paramTaskIncomplete = "INCOMPLETE";
    public ResponseHelper responseHelper = new ResponseHelper();
    public UserLoginResponse currentUser = new UserLoginResponse();
    public UserAuthenticator authenticator = null;
    public DeviceUtils deviceUtils = null;
    public LogHelper logger = null;
    public com.proper.messagequeue.Message thisMessage = new com.proper.messagequeue.Message();
    public HttpMessageResolver resolver = null;
    public ShareActionProvider actionProvider = null;
    //public ViewPager viewPager;
    public BinResponse thisBinResponse = new BinResponse();
    public ProductResponse thisProductResponse = new ProductResponse();
    private List<BinMove> moveList;
    public ActionBar actionBar;




    //private List<BinMove> moveList;
    private String scanInput;
    private String currentSourceBin = "";
    private String currentDestinationBin = "";
    public String currentSourceBin1 = "";
    public String currentDestinationBin1 = "";
    public ViewPager viewPager;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public String getCurrentSourceBin() {
        return currentSourceBin;
    }

    public void setCurrentSourceBin(String currentSourceBin) {
        this.currentSourceBin = currentSourceBin;
    }

    public String getCurrentDestinationBin() {
        return currentDestinationBin;
    }

    public void setCurrentDestinationBin(String currentDestinationBin) {
        this.currentDestinationBin = currentDestinationBin;
    }

    public List<BinMove> getMoveList() {
        return moveList;
    }

    public void setMoveList(List<BinMove> moveList) {
        this.moveList = moveList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmovemanage);
        appContext = (AppContext) getApplication();
        authenticator = new UserAuthenticator(this);
        deviceUtils = new DeviceUtils(this);
        logger = new LogHelper();
        resolver = new HttpMessageResolver(appContext);
        thisMessage = new com.proper.messagequeue.Message();
        deviceID = deviceUtils.getDeviceID();
        deviceIMEI = deviceUtils.getIMEI();
        currentUser = authenticator.getCurrentUser();
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
        getSupportActionBar().setTitle("Move Bin            ");

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS); // prepare for tabs

        viewPager = (ViewPager) this.findViewById(R.id.vp_binmoveManage);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
                Log.d("zzActReplenManageConfig", String.format("onTabReselected at position: %s from: %s with number of pixels:", i, v, i2));
            }

            @Override
            public void onPageSelected(int i) {
                getSupportActionBar().setSelectedNavigationItem(i);     //sets selected page
                Log.d("zzActReplenManageConfig", "onPageSelected at position: " + i);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                Log.d("zzActReplenManageConfig", "onPageSelected at position: " + i);
            }
        });
        viewPager.setAdapter(new NavAdapter(getSupportFragmentManager()));

        ActionBar.Tab selBinTab = getSupportActionBar().newTab();
        selBinTab.setText("Scan Bins");
        selBinTab.setTabListener(new MyTabsListener());
        getSupportActionBar().addTab(selBinTab);

        ActionBar.Tab selProdTab = getSupportActionBar().newTab();
        selProdTab.setText("Confirm");
        selProdTab.setTabListener(new MyTabsListener());
        getSupportActionBar().addTab(selProdTab);

        Intent bundle = getIntent();
        if (bundle == null) {
            String msg = "The Bundle object must not be null.";
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - onCreate - Line:123", deviceIMEI, RuntimeException.class.getSimpleName(), msg, today);
            logger.log(log);
            throw new RuntimeException(msg);
        }

        try {
            mInstance = Barcode1D.getInstance();
            isBarcodeOpened = mInstance.open();
        } catch (SecurityException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        } catch (ConfigurationException e) {
            e.printStackTrace();
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
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
        NAV_INSTRUCTION = bundle.getIntExtra("INSTRUCTION", 0);
    }

    @Override
    public void switchFragment(int target) {
        if (!getCurrentSourceBin().isEmpty() && !getCurrentDestinationBin().isEmpty()) {
            viewPager.setCurrentItem(target);
        }
    }

    class MyTabsListener implements ActionBar.TabListener {

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (!getCurrentSourceBin().isEmpty() && !getCurrentDestinationBin().isEmpty()) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            Log.d("ActManageBinMove", String.format("onTabReselected at position: %s, with name: %s ", tab.getPosition(), tab.getText()));
        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Log.d("ActManageBinMove", String.format("onTabReselected at position: %s, with name: %s ", tab.getPosition(), tab.getText()));
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            Log.d("ActManageBinMove", String.format("onTabReselected at position: %s, with name: %s ", tab.getPosition(), tab.getText()));
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
                fragment = new BinMoveScanFragment();
            }
            if (pos == 1) {
                fragment = new BinMoveConfirmationFragment();
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

//    private void reloadActivity() {
//        currentDestinationBin = "";
//        currentSourceBin = "";
//        txtSourceReception.setText("");
//        txtDestinationReception.setText("");
//        NAV_TURN = R.integer.TURN_SOURCE;
//        formatControls();
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                switch (NAV_TURN) {
                    case R.integer.TURN_DESTINATION:
                        if (currentDestinationBin != null && !currentDestinationBin.equalsIgnoreCase("")) {
                            //do nothing for now -  but more functionality maybe added in the near future
                        } else {
                            //Perform Scan as usual
                            //FragmentManager fm = getSupportFragmentManager();
                            //BinMoveScanFragment scanFragment = (BinMoveScanFragment) fm.findFragmentById(R.id.ItemDetailsFragment);
                            //setSelectedBin(detailsFragment.getSelectedBin());   //get currently selected Bin in fragment
                            //txtDestinationReception.requestFocus();
                            //btnScanDestination.performClick(); //Scan();
                            //IScanKeyDown scanKeyDown = (IScanKeyDown) this;
                            //scanKeyDown.onKeyScan();
                            FragmentPagerAdapter adapter = (FragmentPagerAdapter) viewPager.getAdapter();
                            ((BinMoveScanFragment)adapter.getItem(0)).myOnKeyDown(keyCode);
                        }
                        break;
                    case R.integer.TURN_SOURCE:
                        //do
                        if (currentSourceBin != null && !currentSourceBin.equalsIgnoreCase("")) {
                            //do nothing for now -  but more functionality maybe added in the near future
                        } else {
                            //Perform Scan as usual
//                            txtSourceReception.requestFocus();
//                            btnScanSource.performClick();   //Scan(); viewPager.setCurrentItem(target);
                            //((PastEventListFragment)fragments.get(0)).myOnKeyDown(keyCode);
                            //IScanKeyDown scanKeyDown = (IScanKeyDown) ActManageBinMove.this;
                            //scanKeyDown.onKeyScan();
                            FragmentPagerAdapter adapter = (FragmentPagerAdapter) viewPager.getAdapter();
                            ((BinMoveScanFragment)adapter.getItem(0)).myOnKeyDown(keyCode);
                        }
                        break;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        threadStop = true;
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
            mInstance.close();
        }
        //soundPool.release();
        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
    }
}