package com.proper.warehousetools.replen.ui.speedata_mt02;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.android.barcode.BaseScannerActivityLegacy;
import com.android.barcode.SerialPort;
import com.proper.data.binmove.BinResponse;
import com.proper.data.diagnostics.LogEntry;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.zzActReplenManage;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Timer;

/**
 * Created by Lebel on 02/09/2014.
 */
public class ActReplenScan extends BaseScannerActivityLegacy {
    private static final int MSG_QUERY_DONE = 1;
    private static final int  MSG_QUERY_STARTING = 2;
    private static final String ApplicationID = "BinMove";
    private WebServiceTask wsTask;
    private ReadThread readThread;
    private TextView txtInto;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterBinCode;
    private EditText mReception;
    private static final String TAG = "SerialPort";
    private int wsLineNumber = 0;
    private String originalEAN = "";
    private long startTime;
    private long elapseTime;
    private String backPressedParameter = "";

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_selectbin);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));

        txtInto = (TextView) this.findViewById(R.id.txtvReplenScanIntro);
        btnScan = (Button) this.findViewById(R.id.bnReplenScanPerformScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActReplenScan);
        btnEnterBinCode = (Button) this.findViewById(R.id.bnEnterBinReplenScan);
        mReception = (EditText) this.findViewById(R.id.etxtReplenScanBinCode);

        mReception.addTextChangedListener(new TextChanged());
        btnScan.setOnClickListener(new ClickEvent());
        btnExit.setOnClickListener(new ClickEvent());
        btnEnterBinCode.setOnClickListener(new ClickEvent());

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1 && key_start == false){
                    //mReception.append(buff);
                    setScanInput(msg.obj.toString());   //*************** set input from scan ***************
                    if (!mReception.getText().toString().equalsIgnoreCase("")) {
                        mReception.setText("");
                        mReception.setText(getScanInput());
                    }
                    else{
                        mReception.setText(getScanInput());
                    }
                    appContext.playSound(1);
                    key_start = true;
                    btnScan.setEnabled(true);
                    retrig_timer.cancel();
                }
            }
        };
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
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onPause", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onPause", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
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
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onResume", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                } catch (IOException e) {
                    Log.d(TAG, "DDD");
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onResume", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                    new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_OPEN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            finish();
                        }
                    }).show();
                    ops = false;
                    //soundPool.release();
                    try {
                        DevCtrl.DeviceClose();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log1 = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onResume", deviceIMEI, e1.getClass().getSimpleName(), e1.getMessage(), today);
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
            readThread.setName("MyReadThread_ActSingleMain");
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
                //soundPool.release();
                DevCtrl.DeviceClose();
            } catch (IOException e) {
                Log.d(TAG, "EEE");
                // TODO Auto-generated catch block
                e.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onDestroy", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                logger.log(log);
            }
        }
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            refreshActivity();
        }
        if (resultCode == RESULT_OK) {
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            refreshActivity();
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
            if (s != null && !s.toString().equalsIgnoreCase("")) {
                if (inputByHand == 0) {
                    String eanCode = s.toString().trim();
                    if (eanCode.length() > 0 && eanCode.length() == 5) {
                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                        if (currentUser != null) {
                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                                    currentUser.getUserId(), currentUser.getUserCode(), eanCode);
                            originalEAN = eanCode;
                            today = new java.sql.Timestamp(utilDate.getTime());
                            thisMessage.setSource(deviceIMEI);
                            thisMessage.setMessageType("BinQuery");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value

                            wsTask = new WebServiceTask();
                            wsTask.execute(thisMessage);
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActReplenScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenScan.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    } else {
                        new AlertDialog.Builder(ActReplenScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                refreshActivity();
                            }
                        }).show();
                    }
                }
            }
        }
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v == btnExit) {
                if (readThread != null && readThread.isInterrupted() == false) {
                    readThread.interrupt();
                }
                Intent resultIntent = new Intent();
                if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
                    setResult(1, resultIntent);
                } else {
                    setResult(RESULT_OK, resultIntent);
                }
                ActReplenScan.this.finish();
            }
            else if(v == btnScan)
            {
                try {
                    if(key_start == true)
                    {
                        mReception.requestFocus();
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
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onClick", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                }
            } else if(v == btnEnterBinCode) {
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    if (!mReception.isEnabled()) mReception.setEnabled(true);
                    mReception.setText("");
                    showSoftKeyboard();
                } else {
                    turnOffInputByHand();
                    //mReception.removeTextChangedListener();
                }
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

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
        this.btnScan.setEnabled(false);
        paintByHandButtons();
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
        this.btnScan.setEnabled(true);  //
        setScanInput(mReception.getText().toString());
        if (!getScanInput().isEmpty()) {
            mReception.setText(getScanInput()); // just to trigger text changed
        }
        paintByHandButtons();
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        if (inputByHand == 0) {
            btnEnterBinCode.setText(byHand);
        } else {
            btnEnterBinCode.setText(finish);
        }
    }

    public void refreshActivity() {
        if (!mReception.getText().toString().equalsIgnoreCase("")) mReception.setText("");
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
        if (!btnEnterBinCode.isEnabled()) btnEnterBinCode.setEnabled(true);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_SCAN:
                if(KEY_POSITION == 0) {
                    try {
                        if(key_start == true)
                        {
                            mReception.requestFocus();
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
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onKeyLongPress", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
                break;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KEY_SCAN:
                if(KEY_POSITION == 0){
                    try {
                        if(key_start == true)
                        {
                            mReception.requestFocus();
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
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActRplenScan - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
                break;
            case KEY_YELLOW:
                if (KEY_POSITION == 0) {
                    try {
                        if(key_start == true)
                        {
                            mReception.requestFocus();
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
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                    //scan.performClick();
                }
                break;
            case KEY_F1:
                if(KEY_POSITION == 1){
                    try {
                        if(key_start == true)
                        {
                            mReception.requestFocus();
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
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
                break;
            case KEY_F2:
                if(KEY_POSITION == 2){
                    try {
                        if(key_start == true)
                        {
                            mReception.requestFocus();
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
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActRplenScan - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
                break;
            case KEY_F3:
                if(KEY_POSITION == 3){
                    try {
                        if(key_start == true)
                        {
                            mReception.requestFocus();
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
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - onKeyDown", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                        logger.log(log);
                    }
                }
                break;
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
        ActReplenScan.this.finish();
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
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMain - ReadThread - Run", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActSingleMain - ReadThread - Run", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
                    logger.log(log);
                }
            }
        }
    }

    private class WebServiceTask extends AsyncTask<com.proper.messagequeue.Message, Void, BinResponse> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActReplenScan.this);
            CharSequence message = "Working hard...contacting webservice...";
            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected BinResponse doInBackground(com.proper.messagequeue.Message... params) {
            BinResponse ret = new BinResponse();
            try {
                String response = resolver.resolveMessageQuery(thisMessage);
                response = responseHelper.refineResponse(response);
                if (response.contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - WebServiceTask - Line:655", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                    logger.log(log);
                    throw new RuntimeException("The bin you have scanned have not been recognised. Please check and scan again");
                }else {
                    ObjectMapper mapper = new ObjectMapper();
                    ret = mapper.readValue(response, BinResponse.class);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                logger.log(log);
            } catch (Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectBin - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                logger.log(log);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(BinResponse binResponse) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            //Finally end turn by locking all input controls
            //lockAllControls();
            if (binResponse != null) {
                Intent i = new Intent(ActReplenScan.this, zzActReplenManage.class);
                i.putExtra("RESPONSE_EXTRA",binResponse);
                startActivityForResult(i, RESULT_OK);
            }
            refreshActivity();
        }
    }
}