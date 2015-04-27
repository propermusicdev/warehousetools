package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ListView;
import android.widget.TextView;
import com.proper.data.binmove.*;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.helpers.MyCustomNamingStrategy;
import com.proper.data.replen.ReplenMiniMove;
import com.proper.data.replen.adapters.ReplenMiniMoveAdapter;
import com.proper.utils.BinQuantitySorted;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.ActReplenManager;
import org.apache.commons.collections4.IteratorUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.util.*;

/**
 * Created by Lebel on 04/09/2014.
 */
public class ActReplenSelectProduct extends BaseScanActivity {
    private BinResponse binResponse = new BinResponse();
    private BarcodeResponse bcResponse = null;
    private ProductResponse thisProduct = new ProductResponse();
    private List<ProductBinResponse> foundList =  new ArrayList<ProductBinResponse>();
    protected List<ProductResponse> responseList =  new ArrayList<ProductResponse>();
    private EditText mReception;
    private TextView txtPalate;
    private TextView txtInto;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterBinCode;
    private WebServiceProductTask productAsync;
    private WebServiceBinTask binAsync;
    private boolean qtyChanged;
    private boolean alreadyFired = false;

    /** From ActReplenManager **/
    private SharedPreferences prefs = null;
    private ListView lvRepelen;
    private List<ProductBinResponse> inputList;
    private List<ReplenMiniMove> moveList = new ArrayList<ReplenMiniMove>();
    private ReplenMiniMoveAdapter adapter;
    private List<Bin> primaryList = new ArrayList<Bin>(), populatedBins = new ArrayList<Bin>();
    private String currentSource = "";
    private int tot = 0;
    private int backParam = 0;
    //private boolean qtyChanged = false;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_selectproduct);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
        getSupportActionBar().setTitle("Scan Product");

        Bundle extra = getIntent().getExtras();
        if (extra == null) throw new RuntimeException("onCreate: Bundled Extra cannot be null!, Line: 63");
        binResponse = (BinResponse) extra.getSerializable("RESPONSE_EXTRA");
        prefs = getSharedPreferences("Proper_Replen", Context.MODE_PRIVATE);        //get preferences

        txtInto = (TextView) this.findViewById(R.id.txtvReplenScanProductIntro);
        txtPalate = (TextView) this.findViewById(R.id.txtvReplenScanProductPalate);
        btnScan = (Button) this.findViewById(R.id.bnReplenScanProductPerformScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActReplenScanProduct);
        btnEnterBinCode = (Button) this.findViewById(R.id.bnEnterBinReplenSelectProduct);
        mReception = (EditText) this.findViewById(R.id.etxtReplenScanProductBarcode);

