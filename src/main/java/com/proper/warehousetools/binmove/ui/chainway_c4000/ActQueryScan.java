package com.proper.warehousetools.binmove.ui.chainway_c4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.proper.data.binmove.BarcodeBinResponse;
import com.proper.data.binmove.BarcodeResponse;
import com.proper.data.binmove.BinResponse;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.MyCustomNamingStrategy;
import com.proper.data.helpers.ResponseHelper;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.QueryView;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActQueryScan extends BaseScanActivity {
    private static final int REQUEST_BARCODE = 1001;
    private static final int REQUEST_BINCODE = 1003;
    private static final int REQUEST_BARCODE_BIN = 1005;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterBarcode;
    private Button btnEnterBincode;
    private TextView lblBarcode;
    private TextView lblBin;
    private EditText txtBarcode;
    private EditText txtBin;
    private LinearLayout lytMain;
    private static final String myMessageType = "BarcodeQuery";
    private static final String ApplicationID = "BinMove";
    private long startTime;
    private long elapseTime;
    private String backPressedParameter = "";
    private ResponseHelper responseHelper = new ResponseHelper();
    private WebServiceTask wsTask;
    private static final String TAG = "ActQueryScan";
    private String currentBarcode = "";
    private String currentBincode = "";
    private boolean alreadyFired = false;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_qryscan);


        Bundle extras = getIntent().getExtras();
        NAV_INSTRUCTION = extras.getInt("INSTRUCTION_EXTRA");
        //compare instructions passed by the previous screen and then do stuff

        lytMain = (LinearLayout) this.findViewById(R.id.lytQryScanMain);
        btnScan = (Button) this.findViewById(R.id.bnQryScanPerformScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActQryScan);
        btnEnterBarcode = (Button) this.findViewById(R.id.bnEnterBarcodeQryScan);
        btnEnterBincode = (Button) this.findViewById(R.id.bnEnterBincodeQryScan);
        lblBarcode = (TextView) this.findViewById(R.id.txtvQryScanBarcode);
        lblBin = (TextView) this.findViewById(R.id.txtvQryScanBinCode);
        txtBarcode = (EditText) this.findViewById(R.id.etxtQryScanBarcode);
        txtBin = (EditText) this.findViewById(R.id.etxtQryScanBin);

        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (lblBarcode.getVisibility() == View.VISIBLE) lblBarcode.setVisibility(View.GONE);
                if (txtBarcode.getVisibility() == View.VISIBLE) txtBarcode.setVisibility(View.GONE);
                if (btnEnterBarcode.getVisibility() == View.VISIBLE) btnEnterBarcode.setVisibility(View.GONE);
                if (lblBin.getVisibility() != View.VISIBLE) lblBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                PaintButtonText();
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (lblBin.getVisibility() == View.VISIBLE) lblBin.setVisibility(View.GONE);
                if (txtBin.getVisibility() == View.VISIBLE) txtBin.setVisibility(View.GONE);
                if (btnEnterBincode.getVisibility() == View.VISIBLE) btnEnterBincode.setVisibility(View.GONE);
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                PaintButtonText();
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                lockBinControls();  // disables lblBin & txtBin
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                break;
            default:
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                lockAllControls();
                new AlertDialog.Builder(this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_NAV_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        finish();
                    }
                }).show();
                break;
        }

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
        btnEnterBarcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        btnEnterBincode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ButtonClicked(view);
            }
        });
        txtBarcode.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtBarcode.addTextChangedListener(new TextChanged());
        txtBin.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtBin.addTextChangedListener(new TextChanged());

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if(msg.what == 1) {
                        setScanInput(msg.obj.toString());   //Set object scanned by the hardware
                        switch (NAV_INSTRUCTION) {
                            case R.integer.ACTION_BINQUERY:
                                if (getScanInput().length() == 5) {
                                    if (!getScanInput().isEmpty()) {
                                        txtBin.setText("");     //to counter a weird bug in editText control
                                        txtBin.setText(getScanInput());
                                        appContext.playSound(1);
                                    } else {
                                        //txtBin.setText(getScanInput());
                                        txtBin.setText("");
                                        appContext.playSound(2);
                                    }
                                } else {
                                    //Scanned wrong item, bin code etc...
                                    Log.e("A bad scan has occurred", "Please scan again");
                                    appContext.playSound(2);
                                    String mMsg = "Bad scan occurred \nThis bin code is invalid";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    refreshActivity();
                                    unLockBinControls();
                                    txtBin.setText("");
                                }
                                break;
                            case R.integer.ACTION_BARCODE_BINQUERY:
                                if (NAV_TURN == R.integer.TURN_BIN) {
                                    if (getScanInput().length() == 5) {
                                        //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
                                        if (!getScanInput().isEmpty()) {
                                            txtBin.setText("");     //to counter a weird bug in editText control
                                            txtBin.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            appContext.playSound(1);
                                            lockBinControls();
                                        } else {
                                            txtBin.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            appContext.playSound(2);
                                            lockBinControls();
                                        }
                                    } else {
                                        //Scanned wrong item, bin code etc...
                                        Log.e("A bad scan has occurred", "Please scan again");
                                        appContext.playSound(2);
                                        String mMsg = "Bad scan occurred \nThis bin code is invalid";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                        unLockBinControls();
                                        txtBin.setText("");
                                        alreadyFired = false;
                                    }
                                }
                                if (NAV_TURN == R.integer.TURN_BARCODE) {
                                    int acceptable[] = {12,13,14};
                                    if (getScanInput().length() > 0 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                                        //do barcode```````````````````````````````````````````````````````````````````````````````````````````````
                                        //if (!txtBarcode.getText().toString().isEmpty()) {
                                        if (!getScanInput().isEmpty()) {
                                            txtBarcode.setText(""); //to counter a weird bug in editText control
                                            txtBarcode.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            appContext.playSound(1);
                                            lockBarcodeControls();
                                        } else {
                                            txtBarcode.setText(getScanInput());
                                            //By now the nav turn state has changed
                                            appContext.playSound(2);
                                            lockBarcodeControls();
                                        }
                                        alreadyFired = false;
                                    } else {
                                        //Scanned wrong item, barcode etc...
                                        Log.e("A bad scan has occurred", "Please scan again");
                                        appContext.playSound(2);
                                        String mMsg = "Bad scan occurred \nThis barcode is invalid";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {//do nothing
                                                    }});
                                        builder.show();
                                        refreshActivity();
                                    }
                                }
                                break;
                            case R.integer.ACTION_BARCODEQUERY:
                                int acceptable[] = {12,13,14};
                                if (getScanInput().length() > 0 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                                    //if (!txtBarcode.getText().toString().isEmpty()) {
                                    if (!getScanInput().isEmpty()) {
                                        txtBarcode.setText(""); //to counter a weird bug in editText control
                                        txtBarcode.setText(getScanInput());
                                        appContext.playSound(1);
                                    } else {
                                        //txtBarcode.setText(getScanInput());
                                        txtBarcode.setText("");
                                        appContext.playSound(2);
                                    }
                                } else {
                                    //Scanned wrong item, barcode etc...
                                    Log.e("A bad scan has occurred", "Please scan again");
                                    appContext.playSound(2);
                                    String mMsg = "Bad scan occurred \nThis barcode is invalid";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {//do nothing
                                                }
                                            });
                                    builder.show();
                                    refreshActivity();
                                }
                                break;
                        }
                        if (!btnScan.isEnabled()) {
                            btnScan.setEnabled(true);
                            btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                        }
                    }else {
                        //what = 0
                        alreadyFired = false;
                    }
                }
            }
        };
