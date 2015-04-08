package com.proper.warehousetools.binchecker.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.data.binchecker.IndividualMoveLine;
import com.proper.data.binchecker.IndividualMoveRequest;
import com.proper.data.binchecker.adapters.IndividualMoveAdapter;
import com.proper.data.binmove.*;
import com.proper.data.core.ICommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by lebel on 07/04/2015.
 */
public class ActPrepareMovesOld extends BaseScanActivity implements ICommunicator {
    private RadioGroup radioGroup;
    private RadioButton radSrcBin, radDstBin, radProduct;
    private Button btnEnterByHand, btnScan, btnExit, btnNewMove, btnSubmit;
    private EditText txtBarcode;
    private ListView lvMoves;
    private String scanInput;
    private BinResponse binData = null;
    private LinkedList<IndividualMoveLine> moves = null;
    private IndividualMoveLine currentMove = null;
    private BinCheckerQueryTask binQryTask;
    private PartialBinMoveResponse qryResponse = null;
    private ProductLookupAsync qryProduct = null;
    private IndividualMoveAdapter moveAdapter = null;
    private String currentSrcBin = "", currentDstBin = "", currentProduct = "";
    private int lastScanningMode, selectedMode, initalized;
    private boolean alreadyFired = false, moveInProgress = false; //false
    private static final int MODE_DESTINATION = -17, MODE_SOURCE = -29, MODE_PRODUCT = -31;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public String getCurrentSrcBin() {
        return currentSrcBin;
    }

    public void setCurrentSrcBin(String currentSrcBin) {
        this.currentSrcBin = currentSrcBin;
    }

    public String getCurrentDstBin() {
        return currentDstBin;
    }

    public void setCurrentDstBin(String currentDstBin) {
        this.currentDstBin = currentDstBin;
    }

    public int getLastScanningMode() {
        return lastScanningMode;
    }

    public String getCurrentProduct() {
        return currentProduct;
    }

    public void setCurrentProduct(String currentProduct) {
        this.currentProduct = currentProduct;
    }

    public void setLastScanningMode(int lastScanningMode) {
        this.lastScanningMode = lastScanningMode;
    }

    public int getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(int selectedMode) {
        this.selectedMode = selectedMode;
    }

    public boolean isMoveInProgress() {
        return moveInProgress;
    }

    public void setMoveInProgress(boolean moveInProgress) {
        this.moveInProgress = moveInProgress;
        if (moveInProgress) {
            /** do work **/
            initalized ++;
            onExecuteMove();
        }else {
            /** aftermath **/
            postExecuteMove();
        }
    }

    public IndividualMoveLine getCurrentMove() {
        return currentMove;
    }

