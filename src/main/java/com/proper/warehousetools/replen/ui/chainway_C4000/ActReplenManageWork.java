package com.proper.warehousetools.replen.ui.chainway_C4000;

//import android.app.Activity;
import android.content.Context;
//import android.content.Intent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.*;
//import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
//import android.support.v7.app.ActionBarActivity;
import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.proper.data.core.IReplenCommunicator;
import com.proper.data.core.IScanKeyDown;
import com.proper.data.core.IViewPagerFragmentSwitcher;
//import com.proper.data.customcontrols.NonSwipeableViewPager;
//import com.proper.data.customcontrols.SlidingTabLayout;
import com.proper.data.helpers.BarcodeHelper;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.replen.*;
import com.proper.data.replen.adapters.ReplenAddMoveLineAdapter;
import com.proper.data.replen.adapters.ReplenMoveLineAdapter;
import com.proper.data.replen.adapters.ReplenMoveListAdapter;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.messagequeue.Message;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.AppManager;
import com.proper.warehousetools.MockClass;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.fragments.movelist.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lebel on 01/12/2014.
 */
//public class ActReplenManageWork extends ActionBarActivity implements IViewPagerFragmentSwitcher {
//public class ActReplenManageWork extends FragmentActivity implements IViewPagerFragmentSwitcher {
//public class ActReplenManageWork extends FragmentActivity implements IViewPagerFragmentSwitcher, IReplenCommunicator, IReplenSplitLineCommunicator {
public class ActReplenManageWork extends FragmentActivity implements IViewPagerFragmentSwitcher, IReplenCommunicator {
    public final String TAG = ActReplenManageWork.class.getSimpleName();
    protected final String ApplicationID = "Warehouse Tools";
    protected AppContext appContext;
    protected AppManager appManager;
    protected int screenSize;
    protected String deviceID = "";
    protected String deviceIMEI = "";
    protected Date utilDate = java.util.Calendar.getInstance().getTime();
    protected Timestamp today = null;
    protected DeviceUtils device = null;
    protected HttpMessageResolver resolver = null;
    protected UserAuthenticator authenticator = null;
    protected UserLoginResponse currentUser = null;
    public BarcodeHelper barcodeHelper = null;
    protected LogHelper logger = new LogHelper();
    protected Message thisMessage = null;
    protected MockClass testResolver;

    private final int KEY_SCAN = 139; //  OK >>>>>>>>
    public int PREVIOUS_TAB = -1;
    public static final int MOVE_UPDATE_SCREEN = 2;
    public static final int MOVE_SPLIT_SCREEN = 3;
    public static final int MOVE_LIST_TAB = 0;
    public static final int MOVE_LINE_TAB = 1;
    public static final int MOVE_UPDATE_TAB = 2;
    public static final int MOVE_SPLIT_TAB = 3;
    public static final int MOVE_ADD_TAB = 4;
    public static final int SPLITLINE_CONFIG_PROCEED = 31;
    public static final int SPLITLINE_CONFIG_HALT = 32;
    private ViewPager viewPager;
    private SlidingTabsColorsFragment displayedFragment = null;
    //private NonSwipeableViewPager viewPager;
    private SharedPreferences prefs = null;
    private String moveListReponseString = "";
    private String moveLinesRepsponseString = "";
    private ReplenMoveListResponse moveListResponse = null;
    private ReplenMoveListLinesResponse currentListLines = null;
    //private ReplenMoveListItemResponse SelectedMove;
    private ReplenSelectedMoveWrapper SelectedMove = null;
    private ReplenMoveListLinesItemResponse SelectedLine = null;
    private ReplenMoveListAdapter moveListAdapter = null;
    private ReplenMoveLineAdapter moveLineAdapter = null;
    private ReplenAddMoveLineAdapter splitLineAdapter = null;
    //private ReplenLinesItemResponseSelection split
    private ReplenLinesItemResponseSelection splitMoveLineSelection = null;
    //private List<ReplenMoveListLinesItemResponse> splitMoveLineSelectionList = null;
    private List<ReplenLinesItemResponseSelection> splitMoveLineSelectionList = null;
    private int currentSelectedIndex = -1, currentLineSelectedIndex = -1, splitLineConfig, currentTab = -1;
    private boolean canNavigate, canDisplayUpdateInfo, canDisplaySplitInfo, canDisplayAddInfo, movelinesHasChanged = false;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**----------// REGION: BEGIN - App properties   //-------**/
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public String getApplicationID() {
        return ApplicationID;
    }

