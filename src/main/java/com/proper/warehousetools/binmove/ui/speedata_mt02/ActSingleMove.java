package com.proper.warehousetools.binmove.ui.speedata_mt02;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.*;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.barcode.DeviceControl;
import com.android.barcode.SerialPort;
import com.proper.data.binmove.*;
import com.proper.data.core.ICommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.BarcodeHelper;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.helpers.ResponseHelper;
import com.proper.logger.LogHelper;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.security.UserAuthenticator;
import com.proper.security.UserLoginResponse;
import com.proper.utils.DeviceUtils;
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.fragments.QuantityDialogFragment;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Lebel on 30/10/2014.
 */
public class ActSingleMove extends FragmentActivity implements ICommunicator {
    private AppContext appContext;
    protected int screenSize;
    private final String TAG = ActSingleMove.class.getSimpleName();
    private static final int KEY_SCAN = 111, KEY_F1 = 112, KEY_F2 = 113, KEY_F3 = 114, KEY_YELLOW = 115;
    private int KEY_POSITION = 0, NAV_INSTRUCTION = 0, NAV_TURN = 0, fullTurnCount = 0, inputByHand = 0, readerStatus = 0, fd = 0, wsLineNumber = 0;
    private String deviceIMEI = "", deviceID = "";
    private static final String ApplicationID = "Warehouse Tools";
    private Date utilDate = Calendar.getInstance().getTime();
    private Timestamp today = null;
    private boolean threadStop = true, isBarcodeOpened = false, ops = false, Opened = false, Powered = false, key_start = true;
    private DeviceControl DevCtrl;
    private SerialPort mSerialPort;
    private Thread readThread;
    private String scanInput, originalEAN = "", paramTaskCompleted = "COMPLETED", paramTaskIncomplete = "INCOMPLETE";
    private BarcodeHelper barcodeHelper = null;
    private UserLoginResponse currentUser = null;
    private UserAuthenticator authenticator = null;
    private DeviceUtils deviceUtils = null;
    private LogHelper logger = null;
    private com.proper.messagequeue.Message thisMessage = null;
    private HttpMessageResolver resolver = null;
    private Timer timer = new Timer(), retrig_timer = new Timer();
    private SoundPool soundPool;
    private	int soundId, errorSoundId;
    private float volumeLow, volumeLevel;



    private Handler handler = null, t_handler = null, n_handler = null;
    private Button btnScan, btnExit, btnEnterSrcBin, btnEnterBarcode, btnEnterDstBin;
    private TextView lblSourceBin, lblBarcode, lblDestinationBin;
    private EditText txtSourceBin, txtBarcode, txtDestinationBin;
    //private LinearLayout lytMain;
    private long startTime, elapseTime;
    private String backPressedParameter = "";
    private BinResponse currentBinResponse = null;
    private ProductBinResponse currentProduct = null;
    private ProductBinSelection currentBinSelection = null;
    private ResponseHelper responseHelper = new ResponseHelper();
    private WebServiceTask wsTask = null;
    private String currentSource = "",currentBarcode = "", currentDestination = "";
    private int currentSelectedIndex = -1;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public ProductBinSelection getCurrentBinSelection() {
        return currentBinSelection;
    }

    public void setCurrentBinSelection(ProductBinSelection currentBinSelection) {
        this.currentBinSelection = currentBinSelection;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_singlemove);
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
        //lytMain = (LinearLayout) this.findViewById(R.id.lytBPMMain);
        AudioManager audioMgr = (AudioManager) getSystemService(AUDIO_SERVICE);
        volumeLow = audioMgr.getStreamVolume(AudioManager.STREAM_SYSTEM);
        volumeLevel = volumeLow / audioMgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
        btnScan = (Button) this.findViewById(R.id.bnBPMScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActSingleMove);

        btnEnterSrcBin = (Button) this.findViewById(R.id.bnEnterSrcBinBPM);
        btnEnterBarcode = (Button) this.findViewById(R.id.bnEnterBarcodeBPM);
        btnEnterDstBin = (Button) this.findViewById(R.id.bnEnterDstBinBPM);
        lblSourceBin = (TextView) this.findViewById(R.id.txtvBPMSrcBin);
        lblBarcode = (TextView) this.findViewById(R.id.txtvBPMBarcode);
        lblDestinationBin = (TextView) this.findViewById(R.id.txtvBPMDstBin);
        txtSourceBin =  (EditText) this.findViewById(R.id.etxtBPMSrcBin);
        txtBarcode = (EditText) this.findViewById(R.id.etxtBPMBarcode);
        txtDestinationBin = (EditText) this.findViewById(R.id.etxtBPMDstBin);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnEnterSrcBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnEnterBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnEnterDstBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        txtSourceBin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtSourceBin.addTextChangedListener(new TextChanged());
        txtBarcode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtBarcode.addTextChangedListener(new TextChanged());
        txtDestinationBin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtDestinationBin.addTextChangedListener(new TextChanged());

