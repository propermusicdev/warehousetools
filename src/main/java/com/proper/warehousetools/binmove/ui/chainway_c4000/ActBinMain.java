package com.proper.warehousetools.binmove.ui.chainway_c4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.*;
import android.os.Message;
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
import com.proper.data.binmove.BinMove;
import com.proper.data.binmove.BinResponse;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.ActBinDetails;
import com.proper.warehousetools.binmove.ui.ActBinItemSelection;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.List;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActBinMain extends BaseScanActivity {
    private Button btnExit;
    private Button btnScanSource;
    private Button btnContinue;
    private Button btnScanDestination;
    private Button btnEnterSource;
    private Button btnEnterDestination;
    private TextView txtCurrentUser;
    private EditText txtSourceReception;
    private EditText txtDestinationReception;
    private TextView lblSource;
    private TextView lblDestination;
    private static final String TAG = "ActBinMain";
    private List<BinMove> moveList;
    private String scanInput;
    private String currentSourceBin = "";
    private String currentDestinationBin = "";
    private String backPressedParameter = "";
    private BinResponse qryResponse = new BinResponse();
    private BinResponse currentSourceContents =  new BinResponse();
    private queryTask binQryTask;
    private performBinQueryAsync sourceBinQueryTask;
    private boolean alreadyFired = false;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public List<BinMove> getMoveList() {
        return moveList;
    }

    public void setMoveList(List<BinMove> moveList) {
        this.moveList = moveList;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_binmain);
//        getSupportActionBar().setLogo(R.drawable.ic_launcher);
//        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
        getSupportActionBar().setTitle("Move Bin            ");

        Intent bundle = getIntent();
        if (bundle == null) {
            String msg = "The Bundle object must not be null.";
            today = new java.sql.Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - onCreate - Line:123", deviceIMEI, RuntimeException.class.getSimpleName(), msg, today);
            logger.log(log);
            throw new RuntimeException(msg);
        }
        NAV_INSTRUCTION = bundle.getIntExtra("INSTRUCTION", 0);

        txtCurrentUser = (TextView) this.findViewById(R.id.txtvHeaderTitle);
        txtSourceReception = (EditText) this.findViewById(R.id.txtvSourceText);
        txtSourceReception.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtSourceReception.addTextChangedListener(new TextChanged(this.txtSourceReception));
        txtDestinationReception = (EditText) this.findViewById(R.id.txtvDestinationText);
        txtDestinationReception.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtDestinationReception.addTextChangedListener(new TextChanged(this.txtDestinationReception));
        //TextView txtHeader = (TextView) this.findViewById(R.id.txtvHeaderTitle);
        btnExit = (Button) this.findViewById(R.id.bnExitActBinMain);
        btnExit.setOnClickListener(new ClickEvent());
        btnScanSource = (Button) this.findViewById(R.id.bnScanSource);
        btnScanSource.setOnClickListener(new ClickEvent());
        btnScanDestination = (Button) this.findViewById(R.id.bnScanDestination);
        btnScanDestination.setOnClickListener(new ClickEvent());
        btnContinue = (Button) this.findViewById(R.id.bnContinue);
        btnContinue.setOnClickListener(new ClickEvent());
        btnEnterSource = (Button) this.findViewById(R.id.bnEnterSourceBinMain);
        btnEnterSource.setOnClickListener(new ClickEvent());
        btnEnterDestination = (Button) this.findViewById(R.id.bnEnterDestinationBinMain);
        btnEnterDestination.setOnClickListener(new ClickEvent());
        lblSource = (TextView) this.findViewById(R.id.txtvSourceLabel);
        lblSource.setText("SrcBin:");
        lblDestination = (TextView) this.findViewById(R.id.txtvDestinationLabel);
        lblDestination.setText("DstBin:");

//        if (currentUser != null) {
//            txtHeader.setText(String.format("Current User [%s]", currentUser.getUserFirstName()));
//            txtCurrentUser.setText(currentUser.getUserFirstName());
//        }

        if (!btnContinue.isEnabled()) btnContinue.setEnabled(true);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if(msg.what == 1) {
                        setScanInput(msg.obj.toString());
                        //TODO - Emulate the implementation made in ActQueryScan

                        if (getScanInput().length() == 5) {
                            //******************************************BEGIN*****************************************************CONTINUE
                            switch (NAV_TURN) {
                                case R.integer.TURN_DESTINATION:
                                    //do destination    ````````````````````````````````````````````````````````````````````
                                    //if (!txtDestinationReception.getText().toString().isEmpty()) {
                                    if (!getScanInput().isEmpty()) {
                                        txtDestinationReception.setText(""); //to counter a weird bug in editText control
                                        txtDestinationReception.setText(getScanInput());
                                        appContext.playSound(1);
                                    } else {
                                        txtDestinationReception.setText("");
                                        appContext.playSound(2);
                                    }
                                    break;
                                case R.integer.TURN_SOURCE:
                                    //do source     `````````````````````````````````````````````````````````````````````````````````````
                                    if (!getScanInput().isEmpty()) {
                                        txtSourceReception.setText("");
                                        txtSourceReception.setText(getScanInput());
                                        appContext.playSound(1);
                                    } else {
                                        //txtSourceReception.setText(getScanInput());
                                        txtSourceReception.setText("");
                                        appContext.playSound(2);
                                    }
                                    break;
                            }
                            //******************************************BEGIN*****************************************************CONTINUE
                        } else {
                            //Scanned wrong item, barcode etc...
                            Log.e("A bad scan has occurred", "Please scan again");
                            appContext.playSound(2);
                            String mMsg = "Bad scan occurred \nPlease scan again";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                            switch (NAV_TURN) {
                                case R.integer.TURN_DESTINATION:
                                    //we've just scanned a destination but incorrectly
                                    txtDestinationReception.setEnabled(true);
                                    txtDestinationReception.setText("");
                                    btnScanDestination.setEnabled(true);
                                    btnScanDestination.setPaintFlags(btnScanDestination.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    break;
                                case R.integer.TURN_SOURCE:
                                    //we've just scanned a source but incorrectly
                                    reloadActivity();
                                    break;
                            }
                        }
                    }
                    alreadyFired = false;
                }
            }
        };
        NAV_TURN = R.integer.TURN_SOURCE;   // start with the default turn!
        formatControls();
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
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons();
                    if (!btnEnterSource.isEnabled()) {
                        btnEnterSource.setEnabled(true);
                        btnEnterSource.setPaintFlags(btnEnterSource.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                    if (btnEnterDestination.isEnabled()) {
                        btnEnterDestination.setEnabled(false);
                        btnEnterDestination.setPaintFlags(btnEnterDestination.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    setScanInput(txtDestinationReception.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtDestinationReception.setText(getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        txtSourceReception.requestFocus();
                        lockDestinationControls();
                        //unlockSourceControls();
                    }
                }
                break;
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change NAV-Turn
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    if (!btnEnterDestination.isEnabled()) {
                        btnEnterDestination.setEnabled(true);
                        btnEnterDestination.setPaintFlags(btnEnterDestination.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                    if (btnEnterSource.isEnabled()) {
                        btnEnterSource.setEnabled(false);
                        btnEnterSource.setPaintFlags(btnEnterSource.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    setScanInput(txtSourceReception.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtSourceReception.setText(getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        txtDestinationReception.requestFocus();
                        lockSourceControls();
                        unlockDestinationControls();
                    }
                    //NAV_TURN = R.integer.TURN_BIN;
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
        switch (NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                this.btnScanDestination.setEnabled(false);
                break;
            case R.integer.TURN_SOURCE:
                this.btnScanSource.setEnabled(false);
                break;
        }
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
        switch (NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                this.btnScanDestination.setEnabled(false);
                break;
            case R.integer.TURN_SOURCE:
                this.btnScanSource.setEnabled(false);
                break;
        }
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        switch (NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (inputByHand == 0) {
                    //btnEnterDestination.setText(byHand);
                } else {
                    btnEnterDestination.setText(finish);
                }
                break;
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change Navigation Turn
                if (inputByHand == 0) {
                    btnEnterSource.setText(byHand);
                } else {
                    btnEnterSource.setText(finish);
                }
                break;
            case R.integer.TURN_END:
                btnEnterSource.setText(finish);
                btnEnterDestination.setText(finish);
        }
    }

    private void lockSourceControls() {
        //disable all corresponding controls
        if (txtSourceReception.isEnabled()) {
            txtSourceReception.setEnabled(false);
        }
        if (btnScanSource.isEnabled()) {
            btnScanSource.setEnabled(false);
            btnScanSource.setPaintFlags(btnScanSource.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (btnEnterSource.isEnabled()) {
            btnEnterSource.setEnabled(false);
            btnEnterSource.setPaintFlags(btnEnterSource.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void lockDestinationControls() {
        //disable all corresponding controls
        if (txtDestinationReception.isEnabled() == true) {
            txtDestinationReception.setEnabled(false);
        }
        if (btnScanDestination.isEnabled() == true) {
            btnScanDestination.setEnabled(false);
            btnScanDestination.setPaintFlags(btnScanDestination.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (btnEnterDestination.isEnabled()) {
            btnEnterDestination.setEnabled(false);
            btnEnterDestination.setPaintFlags(btnEnterDestination.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockSourceControls() {
        if (txtSourceReception.isEnabled() == false) {
            txtSourceReception.setEnabled(true);
        }
        if (!btnScanSource.isEnabled()) {
            btnScanSource.setEnabled(true);
            btnScanSource.setPaintFlags(btnScanSource.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnEnterSource.isEnabled()) {
            btnEnterSource.setEnabled(true);
            btnEnterSource.setPaintFlags(btnEnterSource.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void unlockDestinationControls() {
        if (txtDestinationReception.isEnabled() == false) {
            txtDestinationReception.setEnabled(true);
        }
        if (btnScanDestination.isEnabled() == false) {
            btnScanDestination.setEnabled(true);
            btnScanDestination.setPaintFlags(btnScanDestination.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnEnterDestination.isEnabled()) {
            btnEnterDestination.setEnabled(true);
            btnEnterDestination.setPaintFlags(btnEnterDestination.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void formatControls() {
        switch (NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                lockSourceControls();
                unlockDestinationControls();
                break;
            case R.integer.TURN_SOURCE:
                lockDestinationControls();
                unlockSourceControls();
                break;
            case R.integer.TURN_END:
                lockTaskControls();
                paintByHandButtons();
                if (NAV_TURN == R.integer.TURN_END) {
                    btnContinue.performClick();
                }
                break;
        }
    }

    private void lockTaskControls() {
        //disable all corresponding controls
        lockSourceControls();
        lockDestinationControls();
    }

    private void reloadActivity() {
        currentDestinationBin = "";
        currentSourceBin = "";
        txtSourceReception.setText("");
        txtDestinationReception.setText("");
        NAV_TURN = R.integer.TURN_SOURCE;
        formatControls();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        this.finish();
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class TextChanged implements TextWatcher {
        private View myView;
        private TextChanged(View view) {
            this.myView = view;
        }

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

            // Go on with AsyncTask
            if (s != null && !s.toString().equalsIgnoreCase("")) {
                if (inputByHand == 0) {
                    String binCode = s.toString().trim();
                    if (binCode.length() == 5) {
                        switch (NAV_TURN) {
                            case R.integer.TURN_DESTINATION:
                                //currentDestinationBin = binCode;
                                //currentDestinationBin = "4BBB1"; for testing
                                //if (!currentDestinationBin.equalsIgnoreCase(currentSourceBin)) {
                                if (!binCode.equalsIgnoreCase(currentSourceBin)) {
                                    currentDestinationBin = binCode;
                                    NAV_TURN = R.integer.TURN_END; //new state
                                    formatControls();
                                }else {
                                    //Source & destination are the same echo error
                                    String mMsg = "Destination BinCode must not be the same as Source BinCode \nPlease scan again";
                                    appContext.playSound(2);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                                    builder.setMessage(mMsg)
                                            .setCancelable(false)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    txtDestinationReception.setText("");
                                    if (txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(false); //disable the control
                                }

                                break;
                            case R.integer.TURN_SOURCE:
                                currentSourceBin = binCode;
                                //currentSourceBin = "4BBC1";   For testing only
                                //TODO - REQUESTED BY SCOTT ON 12/11/2014 - To help with empty move lists (Perform BinQuery)
                                //If source is empty error and restart activity

                                currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
                                if (currentUser != null) {
                                    today = new java.sql.Timestamp(utilDate.getTime());
                                    String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\", \"BinCode\":\"%s\"}",
                                            currentUser.getUserId(), currentUser.getUserCode(), currentSourceBin);

                                    thisMessage.setSource(deviceIMEI);
                                    thisMessage.setMessageType("BinQuery");
                                    thisMessage.setIncomingStatus(1); //default value
                                    thisMessage.setIncomingMessage(msg);
                                    thisMessage.setOutgoingStatus(0);   //default value
                                    thisMessage.setOutgoingMessage("");
                                    thisMessage.setInsertedTimeStamp(today);
                                    thisMessage.setTTL(100);    //default value
                                    //binQryTask = new queryTask();
                                    //binQryTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                                    sourceBinQueryTask = new performBinQueryAsync();
                                    sourceBinQueryTask.execute(thisMessage);

                                    NAV_TURN = R.integer.TURN_DESTINATION; //new
                                    formatControls();
                                } else {
                                    appContext.playSound(2);
                                    Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                }
//                                NAV_TURN = R.integer.TURN_DESTINATION; //new
//                                formatControls();
                                break;
                        }
                    } else {
                        String mMsg = "Bad scan occurred \nPlease scan again";
                        switch (NAV_TURN) {
                            case R.integer.TURN_SOURCE:
                                appContext.playSound(2);
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                txtSourceReception.setText("");
                                if (txtSourceReception.isEnabled()) txtSourceReception.setEnabled(false); //disable the control
                                break;
                            case R.integer.TURN_DESTINATION:
                                appContext.playSound(2);
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(ActBinMain.this);
                                builder1.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder1.show();
                                txtDestinationReception.setText("");
                                if (txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(false); //disable the control
                                break;
                            case R.integer.TURN_END:
                                //New code to disallow additional scan after a successful one
                                if (binCode.length() > 5) {
                                    binCode = binCode.substring(0, 5);
                                    currentDestinationBin = binCode;
                                    txtDestinationReception.setText(binCode);
                                    if (txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(false); //disable the control
                                } else {
                                    mMsg = "The End of the road my friend!";
                                    appContext.playSound(1);
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(ActBinMain.this);
                                    builder2.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder2.show();
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            boolean bContinuous = true;
            int iBetween = 0;
            switch (v.getId()) {
                case R.id.bnExitActBinMain:
                    ActBinMain.this.finish();
                    break;
                case R.id.bnEnterSourceBinMain:
                    if (!txtSourceReception.isEnabled()) txtSourceReception.setEnabled(true);
                    txtSourceReception.requestFocus();
                    manageInputByHand();
                    break;
                case R.id.bnEnterDestinationBinMain:
                    if (!txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(true);
                    txtDestinationReception.requestFocus();
                    manageInputByHand();
                    break;
                case R.id.bnScanSource:
                    txtSourceReception.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "Source Barcode " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("Source Barcode ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                    break;
                case R.id.bnScanDestination:
                    txtDestinationReception.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "Destination Barcode " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("Destination Barcode ReadThread");
                        readThread.start();
                    } else {
                        threadStop = true;
                    }
                    break;
                case R.id.bnContinue:
                    //check if we already have both (src & dst) values
                    if (NAV_TURN == R.integer.TURN_END) {
                        //UserAuthenticator authenticator = new UserAuthenticator(ActBinMain.this);
                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
                        if (currentUser != null) {
                            today = new java.sql.Timestamp(utilDate.getTime());
                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\", \"BinCode\":\"%s\"}",
                                    currentUser.getUserId(), currentUser.getUserCode(), currentSourceBin);

                            thisMessage.setSource(deviceIMEI);
                            thisMessage.setMessageType("BinQuery");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value
                            binQryTask = new queryTask();
                            binQryTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                if (!alreadyFired) {
                    alreadyFired = true;
                    switch (NAV_TURN) {
                        case R.integer.TURN_DESTINATION:
                            if (currentDestinationBin != null && !currentDestinationBin.equalsIgnoreCase("")) {
                                //do nothing for now -  but more functionality maybe added in the near future
                            } else {
                                //Perform Scan as usual
                                txtDestinationReception.requestFocus();
                                btnScanDestination.performClick(); //Scan();
                            }
                            break;
                        case R.integer.TURN_SOURCE:
                            //do
                            if (currentSourceBin != null && !currentSourceBin.equalsIgnoreCase("")) {
                                //do nothing for now -  but more functionality maybe added in the near future
                            } else {
                                //Perform Scan as usual
                                txtSourceReception.requestFocus();
                                btnScanSource.performClick();   //Scan();
                            }
                            break;
                    }
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (readThread != null && readThread.isInterrupted() == false) {
            readThread.interrupt();
        }
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(0, resultIntent);
        }
        finish();
    }

    private class GetBarcode implements Runnable {

        private boolean isContinuous = false;
        String barCode = "";
        private long sleepTime = 1000;
        Message msg = null;

        public GetBarcode(boolean isContinuous) {
            this.isContinuous = isContinuous;
        }

        public GetBarcode(boolean isContinuous, int sleep) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
        }

        @Override
        public void run() {

            do {
                barCode = mInstance.scan();

                Log.i("MY", "barCode " + barCode.trim());

                msg = new Message();

                if (barCode == null || barCode.isEmpty()) {
                    msg.arg1 = 0;
                    msg.obj = "";
                } else {
                    msg.what = 1;
                    msg.obj = barCode;
                }

                handler.sendMessage(msg);

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (isContinuous && !threadStop);

        }
    }

    private class queryTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActBinMain.this);
            CharSequence message = "Working hard...sending queue [directly] [to webservice]...";
            CharSequence title = "Please Wait";
            wsDialog.setCancelable(true);
            wsDialog.setCanceledOnTouchOutside(false);
            wsDialog.setMessage(message);
            wsDialog.setTitle(title);
            wsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            wsDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... msg) {
            HttpResponseHelper response = null;
            response = resolver.resolveHttpMessage(msg[0]);
            //String response = "{\"RequestedBinCode\" : \"1HCH5\",\"MatchedProducts\" : \"17\",\"Products\" : [{\"ProductId\" : \"25168976\",\"SupplierCat\" : \"SNAP273CD\",\"Artist\" : \"FUNKADELIC\",\"Title\" : \"UNCLE JAM WANTS YOU\",\"Barcode\" : \"803415127320\",\"Format\" : \"CD\",\"EAN\" : \"0803415127320\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"82\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Funkadelic\",\"Full_Title_Value1\" : \"Uncle Jam Wants You\",\"QtyInBin\" : \"10\"},{\"ProductId\" : \"153609737\",\"SupplierCat\" : \"093624948247PMI\",\"Artist\" : \"GREEN DAY\",\"Title\" : \"UNO!\",\"Barcode\" : \"093624948247\",\"Format\" : \"CD\",\"EAN\" : \"0093624948247\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"2\"},{\"ProductId\" : \"143271891\",\"SupplierCat\" : \"3325480644160PMI\",\"Artist\" : \"FRERES PITIGOI & TARA OASULU\",\"Title\" : \"MUSIQUE DE MARIAGE ET FETES RO\",\"Barcode\" : \"3325480644160\",\"Format\" : \"CD\",\"EAN\" : \"3325480644160\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"2\"},{\"ProductId\" : \"138150831\",\"SupplierCat\" : \"651249076723PMI\",\"Artist\" : \"DIGWEED,JOHN\",\"Title\" : \"VOL. 2-RENAISSANCE PRESENTS TR\",\"Barcode\" : \"651249076723\",\"Format\" : \"CD\",\"EAN\" : \"0651249076723\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"138150918\",\"SupplierCat\" : \"824363000121PMI\",\"Artist\" : \"CRANK YANKERS\",\"Title\" : \"VOL. 1-BEST CRANK CALLS\",\"Barcode\" : \"824363000121\",\"Format\" : \"CD\",\"EAN\" : \"0824363000121\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"4993383\",\"SupplierCat\" : \"ANG646662PMI\",\"Artist\" : \"FOLLIES / O.B.C.\",\"Title\" : \"FOLLIES / O.B.C.\",\"Barcode\" : \"077776466620\",\"Format\" : \"CD\",\"EAN\" : \"0077776466620\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Broadway Cast\",\"Full_Title_Value1\" : \"Follies / O.B.C.\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"5469159\",\"SupplierCat\" : \"BAPO39026B2PMI\",\"Artist\" : \"MCNALLY,SHANNON\",\"Title\" : \"NORTH AMERICAN GHOST MUSIC\",\"Barcode\" : \"094633902626\",\"Format\" : \"CD\",\"EAN\" : \"0094633902626\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"158859083\",\"SupplierCat\" : \"CDB56379480762PMI\",\"Artist\" : \"VALLDENEU,MAX\",\"Title\" : \"IT'S ABOUT LOVE (CDRP)\",\"Barcode\" : \"885767060326\",\"Format\" : \"CD\",\"EAN\" : \"0885767060326\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"129403511\",\"SupplierCat\" : \"FAPO803412PMI\",\"Artist\" : \"BURNSIDE,R.L.\",\"Title\" : \"MISSISSIPPI HILL COUNTRY BLUES\",\"Barcode\" : \"045778034123\",\"Format\" : \"CD\",\"EAN\" : \"0045778034123\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"R.L. Burnside\",\"Full_Title_Value1\" : \"Mississippi Country Blues\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"133803673\",\"SupplierCat\" : \"KSCOPE216\",\"Artist\" : \"ULVER\",\"Title\" : \"CHILDHOOD'S END\",\"Barcode\" : \"802644821627\",\"Format\" : \"CD\",\"EAN\" : \"0802644821627\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"46\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Ulver\",\"Full_Title_Value1\" : \"Childhood's End\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"173678827\",\"SupplierCat\" : \"RCA1961202PMI\",\"Artist\" : \"WALKER,HEZEKIAH\",\"Title\" : \"AZUSA THE NEXT GENERATION\",\"Barcode\" : \"886919612028\",\"Format\" : \"CD\",\"EAN\" : \"0886919612028\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Hezekiah Walker\",\"Full_Title_Value1\" : \"Azusa The Next Generation\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"4974588\",\"SupplierCat\" : \"RCA763792PMI\",\"Artist\" : \"ATKINS,CHET / PAUL,LES\",\"Title\" : \"CHESTER & LESTER (BONUS TRACKS\",\"Barcode\" : \"828767637921\",\"Format\" : \"CD\",\"EAN\" : \"0828767637921\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"2497707\",\"SupplierCat\" : \"ROUCD9009\",\"Artist\" : \"BRAVE COMBO\",\"Title\" : \"DELETED - POLKATHARSIS\",\"Barcode\" : \"011661900929\",\"Format\" : \"CD\",\"EAN\" : \"0011661900929\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"4\",\"Full_Artist_Value1\" : \"Brave Combo\",\"Full_Title_Value1\" : \"Polkatharsis\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"86158133\",\"SupplierCat\" : \"WMCD1294\",\"Artist\" : \"MENON,JAY\",\"Title\" : \"24/01THROUGH MY EYES\",\"Barcode\" : \"5016700129427\",\"Format\" : \"CD\",\"EAN\" : \"5016700129427\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"26\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Jay Menon\",\"Full_Title_Value1\" : \"Through My Eyes\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"67978413\",\"SupplierCat\" : \"FATCD62PMI\",\"Artist\" : \"BROSSEAU,TOM\",\"Title\" : \"CAVALIER\",\"Barcode\" : \"000030251304\",\"Format\" : \"CD\",\"EAN\" : \"0000030251304\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"27369877\",\"SupplierCat\" : \"FV12\",\"Artist\" : \"MARTIN,JUAN\",\"Title\" : \"SOLO\",\"Barcode\" : \"5023100081224\",\"Format\" : \"CD\",\"EAN\" : \"5023100081224\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"9\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Juan Martin\",\"Full_Title_Value1\" : \"Solo\",\"QtyInBin\" : \"9\"},{\"ProductId\" : \"2498218\",\"SupplierCat\" : \"SHCD3813\",\"Artist\" : \"RANCH ROMANCE\",\"Title\" : \"FLIP CITY\",\"Barcode\" : \"015891381329\",\"Format\" : \"CD\",\"EAN\" : \"0015891381329\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"3\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Ranch Romance\",\"Full_Title_Value1\" : \"Flip City\",\"QtyInBin\" : \"1\"}]}";
            response.setResponse(responseHelper.refineResponse(response.getResponse().toString()));
            try {

                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object returns null due to improper request.";
                    response.setResponseMessage(iMsg);
                } else {
                    qryResponse = new BinResponse();
                    ObjectMapper mapper = new ObjectMapper();
                    qryResponse = mapper.readValue(response.getResponse().toString(), BinResponse.class);
                    response.setResponse(qryResponse);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setResponseMessage(ex.getMessage());
                response.setExceptionClass(ex.getClass());
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            alreadyFired = false;
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Attempt to reload Activity
                                        if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(BinResponse.class)) {
                        BinResponse resp = (BinResponse) response.getResponse();
                        if (!resp.getProducts().isEmpty() && resp.getMatchedProducts() > 0) {
                            /**--------------------------- Success -------------------------**/
                            if (currentSourceBin != null && !currentSourceBin.equalsIgnoreCase("") &&
                                    currentDestinationBin != null && !currentDestinationBin.equalsIgnoreCase("")) {
                                switch (NAV_INSTRUCTION) {
                                    case R.integer.ACTION_PARTIALMOVE:
                                        Intent i = new Intent(ActBinMain.this, ActBinItemSelection.class);
                                        i.putExtra("SOURCE_EXTRA", currentSourceBin);
                                        i.putExtra("DESTINATION_EXTRA", currentDestinationBin);
                                        i.putExtra("DEVICEIMEI_EXTRA", deviceIMEI);
                                        i.putExtra("LOGIN_EXTRA", currentUser);
                                        i.putExtra("RESPONSE_EXTRA", resp);
                                        startActivityForResult(i, RESULT_OK);
                                        reloadActivity();
                                        break;
                                    case R.integer.ACTION_BINMOVE:
                                        Intent i2 = new Intent(ActBinMain.this, ActBinDetails.class);
                                        i2.putExtra("SOURCE_EXTRA", currentSourceBin);
                                        i2.putExtra("DESTINATION_EXTRA", currentDestinationBin);
                                        i2.putExtra("DEVICEIMEI_EXTRA", deviceIMEI);
                                        i2.putExtra("LOGIN_EXTRA", currentUser);
                                        i2.putExtra("RESPONSE_EXTRA", resp);
                                        startActivityForResult(i2, RESULT_OK);
                                        reloadActivity();
                                        break;
                                }
                            }else{
                                /**------------------------ TODO  Generate a error here ------------------------**/
                                Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(2000);
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                                builder.setMessage("Failed: You haven't scanned a destination")
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) { Intent i = new Intent();
                                                setResult(RESULT_OK, i);
                                                ActBinMain.this.finish();
                                            }
                                        });
                                builder.show();
                                appContext.playSound(2);
                            }
                        }else {
                            /**--------------------------- Error -------------------------**/
                            Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(2000);
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                            builder.setMessage("Failed: This source bin appears empty in the system")
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            Intent i = new Intent();
                                            setResult(RESULT_OK, i);
                                            ActBinMain.this.finish();
                                        }
                                    });
                            builder.show();
                            appContext.playSound(2);
                        }
                    }else {
                        Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                        builder.setMessage("Failed: " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) { Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActBinMain.this.finish();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                    builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Attempt to reload Activity
                                    if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                }
            }
        }
    }

    private class performBinQueryAsync extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            xDialog = new ProgressDialog(ActBinMain.this);
            CharSequence message = "Working hard...sending queue [directly] [to webservice]...";
            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... inputMessage) {
            HttpResponseHelper response = null;
            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                    currentUser.getUserId(), currentUser.getUserCode(), currentSourceBin);
            inputMessage[0].setMessageType("BinQuery");
            inputMessage[0].setIncomingMessage(msg);
            response = resolver.resolveHttpMessage(inputMessage[0]);
            response.setResponse(responseHelper.refineResponse(response.getResponse().toString()));
            try {

                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object returns null due to improper request.";
                    response.setResponseMessage(iMsg);
                }else {
                    BinResponse msgResponse = new BinResponse();
                    ObjectMapper mapper = new ObjectMapper();
                    msgResponse = mapper.readValue(response.getResponse().toString(), BinResponse.class);
                    response.setResponse(msgResponse);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setResponseMessage(ex.getMessage());
                response.setExceptionClass(ex.getClass());
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            //super.onPostExecute(binResponse);
            if (xDialog != null && xDialog.isShowing()) {
                xDialog.dismiss();
            }
            alreadyFired = false;
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActBinMain.this.finish();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(BinResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        BinResponse resp = (BinResponse) response.getResponse();
                        if (resp.getMatchedProducts() < 1) {
                            //Response is null the disable Yes button:
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                            builder.setCancelable(false);
                            String msg = "Failed: Bin Search did NOT return any result. Please check if bin is empty or ask for help";
                            builder.setMessage(msg)
                                    .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // Attempt to reload Activity
                                            //if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                            Intent i = new Intent();
                                            setResult(RESULT_OK, i);
                                            ActBinMain.this.finish();
                                        }
                                    });
                        } else {
                            currentSourceContents = resp;   //Set new source item
                        }
                    } else {// Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                        builder.setMessage("Failed: " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) { Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActBinMain.this.finish();
                                    }
                                });
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActBinMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMain.this);
                    builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent();
                                    setResult(RESULT_OK, i);
                                    ActBinMain.this.finish();
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                }
            }
        }
    }

}