    public AppContext getAppContext() {
        return appContext;
    }

    public void setAppContext(AppContext appContext) {
        this.appContext = appContext;
    }

    public int getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(int screenSize) {
        this.screenSize = screenSize;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceIMEI() {
        return deviceIMEI;
    }

    public void setDeviceIMEI(String deviceIMEI) {
        this.deviceIMEI = deviceIMEI;
    }

    public Timestamp getToday() {
        return today;
    }

    public void setToday(Timestamp today) {
        this.today = today;
    }

    public DeviceUtils getDevice() {
        return device;
    }

    public void setDevice(DeviceUtils device) {
        this.device = device;
    }

    public HttpMessageResolver getResolver() {
        return resolver;
    }

    public void setResolver(HttpMessageResolver resolver) {
        this.resolver = resolver;
    }

    public UserAuthenticator getAuthenticator() {
        return authenticator;
    }

    public void setAuthenticator(UserAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    public UserLoginResponse getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserLoginResponse currentUser) {
        this.currentUser = currentUser;
    }

    public LogHelper getLogger() {
        return logger;
    }

    public void setLogger(LogHelper logger) {
        this.logger = logger;
    }

    public BarcodeHelper getBarcodeHelper() {
        return barcodeHelper;
    }

    public void setBarcodeHelper(BarcodeHelper barcodeHelper) {
        this.barcodeHelper = barcodeHelper;
    }

    public Message getThisMessage() {
        return thisMessage;
    }

    public void setThisMessage(Message thisMessage) {
        this.thisMessage = thisMessage;
    }

    public boolean isCanNavigate() {
        return canNavigate;
    }

    public void setCanNavigate(boolean canNavigate) {
        this.canNavigate = canNavigate;
    }

    public boolean isCanDisplayAddInfo() {
        return canDisplayAddInfo;
    }

    public void setCanDisplayAddInfo(boolean canDisplayAddInfo) {
        this.canDisplayAddInfo = canDisplayAddInfo;
    }

    public boolean isCanDisplayUpdateInfo() {
        return canDisplayUpdateInfo;
    }

    public void setCanDisplayUpdateInfo(boolean canDisplayUpdateInfo) {
        this.canDisplayUpdateInfo = canDisplayUpdateInfo;
    }

    public boolean isCanDisplaySplitInfo() {
        return canDisplaySplitInfo;
    }

    public void setCanDisplaySplitInfo(boolean canDisplaySplitInfo) {
        this.canDisplaySplitInfo = canDisplaySplitInfo;
    }

    public boolean isMovelinesHasChanged() {
        return movelinesHasChanged;
    }

    public void setMovelinesHasChanged(boolean movelinesHasChanged) {
        this.movelinesHasChanged = movelinesHasChanged;
    }

    public int getCurrentSelectedIndex() {
        return currentSelectedIndex;
    }

    public void setCurrentSelectedIndex(int currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }

    public int getCurrentLineSelectedIndex() {
        return currentLineSelectedIndex;
    }

    public void setCurrentLineSelectedIndex(int currentLineSelectedIndex) {
        this.currentLineSelectedIndex = currentLineSelectedIndex;
    }

    public int getSplitLineConfig() {
        return splitLineConfig;
    }

    public void setSplitLineConfig(int splitLineConfig) {
        this.splitLineConfig = splitLineConfig;
    }

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int currentTab) {
        this.currentTab = currentTab;
    }

    public ReplenMoveListAdapter getMoveListAdapter() {
        return moveListAdapter;
    }

    public void setMoveListAdapter(ReplenMoveListAdapter moveListAdapter) {
        this.moveListAdapter = moveListAdapter;
    }

    public ReplenMoveLineAdapter getMoveLineAdapter() {
        return moveLineAdapter;
    }

    public void setMoveLineAdapter(ReplenMoveLineAdapter moveLineAdapter) {
        this.moveLineAdapter = moveLineAdapter;
    }

    public ReplenAddMoveLineAdapter getSplitLineAdapter() {
        return splitLineAdapter;
    }