        try {
            DevCtrl = new DeviceControl("/proc/driver/scan");

        } catch (SecurityException e) {
            e.printStackTrace();
            today = new Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        } catch (IOException e) {
            Log.d(TAG, "AAA");
            today = new Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
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
        ops = true;

        KEY_POSITION = 0; //Set for Yellow button to scan

        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        soundId = soundPool.load(getString(R.string.SOUND_SCAN), 0);
        errorSoundId = soundPool.load(getString(R.string.SOUND_ERROR), 0);

        t_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1) {
                    try {
                        DevCtrl.PowerOffDevice();
                    } catch (IOException e) {
                        Log.d(TAG, "BBB");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }//powerSave
                    Powered = false;
                }
            }
        };

        n_handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1) {
                    try {
                        if(key_start == false)
                        {
                            DevCtrl.TriggerOffDevice();
                            timer = new Timer();				//start a timer, when machine is idle for some time, cut off power to save energy.
                            timer.schedule(new MyTask(), 60000);
                            btnScan.setEnabled(true);
                            key_start = true;
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
            }
        };

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if(msg.what == 1) {
                        setScanInput(msg.obj.toString());   //Set object scanned by the hardware
                        if (NAV_TURN == R.integer.TURN_DESTINATION) {
                            if (getScanInput().length() == 5) {
                                //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
                                if (!txtDestinationBin.getText().toString().isEmpty()) {
                                    txtDestinationBin.setText("");     //to counter a weird bug in editText control
                                    txtDestinationBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    lockAllControls();
                                } else {
                                    txtDestinationBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    lockAllControls();
                                }
                            } else {
                                //Scanned wrong item, bin code etc...
                                Log.e("A bad scan has occurred", "Please scan again");
                                soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                String mMsg = "Bad scan occurred \nThis bin code is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                unlockDestinationControls();
                                txtDestinationBin.setText("");
                            }
                        }
                        if (NAV_TURN == R.integer.TURN_BARCODE) {
                            int acceptable[] = {12,13,14};
                            if (getScanInput().length() > 0 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                                //do barcode```````````````````````````````````````````````````````````````````````````````````````````````
                                if (!txtBarcode.getText().toString().isEmpty()) {
                                    txtBarcode.setText(""); //to counter a weird bug in editText control
                                    txtBarcode.setText(getScanInput());
                                    //We need to check that we have a match
                                    if (currentBinSelection != null) {
                                        lockAllControls();
                                        if (!btnEnterDstBin.isEnabled()) {
                                            btnEnterDstBin.setEnabled(true);
                                            btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                        }
                                    }else{
                                        txtBarcode.setText("");
                                    }
                                } else {
                                    txtBarcode.setText(getScanInput());
                                    //We need to check that we have a match
                                    if (currentBinSelection != null) {
                                        lockAllControls();
                                        if (!btnEnterDstBin.isEnabled()) {
                                            btnEnterDstBin.setEnabled(true);
                                            btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                        }
                                    }else{
                                        txtBarcode.setText("");
                                    }
                                }
                            } else {
                                //Scanned wrong item, barcode etc...
                                Log.e("A bad scan has occurred", "Please scan again");
                                soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                String mMsg = "Bad scan occurred \nThis barcode is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                lockAllControls();
                                unlockBarcodeControls();
                                txtBarcode.setText("");
                            }
                        }
                        if (NAV_TURN == R.integer.TURN_SOURCE) {
                            if (getScanInput().length() == 5) {
                                //do barcode```````````````````````````````````````````````````````````````````````````````````````````````
                                if (!txtSourceBin.getText().toString().isEmpty()) {
                                    txtSourceBin.setText(""); //to counter a weird bug in editText control
                                    txtSourceBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    lockAllControls();
                                    if (!btnEnterBarcode.isEnabled()) {
                                        btnEnterBarcode.setEnabled(true);
                                        btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                } else {
                                    txtSourceBin.setText(getScanInput());
                                    //By now the nav turn state has changed
                                    lockAllControls();
                                    if (!btnEnterBarcode.isEnabled()) {
                                        btnEnterBarcode.setEnabled(true);
                                        btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                }
                            } else {
                                //Scanned wrong item, barcode etc...
                                Log.e("A bad scan has occurred", "Please scan again");
                                soundPool.play(errorSoundId, volumeLevel, volumeLevel, 0, 0, 1);
                                String mMsg = "Bad scan occurred \nThis source bin is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                txtSourceBin.setText("");
                                refreshActivity();
                            }
                        }
                        soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                        unlockScanControls();           //unlocks scan
                    }
                }
            }
        };

        //Handle Wifi Connectivity
