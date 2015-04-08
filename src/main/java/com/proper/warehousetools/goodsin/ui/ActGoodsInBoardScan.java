package com.proper.warehousetools.goodsin.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.goodsin.BoardScanResult;
import com.proper.data.helpers.ResponseHelper;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 14/10/2014.
 */
public class ActGoodsInBoardScan extends PlainActivity {
    private static int SCANREQUEST = 49374;
    private TextView txtIntro, lblGoodsInID;
    private EditText txtGoodsInID;
    private Button btnByHand, btnScan, btnExit;
    private Thread readThread;
    private Handler handler;
    private boolean threadStop = true;
    private GoodsIncomingTask incomingTask;
    private String inputScan;
    private int inputByHand = 0;
    private ResponseHelper responseHelper;

    public String getInputScan() {
        return inputScan;
    }

    public void setInputScan(String inputScan) {
        this.inputScan = inputScan;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsin_boardscan);
        responseHelper = new ResponseHelper();
        txtIntro = (TextView) this.findViewById(R.id.txtGoodsInBoardScanIntro);
        lblGoodsInID = (TextView) this.findViewById(R.id.lblGoodsInBoardScanBoardID);
        txtGoodsInID = (EditText) this.findViewById(R.id.txtGoodsInBoardScanBoardID);
        txtGoodsInID.addTextChangedListener(new TextChanged(this.txtGoodsInID));
        btnScan = (Button) this.findViewById(R.id.bnGoodsInBoardScan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button_Clicked(v);
            }
        });
        btnByHand = (Button) this.findViewById(R.id.bnGoodsInBoardScanByHand);
        btnByHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button_Clicked(v);
            }
        });
        btnExit = (Button) this.findViewById(R.id.bnExitActGoodsInBoardScan);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Button_Clicked(v);
            }
        });
        inputByHand = 0;

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setInputScan(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    if (!txtGoodsInID.getText().toString().equalsIgnoreCase("")) {
                        txtGoodsInID.setText("");
                        txtGoodsInID.setText(getInputScan());
                    }
                    else{
                        txtGoodsInID.setText(getInputScan());
                    }
                    appContext.playSound(1);
                    btnScan.setEnabled(true);
                }
            }
        };
    }

    private void Button_Clicked(View v) {
        if (v == btnExit) {
            Intent i = new Intent();
            setResult(RESULT_OK, i);
            this.finish();
        }
        if (v == btnScan) {
            //initiate scan
            IntentIntegrator integrator = new IntentIntegrator(ActGoodsInBoardScan.this);
            integrator.addExtra("REQUEST", SCANREQUEST);
            integrator.initiateScan();
        }
        if (v == btnByHand) {
            //do

            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtGoodsInID.isEnabled()) txtGoodsInID.setEnabled(true);
                txtGoodsInID.setText("");
                showSoftKeyboard();
            } else {
                turnOffInputByHand();
            }
        }
    }

    private void handleScanResult(String contents) {
        //Scan Goods that comes in through the door
        if (!contents.trim().equalsIgnoreCase("")) {
            if (!contents.isEmpty()) {
                currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
                if (currentUser != null) {
                    today = new java.sql.Timestamp(utilDate.getTime());
                    String msg = String.format("{\"UserCode\":\"%s\", \"UserId\":\"%s\", \"GoodsInBoardId\":\"%s\"}",
                            currentUser.getUserCode(), currentUser.getUserId(), contents);

                    thisMessage.setSource(deviceIMEI);
                    thisMessage.setMessageType("GoodsInBoardScan");
                    thisMessage.setIncomingStatus(1); //default value
                    thisMessage.setIncomingMessage(msg);
                    thisMessage.setOutgoingStatus(0);   //default value
                    thisMessage.setOutgoingMessage("");
                    thisMessage.setInsertedTimeStamp(today);
                    thisMessage.setTTL(100);    //default value
                    incomingTask = new GoodsIncomingTask();
                    incomingTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                } else {
                appContext.playSound(2);
                Vibrator vib = (Vibrator) ActGoodsInBoardScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                vib.vibrate(2000);
                String mMsg = "User not Authenticated \nPlease login";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInBoardScan.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
            }else {
                //report that a miss-scan has occurred
                String mMsg = "Bad Scan Occurred\nPlease try again";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInBoardScan.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        } else {
            //Don't report anything || report that the scan was invalid
            String mMsg = "Unable to scan barcode\nPlease contact IT if error continues";
            AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInBoardScan.this);
            builder.setMessage(mMsg)
                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do nothing
                        }
                    });
            builder.show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