    public void setSplitLineAdapter(ReplenAddMoveLineAdapter splitLineAdapter) {
        this.splitLineAdapter = splitLineAdapter;
    }

    public ReplenSelectedMoveWrapper getSelectedMove() {
        return SelectedMove;
    }

    public void setSelectedMove(ReplenSelectedMoveWrapper selectedMove) {
        pcs.firePropertyChange("SelectedMove", this.SelectedMove, SelectedMove);
        SelectedMove = selectedMove;
    }

    public ReplenMoveListLinesItemResponse getSelectedLine() {
        return SelectedLine;
    }

    public void setSelectedLine(ReplenMoveListLinesItemResponse selectedLine) {
        pcs.firePropertyChange("SelectedLine", this.SelectedLine, SelectedLine);
        SelectedLine = selectedLine;
    }

    public ReplenMoveListResponse getMoveListResponse() {
        return moveListResponse;
    }

    public void setMoveListResponse(ReplenMoveListResponse moveListResponse) {
        pcs.firePropertyChange("moveListResponse", this.moveListResponse, moveListResponse);
        this.moveListResponse = moveListResponse;
    }

    public ReplenMoveListLinesResponse getCurrentListLines() {
        return currentListLines;
    }

    public void setCurrentListLines(ReplenMoveListLinesResponse currentListLines) {
        pcs.firePropertyChange("currentListLines", this.currentListLines, currentListLines);
        this.currentListLines = currentListLines;
    }

    public String getMoveListReponseString() {
        return moveListReponseString;
    }

    public void setMoveListReponseString(String moveListReponseString) {
        pcs.firePropertyChange("moveListReponseString", this.moveListReponseString, moveListReponseString);
        this.moveListReponseString = moveListReponseString;
    }

    public String getMoveLinesRepsponseString() {
        return moveLinesRepsponseString;
    }

    public void setMoveLinesRepsponseString(String moveLinesRepsponseString) {
        pcs.firePropertyChange("moveLinesRepsponseString", this.moveLinesRepsponseString, moveLinesRepsponseString);
        this.moveLinesRepsponseString = moveLinesRepsponseString;
    }

    public ReplenLinesItemResponseSelection getSplitMoveLineSelection() {
        return splitMoveLineSelection;
    }

    public void setSplitMoveLineSelection(ReplenLinesItemResponseSelection splitMoveLineSelection) {
        this.splitMoveLineSelection = splitMoveLineSelection;
    }

    public List<ReplenLinesItemResponseSelection> getSplitMoveLineSelectionList() {
        return splitMoveLineSelectionList;
    }

