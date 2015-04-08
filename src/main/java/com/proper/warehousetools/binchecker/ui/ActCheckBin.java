package com.proper.warehousetools.binchecker.ui;

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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import com.proper.data.binmove.PartialBinMoveResponse;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 18/03/2015.
 * Create a list of IndividualMoveLines then passed it to the server using IndividualMoveRequest wrapper
 */
public class ActCheckBin extends BaseScanActivity {
    private RadioGroup radioGroup;
    private RadioButton radSrcBin, radDstBin, radProduct;
    private Button btnEnterByHand, btnScan, btnExit;
    private EditText txtBin;
    private String scanInput;
    private BinCheckerQueryTask binQryTask;
    private PartialBinMoveResponse qryResponse = null;
    private String currentBin = "";
    private int lastScanningMode, selectedMode;
    private boolean alreadyFired = false, moveInProgress = false;
    private static final int MODE_DESTINATION = -17, MODE_SOURCE = -29, MODE_PRODUCT = -31;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public String getCurrentBin() {
        return currentBin;
    }

    public void setCurrentBin(String currentBin) {
        this.currentBin = currentBin;
    }

    public int getLastScanningMode() {
        return lastScanningMode;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binchecker_checkbin);
        radioGroup = (RadioGroup) this.findViewById(R.id.rgBCKScanMode);
        radSrcBin = (RadioButton) this.findViewById(R.id.rdBCKSrcBin);
        radDstBin = (RadioButton) this.findViewById(R.id.rdBCKDstBin);
        radProduct = (RadioButton) this.findViewById(R.id.rdBCKBarcode);
        btnScan = (Button) this.findViewById(R.id.bnBCKScan);
        btnEnterByHand = (Button) this.findViewById(R.id.bnBCKEnterBin);
        btnExit = (Button) this.findViewById(R.id.bnExitActCheckBin);
        txtBin = (EditText) this.findViewById(R.id.etxtBCKBin);
        btnScan.setOnClickListener(new View.OnClickListener() {
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
        txtBin.addTextChangedListener(new TextChanged());
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupButtonChanged(group, checkedId);
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setScanInput(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    if (!getScanInput().isEmpty() && getScanInput().length() == 5) {
                        txtBin.setText("");
                        txtBin.setText(getScanInput());
                    }else{
                        //txtBin.setText("");
                        alreadyFired = false;
                    }
                    appContext.playSound(1);
                    btnScan.setEnabled(true);
                }else {
                    alreadyFired = false;
                }
            }
        };
        effectRadioButton(selectedMode);
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
            case R.id.rdBCKSrcBin:
                setSelectedMode(MODE_SOURCE);
                break;
            case R.id.rdBCKBarcode:
                setSelectedMode(MODE_PRODUCT);
                break;
            case R.id.rdBCKDstBin:
                setSelectedMode(MODE_DESTINATION);
                break;
        }
    }

    private void buttonClicked(View v) {
        boolean isContinuous = false;   //continuous scan feature?
        int iBetween = 0;
        if (v == btnEnterByHand) {
            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtBin.isEnabled()) txtBin.setEnabled(true);
                txtBin.setText("");
                showSoftKeyboard();
            } else {
                turnOffInputByHand();
            }
        }

        if (v == btnScan) {
            if (!alreadyFired) {
                alreadyFired = true;
                btnScan.setEnabled(false);
                txtBin.requestFocus();
                if (threadStop) {
                    Log.i("Reading", "My Barcode " + readerStatus);
                    readThread = new Thread(new GetBarcode(isContinuous, iBetween));
                    readThread.setName("Single Barcode ReadThread");
                    readThread.start();
                }else {
                    threadStop = true;
                }
                btnScan.setEnabled(true);
            }
        }

        if (v == btnExit) {
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            finish();
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
                    String binCode = s.toString().trim();
                    if (binCode.length() == 5) {
                        setCurrentBin(binCode);
                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
                        if (currentUser != null) {
                            today = new java.sql.Timestamp(utilDate.getTime());
                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\", \"BinCode\":\"%s\"}",
                                    currentUser.getUserId(), currentUser.getUserCode(), binCode);

                            thisMessage.setSource(deviceIMEI);
                            thisMessage.setMessageType("CreateNewMovelist");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value
                            binQryTask = new BinCheckerQueryTask();
                            binQryTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActCheckBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckBin.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
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
        setScanInput(txtBin.getText().toString());
        if (!getScanInput().isEmpty()) {
            txtBin.setText(getScanInput()); // just to trigger text changed
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
        final String scan = "Scan";
        final String newMove = "New Move";
        if (moveInProgress) {
            btnScan.setText(scan);
        }else{
            btnScan.setText(newMove);
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
                    txtBin.requestFocus();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (binQryTask != null) {
            binQryTask.cancel(true);
            binQryTask = null;
        }
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
        alreadyFired = false;
        if (resultCode == RESULT_FIRST_USER) {
            setLastScanningMode(data.getIntExtra("PREVIOUS_MODE", -33));
//            Intent i = new Intent();
//            setResult(RESULT_OK, i);
//            finish();
        }
    }

    private class BinCheckerQueryTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            txtBin.setText("");     //empty control
            wsDialog = new ProgressDialog(ActCheckBin.this);
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
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... msg) {
            HttpResponseHelper response = null;
            Thread.currentThread().setName("StockTakeBinResponseAsyncTask");
            qryResponse = null;

            //String response = resolver.resolveMessageQuery(msg[0]);
            response = resolver.resolveHttpMessage(msg[0]);
            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                //manually error trap this error
                String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage(iMsg);
//                try {
//                    msgResponse = new JSONObject(response.getResponse().toString()).getString("Error");
//                    response.setResponseMessage(msgResponse);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                    response.setExceptionClass(ex.getClass());
//                    response.setResponseMessage(ex.getMessage());
//                }
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
            //alreadyFired = false;
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActCheckBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckBin.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActCheckBin.this.finish();
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
                        Intent i = new Intent(ActCheckBin.this, ActCheckBin.class);
                        i.putExtra("DATA_EXTRA", resp.getKey());
                        i.putExtra("LAST_MODE", lastScanningMode);
                        startActivityForResult(i, RESULT_FIRST_USER);
                    } else { // Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActCheckBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckBin.this);
                        builder.setMessage("Unable to cast result to required object [SimpleEntry<StockTakeBinResponse, String>]")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActCheckBin.this.finish();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    if (response.getResponseMessage().contains("invalid product")) {
                        Vibrator vib = (Vibrator) ActCheckBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckBin.this);
                        builder.setMessage(response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActCheckBin.this.finish();
                                    }
                                });
                        builder.show();
                    }else {
                        Vibrator vib = (Vibrator) ActCheckBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckBin.this);
                        builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActCheckBin.this.finish();
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