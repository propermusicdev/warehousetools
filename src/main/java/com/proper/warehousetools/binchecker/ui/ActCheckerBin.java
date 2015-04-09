package com.proper.warehousetools.binchecker.ui;

import android.app.Activity;
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
import com.proper.data.binmove.BinResponse;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenSelectProduct;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.Date;

/**
 * Created by lebel on 07/04/2015.
 */
public class ActCheckerBin extends BaseScanActivity {
    private EditText mReception;
    private TextView txtInto;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterBinCode;
    private WebServiceTask wsTask;
    private boolean alreadyFired = false;
    private int previousScanMode = -33;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
//        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
        getSupportActionBar().setTitle("Scan Bin");
        setContentView(R.layout.lyt_binchecker_checkbin1);
        txtInto = (TextView) this.findViewById(R.id.txtvBCKCScanIntro);
        btnScan = (Button) this.findViewById(R.id.bnBCKCScanPerformScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActCheckerBin);
        btnEnterBinCode = (Button) this.findViewById(R.id.bnBCKCEnterBin);
        mReception = (EditText) this.findViewById(R.id.etxtBCKCBinCode);

        mReception.addTextChangedListener(new TextChanged());
        mReception.setEnabled(false);                   ///  Disable it upon initiation
        btnScan.setOnClickListener(new ClickEvent());
        btnExit.setOnClickListener(new ClickEvent());
        btnEnterBinCode.setOnClickListener(new ClickEvent());

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setScanInput(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    if (!getScanInput().isEmpty()) {
                        alreadyFired = true;
                        mReception.setText("");
                        mReception.setText(getScanInput());
                        appContext.playSound(1);
                    }
                    else{
                        mReception.setText("");
                        appContext.playSound(2);
                        alreadyFired = false;
                    }
                    btnScan.setEnabled(true);
                } else{
                    alreadyFired = false;
                }
            }
        };
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
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 0) {
            mReception.setText("");  //clear the textbox
            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
                wsTask.cancel(true);
            }
            refreshActivity();
        }
        if (resultCode == RESULT_FIRST_USER) {
            previousScanMode = data.getIntExtra("PREVIOUS_MODE", -33);
            mReception.setText("");  //clear the textbox
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
                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
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

                            if (wsTask != null) {
                                wsTask.cancel(true);
                            }
                            wsTask = new WebServiceTask();
                            wsTask.execute(thisMessage);
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActCheckerBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckerBin.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    } else {
                        new AlertDialog.Builder(ActCheckerBin.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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
            boolean bContinuous = false;
            int iBetween = 0;
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
                finish();
            }
            else if(v == btnScan)
            {
                btnScan.setEnabled(false);
                mReception.requestFocus();
                if (threadStop) {
                    Log.i("Reading", "My Barcode " + readerStatus);
                    readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                    readThread.setName("Single Barcode ReadThread");
                    readThread.start();
                }else {
                    threadStop = true;
                }
                btnScan.setEnabled(true);
            } else if(v == btnEnterBinCode) {
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    if (!mReception.isEnabled()) mReception.setEnabled(true);
                    mReception.setText("");
                    showSoftKeyboard();
                } else {
                    turnOffInputByHand();
                    if (mReception.isEnabled()) mReception.setEnabled(false);
                    //mReception.removeTextChangedListener();
                }
            }
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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

    public static void killApp(boolean killSafely) {
        if (killSafely) {
            /*
             * Alternatively the process that runs the virtual machine could be
             * abruptly killed. This is the quickest way to remove the app from
             * the device but it could cause problems since resources will not
             * be finalized first. For example, all threads running under the
             * process will be abruptly killed when the process is abruptly
             * killed. If one of those threads was making multiple related
             * changes to the database, then it may have committed some of those
             * changes but not all of those changes when it was abruptly killed.
             */
            android.os.Process.killProcess(android.os.Process.myPid());
        }

    }

    public void refreshActivity() {
        if (!mReception.getText().toString().equalsIgnoreCase("")) mReception.setText("");
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                if (!alreadyFired) {
                    alreadyFired = true;
                    boolean bContinuous = false;
                    int iBetween = 0;
                    mReception.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "My Barcode " + readerStatus);
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("Single Barcode ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
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
        //super.onBackPressed();
        finish();
    }

    private class WebServiceTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper>{
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActCheckerBin.this);
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
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... msg) {
            HttpResponseHelper response = null;
            try {
                //String response = resolver.resolveMessageQuery(thisMessage);
                response = resolver.resolveHttpMessage(thisMessage);
                response.setResponse(responseHelper.refineResponse(response.getResponse().toString()));
                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                if (response.getResponse().toString().contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object returns null due to improper request.";
                    response.setResponseMessage(iMsg);
                }else {
                    BinResponse ret = new BinResponse();
                    ObjectMapper mapper = new ObjectMapper();
                    ret = mapper.readValue(response.getResponse().toString(), BinResponse.class);
                    response.setResponse(ret);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setExceptionClass(ex.getClass());
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            alreadyFired = false;

            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActCheckerBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckerBin.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(BinResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        BinResponse resp = (BinResponse) response.getResponse();
                        Intent i = new Intent(ActCheckerBin.this, ActPrepareMoves.class);
                        i.putExtra("RESPONSE_EXTRA", resp);
                        i.putExtra("LAST_MODE", previousScanMode);
                        startActivityForResult(i, RESULT_OK);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActCheckerBin.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActCheckerBin.this);
                    builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                    btnScan.setEnabled(true);
                }
            }
            refreshActivity();
        }

        @Override
        protected void onCancelled() {
            mReception.setText("");
        }
    }
}