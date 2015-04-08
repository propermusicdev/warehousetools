package com.proper.warehousetools.goodsin.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.goodsin.BoardScanResult;
import com.proper.data.goodsin.StockHeaderResponse;
import com.proper.data.helpers.MyCustomNamingStrategy;
import com.proper.data.helpers.ResponseHelper;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by Lebel on 23/10/2014.
 */
public class ActGoodsInStockHeaderProduct extends PlainActivity {
    private Handler handler;
    private boolean threadStop = true;
    private Thread readThread;
    private TextView txtTitle;
    private TextView txtResponse;
    private EditText txtBarcode;
    private Button btnByHand;
    private Button btnScan;
    private Button btnProceed;
    private StockHeaderResponse stockHeaderResponse = new StockHeaderResponse();
    private String stockHeaderResponseString = "";
    private BoardScanResult boardScanResult =  new BoardScanResult();
    private StockHeaderTask stockHeaderTask;
    private String inputScan;
    private int inputByHand = 0;
    private ResponseHelper responseHelper = new ResponseHelper();

    public String getInputScan() {
        return inputScan;
    }

    public void setInputScan(String inputScan) {
        this.inputScan = inputScan;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsin_stockheaderproduct);
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            throw new RuntimeException("ActGoodsInStockHeaderProduct passed extras cannot be null");
        }else {
            boardScanResult = (BoardScanResult) extras.getSerializable("BOARDSCAN_EXTRA");
//            if (stockHeaderResponse == null) {
//                throw new RuntimeException("BoardScanResult extra cannot be null");
//            }
        }
        txtTitle = (TextView) this.findViewById(R.id.txtvStockHeaderProductTitle);
        txtResponse = (TextView) this.findViewById(R.id.txtvStockHeaderProductResponse);
        txtBarcode = (EditText) this.findViewById(R.id.txtStockHeaderBarcode);
        btnByHand = (Button) this.findViewById(R.id.bnStockHeaderByHand);
        btnScan = (Button) this.findViewById(R.id.bnStockHeaderProductScan);
        btnProceed = (Button) this.findViewById(R.id.bnStockHeaderProceed);

        txtBarcode.addTextChangedListener(new TextChanged(txtBarcode));
        txtBarcode.setEnabled(false);
        btnByHand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        btnProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setInputScan(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    if (!txtBarcode.getText().toString().equalsIgnoreCase("")) {
                        txtBarcode.setText("");
                        txtBarcode.setText(getInputScan());
                    }
                    else{
                        txtBarcode.setText(getInputScan());
                    }
                    appContext.playSound(1);
                    btnScan.setEnabled(true);
                }
            }
        };
    }

    private void ButtonClicked(View v) {
        if (v == btnScan) {
            //scan barcode
            IntentIntegrator integrator = new IntentIntegrator(ActGoodsInStockHeaderProduct.this);
            integrator.initiateScan();
        }
        if (v == btnProceed) {
            // Not yet implemented
            if (stockHeaderResponse != null && stockHeaderResponse.getProducts() != null && stockHeaderResponse.getStockHeader() != null) {
                //We will send the pictures to the websericce
                thisMessage = new Message();
            }else {
                String mMsg = "Unable to proceed\nPlease scan a valid product belong to this goods-in";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInStockHeaderProduct.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
        }
        if (v == btnByHand) {
            if (inputByHand == 0) {
                turnOnInputByHand();
                if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
                txtBarcode.setText("");
                showSoftKeyboard();
            } else {
                turnOffInputByHand();
            }
        }
    }

    private void handleScanResult(String contents) {
        //Scan Goods that comes in through the door
        if (inputByHand == 0) {
            if (!contents.isEmpty()) {
                currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
                if (currentUser != null) {
                    today = new java.sql.Timestamp(utilDate.getTime());
                    String msg = String.format("{\"UserCode\":\"%s\", \"UserId\":\"%s\", \"StockHeaderId\":\"%s\", \"Barcode\":\"%s\"}",
                            currentUser.getUserCode(), currentUser.getUserId(), boardScanResult.getGoodsIn().getStockHeaderId(), contents);

                    thisMessage.setSource(deviceIMEI);
                    thisMessage.setMessageType("BarcodeOnStockHeader");
                    thisMessage.setIncomingStatus(1); //default value
                    thisMessage.setIncomingMessage(msg);
                    thisMessage.setOutgoingStatus(0);   //default value
                    thisMessage.setOutgoingMessage("");
                    thisMessage.setInsertedTimeStamp(today);
                    thisMessage.setTTL(100);    //default value
                    stockHeaderTask = new StockHeaderTask();
                    stockHeaderTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                } else {
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) ActGoodsInStockHeaderProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInStockHeaderProduct.this);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInStockHeaderProduct.this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            boolean bContinuous = true;
            int iBetween = 0;
            btnScan.setEnabled(false);
            txtBarcode.requestFocus();
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
                    if (view == txtBarcode) {
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
        setInputScan(txtBarcode.getText().toString());
        if (!getInputScan().isEmpty()) {
            txtBarcode.setText(getInputScan()); // just to trigger text changed
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
        android.os.Message msg = null;

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

                Log.i("MY", "barCode " + barCode.trim());
                msg = new android.os.Message();

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

    private class StockHeaderTask extends AsyncTask<Message, Void, StockHeaderResponse> {
        private ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActGoodsInStockHeaderProduct.this);
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
        protected StockHeaderResponse doInBackground(Message... msg) {
            Thread.currentThread().setName("MyBarcodeOnStockHeaderTask");
            try {
                String response = resolver.resolveMessageQuery(msg[0]);
                response = responseHelper.refineOutgoingMessage(response);
                if (response.contains("No products found")) {
                    //manually error trap this error
                    stockHeaderResponse = null;
                    String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActGoodsInStockHeaderProduct - StockHeaderTask - Line:354", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                    logger.log(log);
                    throw new RuntimeException("The bin you have scanned have not been recognised. Please check and scan again");
                }else {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.setPropertyNamingStrategy(new MyCustomNamingStrategy());
                    stockHeaderResponse = mapper.readValue(response, StockHeaderResponse.class);
                    stockHeaderResponseString = response;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenSelectProduct - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                logger.log(log);
            }
            return stockHeaderResponse;
        }

        @Override
        protected void onPostExecute(StockHeaderResponse response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            if (response != null && response.getProducts() != null && response.getStockHeader() != null) {
                //do indent json
                //JsonParser parser = new JsonParser();
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String jsonOutput = gson.toJson(response);
                txtResponse.setText(jsonOutput);
            }else {
                // alert that something went wrong
                String mMsg = "Unable to retrieve data from web service\nPlease contact IT if error continues";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInStockHeaderProduct.this);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
            }
        }
    }
}