//        Handle Wifi Connectivity
//        NetUtils utils = new NetUtils();
//        boolean isWifiOn = utils.isWiFiSwitchedOn(this);
//        if (!isWifiOn) {
//            utils.turnWifiOn(this);
//            utils.connectToDefaultWifi(this);
//        }

        // Initiate the navigation default turn
        NAV_TURN = R.integer.TURN_BARCODE;
        PaintButtonText();
    }

    private void PaintButtonText() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (NAV_TURN == R.integer.TURN_BARCODE) {
                    //do barcode``````````````````````````````````````````````````````````````````````````````````````````````````
                    btnScan.setText(R.string.but_startbarcode);
                    btnScan.setBackgroundResource(R.drawable.button_blue);
                }
                if (NAV_TURN == R.integer.TURN_BIN) {
                    //do turn``````````````````````````````````````````````````````````````````````````````````````````````````
                    btnScan.setText(R.string.but_startbin);
                    btnScan.setBackgroundResource(R.drawable.button_yellow);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                btnScan.setText(R.string.but_startbarcode);
                btnScan.setBackgroundResource(R.drawable.button_blue);
                break;
            case R.integer.ACTION_BINQUERY:
                btnScan.setText(R.string.but_startbin);
                btnScan.setBackgroundResource(R.drawable.button_yellow);
                break;
        }
    }

    private void ButtonClicked(View view) {
        boolean bContinuous = false;
        int iBetween = 0;
        switch (view.getId()) {
            case R.id.bnQryScanPerformScan:
                switch (NAV_INSTRUCTION) {
                    case R.integer.ACTION_BARCODEQUERY:
                        fullTurnCount = 0;      //set to default if it's not so already
                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                        txtBarcode.requestFocus();
                        if (threadStop) {
                            Log.i("Reading", "Barcode Query Scan " + readerStatus);
                            //init_barcode = et_init_barcode.getText().toString();
                            readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                            readThread.setName("Source Barcode ReadThread");
                            readThread.start();
                        }else {
                            threadStop = true;
                        }
                        fullTurnCount ++;
                        break;
                    case R.integer.ACTION_BINQUERY:
                        fullTurnCount = 0;      //set to default if it's not so already
                        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                        txtBin.requestFocus();
                        if (threadStop) {
                            Log.i("Reading", "BinCode Query scan " + readerStatus);
                            //init_barcode = et_init_barcode.getText().toString();
                            readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                            readThread.setName("BinCode Query Scan ReadThread");
                            readThread.start();
                        }else {
                            threadStop = true;
                        }
                        fullTurnCount ++;
                        break;
                    case R.integer.ACTION_BARCODE_BINQUERY:
                        if (NAV_TURN == R.integer.TURN_BARCODE) {
                            //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                            fullTurnCount = 0;      //set to default if it's not so already
                            lockBinControls();
                            if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                            txtBarcode.requestFocus();
                            if (threadStop) {
                                Log.i("Reading", "BarcodeBin [Barcode] Query scan " + readerStatus);
                                //init_barcode = et_init_barcode.getText().toString();
                                readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                                readThread.setName("BarcodeBin [Barcode] Query ReadThread");
                                readThread.start();
                            }else {
                                threadStop = true;
                            }
                            fullTurnCount ++;
                        }
                        if (NAV_TURN == R.integer.TURN_BIN) {
                            //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                            lockBarcodeControls();
                            unLockBinControls();
                            if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                            txtBin.requestFocus();
                            if (threadStop) {
                                Log.i("Reading", "BarcodeBin [Bin] Query scan " + readerStatus);
                                //init_barcode = et_init_barcode.getText().toString();
                                readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                                readThread.setName("BarcodeBin [BinCode] Query ReadThread");
                                readThread.start();
                            }else {
                                threadStop = true;
                            }
                            fullTurnCount ++;
                        }
                        break;
                }
                btnScan.setEnabled(false);
                break;
            case R.id.bnExitActQryScan:
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
            case R.id.bnEnterBarcodeQryScan:
                manageInputByHand();
                break;
            case R.id.bnEnterBincodeQryScan:
                manageInputByHand();
                break;
            default:
                break;
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
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons();
                    setScanInput(txtBin.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtBin.setText(getScanInput());     // just to trigger text changed
                    }
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons();
                    setScanInput(txtBarcode.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtBarcode.setText(getScanInput());     // just to trigger text changed
                    }
                }
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                switch (NAV_TURN) {
                    case R.integer.TURN_BIN:
                        //1st time = turn on, 2nd FINISH
                        if (inputByHand == 0) {
                            turnOnInputByHand();
                            showSoftKeyboard();
                            paintByHandButtons();
                        } else {
                            turnOffInputByHand();
                            paintByHandButtons();
                            if (!btnEnterBarcode.isEnabled()) {
                                btnEnterBarcode.setEnabled(true);
                                btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                            if (btnEnterBincode.isEnabled()) {
                                btnEnterBincode.setEnabled(false);
                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                            setScanInput(txtBin.getText().toString());
                            if (!getScanInput().isEmpty()) {
                                fullTurnCount ++;
                                txtBin.setText(getScanInput());     // just to trigger text changed
                                paintByHandButtons();
                                txtBarcode.requestFocus();
                                lockBinControls();
                                unLockBarcodeControls();
                            }
                            //NAV_TURN = R.integer.TURN_END;
                        }
                        break;
                    case R.integer.TURN_BARCODE:
                        //1st time = turn on, 2nd change NAV-Turn
                        if (inputByHand == 0) {
                            turnOnInputByHand();
                            showSoftKeyboard();
                            paintByHandButtons();
                        } else {
                            turnOffInputByHand();
                            if (!btnEnterBincode.isEnabled()) {
                                btnEnterBincode.setEnabled(true);
                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            }
                            //Disable Enter Barcode button
                            if (btnEnterBarcode.isEnabled()) {
                                btnEnterBarcode.setEnabled(false);
                                btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                            setScanInput(txtBarcode.getText().toString());
                            if (!getScanInput().isEmpty()) {
                                fullTurnCount ++;
                                txtBarcode.setText(getScanInput());     // just to trigger text changed
                                paintByHandButtons();
                                txtBin.requestFocus();
                                lockBarcodeControls();
                                unLockBinControls();
                            }
                            //NAV_TURN = R.integer.TURN_BIN;
                        }
                        break;
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
        //this.btnScan.setEnabled(false);
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (inputByHand == 0) {
                    btnEnterBincode.setText(byHand);
                } else {
                    btnEnterBincode.setText(finish);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (inputByHand == 0) {
                    btnEnterBarcode.setText(byHand);
                } else {
                    btnEnterBarcode.setText(finish);
                }
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                switch (NAV_TURN) {
                    case R.integer.TURN_BIN:
                        //1st time = turn on, 2nd FINISH
                        if (inputByHand == 0) {
                            btnEnterBincode.setText(byHand);
                        } else {
                            btnEnterBincode.setText(finish);
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
                }
                break;
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

    private void unLockBarcodeControls() {
        if (!lblBarcode.isEnabled()) lblBarcode.setEnabled(true);
        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
        if (!btnEnterBarcode.isEnabled()) {
            btnEnterBarcode.setEnabled(true);
            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockBinControls() {
        if (lblBin.isEnabled()) lblBin.setEnabled(false);
        if (txtBin.isEnabled()) txtBin.setEnabled(false);
        if (btnEnterBincode.isEnabled()) {
            btnEnterBincode.setEnabled(false);
            btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unLockBinControls() {
        if (!lblBin.isEnabled()) lblBin.setEnabled(true);
        if (!txtBin.isEnabled()) txtBin.setEnabled(true);
        if (!btnEnterBincode.isEnabled()) {
            btnEnterBincode.setEnabled(true);
            btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void lockAllControls() {
        lockBarcodeControls();
        lockBinControls();

        if (btnScan.isEnabled()) {
            btnScan.setEnabled(false);
            btnScan.setPaintFlags(btnScan.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void refreshActivity() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (lblBarcode.getVisibility() == View.VISIBLE) lblBarcode.setVisibility(View.GONE);
                if (txtBarcode.getVisibility() == View.VISIBLE) txtBarcode.setVisibility(View.GONE);
                if (btnEnterBarcode.getVisibility() == View.VISIBLE) btnEnterBarcode.setVisibility(View.GONE);
                if (lblBin.getVisibility() != View.VISIBLE) lblBin.setVisibility(View.VISIBLE);
                if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                if (btnEnterBincode.getVisibility() != View.VISIBLE) btnEnterBincode.setVisibility(View.VISIBLE);
                if (!txtBarcode.getText().toString().isEmpty()) txtBarcode.setText("");
                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                alreadyFired = false;
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (lblBin.getVisibility() == View.VISIBLE) lblBin.setVisibility(View.GONE);
                if (txtBin.getVisibility() == View.VISIBLE) txtBin.setVisibility(View.GONE);
                if (btnEnterBincode.getVisibility() == View.VISIBLE) btnEnterBincode.setVisibility(View.GONE);
                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                if (btnEnterBarcode.getVisibility() != View.VISIBLE) btnEnterBarcode.setVisibility(View.VISIBLE);
                if (!txtBarcode.getText().toString().equalsIgnoreCase("")) txtBarcode.setText("");
                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                alreadyFired = false;
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (!txtBarcode.getText().toString().isEmpty()) txtBarcode.setText("");
                if (!txtBin.getText().toString().equalsIgnoreCase("")) txtBin.setText("");
                switch (NAV_TURN) {
                    case R.integer.TURN_BARCODE:
                        lockBinControls();  // disables lblBin & txtBin
                        if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
                        if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                        // Enable barcode button controls to enable manual barcode entry
                        if (!btnEnterBarcode.isEnabled()) {
                            btnEnterBarcode.setEnabled(true);
                            btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            lockBinControls();
                            if (btnEnterBincode.isEnabled()) {
                                btnEnterBincode.setEnabled(false);
                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                        }
                        break;
                    case R.integer.TURN_BIN:
                        lockBarcodeControls();
                        if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                        if (txtBin.getVisibility() != View.VISIBLE) txtBin.setVisibility(View.VISIBLE);
                        // Enable binCode button controls to enable manual barcode entry
                        if (!btnEnterBincode.isEnabled()) {
                            btnEnterBincode.setEnabled(true);
                            btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                            lockBarcodeControls();
                            if (btnEnterBarcode.isEnabled()) {
                                btnEnterBarcode.setEnabled(false);
                                btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                            }
                        }
                        alreadyFired = false;
                        break;
                }
                break;
        }
        if (!btnScan.isEnabled()) {
            btnScan.setEnabled(true);
            btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        NAV_TURN = R.integer.TURN_BARCODE;
        PaintButtonText();
    }

    private void tidyControls() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (txtBin.getText() != null && !txtBin.getText().toString().equalsIgnoreCase("")) btnScan.setEnabled(false);
                if (!btnEnterBincode.isEnabled()) {
                    btnEnterBincode.setEnabled(true);
                    btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (txtBarcode.getText() != null && !txtBarcode.getText().toString().equalsIgnoreCase("")) btnScan.setEnabled(false);
                if (!btnEnterBarcode.isEnabled()) {
                    btnEnterBarcode.setEnabled(true);
                    btnEnterBarcode.setPaintFlags(btnEnterBarcode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                if (txtBarcode.getText() != null && !txtBarcode.getText().toString().equalsIgnoreCase("")) {
                    //if (!btnScan.isEnabled()) btnScan.setEnabled(true);
                    if (!btnScan.isEnabled()) {
                        btnScan.setEnabled(true);
                        btnScan.setPaintFlags(btnScan.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
                if (txtBin.getText() != null && !txtBin.getText().toString().equalsIgnoreCase("")) {
                    if (btnScan.isEnabled()) btnScan.setEnabled(false);
                }
                break;
            default:
                break;
        }
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
            mInstance.close();
        }
        //soundPool.release();
        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            wsTask.cancel(true);
        }
        Intent i = new Intent();
        setResult(RESULT_OK, i);
        ActQueryScan.this.finish();
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
//                    int allAccepted[] = {5,12,13,14};
//                    if (eanCode.length() > 0 && !(Arrays.binarySearch(allAccepted, eanCode.length()) == -1)) {
//                    }
                    if (inputByHand == 0) {

                        switch (NAV_INSTRUCTION) {
                            case R.integer.ACTION_BARCODEQUERY:
                                int acceptableA[] = {12,13,14};
                                if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptableA, eanCode.length()) == -1)) {

                                    //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                    currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                    if (currentUser != null) {

                                        currentBarcode = eanCode;

                                        wsTask = new WebServiceTask();
                                        wsTask.execute(String.format("%s", NAV_INSTRUCTION), eanCode);
                                    } else {
                                        appContext.playSound(2);
                                        Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                    }
                                } else {
                                    //Check to see if we're making entry by hand
                                    if (inputByHand == 0) {
                                        new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                // TODO Auto-generated method stub
                                                refreshActivity();
                                            }
                                        }).show();
                                    }
                                }
                                break;
                            case R.integer.ACTION_BINQUERY:
                                if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                    //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                    currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                    if (currentUser != null) {

                                        currentBincode = eanCode;

                                        wsTask = new WebServiceTask();
                                        wsTask.execute(String.format("%s", NAV_INSTRUCTION), eanCode);
                                    } else {
                                        appContext.playSound(2);
                                        Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        //do nothing
                                                    }
                                                });
                                        builder.show();
                                    }
                                } else {
                                    //Check to see if we're making entry by hand
                                    if (inputByHand == 0) {
                                        new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                refreshActivity();
                                            }
                                        }).show();
                                    }
                                }
                                break;
                            case R.integer.ACTION_BARCODE_BINQUERY:
                                if (NAV_TURN == R.integer.TURN_BIN) {
                                    //do bin``````````````````````````````````````````````````````````````````````````````````````````````````
                                    //lockBarcodeControls();
                                    if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                        lockBarcodeControls();
                                        currentBincode = ""; //set to default
                                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                        if (currentUser != null) {

                                            currentBincode = eanCode;

                                            wsTask = new WebServiceTask();
                                            wsTask.execute(String.format("%s", NAV_INSTRUCTION), currentBarcode, eanCode);

                                            //Finally end turn by locking all input controls
                                            //lockAllControls();
                                        } else {
                                            appContext.playSound(2);
                                            Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "User not Authenticated \nPlease login";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
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
                                            new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //refreshActivity();
                                                }
                                            }).show();
                                        } else {
                                            //Warn then Refresh Activity
                                            new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    refreshActivity();
                                                }
                                            }).show();
                                        }
                                    }
                                }
                                if (NAV_TURN == R.integer.TURN_BARCODE) {
                                    //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                    lockBinControls();
                                    int acceptableB[] = {12,13,14};
                                    if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptableB, eanCode.length()) == -1)) {


                                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                                        if (currentUser != null) {

                                            //wsTask = new WebServiceTask();
                                            //wsTask.execute(String.format("%s", NAV_INSTRUCTION), eanCode);
                                            currentBarcode = eanCode;
                                            //Finally switch turn (bin)
                                            NAV_TURN = R.integer.TURN_BIN;
                                            if (!btnEnterBincode.isEnabled()) {
                                                btnEnterBincode.setEnabled(true);
                                                btnEnterBincode.setPaintFlags(btnEnterBincode.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                            }
                                            PaintButtonText();
                                        } else {
                                            appContext.playSound(2);
                                            Vibrator vib = (Vibrator) ActQueryScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "User not Authenticated \nPlease login";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActQueryScan.this);
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
                                            new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    // TODO Auto-generated method stub
                                                    refreshActivity();
                                                }
                                            }).show();
                                        }
                                    }
                                } else {
//                                new AlertDialog.Builder(ActQueryScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        refreshActivity();
//                                    }
//                                }).show();
                                }
                                break;
                        }

                        //End full turn
                        fullTurnCount = 0;
                    }

                }
                //fullTurnCount = 0;  old code
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                if (!alreadyFired) {
                    alreadyFired = true;
                    boolean continuous = false;
                    int intervals = 0;
                    switch (NAV_INSTRUCTION) {
                        case R.integer.ACTION_BARCODEQUERY:
                            //if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                            fullTurnCount = 0;      //set to default if it's not so already
                            if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                            txtBarcode.requestFocus();
                            if (threadStop) {
                                Log.i("Reading", "Barcode Query Scan " + readerStatus);
                                //init_barcode = et_init_barcode.getText().toString();
                                readThread = new Thread(new GetBarcode(continuous, intervals));
                                readThread.setName("Source Barcode ReadThread");
                                readThread.start();
                            }else {
                                threadStop = true;
                            }
                            fullTurnCount ++;
                            break;
                        case R.integer.ACTION_BINQUERY:
                            fullTurnCount = 0;      //set to default if it's not so already
                            if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                            txtBin.requestFocus();
                            if (threadStop) {
                                Log.i("Reading", "BinCode Query scan " + readerStatus);
                                //init_barcode = et_init_barcode.getText().toString();
                                readThread = new Thread(new GetBarcode(continuous, intervals));
                                readThread.setName("BinCode Query Scan ReadThread");
                                readThread.start();
                            }else {
                                threadStop = true;
                            }
                            fullTurnCount ++;
                            break;
                        case R.integer.ACTION_BARCODE_BINQUERY:
                            if (NAV_TURN == R.integer.TURN_BARCODE) {
                                //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                                fullTurnCount = 0;      //set to default if it's not so already
                                lockBinControls();
                                if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                                txtBarcode.requestFocus();
                                if (threadStop) {
                                    Log.i("Reading", "BarcodeBin [Barcode] Query scan " + readerStatus);
                                    //init_barcode = et_init_barcode.getText().toString();
                                    readThread = new Thread(new GetBarcode(continuous, intervals));
                                    readThread.setName("BarcodeBin [Barcode] Query ReadThread");
                                    readThread.start();
                                }else {
                                    threadStop = true;
                                }
                                fullTurnCount ++;
                            }
                            if (NAV_TURN == R.integer.TURN_BIN) {
                                //do bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                                lockBarcodeControls();
                                if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                                txtBin.requestFocus();
                                if (threadStop) {
                                    Log.i("Reading", "BarcodeBin [Bin] Query scan " + readerStatus);
                                    //init_barcode = et_init_barcode.getText().toString();
                                    readThread = new Thread(new GetBarcode(continuous, intervals));
                                    readThread.setName("BarcodeBin [BinCode] Query ReadThread");
                                    readThread.start();
                                }else {
                                    threadStop = true;
                                }
                                fullTurnCount ++;
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
        if (readThread != null && readThread.isInterrupted() == false) {
            readThread.interrupt();
        }
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(RESULT_OK, resultIntent);
        }
        ActQueryScan.this.finish();
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
                isBarcodeOpened = mInstance.open();
                barCode = mInstance.scan();

                Log.i("MY", "barCode " + barCode.trim());

                msg = new Message();

                if (barCode == null || barCode.isEmpty()) {
                    msg.what = 0;
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

    private class WebServiceTask extends AsyncTask<String, Void, Object> {
        protected ProgressDialog xDialog;
        private String originalEAN = "";


        @Override
        protected Object doInBackground(String... input) {
            int instruction = Integer.parseInt(input[0]);
            String barcode = "";
            String bincode = "";
            String msg = "";
            originalEAN = barcode.toString().trim();
            today = new java.sql.Timestamp(utilDate.getTime());
            thisMessage.setSource(deviceIMEI);
            //thisMessage.setMessageType(myMessageType);
            thisMessage.setIncomingStatus(1); //default value
            //thisMessage.setIncomingMessage(msg);
            thisMessage.setOutgoingStatus(0);   //default value
            thisMessage.setOutgoingMessage("");
            thisMessage.setInsertedTimeStamp(today);
            thisMessage.setTTL(100);    //default value
            Object retObject = null;
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(new MyCustomNamingStrategy());
            switch (instruction) {
                case R.integer.ACTION_BARCODEQUERY:
                    barcode = input[1];
                    msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\":\"%s\"}",
                            currentUser.getUserId(), currentUser.getUserCode(), barcode);
                    BarcodeResponse bcResponse = new BarcodeResponse();
                    thisMessage.setMessageType("BarcodeQuery");
                    thisMessage.setIncomingMessage(msg);
                    try {
                        String response = resolver.resolveMessageQuery(thisMessage);    //We hide the inner workings of the http being sent
                        response = responseHelper.refineProductResponse(response);
                        if (response.contains("not recognised")) {
                            //manually error trap this error
                            String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - WebServiceTask - Line:1257", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                            //logger.log(log);
                            throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
                        }else {
                            bcResponse = mapper.readValue(response, BarcodeResponse.class);
                            retObject = bcResponse;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        //logger.log(log);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        //logger.log(log);
                    }
                    break;
                case R.integer.ACTION_BINQUERY:
                    bincode = input[1];
                    msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                            currentUser.getUserId(), currentUser.getUserCode(), bincode);
                    BinResponse msgResponse = new BinResponse();
                    thisMessage.setMessageType("BinQuery");
                    thisMessage.setIncomingMessage(msg);
                    try {
                        String response = resolver.resolveMessageQuery(thisMessage);
                        //response = responseHelper.refineOutgoingMessage(response);
                        response = responseHelper.refineResponse(response);
                        if (response.contains("not recognised")) {
                            //manually error trap this error
                            String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - WebServiceTask - Line:1291", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                            //logger.log(log);
                            throw new RuntimeException("The bin you have scanned have not been recognised. Please check and scan again");
                        }else {
                            msgResponse = mapper.readValue(response, BinResponse.class);
                            retObject = msgResponse;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.log(log);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        //logger.log(log);
                    }
                    break;
                case R.integer.ACTION_BARCODE_BINQUERY:
                    barcode = input[1];
                    bincode = input[2];
                    msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\" : \"%s\", \"BinCode\" : \"%s\"}",
                            currentUser.getUserId(), currentUser.getUserCode(), barcode, bincode);
                    BarcodeBinResponse thisResponse = new BarcodeBinResponse();
                    thisMessage.setMessageType("BarcodeBinQuery");
                    thisMessage.setIncomingMessage(msg);
                    try {
                        String response = resolver.resolveMessageQuery(thisMessage);
                        response = responseHelper.refineResponse(response);
                        if (response.contains("not recognised")) {
                            //manually error trap this error
                            String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                            today = new java.sql.Timestamp(utilDate.getTime());
                            LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - WebServiceTask - Line:1325", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                            //logger.log(log);
                            throw new RuntimeException("The product and bin combination you have scanned have not been recognised. Please check and scan again");
                        }else {
                            thisResponse = mapper.readValue(response, BarcodeBinResponse.class);
                            retObject = thisResponse;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        //logger.log(log);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        //logger.log(log);
                    }
                    break;
            }
            //Log.d("===============Instruction: ", input[0]);
            //Log.d("===============Barcode: ", barcode);
            //Log.d("===============Bincode: ", bincode);
            return retObject;
        }

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActQueryScan.this);
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
        protected void onPostExecute(Object responseObject) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            alreadyFired = false;
            //Finally end turn by locking all input controls
            lockAllControls();
            //Navigate to QueryView screen with an appropriate dataSet
            switch (NAV_INSTRUCTION) {
                case R.integer.ACTION_BARCODEQUERY:
                    BarcodeResponse bcResponse = (BarcodeResponse) responseObject;
                    Intent intent = new Intent(ActQueryScan.this, QueryView.class);
                    intent.putExtra("BARCODERESPONSE_EXTRA", bcResponse);
                    intent.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(intent, REQUEST_BARCODE);
                    break;
                case R.integer.ACTION_BARCODE_BINQUERY:
                    BarcodeBinResponse bbResponse = (BarcodeBinResponse) responseObject;
                    Intent i2 = new Intent(ActQueryScan.this, QueryView.class);
                    i2.putExtra("PRODUCTBINRESPONSE_EXTRA", bbResponse);
                    i2.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i2, REQUEST_BARCODE_BIN);
                    break;
                case R.integer.ACTION_BINQUERY:
                    BinResponse bResponse = (BinResponse) responseObject;
                    Intent i = new Intent(ActQueryScan.this, QueryView.class);
                    i.putExtra("BINRESPONSE_EXTRA", bResponse);
                    i.putExtra("INSTRUCTION_EXTRA", NAV_INSTRUCTION);
                    startActivityForResult(i, REQUEST_BINCODE);
                    break;
            }
            //Finally we restore the default nav turn
            refreshActivity();
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