        mReception.addTextChangedListener(new TextChanged());
        mReception.setEnabled(false);                   ///  Disable it upon initiation
        btnScan.setOnClickListener(new ClickEvent());
        btnExit.setOnClickListener(new ClickEvent());
        btnEnterBinCode.setOnClickListener(new ClickEvent());
        txtPalate.setText(binResponse.getRequestedBinCode());

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setScanInput(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    //if (!mReception.getText().toString().equalsIgnoreCase("")) {
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
                }else {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (isBarcodeOpened) {
            mInstance.close();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (productAsync != null) {
            productAsync.cancel(true);
            productAsync = null;
        }
        if (binAsync != null) {
            binAsync.cancel(true);
            binAsync = null;
        }
        if (resultCode == 0) {
            mReception.setText("");  //clear the textbox
            refreshActivity();
        }
        if (resultCode == RESULT_OK) {
            mReception.setText("");  //clear the textbox
            qtyChanged = data.getBooleanExtra("QTYCHANGED_EXTRA", false);
            if (qtyChanged) {
                binAsync = new WebServiceBinTask();
                binAsync.execute(data.getStringExtra("BIN_EXTRA"));
            }
            refreshActivity();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
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
        ActReplenSelectProduct.this.finish();
    }

    private void saveQuantityData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("From", currentSource);
        editor.putInt("Quantity", tot);
        editor.commit();
    }

    private void buildPrimaryLocations() {
        //TODO - Fix this method when you can find some time
        if (!adapter.isEmpty()) {
            //List<Bin> bins = adapter.getAllBins();
            //List<Bin> bins = moveList;
            List<Bin> bins = new ArrayList<Bin>();
            List<ReplenMiniMove> newMoveList = new ArrayList<ReplenMiniMove>();
            List<ReplenMiniMove> newMoveListPrep = new ArrayList<ReplenMiniMove>();
            List<ReplenMiniMove> moveSearch = new ArrayList<ReplenMiniMove>();
            List<ReplenMiniMove> moveDup = new ArrayList<ReplenMiniMove>();
            ListIterator<ReplenMiniMove> moveIterator = moveList.listIterator();
            // ListIterator<AbstractMap.SimpleEntry<String, int[]>> refined = new ListIterator<AbstractMap.SimpleEntry<String, int[]>>().set();
            List<AbstractMap.SimpleEntry<String, int[]>> refined = null;

            int found = 0;
            for (ReplenMiniMove move : moveList) {
                if (moveSearch.isEmpty()) {
                    moveSearch.add(move);
                } else {
                    if (!moveSearch.isEmpty()) {
                        for (int i = 0; i < moveSearch.size(); i++) {
                            ReplenMiniMove xMove = moveSearch.get(i);
                            if (move.getDestination().equalsIgnoreCase(xMove.getDestination())) {
                                //Find duplicates ....
                                found++;
                                moveDup.add(xMove);
                            }
                        }
                    }
                }
            }

            if (moveList.size() > 1) {
                for (ReplenMiniMove move : moveList) {
                    while(moveIterator.hasNext()){
                        ReplenMiniMove item = moveIterator.next();
                        if(item.getDestination().equals(move.getDestination())){
                            //moveIterator.set(new ReplenMiniMove(move.getDestination(), moveIterator.next().getQuantity() + move.getQuantity()));
                            item.setQuantity(item.getQuantity() + move.getQuantity());
                            moveIterator.set(item);
                        }
                    }
                }
            }


//            for (ReplenMiniMove move : moveList) {
//                refined = new ArrayList<AbstractMap.SimpleEntry<String, int[]>>();
//                if (refined.isEmpty()) {
//                    refined.add(new AbstractMap.SimpleEntry<String, int[]>(move.getDestination(), new int[move.getQuantity()]));
//                }else {
//                    for (int p = 0; p < refined.size(); p ++) {
//                        AbstractMap.SimpleEntry<String, int[]> item = refined.get(p);
//                        if (item.getKey().equalsIgnoreCase(move.getDestination())) {
//                            //add
//                        }
//                    }
//                }
//            }
//            for (ReplenMiniMove move : moveList) {
//                //check for duplicates, if list contains item just add qty  -- !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)
//                if (moveDup.contains(move)) {
//                    int qty = 0;
//                    for (int j = 0; j < moveDup.size(); j++) {
//                        qty = moveDup.get(j).getQuantity() + qty;
//                    }
//                    newMoveListPrep.add(new ReplenMiniMove(move.getDestination(), qty));
//                }
//
//
//
//                if (newMoveList.isEmpty()) {
//                    newMoveList.add(move);
//                    newMoveListPrep.add(move);
//                } else {
//                    for (int i = 0; i < newMoveList.size(); i++) {
//                        ReplenMiniMove imove = newMoveList.get(i);
//                        //check if is in the duplicate list
//                        if (moveDup.contains(imove)) {
//                            //Add the quantity
//                            int qty = 0;
//                            for (int j = 0; j < moveDup.size(); j++) {
//                                qty = moveDup.get(j).getQuantity() + qty;
//                            }
//                            newMoveListPrep.add(new ReplenMiniMove(imove.getDestination(), qty));
//                        } else {
//                            newMoveListPrep.add(imove);
//                        }
////                        if (imove.getDestination().equalsIgnoreCase(move.getDestination())) {
////                            //newMoveList.add(new ReplenMiniMove(move.getDestination(), move.getQuantity() + imove.getQuantity()));
////                            newMoveListPrep.add(new ReplenMiniMove(move.getDestination(), move.getQuantity() + imove.getQuantity()));
////                        }else {
////                            //newMoveList.add(imove);
////                            newMoveListPrep.add(imove);
////                        }
//                    }
//                }
//            }
            moveList = new ArrayList<ReplenMiniMove>();
            //moveList = newMoveListPrep;
            moveList = IteratorUtils.toList(moveIterator);

            for (ReplenMiniMove move : moveList) {
                Bin bin = new Bin(move.getDestination(), move.getQuantity());

                if (bins.isEmpty()) {
                    bins.add(bin);
                }else {
                    if (!bins.contains(bin)) {
                        bins.add(bin);
                    }
                }
            }
            for (int i = 0; i < bins.size(); i ++) {
                if (bins.get(i).getBinCode().substring(0, 1).equalsIgnoreCase("1")) {
                    primaryList.add(bins.get(i));
                }
            }
            //primaryList = bins;
        }

    }

    private void updateInputListQuantity(ProductBinSelection moveItem) {
        if (moveItem != null && !inputList.isEmpty()) {
            inputList.get(0).setQtyInBin(moveItem.getQtyInBin()); //TODO - updates values manually <<< Find a better way to minimise RISK >>>
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

    public void refreshActivity() {
        if (!mReception.getText().toString().equalsIgnoreCase("")) mReception.setText("");
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
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
                    int acceptable[] = {12,13,14};
                    String eanCode = s.toString().trim();
                    if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptable, eanCode.length()) == -1)) {
                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                        if (currentUser != null) {
                            if (binResponse != null) {
                                if (binResponse.getMatchedProducts() > 0) {
                                    int found = 0;
                                    String newEan = barcodeHelper.formatBarcode(eanCode);
                                    foundList = new ArrayList<ProductBinResponse>();
                                    for(ProductBinResponse prod : binResponse.getProducts()) {
                                        if (prod.getEAN().equalsIgnoreCase(newEan)) {
                                            found ++;
                                            foundList.add(prod);
                                        }
                                    }
                                    if (found == 0) {
                                        appContext.playSound(2);
                                        Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                                        // Vibrate for 500 milliseconds
                                        vib.vibrate(2000);
                                        String mMsg = "There is no such product in this Bin \nPlease re-scan";
                                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
                                        builder.setMessage(mMsg)
                                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        refreshActivity();
                                                    }
                                                });
                                        builder.show();
                                    } else {
                                        //TODO - Determine if the quantity > zero
                                        if (foundList.get(0).getQtyInBin() > 0) {
                                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\":\"%s\"}",
                                                    currentUser.getUserId(), currentUser.getUserCode(), foundList.get(0).getBarcode());
                                            today = new java.sql.Timestamp(utilDate.getTime());
                                            thisMessage.setSource(deviceIMEI);
                                            thisMessage.setMessageType("BarcodeQuery");
                                            thisMessage.setIncomingStatus(1); //default value
                                            thisMessage.setIncomingMessage(msg);
                                            thisMessage.setOutgoingStatus(0);   //default value
                                            thisMessage.setOutgoingMessage("");
                                            thisMessage.setInsertedTimeStamp(today);
                                            thisMessage.setTTL(100);    //default value
                                            if (productAsync != null) {
                                                productAsync.cancel(true);
                                                productAsync = null;
                                            }
                                            productAsync = new WebServiceProductTask();
                                            productAsync.execute(thisMessage);
                                        }else{
                                            // alert that qty in bi8n is less than zero
                                            String mMsg = String.format("This product's quantity of (%s) is less than 1.", foundList.get(0).getQtyInBin());
                                            AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
                                            builder.setMessage(mMsg)
                                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int id) {
                                                            refreshActivity();
                                                        }
                                                    });
                                            builder.show();
                                        }
                                    }
                                }
                            }
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    } else {
                        new AlertDialog.Builder(ActReplenSelectProduct.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
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
                ActReplenSelectProduct.this.finish();
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

    private class WebServiceProductTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper>{
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(ActReplenSelectProduct.this);
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
            Thread.currentThread().setName("MyGetProductsTask");
            responseList = new ArrayList<ProductResponse>();
            HttpResponseHelper response = null;
            try {
                //String response = resolver.resolveMessageQuery(thisMessage);
                response = resolver.resolveHttpMessage(thisMessage);
                response.setResponse(responseHelper.refineResponse(response.getResponse().toString()));
                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    //throw new RuntimeException("Network Error has occurred that resulted in package loss. Please check Wi-Fi");
//                    Message message = new Message();
//                    message.what = 1;
//                    message.obj = new AbstractMap.SimpleEntry<String, HttpResponseHelper>(ymsg, response);
//                    taskErrorHandler.sendMessage(message);
                    response.setResponseMessage(ymsg);
                }else {
                    if (response.getResponse().toString().contains("not recognised")) {
                        //manually error trap this error
                        String iMsg = "The Response object returns null due to improper request.";
//                        Message message1 = new Message();
//                        message1.what = 1;
//                        message1.obj = new AbstractMap.SimpleEntry<String, HttpResponseHelper>(iMsg, response);
//                        taskErrorHandler.sendMessage(message1);
                        response.setResponseMessage(iMsg);
                    }else {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.setPropertyNamingStrategy(new MyCustomNamingStrategy());
                        bcResponse = new BarcodeResponse();
                        bcResponse = mapper.readValue(response.getResponse().toString(), BarcodeResponse.class);
                        responseList = bcResponse.getProducts();
                        response.setResponse(bcResponse);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setExceptionClass(ex.getClass());
//                Message message = new Message();
//                message.what = 1;
//                message.obj = new AbstractMap.SimpleEntry<String, HttpResponseHelper>(ex.getMessage(), response);
//                taskErrorHandler.sendMessage(message);
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper pResponse) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();

            if (!pResponse.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(pResponse.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
                        builder.setMessage(statusCode.toString() + ": - " + pResponse.getResponseMessage())
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
                if (pResponse.getResponse() != null) {
                    if (pResponse.getResponse().getClass().equals(BarcodeResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        if (responseList != null && responseList.size() != 0) { //unnecessary but just to make sure...
                            //TODO - Find the primary location & pass it to the next activity
                            //If more that one supplier (SuppCat) then check if 1st primary loc is full then suggest the 2nd if better
                            final BinQuantitySorted sorter = new BinQuantitySorted();
                            List<Bin> foundBins = new ArrayList<Bin>();
                            final int prodFound = responseList.size();
                            if (prodFound > 0) {
                                if (prodFound == 1) {
                                    for (Bin bin : responseList.get(0).getBins()) {
                                        if (bin.getBinCode().substring(0, 1).equalsIgnoreCase("1")) {
                                            foundBins.add(bin);
                                        }
                                    }
                                    Intent i = new Intent(ActReplenSelectProduct.this, ActReplenManager.class);
                                    //i.putExtra("DATA_EXTRA", responseList)
                                    i.putExtra("PRODUCT_EXTRA", (java.io.Serializable) foundList);
                                    i.putExtra("SOURCE_EXTRA", binResponse.getRequestedBinCode());
                                    i.putExtra("PRIMARY_EXTRA", (java.io.Serializable) foundBins);
                                    startActivityForResult(i, 10);
                                } else {
                                    //do for multiple products (same barcode but different SuppCat)
                                    for (ProductResponse prod : responseList) {
                                        for (Bin bin : prod.getBins()) {
                                            if (bin.getBinCode().substring(0, 1).equalsIgnoreCase("1")) {
                                                foundBins.add(bin);
                                            }
                                        }
                                    }
                                    //Sort by giving us a bin with the lowest quantity
                                    Collections.sort(foundBins, sorter);

                                    Intent i = new Intent(ActReplenSelectProduct.this, ActReplenManager.class);
                                    i.putExtra("PRODUCT_EXTRA", (java.io.Serializable) foundList);
                                    i.putExtra("SOURCE_EXTRA", binResponse.getRequestedBinCode());
                                    i.putExtra("PRIMARY_EXTRA", (java.io.Serializable) foundBins);
                                    startActivityForResult(i, 10);
                                }
                            }
                        } else { //unnecessary but just to make sure...
                            /**--------------------------- Failed because of Bad scan -------------------------**/
                            Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(2000);
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
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
                    } else {
                        /**--------------------------- Failed -------------------------**/
                        HttpResponseCodes statusCode = HttpResponseCodes.findCode(pResponse.getHttpResponseCode());
                        if (statusCode != null) {
                            if (statusCode != HttpResponseCodes.OK) {
                                Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                                vib.vibrate(2000);
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
                                builder.setMessage(pResponse.getResponseMessage() + "\n" + statusCode.toString())
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
                    }
                } else {
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
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
//            if (responseList != null && responseList.size() != 0) {
//                //TODO - Find the primary location & pass it to the next activity
//                //If more that one supplier (SuppCat) then check if 1st primary loc is full then suggest the 2nd if better
//                final BinQuantitySorted sorter = new BinQuantitySorted();
//                List<Bin> foundBins = new ArrayList<Bin>();
//                final int prodFound = responseList.size();
//                if (prodFound > 0) {
//                    if (prodFound == 1) {
//                        for (Bin bin : responseList.get(0).getBins()) {
//                            if (bin.getBinCode().substring(0, 1).equalsIgnoreCase("1")) {
//                                foundBins.add(bin);
//                            }
//                        }
//                        Intent i = new Intent(ActReplenSelectProduct.this, ActReplenManager.class);
//                        //i.putExtra("DATA_EXTRA", responseList)
//                        i.putExtra("PRODUCT_EXTRA", (java.io.Serializable) foundList);
//                        i.putExtra("SOURCE_EXTRA", binResponse.getRequestedBinCode());
//                        i.putExtra("PRIMARY_EXTRA", (java.io.Serializable) foundBins);
//                        startActivityForResult(i, 10);
//                    }else {
//                        //do for multiple products (same barcode but different SuppCat)
//                        for (ProductResponse prod : responseList) {
//                            for (Bin bin : prod.getBins()) {
//                                if (bin.getBinCode().substring(0, 1).equalsIgnoreCase("1")) {
//                                    foundBins.add(bin);
//                                }
//                            }
//                        }
//                        //Sort by giving us a bin with the lowest quantity
//                        Collections.sort(foundBins, sorter);
//
//                        Intent i = new Intent(ActReplenSelectProduct.this, ActReplenManager.class);
//                        i.putExtra("PRODUCT_EXTRA", (java.io.Serializable) foundList);
//                        i.putExtra("SOURCE_EXTRA", binResponse.getRequestedBinCode());
//                        i.putExtra("PRIMARY_EXTRA", (java.io.Serializable) foundBins);
//                        startActivityForResult(i, 10);
//                    }
//                } else {
//                    //Yell murder, notify that product scanned has yielded no result, then clear activity
//                    appContext.playSound(2);
//                    Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
//                    // Vibrate for 500 milliseconds
//                    vib.vibrate(2000);
//                    String mMsg = "The product scanned is not suppose to exist in this bin\nPlease verify then re-scan";
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
//                    builder.setMessage(mMsg)
//                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    //do nothing
//                                }
//                            });
//                    builder.show();
//                }
//            }else {
//                appContext.playSound(2);
//                Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
//                // Vibrate for 500 milliseconds
//                vib.vibrate(2000);
//                String mMsg = "The product query has return no result\nPlease verify then re-scan";
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
//                builder.setMessage(mMsg)
//                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                //do nothing
//                            }
//                        });
//                builder.show();
//            }
            refreshActivity();
        }

        @Override
        protected void onCancelled() {
            mReception.setText("");
        }
    }

    private class WebServiceBinTask extends AsyncTask<String, Void, HttpResponseHelper>{
        protected ProgressDialog bDialog;

        @Override
        protected void onPreExecute() {
            startTime = new Date().getTime(); //get start time
            bDialog = new ProgressDialog(ActReplenSelectProduct.this);
            CharSequence message = "Working hard...contacting webservice...";
            CharSequence title = "Please Wait";
            bDialog.setCancelable(true);
            bDialog.setCanceledOnTouchOutside(false);
            bDialog.setMessage(message);
            bDialog.setTitle(title);
            bDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            bDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(String... bin) {
            //BinResponse ret = new BinResponse();
            HttpResponseHelper response = null;
            try {
                String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                        currentUser.getUserId(), currentUser.getUserCode(), bin[0]);
                thisMessage = new com.proper.messagequeue.Message();
                today = new java.sql.Timestamp(utilDate.getTime());
                thisMessage.setSource(deviceIMEI);
                thisMessage.setMessageType("BinQuery");
                thisMessage.setIncomingStatus(1); //default value
                thisMessage.setIncomingMessage(msg);
                thisMessage.setOutgoingStatus(0);   //default value
                thisMessage.setOutgoingMessage("");
                thisMessage.setInsertedTimeStamp(today);
                thisMessage.setTTL(100);    //default value
                response = resolver.resolveHttpMessage(thisMessage);
                response.setResponse(responseHelper.refineResponse(response.getResponse().toString()));
                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                if (response.getResponse().toString().contains("not recognised")) {
                    //manually error trap this error
                    //String iMsg = "The bin you have scanned has not been recognised. Please check and scan again";
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
            if (bDialog != null && bDialog.isShowing()) bDialog.dismiss();
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
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
                        binResponse = (BinResponse) response.getResponse();
                    }else {
                        /**--------------------------- Bad result -------------------------**/
                        Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
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
                } else {
                    /**--------------------------- Failed -------------------------**/
                    HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                    if (statusCode != null) {
                        if (statusCode != HttpResponseCodes.OK) {
                            Vibrator vib = (Vibrator) ActReplenSelectProduct.this.getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(2000);
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSelectProduct.this);
                            builder.setMessage(response.getResponseMessage() + "\n" + statusCode.toString())
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
                }
            }
            refreshActivity();
        }
    }
}