    public void setSplitMoveLineSelectionList(List<ReplenLinesItemResponseSelection> splitMoveLineSelectionList) {
        this.splitMoveLineSelectionList = splitMoveLineSelectionList;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public void setPrefs(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public SlidingTabsColorsFragment getDisplayedFragment() {
        return displayedFragment;
    }

    public void setDisplayedFragment(SlidingTabsColorsFragment displayedFragment) {
        this.displayedFragment = displayedFragment;
    }

    public Date getUtilDate() {
        return utilDate;
    }

    public void setUtilDate(Date utilDate) {
        this.utilDate = utilDate;
    }

    /**----------// REGION: END - App properties //-------**/


    public void removeSharedPreferences() {
        prefs = getSharedPreferences(getString(R.string.preference_replenmovelist), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("ApplicationID");
        editor.remove("IMEI");
        editor.remove("Device");
        editor.remove("UserToken");
        editor.remove("Movelist");
        editor.apply();
    }

//    public void saveSharedPreferences() {
//        SharedPreferences credPref = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
//        prefs = getSharedPreferences(getString(R.string.preference_replenmovelist), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        editor.putString("ApplicationID", ApplicationID);
//        editor.putString("IMEI", deviceIMEI);
//        editor.putString("Device", deviceID);
//        editor.putString("UserToken", credPref.getString("UserToken", ""));
//        editor.putString("Movelist", moveListReponseString);
//        editor.commit();
//    }
//
//    public void loadSharedPreferences() {
//        SharedPreferences credPref = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
//        prefs = getSharedPreferences(getString(R.string.preference_replenmovelist), Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = prefs.edit();
//        prefs.getString("ApplicationID", ApplicationID);
//        prefs.getString("IMEI", deviceIMEI);
//        prefs.getString("Device", deviceID);
//        prefs.getString("UserToken", credPref.getString("UserToken", ""));
//        prefs.getString("Movelist", moveListReponseString);
//    }

    List<WeakReference<Fragment>> fragList = new ArrayList<WeakReference<Fragment>>();
    @Override
    public void onAttachFragment (Fragment fragment) {
        fragList.add(new WeakReference(fragment));
    }

    public List<Fragment> getActiveFragments() {
        ArrayList<Fragment> ret = new ArrayList<Fragment>();
        for(WeakReference<Fragment> ref : fragList) {
            Fragment f = ref.get();
            if(f != null) {
                if(f.isVisible()) {
                    ret.add(f);
                }
            }
        }
        return ret;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_manage_work);
        this.addPropertyChangeListener(new MoveListChangedListener()); // add property change listener
        appContext = (AppContext) getApplication();
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        authenticator = new UserAuthenticator(this);
        device = new DeviceUtils(this);
        logger = new LogHelper();
        thisMessage = new Message();
        deviceID = device.getDeviceID();
        deviceIMEI = device.getIMEI();
        currentUser = authenticator.getCurrentUser();
        resolver = new HttpMessageResolver(appContext);
        barcodeHelper = new BarcodeHelper();
        testResolver = new MockClass();
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            displayedFragment = new SlidingTabsColorsFragment();
            transaction.replace(R.id.sample_content_fragment, displayedFragment);
            transaction.commit();
        }
    }

    public synchronized void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = getSupportFragmentManager();

        //DialogHelper dialog = new DialogHelper(severity, dialogType, message, title);
        DialogHelper dialog = new DialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message", message);
        args.putString("Title_ARG", title);
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    @Override
    public void switchFragment(int target) {
//        if (viewPager != null) {
//            viewPager.setCurrentItem(target);
//        }
        if (displayedFragment.getmViewPager() != null) {
            displayedFragment.getmViewPager().setCurrentItem(target);
        }
    }

    @Override
    public void onDialogMessage_IReplenCommunicator(int buttonClicked, String originatedClass) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                if (originatedClass.equalsIgnoreCase(ManageWorkFragment.class.getSimpleName())) {
                    if (canNavigate) {
                        setCanNavigate(false);
                        getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
                    }
                }
                if (originatedClass.equalsIgnoreCase(ManageMoveLineFragment.class.getSimpleName())) {
                    if (canDisplayUpdateInfo) {
                        setCanDisplayUpdateInfo(false);
                        Intent i = new Intent(this, ActReplenUpdateLine.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("SelectedLine_Extra", SelectedLine);
                        bundle.putInt("MovelistId_Extra", getSelectedMove().getItem().getMovelistId());
                        i.putExtras(bundle);
                        startActivityForResult(i,MOVE_UPDATE_SCREEN);
                    }
                    if (canDisplayAddInfo) {
                        setCanDisplayAddInfo(false);
                        getDisplayedFragment().switchFragment(MOVE_ADD_TAB);
                    }
                    if (canDisplaySplitInfo) {
                        setCanDisplaySplitInfo(false);
                        // TODO - set currentTab then navigate passing necessary input
                        Intent i = new Intent(this, ActReplenSplitLine.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("SplitMoveLineSelectionList_Extra", (Serializable) getSplitMoveLineSelectionList());
                        bundle.putSerializable("SelectedLine_Extra", getSelectedLine());
                        bundle.putInt("MovelistId_Extra", getSelectedMove().getItem().getMovelistId());
                        i.putExtras(bundle);
                        startActivityForResult(i,SPLITLINE_CONFIG_PROCEED);
                    }
                }
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //return super.onKeyDown(keyCode, event);
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                if (getCurrentTab() == MOVE_ADD_TAB) {
                    //TODO - Complete this function, Need a way to get fragment
                    SlidingTabsColorsFragment.SampleFragmentPagerAdapter adapter = (SlidingTabsColorsFragment.SampleFragmentPagerAdapter) displayedFragment.getmViewPager().getAdapter();
                    //SlidingTabsColorsFragment.SampleFragmentPagerAdapter adapter = (SlidingTabsColorsFragment.SampleFragmentPagerAdapter) viewPager.getAdapter();
                    IScanKeyDown listner = (IScanKeyDown) ((zzAddLineFragment)adapter.getItem(MOVE_ADD_TAB));
                    listner.onKeyScan(keyCode, event);
                }
                if (getCurrentTab() == MOVE_SPLIT_TAB) {
                    //TODO - Complete this function, Need a way to get fragment
                    SlidingTabsColorsFragment.SampleFragmentPagerAdapter adapter = (SlidingTabsColorsFragment.SampleFragmentPagerAdapter) displayedFragment.getmViewPager().getAdapter();
                    //SlidingTabsColorsFragment.SampleFragmentPagerAdapter adapter = (SlidingTabsColorsFragment.SampleFragmentPagerAdapter) viewPager.getAdapter();
                    IScanKeyDown listner = (IScanKeyDown) ((zzSplitLineFragment)adapter.getItem(MOVE_SPLIT_TAB));
                    listner.onKeyScan(keyCode, event);
                }
            }
        }
        if (keyCode == 4) {
            onBackPressed();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case MOVE_SPLIT_SCREEN:
                Bundle bundle = data.getExtras();
                setSplitMoveLineSelectionList((List<ReplenLinesItemResponseSelection>) bundle.getSerializable("SplitMoveLineSelectionList_Return"));
                getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
                break;
            case MOVE_UPDATE_SCREEN:
                Bundle bundle1 = data.getExtras();
//                setCurrentListLines((ReplenMoveListLinesResponse) bundle1.getSerializable("CurrentListLines_Extra"));
//                setMoveLinesRepsponseString(bundle1.getString("MoveLinesRepsponseString_Extra"));
                getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
        }
    }

