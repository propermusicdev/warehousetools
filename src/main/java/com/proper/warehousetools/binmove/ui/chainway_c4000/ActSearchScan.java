package com.proper.warehousetools.binmove.ui.chainway_c4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.*;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.data.binmove.*;
import com.proper.data.binmove.adapters.BarcodeResponseAdapter;
import com.proper.data.binmove.adapters.ProductBinAdapterOptimized;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.MyCustomNamingStrategy;
import com.proper.data.helpers.ResponseHelper;
//import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.BaseFragmentActivity;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by lebel on 09/04/2015.
 */
public class ActSearchScan extends BaseFragmentActivity {
    private LinearLayout lytHeader;
    private TextView txtHeader;
    private Button btnExit, btnGo;
    private EditText txtEAN;
    private ViewFlipper flipper;
    private ListView lvResult;
    private ExpandableListView lvxResult;
    private long startTime;
    //Search View
    private ResponseHelper responseHelper = new ResponseHelper();
    private WebServiceTask wsTask;
    private static final String TAG = "ActQueryScan";
    private String currentBarcode = "";
    private String currentBincode = "";
    private boolean alreadyFired = false;
    //Result View
    private List<ProductResponse> productsList = new ArrayList<ProductResponse>();
    private List<Bin> binList = new ArrayList<Bin>();
    private ProductResponse currentProduct = new ProductResponse();
    private Bin currentBin = new Bin();
    private BinResponse binResponse = null;
    private BarcodeResponse barcodeResponse = null;
    private ProductBinAdapterOptimized binAdapter = null;
    private BarcodeResponseAdapter barcodeAdapter = null;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_searchscan);

        //controls
        lytHeader = (LinearLayout) this.findViewById(R.id.lytBMSSInnerTop);
        txtHeader = (TextView) this.findViewById(R.id.txtBMSSHeader_qryview);
        txtEAN = (EditText) this.findViewById(R.id.etxtBMSSEAN);
        flipper = (ViewFlipper) this.findViewById(R.id.vfBMSSSearchResult);
        btnGo = (Button) this.findViewById(R.id.bnBMSSSearch);
        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnExit = (Button) this.findViewById(R.id.bnExitActSearchScan);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        lvResult = (ListView) this.findViewById(R.id.qryBMSSListView);
        lvxResult = (ExpandableListView) this.findViewById(R.id.qryXpBMSSListView);
        txtEAN.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtEAN.addTextChangedListener(new TextChanged());