//        if (resultCode == R.integer.ACTION_PHOTO_SENT) {
//            //do get data
//            Bundle extras = intent.getExtras();
//            String photoAbsolutePath = "";
//            if (extras == null) {
//                throw new RuntimeException("Result Intent's extras should not be null");
//            } else {
//                photoAbsolutePath = extras.getString("PHOTOLOCATION_EXTRA");
//                if (photoAbsolutePath.isEmpty()) {
//                    throw new RuntimeException("Result Intent's extras should not be null");
//                }
//            }
//            photoAbsolutePath = intent.getExtras().getString("PHOTOLOCATION_EXTRA");
//            Intent i = new Intent(ActGoodsInBoardScan.this, ActGoodsInStockHeaderProduct.class);
//        } else {
//            //finished scan now handle content
//            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//            if (scanResult != null) {
//                boolean bContinuous = true;
//                int iBetween = 0;
//                btnScan.setEnabled(false);
//                txtGoodsInID.requestFocus();
//                if (threadStop) {
//                    //Log.i("Reading", "My Barcode " + readerStatus);
//                    readThread = new Thread(new Retrieve(scanResult.getContents(), bContinuous, iBetween));
//                    readThread.setName("Single Barcode ReadThread");
//                    readThread.start();
//                } else {
//                    threadStop = true;
//                }
//                btnScan.setEnabled(true);
//            }
//        }
        //finished scan now handle content
        if (requestCode == SCANREQUEST) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                boolean bContinuous = true;
                int iBetween = 0;
                btnScan.setEnabled(false);
                txtGoodsInID.requestFocus();
                if (threadStop) {
                    //Log.i("Reading", "My Barcode " + readerStatus);
                    readThread = new Thread(new Retrieve(scanResult.getContents(), bContinuous, iBetween));
                    readThread.setName("Single Barcode ReadThread");
                    readThread.start();
                } else {
                    threadStop = true;
                }
                btnScan.setEnabled(true);
            }
        } else {
            if (resultCode == RESULT_OK) {
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                ActGoodsInBoardScan.this.finish();
            }
        }
    }

    private class TextChanged implements TextWatcher {
        private View view;

        private TextChanged(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && !s.toString().trim().equalsIgnoreCase("")) {
                if (inputByHand == 0) {
                    if (view == txtGoodsInID) {
                        String value = s.toString();
                        setInputScan(value);
                        handleScanResult(value);        // look up
                    }
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
        setInputScan(txtGoodsInID.getText().toString());
        if (!getInputScan().isEmpty()) {
            txtGoodsInID.setText(getInputScan()); // just to trigger text changed
        }
        paintByHandButtons();
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        if (inputByHand == 0) {
            btnByHand.setText(byHand);
        } else {
            btnByHand.setText(finish);
        }
    }

    private class Retrieve implements Runnable {

        private boolean isContinuous = false;
        String barCode = "";
        private long sleepTime = 1000;
        Message msg = null;

        public Retrieve(String barcode, boolean isContinuous) {
            this.isContinuous = isContinuous;
            this.barCode = barcode;
        }

        public Retrieve(String barcode, boolean isContinuous, int sleep) {
            this.barCode = barcode;
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
        }

        @Override
        public void run() {

            do {
                msg = new Message();

                if (barCode == null || barCode.isEmpty()) {
                    Log.i("MY", "Barcode was not clear to read");
                    msg.what = 0;
                    msg.obj = "";
                } else {
                    Log.i("MY", "Barcode scanned: " + barCode.trim());
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

    private class GoodsIncomingTask extends AsyncTask<com.proper.messagequeue.Message, Integer, BoardScanResult> {
        private ProgressDialog wsDialog;
        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActGoodsInBoardScan.this);
            CharSequence message = "Working hard... contacting webservice...";
            CharSequence title = "Please Wait";
            wsDialog.setCancelable(true);
            wsDialog.setCanceledOnTouchOutside(false);
            wsDialog.setMessage(message);
            wsDialog.setTitle(title);
            wsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            wsDialog.show();
        }

        @Override
        protected BoardScanResult doInBackground(com.proper.messagequeue.Message... msg) {
            BoardScanResult qryResponse = new BoardScanResult();
            try {
                String response = resolver.resolveMessageQuery(msg[0]);
                //String response = testResolver.resolveBoardScan();        // test
                if (response != null && !response.equalsIgnoreCase("")) {
                    if (response.contains("not recognised") || response.contains("Error")) {
                        //manually error trap this error
                        qryResponse = null;
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        today = new java.sql.Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInBoardScan - queryTask - Line:1192", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                        logger.log(log);
                        throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
                    }else {
                        qryResponse = responseHelper.refineBoardScanResult(response);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInBoardScan - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                logger.log(log);
                qryResponse = null;
            }
            return qryResponse;
        }

        @Override
        protected void onPostExecute(BoardScanResult result) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            if (result != null) {
                //Intent i = new Intent(ActGoodsInBoardScan.this, zzActGoodsInInfoDisplay.class);
                Intent i = new Intent(ActGoodsInBoardScan.this, ActGoodsInReceive.class);
                i.putExtra("BOARDSCAN_EXTRA", result);
                startActivityForResult(i, RESULT_OK);
            }
        }
    }
}