//        NetUtils utils = new NetUtils();
//        boolean isWifiOn = utils.isWiFiSwitchedOn(this);
//        if (!isWifiOn) {
//            utils.turnWifiOn(this);
//            utils.connectToDefaultWifi(this);
//        }

        // Initiate the navigation default turn
        NAV_TURN = R.integer.TURN_SOURCE;
        PaintButtonText();
        refreshActivity();
    }

    class MyTask extends TimerTask
    {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            Message message = new Message();
            message.what = 1;
            t_handler.sendMessage(message);
        }
    }

    class RetrigTask extends TimerTask
    {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //startTime = System.currentTimeMillis(); // begin long process time elapse count
            Message message = new Message();
            message.what = 1;
            n_handler.sendMessage(message);
        }
    }

    private void PaintButtonText() {
        if (NAV_TURN == R.integer.TURN_DESTINATION) {
            //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
            btnScan.setText(R.string.action_destBin);
            btnScan.setBackgroundResource(R.drawable.button_yellow);
            if (!btnScan.isEnabled()) {
                btnScan.setEnabled(true);
                btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
        if (NAV_TURN == R.integer.TURN_BARCODE) {
            //do barcode``````````````````````````````````````````````````````````````````````````````````````````````````
            btnScan.setText(R.string.but_startbarcode);
            btnScan.setBackgroundResource(R.drawable.button_blue);
            if (!btnScan.isEnabled()) {
                btnScan.setEnabled(true);
                btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
        if (NAV_TURN == R.integer.TURN_SOURCE) {
            //do barcode``````````````````````````````````````````````````````````````````````````````````````````````````
            btnScan.setText(R.string.action_srcBin);
            btnScan.setBackgroundResource(R.drawable.button_green);
            if (!btnScan.isEnabled()) {
                btnScan.setEnabled(true);
                btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        //inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void manageInputByHand() {
        switch (NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    paintByHandButtons();
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    txtDestinationBin.requestFocus();
                    showSoftKeyboard();
                } else {
                    turnOffInputByHand();
                    setScanInput(txtDestinationBin.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtDestinationBin.setText(getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        lockAllControls();
                    }
                }
                break;
            case R.integer.TURN_BARCODE:
                //1st time = turn on, 2nd change NAV-Turn to Destination
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    paintByHandButtons();
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    txtBarcode.requestFocus();
                    showSoftKeyboard();

                } else {
                    turnOffInputByHand();
                    setScanInput(txtBarcode.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        paintByHandButtons();
                        txtBarcode.setText(getScanInput());     // just to trigger text changed
                        if (currentBinSelection != null) {
                            lockBarcodeControls();
                            unlockDestinationControls();
                            txtDestinationBin.requestFocus();
                        }else{
                            txtBarcode.setText("");
                            //txtBarcode.setEnabled(true);
                        }
                    }
                }
                break;
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change NAV-Turn
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    paintByHandButtons();
                    if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
                    txtSourceBin.requestFocus();
                    showSoftKeyboard();
                } else {
                    turnOffInputByHand();
                    if (btnEnterSrcBin.isEnabled()) {
                        btnEnterSrcBin.setEnabled(false);
                        btnEnterSrcBin.setPaintFlags(btnEnterSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    setScanInput(txtSourceBin.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtSourceBin.setText(getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        lockSourceControls();   //lockAllControls();
                        unlockBarcodeControls();
                        txtBarcode.requestFocus();
                    }
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        switch (NAV_TURN) {
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change NAV-Turn
                if (inputByHand == 0) {
                    btnEnterSrcBin.setText(byHand);
                } else {
                    btnEnterSrcBin.setText(finish);
                }
                break;
            case R.integer.TURN_BARCODE:
                //1st time = turn on, 2nd change NAV-Turn
                if (inputByHand == 0) {
                    btnEnterBarcode.setText(byHand);
                } else {
                    btnEnterBarcode.setText(finish);
                }
                break;
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (inputByHand == 0) {
                    btnEnterDstBin.setText(byHand);
                } else {
                    btnEnterDstBin.setText(finish);
                }
        }
    }

    private void lockSourceControls() {
        if (lblSourceBin.isEnabled()) lblSourceBin.setEnabled(false);
        if (txtSourceBin.isEnabled()) txtSourceBin.setEnabled(false);
        if (btnEnterSrcBin.isEnabled()) {
            btnEnterSrcBin.setEnabled(false);
            btnEnterSrcBin.setPaintFlags(btnEnterSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockSourceControls() {
        if (!lblSourceBin.isEnabled()) lblSourceBin.setEnabled(true);
        if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
        if (!btnEnterSrcBin.isEnabled()) {
            btnEnterSrcBin.setEnabled(true);
            btnEnterSrcBin.setPaintFlags(btnEnterSrcBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockBarcodeControls() {
        if (lblBarcode.isEnabled()) lblBarcode.setEnabled(false);
        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
        if (btnEnterBarcode.isEnabled()) {
            btnEnterBarcode.setEnabled(false);
            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockBarcodeControls() {
        if (!lblBarcode.isEnabled()) lblBarcode.setEnabled(true);
        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
        if (!btnEnterBarcode.isEnabled()) {
            btnEnterBarcode.setEnabled(true);
            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockDestinationControls() {
        if (lblDestinationBin.isEnabled()) lblDestinationBin.setEnabled(false);
        if (txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(false);
        if (btnEnterDstBin.isEnabled()) {
            btnEnterDstBin.setEnabled(false);
            btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockDestinationControls() {
        if (!lblDestinationBin.isEnabled()) lblDestinationBin.setEnabled(true);
        if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
        if (!btnEnterDstBin.isEnabled()) {
            btnEnterDstBin.setEnabled(true);
            btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockScanControl() {
        if (btnScan.isEnabled()) {
            btnScan.setEnabled(false);
            btnScan.setPaintFlags(btnScan.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockScanControls() {
        if (!btnScan.isEnabled()) {
            btnScan.setEnabled(true);
            btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockAllControls() {
        lockSourceControls();
        lockBarcodeControls();
        lockDestinationControls();
        lockScanControl();
    }

    private void refreshActivity() {
        switch (NAV_TURN) {
            case R.integer.TURN_SOURCE:
                lockAllControls();
                unlockSourceControls();
                break;
            case R.integer.TURN_BARCODE:
                lockAllControls();
                unlockBarcodeControls();
                break;
            case R.integer.TURN_DESTINATION:
                lockAllControls();
                unlockDestinationControls();
                break;
        }
        unlockScanControls();
        PaintButtonText();
    }

    private void restartActivity() {
        currentSource = "";
        currentBarcode = "";
        currentDestination = "";
        NAV_TURN = R.integer.TURN_SOURCE;
        clearFields();
        PaintButtonText();
        refreshActivity();

    }

    private void clearFields() {
        if (!txtDestinationBin.getText().toString().isEmpty()) {
            txtDestinationBin.setText("");
        }
        if (!txtBarcode.getText().toString().isEmpty()) {
            txtBarcode.setText("");
        }
        if (!txtSourceBin.getText().toString().isEmpty()) {
            txtSourceBin.setText("");
        }
    }

    private void showQuantityDialog() {
        FragmentManager fm = getSupportFragmentManager();
        QuantityDialogFragment dialog = new QuantityDialogFragment();
        dialog.show(fm, "QuantityDialog");
    }

    private void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = getSupportFragmentManager();
        //DialogHelper dialog = new DialogHelper(subjectReferenceType, dialogType, message, title);
        DialogHelper dialog = new DialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message_ARG", message);
        args.putString("Title_ARG", title);
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        if(ops == true)
        {
            readThread.interrupt();
            timer.cancel();
            retrig_timer.cancel();
            try {
                DevCtrl.PowerOffDevice();
                Thread.sleep(1000);
            } catch (IOException e) {
                Log.d(TAG, "CCC");
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onPause", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onPause", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            }
            Powered = false;
            if(Opened == true)
            {
                mSerialPort.close(fd);
                Opened = false;
            }
        }
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        if(ops == true)
        {
            if(Opened == false) {
                try {
                    //mSerialPort = new SerialPort("/dev/eser1",9600);//3a
                    mSerialPort = new SerialPort("/dev/eser0",9600);//35
                } catch (SecurityException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onResume", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                } catch (IOException e) {
                    Log.d(TAG, "DDD");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onResume", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                    new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            finish();
                        }
                    }).show();
                    ops = false;
                    soundPool.release();
                    try {
                        DevCtrl.DeviceClose();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log1 = new LogEntry(1L, ApplicationID, "ActSingleMove - onResume", deviceIMEI, e1.getClass().getSimpleName(), e1.getMessage(), today);
                        logger.log(log1);
                    }
                    super.onResume();
                    return;
                }
                fd = mSerialPort.getFd();
                if(fd > 0){
                    Log.d(TAG,"opened");
                    Opened = true;
                }
            }
            readThread = new ReadThread();
            readThread.setName("MyReadThread_ActSingleMove");
            readThread.start();
        }
        Log.d(TAG, "onResume");
        super.onResume();
    }

    public void onDestroy() {
        // TODO Auto-generated method stub
        if(ops == true)
        {
            try {
                soundPool.release();
                DevCtrl.DeviceClose();
            } catch (IOException e) {
                Log.d(TAG, "EEE");
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            }
        }
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_CANCELED) {
            //tidyControls();  //clear all controls
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            restartActivity();
        }

        if (resultCode == RESULT_FIRST_USER) {
            //New Code Direct the activity to just close itself
            Intent resultIntent = new Intent();
            if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
                setResult(1, resultIntent);
            } else {
                setResult(RESULT_OK, resultIntent);
            }
            ActSingleMove.this.finish();
        }

        if (requestCode == RESULT_OK && resultCode == RESULT_OK) {
            //tidyControls();  //clear all controls
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            //Just do nothing and return to sequence as normal
        }
    }

    private void ButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.bnBPMScan:
                if (NAV_TURN == R.integer.TURN_DESTINATION) {
                    //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                    lockAllControls();
                    unlockDestinationControls();
                    unlockScanControls();
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    try {
                        if(key_start == true)
                        {
                            txtDestinationBin.requestFocus();
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the scan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_BARCODE) {
                    lockAllControls();
                    unlockBarcodeControls();
                    unlockScanControls();
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    try {
                        if(key_start == true)
                        {
                            txtBarcode.requestFocus();
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the scan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_SOURCE) {
                    //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                    lockAllControls();
                    unlockSourceControls();
                    unlockScanControls();
                    fullTurnCount = 0;      //set to default if it's not so already
                    if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
                    try {
                        if(key_start == true)
                        {
                            txtSourceBin.requestFocus();
                            if(Powered == false)
                            {
                                Powered = true;
                                DevCtrl.PowerOnDevice();
                            }
                            timer.cancel();
                            DevCtrl.TriggerOnDevice();
                            btnScan.setEnabled(false);
                            key_start = false;
                            retrig_timer = new Timer();
                            retrig_timer.schedule(new RetrigTask(), 3500);	//start a timer, if the data is not received within a period of time, stop the scan.
                        }
                    } catch (IOException e) {
                        Log.d(TAG, "FFF");
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                    fullTurnCount ++;
                }
                break;
            case R.id.bnExitActSingleMove:
                if (readThread != null && readThread.isInterrupted() == false) {
                    readThread.interrupt();
                }
                Intent resultIntent = new Intent();
                if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
                    setResult(1, resultIntent);
                } else {
                    setResult(RESULT_OK, resultIntent);
                }
                this.finish();
                break;
            default:
                manageInputByHand();
                break;
        }
    }

    @Override
    public void onDialogMessage_ICommunicator(int buttonClicked) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                this.setTitle(String.format("Moving [%s] item(s)", this.getCurrentBinSelection().getQtyToMove()));
                if (NAV_TURN == R.integer.TURN_END) {
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    ActSingleMove.this.finish();
                } else {
                    //assume that we're on destination turn
                    lockBarcodeControls();
                }
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

    class TextChanged implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            // TODO Auto-generated method stub

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (fullTurnCount > 0) {
                if (s != null && !s.toString().equalsIgnoreCase("")) {
                    String eanCode = s.toString().trim();
                    if (inputByHand == 0) {

                        if (NAV_TURN == R.integer.TURN_DESTINATION) {
                            //do bin``````````````````````````````````````````````````````````````````````````````````````````````````
                            //lockBarcodeControls();
                            if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                if (eanCode.equalsIgnoreCase(currentSource)) {
                                    soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                    Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "Incorrect Combination \nSource & Destination Bin must NOT be the same";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    txtDestinationBin.setText("");
                                    if (txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(false); //disable the control
                                } else {
//                                    lockBarcodeControls();
//                                    lockDestinationControls();
                                    lockAllControls();
                                    unlockScanControls();
                                    currentDestination = ""; //set to default
                                    //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                    currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                    if (currentUser != null  && currentProduct != null && currentBinSelection != null) {

                                        currentDestination = eanCode;

                                        MoveRequest req = new MoveRequest();
                                        List<MoveRequestItem> list = new ArrayList<MoveRequestItem>();
                                        req.setUserCode(currentUser.getUserCode());
                                        req.setUserId(String.format("%s", currentUser.getUserId()));
                                        req.setSrcBin(currentSource);
                                        req.setDstBin(currentDestination);
                                        MoveRequestItem item = new MoveRequestItem();
                                        item.setProductID(getCurrentBinSelection().getProductId());
                                        item.setSuppliercat(getCurrentBinSelection().getSupplierCat());
                                        item.setQty(getCurrentBinSelection().getQtyToMove());
                                        list.add(item);

                                        //Build message request
                                        req.setProducts(list);
                                        ObjectMapper mapper = new ObjectMapper();
                                        String msg = null;
                                        try {
                                            msg = mapper.writeValueAsString(req);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            today = new Timestamp(utilDate.getTime());
                                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - afterTextChanged", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                                            logger.log(log);
                                        }
                                        today = new Timestamp(utilDate.getTime());
                                        com.proper.messagequeue.Message thisMessage = new com.proper.messagequeue.Message();

                                        thisMessage.setSource(deviceIMEI);
                                        thisMessage.setMessageType("CreateMovelist");
                                        thisMessage.setIncomingStatus(1); //default value
                                        thisMessage.setIncomingMessage(msg);
                                        thisMessage.setOutgoingStatus(0);   //default value
                                        thisMessage.setOutgoingMessage("");
                                        thisMessage.setInsertedTimeStamp(today);
                                        thisMessage.setTTL(100);    //default value
                                        AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>> entry = new
                                                AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>>
                                                (R.integer.TURN_DESTINATION, new AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>(currentDestination, thisMessage));
                                        wsTask = new WebServiceTask(R.integer.TURN_DESTINATION);
                                        wsTask.execute(entry);

                                        //Finally end turn by locking all input controls
                                        //lockAllControls();
                                    } else {
                                        soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                        Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                    }
                                }
                            } else {
                                //Check to see if we're making entry by hand, display error and continue nav order
                                if (inputByHand == 0) {
                                    new AlertDialog.Builder(ActSingleMove.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            //refreshActivity();
                                        }
                                    }).show();
                                } else {
                                    //Warn then Refresh Activity
                                    new AlertDialog.Builder(ActSingleMove.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshActivity();
                                        }
                                    }).show();
                                }
                            }
                            //---
                        }
                        if (NAV_TURN == R.integer.TURN_BARCODE) {
                            //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                            //lockDestinationControls();
                            int acceptable[] = {12,13,14};
                            if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptable, eanCode.length()) == -1)) {

                                //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                if (currentUser != null) {
                                    //Check if product exist in Bin
                                    if (currentBinResponse != null) {
                                        if (currentBinResponse.getMatchedProducts() > 0) {
                                            int totalProd = currentBinResponse.getMatchedProducts();
                                            String newEanCode = "";
                                            boolean found = false;
                                            for (ProductBinResponse prod : currentBinResponse.getProducts()) {
                                                //if (eanCode.length() == 12) eanCode = "0" + eanCode;  //stuff a zero - Patch code requested by Richard Dodd - 09/10/2014 @17:44pm
                                                newEanCode = barcodeHelper.formatBarcode(eanCode);
                                                if (prod.getEAN().equalsIgnoreCase(newEanCode)) {
                                                    found = true;
                                                    currentProduct = prod;  // <<< set Current Product >>>
                                                    currentBinSelection = new ProductBinSelection(prod);  //<<< set Current Selection >>>
                                                    currentBarcode = eanCode;

                                                    //Finally switch turn (destination)
                                                    NAV_TURN = R.integer.TURN_DESTINATION;
                                                    if (!btnEnterDstBin.isEnabled()) {
                                                        btnEnterDstBin.setEnabled(true);
                                                        btnEnterDstBin.setPaintFlags(btnEnterDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                                    }
                                                    PaintButtonText();
                                                    // TODO - Navigate to request quantity
                                                    showQuantityDialog();
                                                }
                                                totalProd --;
                                                if (found == false && totalProd == 0) {
                                                    //Report no products in Bin
                                                    soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                                    Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                                    // Vibrate for 500 milliseconds
                                                    vib.vibrate(2000);
                                                    String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                                    builder.setMessage(mMsg)
                                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int id) {
                                                                    //do nothing
                                                                }
                                                            });
                                                    builder.show();
                                                }
                                            }
                                        }else {
                                            //Report no products in Bin
                                            soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                            Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                            builder.setMessage(mMsg)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            //do nothing
                                                        }
                                                    });
                                            builder.show();
                                            unlockBarcodeControls();    //New
                                            txtBarcode.setText("");     //New
                                        }

                                    }else {
                                        //Report no products in Bin
                                        soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                        Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "There is no such product in Bin specified \nPlease re-scan";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                        unlockBarcodeControls();    //New
                                        txtBarcode.setText("");     //New
                                    }
                                } else {
                                    soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                    Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                }
                            } else {
                                //Check to see if we're making entry by hand, display error and continue nav order
                                if (inputByHand == 0) {
                                    new AlertDialog.Builder(ActSingleMove.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshActivity();
                                        }
                                    }).show();
                                }
                            }
                        }
                        if (NAV_TURN == R.integer.TURN_SOURCE) {
                            //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                            //lockDestinationControls();        --removed old code
                            if ((eanCode.length() > 0 && (eanCode.length() == 5))) {

                                //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                if (currentUser != null) {
                                    currentSource = eanCode;

                                    //TODO - Query Bin :

                                    today = new Timestamp(utilDate.getTime());
                                    String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                                            currentUser.getUserId(), currentUser.getUserCode(), currentSource);
                                    thisMessage.setSource(deviceIMEI);
                                    thisMessage.setIncomingStatus(1); //default value
                                    thisMessage.setOutgoingStatus(0);   //default value
                                    thisMessage.setOutgoingMessage("");
                                    thisMessage.setInsertedTimeStamp(today);
                                    thisMessage.setTTL(100);    //default value
                                    thisMessage.setMessageType("BinQuery");
                                    thisMessage.setIncomingMessage(msg);
                                    AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>> entry = new
                                            AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>>
                                            (R.integer.TURN_SOURCE, new AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>(currentSource, thisMessage));
                                    wsTask = new WebServiceTask(R.integer.TURN_SOURCE);
                                    wsTask.execute(entry);

                                    //Finally switch turn (barcode)
                                    NAV_TURN = R.integer.TURN_BARCODE;
                                    lockAllControls();
                                    unlockScanControls();
                                    if (!btnEnterBarcode.isEnabled()) {
                                        btnEnterBarcode.setEnabled(true);
                                        btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    }
                                    PaintButtonText();
                                } else {
                                    soundPool.play(soundId, volumeLevel, volumeLevel, 0, 0, 1);
                                    Vibrator vib = (Vibrator) ActSingleMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActSingleMove.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                }
                            } else {
                                //Check to see if we're making entry by hand, display error and continue nav order
                                if (inputByHand == 0) {
                                    new AlertDialog.Builder(ActSingleMove.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            refreshActivity();
                                        }
                                    }).show();
                                }
                            }
                        }
                        //End full turn
                        fullTurnCount = 0;
                    }

                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (event.getRepeatCount() == 0) {
            switch (keyCode) {
                case KEY_SCAN:
                    if (NAV_TURN == R.integer.TURN_DESTINATION) {
                        //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                        lockAllControls();
                        unlockDestinationControls();
                        unlockScanControls();
                        if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                        try {
                            if (key_start == true) {
                                txtDestinationBin.requestFocus();
                                if (Powered == false) {
                                    Powered = true;
                                    DevCtrl.PowerOnDevice();
                                }
                                timer.cancel();
                                DevCtrl.TriggerOnDevice();
                                btnScan.setEnabled(false);
                                key_start = false;
                                retrig_timer = new Timer();
                                retrig_timer.schedule(new RetrigTask(), 3500);    //start a timer, if the data is not received within a period of time, stop the scan.
                            }
                        } catch (IOException e) {
                            Log.d(TAG, "FFF");
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                            logger.log(log);
                        }
                        fullTurnCount++;
                    }
                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                        lockAllControls();
                        unlockBarcodeControls();
                        unlockScanControls();
                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                        try {
                            if (key_start == true) {
                                txtBarcode.requestFocus();
                                if (Powered == false) {
                                    Powered = true;
                                    DevCtrl.PowerOnDevice();
                                }
                                timer.cancel();
                                DevCtrl.TriggerOnDevice();
                                btnScan.setEnabled(false);
                                key_start = false;
                                retrig_timer = new Timer();
                                retrig_timer.schedule(new RetrigTask(), 3500);    //start a timer, if the data is not received within a period of time, stop the scan.
                            }
                        } catch (IOException e) {
                            Log.d(TAG, "FFF");
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                            logger.log(log);
                        }
                        fullTurnCount++;
                    }
                    if (NAV_TURN == R.integer.TURN_SOURCE) {
                        //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                        lockAllControls();
                        unlockSourceControls();
                        unlockScanControls();
                        fullTurnCount = 0;      //set to default if it's not so already
                        if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
                        try {
                            if (key_start == true) {
                                txtSourceBin.requestFocus();
                                if (Powered == false) {
                                    Powered = true;
                                    DevCtrl.PowerOnDevice();
                                }
                                timer.cancel();
                                DevCtrl.TriggerOnDevice();
                                btnScan.setEnabled(false);
                                key_start = false;
                                retrig_timer = new Timer();
                                retrig_timer.schedule(new RetrigTask(), 3500);    //start a timer, if the data is not received within a period of time, stop the scan.
                            }
                        } catch (IOException e) {
                            Log.d(TAG, "FFF");
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                            logger.log(log);
                        }
                        fullTurnCount++;
                    }
                    break;
                case KEY_YELLOW:
                    if (NAV_TURN == R.integer.TURN_DESTINATION) {
                        //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                        lockAllControls();
                        unlockDestinationControls();
                        unlockScanControls();
                        if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                        try {
                            if (key_start == true) {
                                txtDestinationBin.requestFocus();
                                if (Powered == false) {
                                    Powered = true;
                                    DevCtrl.PowerOnDevice();
                                }
                                timer.cancel();
                                DevCtrl.TriggerOnDevice();
                                btnScan.setEnabled(false);
                                key_start = false;
                                retrig_timer = new Timer();
                                retrig_timer.schedule(new RetrigTask(), 3500);    //start a timer, if the data is not received within a period of time, stop the scan.
                            }
                        } catch (IOException e) {
                            Log.d(TAG, "FFF");
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                            logger.log(log);
                        }
                        fullTurnCount++;
                    }
                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                        lockAllControls();
                        unlockBarcodeControls();
                        unlockScanControls();
                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                        try {
                            if (key_start == true) {
                                txtBarcode.requestFocus();
                                if (Powered == false) {
                                    Powered = true;
                                    DevCtrl.PowerOnDevice();
                                }
                                timer.cancel();
                                DevCtrl.TriggerOnDevice();
                                btnScan.setEnabled(false);
                                key_start = false;
                                retrig_timer = new Timer();
                                retrig_timer.schedule(new RetrigTask(), 3500);    //start a timer, if the data is not received within a period of time, stop the scan.
                            }
                        } catch (IOException e) {
                            Log.d(TAG, "FFF");
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                            logger.log(log);
                        }
                        fullTurnCount++;
                    }
                    if (NAV_TURN == R.integer.TURN_SOURCE) {
                        //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                        lockAllControls();
                        unlockSourceControls();
                        unlockScanControls();
                        fullTurnCount = 0;      //set to default if it's not so already
                        if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
                        try {
                            if (key_start == true) {
                                txtSourceBin.requestFocus();
                                if (Powered == false) {
                                    Powered = true;
                                    DevCtrl.PowerOnDevice();
                                }
                                timer.cancel();
                                DevCtrl.TriggerOnDevice();
                                btnScan.setEnabled(false);
                                key_start = false;
                                retrig_timer = new Timer();
                                retrig_timer.schedule(new RetrigTask(), 3500);    //start a timer, if the data is not received within a period of time, stop the scan.
                            }
                        } catch (IOException e) {
                            Log.d(TAG, "FFF");
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                            logger.log(log);
                        }
                        fullTurnCount++;
                    }
                    break;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (readThread != null && readThread.isInterrupted() == false) {
            readThread.interrupt();
        }
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(RESULT_OK, resultIntent);
        }
        ActSingleMove.this.finish();
    }

    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                try {
                    Log.d(TAG,"read");
                    String buff = new String();
                    buff = mSerialPort.ReadSerial(fd, 1024);
                    Log.d(TAG,"end");
                    if(buff != null){
                        //ImHit = 0;
                        Message msg = new Message();
                        msg.what = 1;
                        msg.obj = buff; // Pass scanned object to ui wrapped in a message
                        handler.sendMessage(msg);
                        timer = new Timer();
                        timer.schedule(new MyTask(), 60000);
                    }else{
                        Message msg = new Message();
                        msg.what = 0;
                        handler.sendMessage(msg);
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - ReadThread - Run", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - ReadThread - Run", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                }
            }
        }
    }

    private class WebServiceTask extends AsyncTask<AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>>, Void, AbstractMap.SimpleEntry<Integer, Object>> {
        protected ProgressDialog xDialog;
        protected int instruction;

        private WebServiceTask(int instruction) {
            this.instruction = instruction;
        }

        @Override
        protected AbstractMap.SimpleEntry<Integer, Object> doInBackground(AbstractMap.SimpleEntry<Integer, AbstractMap.SimpleEntry<String, com.proper.messagequeue.Message>>... input) {
            int instruction = input[0].getKey();
            AbstractMap.SimpleEntry<Integer, Object> retObject = null;

            if (instruction == R.integer.TURN_DESTINATION) {
                PartialBinMoveResponse qryResponse = new PartialBinMoveResponse();

                //HttpMessageResolver resolver = new HttpMessageResolver();
                String response = resolver.resolveMessageQuery(input[0].getValue().getValue());
                if (response != null && !response.equalsIgnoreCase("")) {
                    if (response.contains("not recognised")) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - WebServiceTask - Line:1298", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                        logger.log(log);
                        throw new RuntimeException("Warehouse Support webservice is currently down. Please contact the IT department");
                    }else {
                        //Manually process this response
                        try {
                            JSONObject resp = new JSONObject(response);
                            JSONArray messages = resp.getJSONArray("Messages");
                            JSONArray actions = resp.getJSONArray("MessageObjects");
                            String RequestedSrcBin = resp.getString("RequestedSrcBin");
                            String RequestedDstBin = resp.getString("RequestedDstBin");
                            //String Result = resp.getString("Result");
                            List<BinMoveMessage> messageList = new ArrayList<BinMoveMessage>();
                            List<BinMoveObject> actionList = new ArrayList<BinMoveObject>();
                            //get messages
                            for (int i = 0; i < messages.length(); i++) {
                                JSONObject message = messages.getJSONObject(i);
                                String name = message.getString("MessageName");
                                String text = message.getString("MessageText");
                                Timestamp time = Timestamp.valueOf(message.getString("MessageTimeStamp"));

                                messageList.add(new BinMoveMessage(name, text, time));
                            }
                            //get actions
                            for (int i = 0; i < actions.length(); i++) {
                                JSONObject action = actions.getJSONObject(i);
                                String act = action.getString("Action");
                                int prodId = Integer.parseInt(action.getString("ProductId"));
                                String cat = action.getString("SupplierCat");
                                String ean = action.getString("EAN");
                                int qty = Integer.parseInt(action.getString("Qty"));
                                actionList.add(new BinMoveObject(act, prodId, cat, ean, qty));
                            }
                            qryResponse.setRequestedSrcBin(RequestedSrcBin);
                            qryResponse.setRequestedDstBin(RequestedDstBin);
                            //qryResponse.setResult(Result);
                            qryResponse.setMessages(messageList);
                            qryResponse.setMessageObjects(actionList);
                            retObject = new AbstractMap.SimpleEntry<Integer, Object>(instruction, qryResponse);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, this.getClass().getSimpleName() + " - WebServiceTask - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                            logger.log(log);
                        }
                    }
                }
            }

            //  Do Source HTTP Method
            if (instruction == R.integer.TURN_SOURCE) {
                try {
                    String response = resolver.resolveMessageQuery(input[0].getValue().getValue());
                    //response = responseHelper.refineOutgoingMessage(response);
                    response = responseHelper.refineResponse(response);
                    if (response.contains("not recognised")) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - WebServiceTask - Line:1291", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                        logger.log(log);
                        throw new RuntimeException("The bin you have scanned have not been recognised. Please check and scan again");
                    }else {
                        ObjectMapper mapper = new ObjectMapper();
                        BinResponse msgResponse = mapper.readValue(response, BinResponse.class);
                        retObject = new AbstractMap.SimpleEntry<Integer, Object>(instruction, msgResponse);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                    logger.log(log);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMove - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                    logger.log(log);
                }
            }

            return retObject;
        }

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActSingleMove.this);
            CharSequence message = "Working hard...contacting webservice...";
            if (instruction == R.integer.TURN_DESTINATION) {
                message = "Working hard...Moving Product...";
            }
            if (instruction == R.integer.TURN_SOURCE) {
                message = "Working hard...Searching Bin...";
            }

            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected void onPostExecute(AbstractMap.SimpleEntry<Integer, Object> responseObject) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            //Finally end turn by locking all input controls
            //lockAllControls();

            int instruction = responseObject.getKey();

            switch (instruction) {
                case R.integer.TURN_END:
                    PartialBinMoveResponse resp = (PartialBinMoveResponse) responseObject.getValue();
                    String pos1 = "Success: BinMove completed!";
                    String neg1 = "Failure: BinMove has failed!";
                    if (resp != null) {
                        //END Navigation turn
                        NAV_TURN = R.integer.TURN_END;
                        //ShowDialog:
                        showDialog(R.integer.MSG_SEVERITY_POSITIVE, R.integer.MSG_TYPE_NOTIFICATION, pos1, "Move Result");
                    } else {
                        //Response is null the disable Yes button:
                        showDialog(R.integer.MSG_TYPE_NOTIFICATION, R.integer.MSG_SEVERITY_FAILURE, neg1, "Move Result");
                    }
                    refreshActivity();
                    break;
                case R.integer.TURN_DESTINATION:
                    PartialBinMoveResponse response = (PartialBinMoveResponse) responseObject.getValue();
                    String pos = "Success: BinMove completed!";
                    String neg = "Failure: BinMove has failed!";
                    if (response != null) {
                        //END Navigation turn
                        NAV_TURN = R.integer.TURN_END;
                        //ShowDialog:
                        showDialog(R.integer.MSG_SEVERITY_POSITIVE, R.integer.MSG_TYPE_NOTIFICATION, pos, "Move Result");
                    } else {
                        //Response is null the disable Yes button:
                        showDialog(R.integer.MSG_TYPE_NOTIFICATION, R.integer.MSG_SEVERITY_FAILURE, neg, "Move Result");
                    }
                    refreshActivity();
                    break;
                case R.integer.TURN_SOURCE:
                    currentBinResponse = (BinResponse) responseObject.getValue();
                    break;
                default:
                    //yell foul and exit !
                    String error = "Undetermined: Please contact your IT team!";
                    showDialog(R.integer.MSG_TYPE_NOTIFICATION, R.integer.MSG_SEVERITY_FAILURE, error, "Move Result");
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    ActSingleMove.this.finish();
            }
        }

        @Override
        protected void onCancelled() {
            /*if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }*/
            wsTask = null;
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            refreshActivity();
        }
    }
}