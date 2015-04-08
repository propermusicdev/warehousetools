package com.proper.warehousetools.replen;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.proper.warehousetools.MockClass;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lebel on 01/12/2014.
 */
public class BaseReplenScanActivity extends Fragment{
    public AppContext appContext;
    protected int screenSize;
    public final String TAG = BaseReplenScanActivity.class.getSimpleName();
    public final int KEY_SCAN = 139; //  OK >>>>>>>>
    public int NAV_INSTRUCTION = 0;
    public int NAV_TURN = 0;
    public int fullTurnCount = 0;
    public int inputByHand = 0;
    public String deviceIMEI = "";
    public String deviceID = "";
    public static final String ApplicationID = "Replenishment";
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
    protected MockClass testResolver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        appContext = (AppContext) getActivity().getApplication();
        screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;
        authenticator = new UserAuthenticator(getActivity());
        deviceUtils = new DeviceUtils(getActivity());
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
            new AlertDialog.Builder(getActivity()).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub
                    getActivity().finish();
                }
            }).show();
            return inflater.inflate(R.layout.lyt_replen_selectbin, container, false);
        }
        return inflater.inflate(R.layout.lyt_replen_selectbin, container, false);
    }
}
