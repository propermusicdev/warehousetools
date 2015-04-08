package com.proper.warehousetools.binmove.ui.chainway_c4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.*;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.proper.data.binmove.*;
import com.proper.data.core.ICommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.helpers.ResponseHelper;
import com.proper.warehousetools.ActMain;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.fragments.QuantityDialogFragment;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActBinProductMain extends BaseScanActivity implements ICommunicator {
    private Handler handler = null;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterSrcBin;
    private Button btnEnterBarcode;
    private Button btnEnterDstBin;
    private TextView lblSourceBin;
    private TextView lblBarcode;
    private TextView lblDestinationBin;
    private EditText txtSourceBin;
    private EditText txtBarcode;
    private EditText txtDestinationBin;
    private LinearLayout lytMain;
    private long startTime;
    private long elapseTime;
    private String backPressedParameter = "";
    private BinResponse currentBinResponse = null;
    private ProductBinResponse currentProduct = null;
    private ProductBinSelection currentBinSelection = null;
    private ResponseHelper responseHelper = new ResponseHelper();
    private WebServiceTask wsTask;
    private static final String TAG = "ActBinProductMain";
    private String currentSource = "";
    private String currentBarcode = "";
    private String currentDestination = "";
    private boolean alreadyFired = false, scannedThrough = true;

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
        setContentView(R.layout.lyt_binmove_binproductmain);
        //getWindow().setBackgroundDrawableResource(R.drawable.flat_button_palebrown);

        lytMain = (LinearLayout) this.findViewById(R.id.lytBPMMain);
        btnScan = (Button) this.findViewById(R.id.bnBPMScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActBinProductMain);

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
                                appContext.playSound(2);
                                String mMsg = "Bad scan occurred \nThis bin code is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                appContext.playSound(2);
                                String mMsg = "Bad scan occurred \nThis barcode is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                appContext.playSound(2);
                                String mMsg = "Bad scan occurred \nThis source bin is invalid";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                        appContext.playSound(1);
                        unlockScanControls();           //unlocks scan
                    }
                    alreadyFired = false;
                    scannedThrough = true;
                }
            }
        };

        NAV_TURN = R.integer.TURN_SOURCE;
        PaintButtonText();
        refreshActivity();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu
//        getMenuInflater().inflate(R.menu.mnu_main, menu);
//
//        // Find the share item
//        MenuItem shareItem = menu.findItem(R.id.menu_logout);
//
//        // Need to use MenuItemCompat to retrieve the Action Provider
//        //actionProvider = (ShareActionProvider)MenuItemCompat.getActionProvider(shareItem);
//
//        return super.onCreateOptionsMenu(menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case android.R.id.home:
//                Intent intent = new Intent();
//                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                //startActivity(intent);
//                setResult(RESULT_OK, intent);
//                finish();
//                break;
//            // Something else
//            case R.id.menu_logout:
//                Intent i = new Intent(this, ActMain.class);
//                i.putExtra("RESULT_EXTRA", 666);
//                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                setResult(666, i);
//                finish();
//                break;
//            default:
//                break;
//        }
//        return super.onOptionsItemSelected(item);
//    }

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
//                if (lblBarcode.getVisibility() != View.VISIBLE) lblBarcode.setVisibility(View.VISIBLE);
//                if (txtBarcode.getVisibility() != View.VISIBLE) txtBarcode.setVisibility(View.VISIBLE);
                break;
            case R.integer.TURN_DESTINATION:
                lockAllControls();
                unlockDestinationControls();
