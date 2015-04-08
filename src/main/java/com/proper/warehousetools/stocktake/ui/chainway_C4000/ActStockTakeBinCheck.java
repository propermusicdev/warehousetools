package com.proper.warehousetools.stocktake.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.stocktake.StockTakeBinResponse;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONObject;

import java.util.AbstractMap;

/**
 * Created by Lebel on 20/01/2015.
 * Scan a bin code to return some Stock take properties
 */
//public class ActStockTakeBinCheck extends BaseFragmentActivity {
public class ActStockTakeBinCheck extends BaseScanActivity {
    private Button btnEnterBin, btnScan, btnExit;
    private EditText txtBin;
    private String scanInput;
    private StockTakeBinQueryTask binQryTask;
    private StockTakeBinResponse qryResponse = null;
    private String currentBin = "";
    private int lastScanningMode;
    private boolean alreadyFired = false;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_stocktake_bincheck);
        btnScan = (Button) this.findViewById(R.id.bnSTBScan);
        btnEnterBin = (Button) this.findViewById(R.id.bnSTBEnterBin);
        btnExit = (Button) this.findViewById(R.id.bnExitActStockTakeBinCheck);
        txtBin = (EditText) this.findViewById(R.id.etxtSTBBin);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnEnterBin.setOnClickListener(new View.OnClickListener() {
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

    private void buttonClicked(View v) {
        boolean isContinuous = false;   //continuous scan feature?
        int iBetween = 0;
        if (v == btnEnterBin) {
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
                            thisMessage.setMessageType("GetStockTakeBin");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value
                            binQryTask = new StockTakeBinQueryTask();
                            binQryTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActStockTakeBinCheck.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeBinCheck.this);
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
            btnEnterBin.setText(byHand);
        } else {
            btnEnterBin.setText(finish);
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

    private class StockTakeBinQueryTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            txtBin.setText("");     //empty control
            wsDialog = new ProgressDialog(ActStockTakeBinCheck.this);
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
            Thread.currentThread().setName("StockTakeBinResponseAsyncTask");
            qryResponse = null;
            String msgResponse = "";

            response = resolver.resolveHttpMessage(msg[0]);
            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                try {
                    msgResponse = new JSONObject(response.getResponse().toString()).getString("Error");
                    response.setResponseMessage(msgResponse);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response.setExceptionClass(ex.getClass());
                    response.setResponseMessage(ex.getMessage());
                }

            }else if(response.getResponse().toString().contains("invalid product")){
                String ymsg = "Scanner has returned some invalid products, Please contact your IT staff before skipping this bin";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            } else {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    qryResponse = new StockTakeBinResponse();
                    qryResponse = mapper.readValue(response.getResponse().toString(), StockTakeBinResponse.class);
                }catch (Exception ex) {
                    ex.printStackTrace();
                    response.setExceptionClass(ex.getClass());
                }
                response.setResponse(new AbstractMap.SimpleEntry<StockTakeBinResponse, String>(qryResponse, msgResponse));
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
                        Vibrator vib = (Vibrator) ActStockTakeBinCheck.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeBinCheck.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActStockTakeBinCheck.this.finish();
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
                        /**--------------------------- Success -------------------------**/
                        AbstractMap.SimpleEntry<StockTakeBinResponse, String> resp =
                                (AbstractMap.SimpleEntry<StockTakeBinResponse, String>) response.getResponse();
                        Intent i = new Intent(ActStockTakeBinCheck.this, ActStockTakeWorkLines.class);
                        i.putExtra("DATA_EXTRA", resp.getKey());
                        i.putExtra("LAST_MODE", lastScanningMode);
                        startActivityForResult(i, RESULT_FIRST_USER);
                    } else { // Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActStockTakeBinCheck.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeBinCheck.this);
                        builder.setMessage("Unable to cast result to required object [SimpleEntry<StockTakeBinResponse, String>]")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActStockTakeBinCheck.this.finish();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    if (response.getResponseMessage().contains("invalid product")) {
                        Vibrator vib = (Vibrator) ActStockTakeBinCheck.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeBinCheck.this);
                        builder.setMessage(response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActStockTakeBinCheck.this.finish();
                                    }
                                });
                        builder.show();
                    }else {
                        Vibrator vib = (Vibrator) ActStockTakeBinCheck.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeBinCheck.this);
                        builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent i = new Intent();
                                        setResult(RESULT_OK, i);
                                        ActStockTakeBinCheck.this.finish();
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