    public void setCurrentMove(IndividualMoveLine currentMove) {
        this.currentMove = currentMove;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binchecker_preparemovesold);
        radioGroup = (RadioGroup) this.findViewById(R.id.rgBCPMScanMode);
        radSrcBin = (RadioButton) this.findViewById(R.id.rdBCPMSrcBin);
        radDstBin = (RadioButton) this.findViewById(R.id.rdBCPMDstBin);
        radProduct = (RadioButton) this.findViewById(R.id.rdBCPMBarcode);
        btnScan = (Button) this.findViewById(R.id.bnBCPMScan);
        btnEnterByHand = (Button) this.findViewById(R.id.bnBCPMEnterBin);
        btnExit = (Button) this.findViewById(R.id.bnExitActPrepareMoves);
        btnNewMove = (Button) this.findViewById(R.id.bnBCPMNewMove);
        btnSubmit = (Button) this.findViewById(R.id.bnBCPMSubmit);
        txtBarcode = (EditText) this.findViewById(R.id.etxtBCPMScanValue);
        lvMoves = (ListView) this.findViewById(R.id.lvBCPMMoves);
        btnNewMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnEnterByHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        txtBarcode.addTextChangedListener(new TextChanged());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupButtonChanged(group, checkedId);
            }
        });

        Bundle extras = getIntent().getExtras();
        if(extras == null) throw new NullPointerException("Unable to extract Extra data in onCreate");
        binData = (BinResponse) extras.getSerializable("RESPONSE_EXTRA");
        if(binData == null) throw new NullPointerException("Bin Data should not be null");

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setScanInput(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    if (!getScanInput().isEmpty() && getScanInput().length() >= 5) {
                        txtBarcode.setText("");
                        txtBarcode.setText(getScanInput());
                    }else{
                        //txtBarcode.setText("");
                        alreadyFired = false;
                    }
                    appContext.playSound(1);
                    btnScan.setEnabled(true);
                }else {
                    alreadyFired = false;
                }
            }
        };
        setSelectedMode(MODE_PRODUCT);
        //effectRadioButton(selectedMode);
        lockAllControls();
        if (!btnNewMove.isEnabled()) btnNewMove.setEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        threadStop = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBarcodeOpened) {
            mInstance.close();
        }
    }

    private void effectRadioButton(int mode) {
        switch (mode) {
            case MODE_DESTINATION:
                radDstBin.performClick();
                break;
            case MODE_PRODUCT:
                radProduct.performClick();
                break;
            case MODE_SOURCE:
                radSrcBin.performClick();
        }
    }

    private void radioGroupButtonChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rdBCPMSrcBin:
                setSelectedMode(MODE_SOURCE);
                paintScanButton();
                break;
            case R.id.rdBCPMBarcode:
                setSelectedMode(MODE_PRODUCT);
                paintScanButton();
                break;
            case R.id.rdBCPMDstBin:
                setSelectedMode(MODE_DESTINATION);
                paintScanButton();
                break;
        }
    }

    private void buttonClicked(View v) {
        boolean isContinuous = false;   //continuous scan feature?
        int iBetween = 0;
        if (v == btnEnterByHand) {
            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                txtBarcode.setText("");
                showSoftKeyboard();
            } else {
                turnOffInputByHand();
            }
        }

        if (v == btnScan) {
            if (moveInProgress) {
                if (!alreadyFired) {
                    alreadyFired = true;
                    btnScan.setEnabled(false);
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    txtBarcode.setText("");
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "My Barcode " + readerStatus);
                        readThread = new Thread(new GetBarcode(isContinuous, iBetween));
                        readThread.setName("Single Barcode ReadThread");
                        readThread.start();
                    } else {
                        threadStop = true;
                    }
                    btnScan.setEnabled(true);
                }
            }else{
                setMoveInProgress(true);
            }
        }

        if (v == btnExit) {
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            finish();
        }

        if (v == btnNewMove) {
            //change value appropriately
            boolean value = moveInProgress;
            if (moveInProgress) {
                if (!isCurrentMoveEmpty()) {
                    value = false;
                } else {
                    //TODO - Warn of a violation of rules - moves cannot be empty at this point
                    String msg = "Error: moves cannot be null";
                    if (currentMove.getDstBin() == null && currentMove.getSrcBin() == null && currentMove.getSupplierCat() == null
                            && currentMove.getProductId() == 0 && currentMove.getQty() == 0) {
                        msg = "Error: moves cannot be empty at this point";
                    }
                    if (currentMove.getSrcBin() == null && currentMove.getDstBin() == null) {
                        msg = "Error: Both source and destination bin cannot be null";
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this)
                            .setTitle("Rule Violation !!")
                            .setMessage(msg)
                            .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) { //do nothing
                                }
                            });
                    builder.show();
                }
            } else {
                value = true;
            }
            setMoveInProgress(value);
        }

        if (v == btnSubmit) {
            if (!moves.isEmpty()) {
                IndividualMoveRequest req = new IndividualMoveRequest("Correction",
                        new ArrayList<IndividualMoveLine>(moves), currentUser.getUserCode(), currentUser.getUserId());
                binQryTask = new BinCheckerQueryTask();
                binQryTask.execute(req);  //executes both -> Send Queue Directly AND Send queue to Service
            }
        }

    }

    private boolean isCurrentMoveEmpty() {
        if (currentMove == null) {
            return true;
        } else {
            if (currentMove.getDstBin() == null && currentMove.getSrcBin() == null && currentMove.getSupplierCat() == null
                    && currentMove.getProductId() == 0 && currentMove.getQty() == 0) {
                return true;
            } else if (currentMove.getDstBin() == null && currentMove.getSrcBin() == null) {
                return true;
            } else {
                return false;
            }
        }
    }

    private void revertToDefaultValues(){
        currentMove = null;
        moves = null;
        txtBarcode.setText("");
    }

    private void postExecuteMove() {
        paintScanButton();
        if (!isCurrentMoveEmpty()) {
            if (moves == null) {
                moves = new LinkedList<IndividualMoveLine>();
            }
            moves.add(currentMove);
            moveAdapter = new IndividualMoveAdapter(this, moves);
            lvMoves.setAdapter(moveAdapter);
            currentMove = null;
            txtBarcode.setEnabled(false);
            effectRadioButton(MODE_PRODUCT);        /** Manually set **/
            initalized = 0;
        }
    }

    private void onExecuteMove() {
        if (moveInProgress == true) {
            if (isCurrentMoveEmpty()) {
                currentMove = new IndividualMoveLine();
            }
            if (initalized == 1) {
                effectRadioButton(selectedMode);
            }
            paintScanButton();
        }
    }

    class TextChanged implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && !s.toString().equalsIgnoreCase("")) {
                if (inputByHand == 0) {
                    String eanCode = s.toString().trim();
                    switch (selectedMode) {
                        case MODE_PRODUCT:
                            int acceptable[] = {12,13,14};
                            if (getScanInput().length() > 0 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                                setCurrentProduct(eanCode);

                                today = new java.sql.Timestamp(utilDate.getTime());
                                String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\":\"%s\"}",
                                        currentUser.getUserId(), currentUser.getUserCode(), eanCode);
                                thisMessage = new com.proper.messagequeue.Message();
                                thisMessage.setSource(deviceIMEI);
                                thisMessage.setMessageType("BarcodeQuery");
                                thisMessage.setIncomingStatus(1); //default value
                                thisMessage.setIncomingMessage(msg);
                                thisMessage.setOutgoingStatus(0);   //default value
                                thisMessage.setOutgoingMessage("");
                                thisMessage.setInsertedTimeStamp(today);
                                thisMessage.setTTL(100);    //default value
                                qryProduct = new ProductLookupAsync();
                                qryProduct.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                            }
                            break;
                        default:
                            if (eanCode.length() == 5) {
                                if (selectedMode == MODE_DESTINATION) {
                                    setCurrentDstBin(eanCode);
                                    //paintScanButton();
                                    currentMove.setDstBin(eanCode);
                                    txtBarcode.setEnabled(false);
                                    btnSubmit.setEnabled(true);
                                    alreadyFired = false;
                                }
                                if (selectedMode == MODE_SOURCE) {
                                    setCurrentSrcBin(eanCode);
                                    //paintScanButton();
                                    currentMove.setSrcBin(eanCode);
                                    txtBarcode.setEnabled(false);
                                    btnSubmit.setEnabled(true);
                                    alreadyFired = false;
                                }
                            } else {
                                appContext.playSound(2);
                                Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                                // Vibrate for 500 milliseconds
                                vib.vibrate(2000);
                                String mMsg = "Please re-scan";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                                builder.setTitle("Bad Scan");
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                txtBarcode.setText("");
                                                alreadyFired = false;
                                                txtBarcode.setEnabled(false);
                                            }
                                        });
                                builder.show();
                            }
                            break;
                    }
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
        setScanInput(txtBarcode.getText().toString());
        if (!getScanInput().isEmpty()) {
            txtBarcode.setText(getScanInput()); // just to trigger text changed
        }
        paintByHandButtons();
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        if (inputByHand == 0) {
            btnEnterByHand.setText(byHand);
        } else {
            btnEnterByHand.setText(finish);
        }
    }

    private void paintScanButton(){
        final String scanProd = "Scan Product", scanBin = "Scan Bin",
                newMove = "New Move", finishMove =  "Add Move";
        if (moveInProgress) {
            btnScan.setText(scanBin);
            btnNewMove.setText(finishMove);
            switch (selectedMode) {
                case MODE_PRODUCT:
                    btnScan.setText(scanProd);
                    btnScan.setBackgroundResource(R.drawable.button_blue);
                    btnEnterByHand.setBackgroundResource(R.drawable.button_blue);
                    break;
                case MODE_DESTINATION:
                    btnScan.setBackgroundResource(R.drawable.button_yellow);
                    btnEnterByHand.setBackgroundResource(R.drawable.button_yellow);
                    break;
                case MODE_SOURCE:
                    btnScan.setBackgroundResource(R.drawable.button_black);
                    btnEnterByHand.setBackgroundResource(R.drawable.button_black);
                    break;
            }
            unlockRadioControls();
            unlockScanControls();
        }else{
            btnNewMove.setText(newMove);
            lockRadioControls();
            lockScanControls();
            //btnScan.setBackgroundResource(R.drawable.button_purpple);
            //btnEnterByHand.setBackgroundResource(R.drawable.button_purpple);
        }
    }

    private void lockAllControls(){
        lockScanControls();
        if (radioGroup.isEnabled()) radioGroup.setEnabled(false);
        if (btnNewMove.isEnabled()) btnNewMove.setEnabled(false);
        if (btnSubmit.isEnabled()) btnSubmit.setEnabled(false);
    }

    private void lockScanControls() {
        if (btnScan.isEnabled()) btnScan.setEnabled(false);
        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
        if (btnEnterByHand.isEnabled()) btnEnterByHand.setEnabled(false);
    }

    private void unlockScanControls() {
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
        if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
        if (!btnEnterByHand.isEnabled()) btnEnterByHand.setEnabled(true);
    }

    private void lockRadioControls() {
        if (radioGroup.isEnabled()) radioGroup.setEnabled(false);
        if (radSrcBin.isEnabled()) radSrcBin.setEnabled(false);
        if (radProduct.isEnabled()) radProduct.setEnabled(false);
        if (radDstBin.isEnabled()) radDstBin.setEnabled(false);
    }

    private void unlockRadioControls() {
        if (!radioGroup.isEnabled()) radioGroup.setEnabled(true);
        if (!radSrcBin.isEnabled()) radSrcBin.setEnabled(true);
        if (!radProduct.isEnabled()) radProduct.setEnabled(true);
        if (!radDstBin.isEnabled()) radDstBin.setEnabled(true);
    }

    private void showQuantityDialog() {
        BinCheckerQuantityFragment dialog = new BinCheckerQuantityFragment();
        FragmentManager fm = getSupportFragmentManager(); //declare bundles here
        Bundle bundle = new Bundle();
        bundle.putSerializable("MOVE", currentMove);
        dialog.setArguments(bundle);
        dialog.show(fm, "BinCheckerQuantityFragment");
    }

    private void reloadActivity() {
        finish();
        startActivity(getIntent());
    }

    @Override
    public void onDialogMessage_ICommunicator(int buttonClicked) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                if (currentMove.getQty() > 1) {
                    this.setTitle(String.format("Moving %s items", currentMove.getQty()));
                } else {
                    this.setTitle(String.format("Moving %s item", currentMove.getQty()));
                }
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (!alreadyFired) {
                alreadyFired = true;
                if (event.getRepeatCount() == 0) {
                    boolean bContinuous = false;
                    int iBetween = 0;
                    if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                    txtBarcode.setText("");
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "My Barcode " + readerStatus);
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("Single Barcode ReadThread");
                        readThread.start();
                    } else {
                        threadStop = true;
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
        alreadyFired = false;
        if (resultCode == RESULT_FIRST_USER) {
            setLastScanningMode(data.getIntExtra("PREVIOUS_MODE", -33));
        }
        if (resultCode == 111) {
            /**     Start Again    s**/
            revertToDefaultValues();
        }
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

    private class ProductLookupAsync extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog pmDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pmDialog = new ProgressDialog(ActPrepareMovesOld.this);
            CharSequence message = "Checking barcode...";
            CharSequence title = "Please Wait";
            pmDialog.setCancelable(true);
            pmDialog.setCanceledOnTouchOutside(false);
            pmDialog.setMessage(message);
            pmDialog.setTitle(title);
            pmDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pmDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... params) {
            HttpResponseHelper response = null;
            try {
                //String response = resolver.resolveMessageQuery(thisMessage);    //We hide the inner workings of the http being sent
                response = resolver.resolveHttpMessage(thisMessage);
                response.setResponseMessage(responseHelper.refineProductResponse(response.getResponse().toString()));
                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - WebServiceTask - Line:1257", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                    //logger.log(log);
                    throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
                }else {
                    ObjectMapper mapper = new ObjectMapper();
                    BarcodeResponse bcResponse = mapper.readValue(response.getResponseMessage(), BarcodeResponse.class);
                    if(bcResponse != null) response.setResponse(bcResponse);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActQueryScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                //logger.log(log);
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            super.onPostExecute(response);
            if (pmDialog != null && pmDialog.isShowing()) pmDialog.dismiss();
            alreadyFired = false;
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        txtBarcode.setText("");
                                        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(BarcodeResponse.class)) {
                        /**TODO - -------------------------------------- Success ----------------------------------**/
                        BarcodeResponse resp = (BarcodeResponse) response.getResponse();
                        currentMove.setProductId(resp.getProducts().get(0).getProductId());
                        currentMove.setSupplierCat(resp.getProducts().get(0).getSupplierCat());
                        txtBarcode.setText("");
                        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
                        showQuantityDialog();
                    } else { // Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage("Unable to cast result to required object [BarcodeResponse]")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        txtBarcode.setText("");
                                        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    if (response.getResponseMessage().contains("invalid product")) {
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage(response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        txtBarcode.setText("");
                                        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
                                    }
                                });
                        builder.show();
                    }else {
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        txtBarcode.setText("");
                                        if (txtBarcode.isEnabled()) txtBarcode.setEnabled(false);
                                    }
                                });
                        builder.show();
                    }
                    appContext.playSound(2);
                    btnScan.setEnabled(true);
                }
            }
        }
    }

    private class BinCheckerQueryTask extends AsyncTask<IndividualMoveRequest, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            //txtBin.setText("");     //empty control
            wsDialog = new ProgressDialog(ActPrepareMovesOld.this);
            CharSequence message = "Checking bin...";
            CharSequence title = "Please Wait";
            wsDialog.setCancelable(true);
            wsDialog.setCanceledOnTouchOutside(false);
            wsDialog.setMessage(message);
            wsDialog.setTitle(title);
            wsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            wsDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(IndividualMoveRequest... req) {
            HttpResponseHelper response = null;
            Thread.currentThread().setName("StockTakeBinResponseAsyncTask");
            qryResponse = null;
            thisMessage = new com.proper.messagequeue.Message();
            today = new java.sql.Timestamp(utilDate.getTime());
            ObjectMapper mapper = new ObjectMapper();
            String request = null;
            try {
                request = mapper.writeValueAsString(req[0]);
            } catch (IOException e) {
                e.printStackTrace();
                /** Escaped with returned because we could not create a proper request **/
                return null;
            }
            thisMessage.setSource(deviceIMEI);
            thisMessage.setMessageType("CreateNewMovelist");
            thisMessage.setIncomingStatus(1); //default value
            thisMessage.setIncomingMessage(request);
            thisMessage.setOutgoingStatus(0);   //default value
            thisMessage.setOutgoingMessage("");
            thisMessage.setInsertedTimeStamp(today);
            thisMessage.setTTL(100);    //default value
            response = resolver.resolveHttpMessage(thisMessage);
            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                //manually error trap this error
                String iMsg = "Response returned null due to improper request.";
                response.setResponseMessage(iMsg);
            }else {
                //Manually process this response
                try {
                    JSONObject resp = new JSONObject(response.getResponse().toString());
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
                    response.setResponse(qryResponse);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response.setExceptionClass(ex.getClass());
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            //super.onPostExecute(binResponse);
            if (wsDialog != null && wsDialog.isShowing()) wsDialog.dismiss();
            alreadyFired = false;
            revertToDefaultValues();
            setMoveInProgress(false);
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActPrepareMovesOld.this.finish();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(AbstractMap.SimpleEntry.class)) {
                        /**TODO - -------------------------------------- Success ----------------------------------**/
                        AbstractMap.SimpleEntry<PartialBinMoveResponse, String> resp =
                                (AbstractMap.SimpleEntry<PartialBinMoveResponse, String>) response.getResponse();
//                        Intent i = new Intent(ActPrepareMovesOld.this, ActCheckBin.class);
//                        i.putExtra("DATA_EXTRA", resp.getKey());
//                        i.putExtra("LAST_MODE", lastScanningMode);
//                        startActivityForResult(i, RESULT_FIRST_USER);
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setTitle("Success")
                                .setMessage("Corrected Successfully !!")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        reloadActivity();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    } else { // Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage(response.getResponseMessage() + "->" + response.getResponse().toString())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        reloadActivity();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    if (response.getResponseMessage().contains("invalid product")) {
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage(response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActPrepareMovesOld.this.finish();
                                    }
                                });
                        builder.show();
                    }else {
                        Vibrator vib = (Vibrator) ActPrepareMovesOld.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMovesOld.this);
                        builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActPrepareMovesOld.this.finish();
                                    }
                                });
                        builder.show();
                    }
                    appContext.playSound(2);
                    btnScan.setEnabled(true);
                }
            }
        }
    }
}