    @Override
    public void onBackPressed() {
        switch (getCurrentTab()) {
//            case MOVE_ADD_TAB:
//                //do
//                setCanDisplayUpdateInfo(false);
//                getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
//                break;
//            case MOVE_UPDATE_TAB:
//                //do
//                setCanDisplayUpdateInfo(false);
//                getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
//                break;
//            case MOVE_SPLIT_TAB:
//                //do
//                setCanDisplayUpdateInfo(false);
//                getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
//                break;
            case MOVE_LINE_TAB:
                //do
                setCanDisplayUpdateInfo(false);
                getDisplayedFragment().switchFragment(MOVE_LIST_TAB);
                break;
            case MOVE_LIST_TAB:
                //do
                setCanDisplayUpdateInfo(false);
                //super.onBackPressed();
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.mnu_replen_managework, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //return super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.action_replenMWBack:
                onBackPressed();
                break;
            case R.id.action_replen_MWExit:
                //TODO - Exit Current Activity
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                finish();
                break;
        }
        return false;
    }

    public class MoveListChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if (propertyName.equalsIgnoreCase("moveListResponse")) {
            }
            if (propertyName.equalsIgnoreCase("currentListLines")) {
                /**     Update Line Adapter <<In the event of line: Update, Add, Complete>></in>   **/
                //setMoveLineAdapter(new ReplenMoveLineAdapter(ActReplenManageWork.this, (List<ReplenMoveListLinesItemResponse>)e.getNewValue())); //removed because of error
                //lvWorkLines.setAdapter(mActivity.getMoveLineAdapter());
            }
            if (propertyName.equalsIgnoreCase("moveListReponseString")) {
                //the only way this can change is through a new session, in this case we will change all selected indexes to -1 and values to null
                setCurrentLineSelectedIndex(-1);
                setSelectedLine(null);
                setCurrentSelectedIndex(-1);
                setSelectedMove(null);
                moveLineAdapter = null;
            }
            if (propertyName.equalsIgnoreCase("moveLinesRepsponseString")) {
                //3 ways this can change: 1. by working a line (Add, Update), 2. by moveListResponse selection change 3. by moveListResponse change
            }
        }
    }

    /**
     * The ScrollView highly targeted based on internal controls in the end
     *
     * @param scroll
     * @param inner
     */
    public void scrollToBottom(final View scroll, final View inner) {

//        Handler mHandler = new Handler();
//
//        mHandler.post(new Runnable() {
//            public void run() {
//                if (scroll == null || inner == null) {
//                    return;
//                }
//                int offset = inner.getMeasuredHeight() - scroll.getHeight();
//                if (offset < 0) {
//                    offset = 0;
//                }
//
//                scroll.scrollTo(0, offset);
//            }
//        });
    }
}