//                if (txtDestinationBin.getVisibility() != View.VISIBLE) txtDestinationBin.setVisibility(View.VISIBLE);
//                if (txtDestinationBin.getVisibility() != View.VISIBLE) txtDestinationBin.setVisibility(View.VISIBLE);
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

    private synchronized void startMainThread(boolean continuous, int intervals) {
        //do start thread
        threadStop = false;
        readThread = new Thread(new GetBarcode(continuous, intervals));
        readThread.setName("[Destination Bin Scan] Query ReadThread");
        readThread.start();
    }

    private synchronized void pauseMainThread() {
        //do pause thread
        //if (threadStop) threadStop = false;
        try {
            if (readThread != null && readThread.isInterrupted() == false) {
                readThread.wait();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void resumeMainThread() {
        //do resume thread
        //threadStop = true;
        readThread.notifyAll();
    }

    private synchronized void stopMainThread() {
        //do stop thread
        threadStop = true;
    }

//    @Override
//    protected void onPause() {
//        threadStop = true;
//        pauseMainThread();
////        try {
////            if (readThread != null && readThread.isInterrupted() == false) {
////                readThread.wait();
////            }
////        } catch (InterruptedException ex) {
////            ex.printStackTrace();
////        }
//        Log.d(TAG, "onPause");
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        Log.d(TAG, "onResume");
//        threadStop = false;
//        if (readThread != null) {
//            //readThread.notifyAll();
//            resumeMainThread();
//        }
//        super.onResume();
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy");
//        if (isBarcodeOpened) {
//            if (readThread != null && readThread.isInterrupted() == false) {
//                readThread.interrupt();
//            }
//            //soundPool.release();
//            mInstance.close();
//        }
//        //android.os.Process.killProcess(android.os.Process.myPid()); Since it's not longer main entry then we're not killing app *LEBEL*
//    }

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
            ActBinProductMain.this.finish();
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
        boolean bContinuous = true;
        int iBetween = 0;
        switch (view.getId()) {
            case R.id.bnBPMScan:
                if (NAV_TURN == R.integer.TURN_DESTINATION) {
                    //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
//                    lockAllControls();
//                    unlockDestinationControls();
//                    unlockScanControls();
//                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
//                    txtDestinationBin.requestFocus();
//                    if (threadStop) {
//                        Log.i("Reading", "[Destination Bin Scan] BinProductMain " + readerStatus);
//                        //init_barcode = et_init_barcode.getText().toString();
////                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
////                        readThread.setName("[Destination Bin Scan] Query ReadThread");
////                        readThread.start();
//                        startMainThread(bContinuous, iBetween);
//                    }else {
//                        threadStop = true;
//                    }
//                    fullTurnCount ++;
                    lockAllControls();
                    unlockDestinationControls();
                    unlockScanControls();
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    txtDestinationBin.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Destination Bin Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("[Destination Bin Scan] Query ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_BARCODE) {
//                    lockAllControls();
//                    unlockBarcodeControls();
//                    unlockScanControls();
//                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
//                    txtBarcode.requestFocus();
//                    if (threadStop) {
//                        Log.i("Reading", "[Barcode Scan] BinProductMain " + readerStatus);
//                        //init_barcode = et_init_barcode.getText().toString();
////                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
////                        readThread.setName("[Barcode Scan] Query ReadThread");
////                        readThread.start();
//                        startMainThread(bContinuous, iBetween);
//                    }else {
//                        threadStop = true;
//                    }
//                    fullTurnCount ++;
                    lockAllControls();
                    unlockBarcodeControls();
                    unlockScanControls();
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Barcode Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("[Barcode Scan] Query ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                if (NAV_TURN == R.integer.TURN_SOURCE) {
                    //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
//                    lockAllControls();
//                    unlockSourceControls();
//                    unlockScanControls();
//                    fullTurnCount = 0;      //set to default if it's not so already
//                    if (!txtSourceBin.isEnabled()) txtSourceBin.setEnabled(true);
//                    txtSourceBin.requestFocus();
//                    if (threadStop) {
//                        Log.i("Reading", "[Source Bin Scan] BinProductMain " + readerStatus);
//                        //init_barcode = et_init_barcode.getText().toString();
////                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
////                        readThread.setName("[Source Bin Scan] Query ReadThread");
////                        readThread.start();
//                        startMainThread(bContinuous, iBetween);
//                    }else {
//                        threadStop = true;
//                    }
//                    fullTurnCount ++;

                    lockAllControls();
                    unlockSourceControls();
                    unlockScanControls();
                    fullTurnCount = 0;      //set to default if it's not so already
                    if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                    txtDestinationBin.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "[Source Bin Scan] BinProductMain " + readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("[Source Bin Scan] Query ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                    fullTurnCount ++;
                }
                break;
            case R.id.bnExitActBinProductMain:
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
                    ActBinProductMain.this.finish();
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
                    alreadyFired = false;
                    scannedThrough = true;
                    String eanCode = s.toString().trim();
                    if (inputByHand == 0) {

                        if (NAV_TURN == R.integer.TURN_DESTINATION) {
                            if (eanCode.length() > 0 && (eanCode.length() == 5)) {
                                if (eanCode.equalsIgnoreCase(currentSource)) {
                                    appContext.playSound(2);
                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "Incorrect Combination \nSource & Destination Bin must NOT be the same";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinProductMain - afterTextChanged", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
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
                                        appContext.playSound(2);
                                        Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT)
                                            .setMessage(R.string.DEV_BIN_ERR)
                                            .setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    //refreshActivity();
                                        }
                                    }).show();
                                } else {
                                    //Warn then Refresh Activity
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT)
                                            .setMessage(R.string.DEV_BIN_ERR)
                                            .setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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
                                                    if (prod.getQtyInBin() <= 0) {
                                                        //Yell error
                                                        String mMsg = "There is no quantity for this product \nPlease re-scan";
                                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
                                                        builder.setMessage(mMsg)
                                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                                    public void onClick(DialogInterface dialog, int id) {
                                                                        //do nothing
                                                                        alreadyFired = false;
                                                                        scannedThrough = true;
                                                                    }
                                                                });
                                                        builder.show();
                                                    }else {
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
                                                        //currentBinSelection.getQtyInBin()
                                                        showQuantityDialog();
                                                    }
                                                }
                                                totalProd --;
                                                if (found == false && totalProd == 0) {
                                                    //Report no products in Bin
                                                    appContext.playSound(2);
                                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                                    // Vibrate for 500 milliseconds
                                                    vib.vibrate(2000);
                                                    String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                            appContext.playSound(2);
                                            Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                            // Vibrate for 500 milliseconds
                                            vib.vibrate(2000);
                                            String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                        appContext.playSound(2);
                                        Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "There is no such product in Bin specified \nPlease re-scan";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                    appContext.playSound(2);
                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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
                                    unlockScanControls();
                                    PaintButtonText();
                                } else {
                                    appContext.playSound(2);
                                    Vibrator vib = (Vibrator) ActBinProductMain.this.getSystemService(Context.VIBRATOR_SERVICE);
                                    // Vibrate for 500 milliseconds
                                    vib.vibrate(2000);
                                    String mMsg = "User not Authenticated \nPlease login";
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinProductMain.this);
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
                                    new AlertDialog.Builder(ActBinProductMain.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                if (!alreadyFired && scannedThrough) {
                    alreadyFired = true;
                    scannedThrough = false;
                    boolean continuous = false;
                    int intervals = 0;
                    if (NAV_TURN == R.integer.TURN_DESTINATION) {
                        //do destination bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                        lockAllControls();
                        unlockDestinationControls();
                        unlockScanControls();
                        if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                        txtDestinationBin.requestFocus();
                        if (threadStop) {
                            Log.i("Reading", "[Destination Bin Scan] BinProductMain " + readerStatus);
                            //init_barcode = et_init_barcode.getText().toString();
                            readThread = new Thread(new GetBarcode(continuous, intervals));
                            readThread.setName("[Destination Bin Scan] Query ReadThread");
                            readThread.start();
                        } else {
                            threadStop = true;
                        }
                        fullTurnCount++;
                    }
                    if (NAV_TURN == R.integer.TURN_BARCODE) {
                        //do barcode ``````````````````````````````````````````````````````````````````````````````````````````````````
                        //fullTurnCount = 0;      //set to default if it's not so already
                        lockAllControls();
                        unlockBarcodeControls();
                        unlockScanControls();
                        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                        txtBarcode.requestFocus();
                        if (threadStop) {
                            Log.i("Reading", "[Barcode Scan] BinProductMain " + readerStatus);
                            //init_barcode = et_init_barcode.getText().toString();
                            readThread = new Thread(new GetBarcode(continuous, intervals));
                            readThread.setName("[Barcode Scan] Query ReadThread");
                            readThread.start();
                        } else {
                            threadStop = true;
                        }
                        fullTurnCount++;
                    }
                    if (NAV_TURN == R.integer.TURN_SOURCE) {
                        //do source bin ``````````````````````````````````````````````````````````````````````````````````````````````````
                        lockAllControls();
                        unlockSourceControls();
                        unlockScanControls();
                        fullTurnCount = 0;      //set to default if it's not so already
                        if (!txtDestinationBin.isEnabled()) txtDestinationBin.setEnabled(true);
                        txtDestinationBin.requestFocus();
                        if (threadStop) {
                            Log.i("Reading", "[Source Bin Scan] BinProductMain " + readerStatus);
                            //init_barcode = et_init_barcode.getText().toString();
                            readThread = new Thread(new GetBarcode(continuous, intervals));
                            readThread.setName("[Source Bin Scan] Query ReadThread");
                            readThread.start();
                        } else {
                            threadStop = true;
                        }
                        fullTurnCount++;
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
        ActBinProductMain.this.finish();
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
                    if (response.contains("not recognised") || (response.contains("Error"))) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActBinProductMain - WebServiceTask - Line:1298", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                        //logger.log(log);
                        throw new NullPointerException("Warehouse Support webservice is currently down. Please contact the IT department");
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
                            //logger.log(log);
                            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
                            throw new NullPointerException(ex.getMessage());
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
                    if (response.contains("not recognised") || response.contains("Error")) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActBinMain - WebServiceTask - Line:1291", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                        logger.log(log);
                        if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
                        throw new NullPointerException("The bin you have scanned have not been recognised. Please check and scan again");
                    }else {
                        ObjectMapper mapper = new ObjectMapper();
                        BinResponse msgResponse = mapper.readValue(response, BinResponse.class);
                        retObject = new AbstractMap.SimpleEntry<Integer, Object>(instruction, msgResponse);
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                    //logger.log(log);
                    if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
                    throw new NullPointerException(ex.getMessage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                    //logger.log(log);
                    if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
                    throw new NullPointerException(ex.getMessage());
                }
            }

            return retObject;
        }

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActBinProductMain.this);
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
            alreadyFired = false;
            scannedThrough = true;
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
                    ActBinProductMain.this.finish();
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