//        LayoutInflater inflater = (LayoutInflater) this
//                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        Bundle extras = getIntent().getExtras();
//        NAV_INSTRUCTION = extras.getInt("INSTRUCTION_EXTRA");
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if (msg.what == 1) {
                        setScanInput(msg.obj.toString());
                        int acceptable[] = {12,13,14};
                        /**determine which method we're scanning for based on length of string**/
                        if (scanInput.length() == 5) {
                            /** Bin code **/
                            NAV_INSTRUCTION = R.integer.ACTION_BINQUERY;
                            if (!getScanInput().isEmpty()) {
                                txtEAN.setText("");     //to counter a weird bug in editText control
                                txtEAN.setText(getScanInput());
                                appContext.playSound(1);
                            } else {
                                //txtEAN.setText(getScanInput());
                                txtEAN.setText("");
                                appContext.playSound(2);
                            }
                        }else if (getScanInput().length() > 5 && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                            /** Barcode **/
                            NAV_INSTRUCTION = R.integer.ACTION_BARCODEQUERY;
                            if (!getScanInput().isEmpty()) {
                                txtEAN.setText("");     //to counter a weird bug in editText control
                                txtEAN.setText(getScanInput());
                                appContext.playSound(1);
                            } else {
                                //txtEAN.setText(getScanInput());
                                txtEAN.setText("");
                                appContext.playSound(2);
                            }
                        } else {
                            Log.e("A bad scan has occurred", "Please scan again");
                            appContext.playSound(2);
                            String mMsg = "Bad scan occurred \nThis bin code is invalid";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActSearchScan.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                            refreshActivity();
                            //unLockBinControls();
                            txtEAN.setText("");
                        }
                    } else{
                        alreadyFired = false;
                    }
                }
            }
        };
    }

    private void updateControlsAfterScan() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                //  load QueryView with BinQuery properties
                if (binResponse != null && !binResponse.getProducts().isEmpty()) {
                    binAdapter = new ProductBinAdapterOptimized(ActSearchScan.this, binResponse.getProducts(), deviceIMEI);
                    if (lytHeader.getVisibility() != View.GONE) lytHeader.setVisibility(View.GONE);
                    if (lvxResult.getVisibility() != View.GONE) lvxResult.setVisibility(View.GONE);
                    if (lvResult.getVisibility() != View.VISIBLE) lvResult.setVisibility(View.VISIBLE);
                    if (binResponse.getProducts().size() > 1) {
                        this.setTitle(binResponse.getProducts().get(0).getTitle().isEmpty() ?
                                binResponse.getProducts().get(0).getTitle() : binResponse.getProducts().get(1).getTitle());
                    } else {
                        this.setTitle(binResponse.getProducts().get(0).getTitle());
                    }
                    txtHeader.setText(String.format("Artist: %s", binResponse.getProducts().get(0).getArtist() != null ?
                            binResponse.getProducts().get(0).getArtist() : binResponse.getProducts().get(1).getArtist()));
                    txtHeader.setTypeface(null, Typeface.BOLD);
                    lvResult.setAdapter(binAdapter);
                    flipper.setDisplayedChild(1);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                //  load QueryView with BarcodeQuery properties
                if (barcodeResponse != null && !barcodeResponse.getProducts().isEmpty()) {
                    barcodeAdapter = new BarcodeResponseAdapter(this, barcodeResponse);
                    txtHeader.setText(String.format("Artist: %s", barcodeResponse.getProducts().get(0).getArtist() != null ?
                            barcodeResponse.getProducts().get(0).getArtist() : barcodeResponse.getProducts().get(1).getArtist()));
                    txtHeader.setTypeface(null, Typeface.BOLD);
                    if (barcodeResponse.getProducts().size() > 1) {
                        this.setTitle(!barcodeResponse.getProducts().get(0).getTitle().isEmpty() ?
                                barcodeResponse.getProducts().get(0).getTitle() : barcodeResponse.getProducts().get(1).getTitle());
                    } else {
                        this.setTitle(barcodeResponse.getProducts().get(0).getTitle());
                    }
                    if (lvResult.getVisibility() != View.GONE) lvResult.setVisibility(View.GONE);
                    if (lvxResult.getVisibility() != View.VISIBLE) lvxResult.setVisibility(View.VISIBLE);
                    lvxResult.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                    lvxResult.setAdapter(barcodeAdapter);
                    lvxResult.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView expandableListView, View view, int pos, long id) {
                            expandableListView.setItemChecked(pos, true);
                            productsList = barcodeResponse.getProducts();
                            currentProduct = productsList.get(pos);
                            binList = barcodeResponse.getProducts().get(pos).getBins();
                            return false;
                        }
                    });
                    lvxResult.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long id) {
                            view.setSelected(true);
                            currentBin = barcodeResponse.getProducts().get(i).getBins().get(i2);
                            return false;
                        }
                    });
                    flipper.setDisplayedChild(1);
                    lvxResult.expandGroup(0);    // Expand the first item
                }
                break;
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
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
                    setScanInput(txtEAN.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtEAN.setText(getScanInput());     // just to trigger text changed
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
                    setScanInput(txtEAN.getText().toString());
                    if (!getScanInput().isEmpty()) {
                        fullTurnCount ++;
                        txtEAN.setText(getScanInput());     // just to trigger text changed
                    }
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
        //this.btnGo.setEnabled(false);
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Go";
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                if (inputByHand == 0) {
                    btnGo.setText(byHand);
                } else {
                    btnGo.setText(finish);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                if (inputByHand == 0) {
                    btnGo.setText(byHand);
                } else {
                    btnGo.setText(finish);
                }
                break;
        }
    }

    private void PaintButtonText() {
        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BARCODEQUERY:
                btnGo.setText(R.string.but_startbarcode);
                btnGo.setBackgroundResource(R.drawable.button_blue);
                break;
            case R.integer.ACTION_BINQUERY:
                btnGo.setText(R.string.but_startbin);
                btnGo.setBackgroundResource(R.drawable.button_yellow);
                break;
        }
    }

    private void refreshActivity() {
        if (!txtEAN.isEnabled()) txtEAN.setEnabled(true);
//        paintByHandButtons();
//        PaintButtonText();
    }

    private void buttonClicked(View v) {
        boolean bContinuous = false;
        int iBetween = 0;
        if (v == btnGo) {
            String edited = txtEAN.getText().toString();
            Message msg = new Message();

            if (edited == null || edited.isEmpty()) {
                msg.what = 0;
                msg.obj = "";
            } else {
                msg.what = 1;
                msg.obj = edited;
            }

            handler.sendMessage(msg);
        }
        if (v == btnExit) {
            Intent i = new Intent();
            setResult(RESULT_FIRST_USER, i);
            finish();
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
                if (s != null && !s.toString().isEmpty()) {
                    String eanCode = s.toString().trim();

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
                                        Vibrator vib = (Vibrator) ActSearchScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActSearchScan.this);
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
                                        new AlertDialog.Builder(ActSearchScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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
                                        Vibrator vib = (Vibrator) ActSearchScan.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "User not Authenticated \nPlease login";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActSearchScan.this);
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
                                        new AlertDialog.Builder(ActSearchScan.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_BIN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                refreshActivity();
                                            }
                                        }).show();
                                    }
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
                    fullTurnCount = 0;      //set to default if it's not so already
                    if (!txtEAN.isEnabled()) txtEAN.setEnabled(true);
                    btnGo.setEnabled(false);
                    txtEAN.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "My Search " + readerStatus);
                        readThread = new Thread(new GetBarcode(continuous, intervals));
                        readThread.setName("Search ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                    btnGo.setEnabled(true);
                    fullTurnCount ++;
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
        ActSearchScan.this.finish();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isBarcodeOpened) {
            if (readThread != null && readThread.isInterrupted() == false) {
                readThread.interrupt();
            }
            mInstance.close();
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

    /*************************** Query Look up *************************/
    private class WebServiceTask extends AsyncTask<String, Void, Object> {
        protected ProgressDialog xDialog;
        private String originalEAN = "";

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActSearchScan.this);
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
        protected Object doInBackground(String... input) {
            int instruction = Integer.parseInt(input[0]);
            String barcode = "";
            String bincode = "";
            String msg = "";
            originalEAN = barcode.toString().trim();
            today = new java.sql.Timestamp(utilDate.getTime());
            thisMessage.setSource(deviceIMEI);
            thisMessage.setIncomingStatus(1); //default value
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
            return retObject;
        }

        @Override
        protected void onPostExecute(Object responseObject) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            alreadyFired = false;
            switch (NAV_INSTRUCTION) {
                case R.integer.ACTION_BARCODEQUERY:
                    barcodeResponse = (BarcodeResponse) responseObject;
                    updateControlsAfterScan();
                    break;
                case R.integer.ACTION_BINQUERY:
                    binResponse = (BinResponse) responseObject;
                    updateControlsAfterScan();
                    break;
            }
            //Finally we restore the default nav turn
            //refreshActivity();
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