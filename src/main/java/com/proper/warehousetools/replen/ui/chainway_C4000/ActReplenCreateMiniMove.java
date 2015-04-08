package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.*;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.data.binmove.*;
import com.proper.data.binmove.adapters.BinListAdapter;
import com.proper.data.core.ICommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.replen.ReplenMiniMove;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.fragments.QuantityDialogFragment;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 09/09/2014.
 */
public class ActReplenCreateMiniMove extends BaseScanActivity implements ICommunicator {
    private SharedPreferences prefs = null;
    private LinearLayout mainLayout;
    private Button btnScan, btnExit, btnEnterBin, btnSelect, btnToggle;
    private EditText mReception;
    private TextView lblIntro, lblBin, lblMoveQty, lblTotal;
    private ViewFlipper flipper;
    private Spinner cmbSelectBin;
    private ReplenMiniMove move;
    private static final int INITIALIZE_VIEW = 321, UPDATE_VIEW = 222;
    private int qtyTotal = 0;
    private ProductBinSelection moveItem;
    private List<ProductBinResponse> inputList;
    private String currentSource = "", currentDetination = "";
    private List<Bin> primaryList = new ArrayList<Bin>();
    private BinListAdapter adapter;
    private Bin selectedDestination;
    private MoveQrytask moveTask = null;

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public ProductBinSelection getMoveItem() {
        return moveItem;
    }

    public void setMoveItem(ProductBinSelection moveItem) {
        this.moveItem = moveItem;
    }

    public Bin getSelectedDestination() {
        return selectedDestination;
    }

    public void setSelectedDestination(Bin selectedDestination) {
        this.selectedDestination = selectedDestination;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_createminimove);
//        getSupportActionBar().setLogo(R.drawable.ic_launcher);
//        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
//        getSupportActionBar().setTitle("Scan Destination");

        Bundle extra = getIntent().getExtras();
        if (extra == null) throw new RuntimeException("onCreate: Bundled Extra cannot be null!, Line: 44");
        prefs = getSharedPreferences("Proper_Replen", Context.MODE_PRIVATE);    //get preferences
        qtyTotal = extra.getInt("QUANTITY_EXTRA");
        inputList = (List<ProductBinResponse>) extra.getSerializable("DATA_EXTRA");
        currentSource = extra.getString("SOURCE_EXTRA");
        primaryList = (List<Bin>) extra.getSerializable("PRIMARY_EXTRA");
        moveItem = new ProductBinSelection(inputList.get(0));

        mainLayout = (LinearLayout) this.findViewById(R.id.lytReplenCMM);
        lblIntro = (TextView) this.findViewById(R.id.txtvReplenCMMIntro);
        flipper = (ViewFlipper) this.findViewById(R.id.vfReplenCMMGetBin);
        cmbSelectBin = (Spinner) this.findViewById(R.id.spReplenCMMSelectBin);
        btnSelect = (Button) this.findViewById(R.id.bnReplenCMMSelectBin);
        btnToggle = (Button) this.findViewById(R.id.bnReplenCMMToggleCommand);
        btnScan = (Button) this.findViewById(R.id.bnReplenCMMScan);
        btnExit = (Button) this.findViewById(R.id.bnExitActReplenCMM);
        mReception = (EditText) this.findViewById(R.id.etxtReplenCMMBincode);
        btnEnterBin = (Button) this.findViewById(R.id.bnReplenCMMEnterBin);
        lblBin = (TextView) this.findViewById(R.id.txtvReplenCMMBin);
        lblMoveQty = (TextView) this.findViewById(R.id.txtvReplenCMMMoveQty);
        lblTotal = (TextView) this.findViewById(R.id.txtvReplenCMMTotalQty);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(v);
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(v);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(v);
            }
        });
        btnEnterBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(v);
            }
        });
        btnToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick(v);
            }
        });

        mReception.addTextChangedListener(new TextChanged());

        cmbSelectBin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                onSelectedItem(adapterView, view, position, id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(msg.what == 1){
                    setScanInput(msg.obj.toString());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    if (!mReception.getText().toString().equalsIgnoreCase("")) {
                        mReception.setText("");
                        mReception.setText(getScanInput());
                    }
                    else{
                        mReception.setText(getScanInput());
                    }
                    appContext.playSound(1);
                    btnScan.setEnabled(true);
                }
            }
        };

        adapter = new BinListAdapter(ActReplenCreateMiniMove.this, primaryList);
        cmbSelectBin.setAdapter(adapter);
        updateControls(INITIALIZE_VIEW);
        if (primaryList.size() == 0) {
            flipper.setDisplayedChild(1);   // flip view to enable scanning
            btnToggle.setVisibility(View.GONE);
        }
        //TODO - implement some scanning stuff here to enable onResultActivity to work with a plain activity
    }

    private void onSelectedItem(AdapterView<?> adapterView, View view, int position, long id) {
        this.setSelectedDestination(primaryList.get(position)); //set selection
        currentDetination = this.getSelectedDestination().getBinCode(); //set current
        updateControls(UPDATE_VIEW);
    }

    private void updateControls(int viewPhase) {
        lblIntro.setText(inputList.get(0).getArtist() + " - " + inputList.get(0).getTitle());
        lblBin.setText(currentDetination);
        if (viewPhase == INITIALIZE_VIEW) {
            //lblMoveQty.setText(String.format("%s", qtyTotal));
            lblMoveQty.setText("0");
        }
        if (viewPhase == UPDATE_VIEW) {
            lblMoveQty.setText(String.format("%s", moveItem.getQtyToMove()));
        }
        lblTotal.setText(String.format("%s", qtyTotal));
    }

    private void saveQuantityData(int oldQty, int newQty) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("From", currentSource);
        editor.putInt("OldQuantity", oldQty);
        editor.putInt("NewQuantity", newQty);
        editor.putString("LastDestination", currentDetination);
        editor.putInt("LastQuantity", newQty);
        editor.commit();
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
                            currentDetination = eanCode;
                            //TODO - Bing up dialog for quantity then create a move list- Option Now or Later...
                            showQuantityDialog();
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActReplenCreateMiniMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    } else {
                        new AlertDialog.Builder(ActReplenCreateMiniMove.this).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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

    private void onButtonClick(View v) {
        boolean bContinuous = true;
        int iBetween = 0;
        switch (v.getId()) {
            case R.id.bnExitActReplenCMM:
                Intent intent = new Intent();
                intent.putExtra("RETURN_EXTRA", move);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case R.id.bnReplenCMMScan:
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
                break;
            case R.id.bnReplenCMMSelectBin:
                if (getSelectedDestination() != null) {
                    showQuantityDialog();
                }
                break;
            case R.id.bnReplenCMMToggleCommand:
                if (flipper.getDisplayedChild() == 0) {
                    //primary loc
                    flipper.setDisplayedChild(1);
                    btnToggle.setVisibility(View.GONE);
                } else {
                    //alternate loc
                    flipper.setDisplayedChild(0);
                }
                break;
            case R.id.bnReplenCMMEnterBin:
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    if (!mReception.isEnabled()) mReception.setEnabled(true);
                    mReception.setText("");
                    showSoftKeyboard();
                } else {
                    turnOffInputByHand();
                    //mReception.removeTextChangedListener();
                }
                break;
        }
    }

    private void commitMiniMove() {
        if (!currentDetination.isEmpty()) {
            if (qtyTotal > 0 && moveItem != null) {
                updateControls(UPDATE_VIEW);

                //moveList = adapter.getThisSelection();
                MoveRequest req = new MoveRequest();
                List<MoveRequestItem> list = new ArrayList<MoveRequestItem>();
                req.setUserCode(currentUser.getUserCode());
                req.setUserId(String.format("%s", currentUser.getUserId()));
                req.setSrcBin(currentSource);
                req.setDstBin(currentDetination);

                //Build Request
                MoveRequestItem item = new MoveRequestItem();
                item.setProductID(moveItem.getProductId());
                item.setSuppliercat(moveItem.getSupplierCat());
                item.setQty(moveItem.getQtyToMove());
                list.add(item);
                if (!list.isEmpty()) {
                    try {
                        //Build message request
                        req.setProducts(list);
                        ObjectMapper mapper = new ObjectMapper();
                        String msg = mapper.writeValueAsString(req);
                        today = new Timestamp(utilDate.getTime());
                        thisMessage = new com.proper.messagequeue.Message();

                        thisMessage.setSource(deviceIMEI);
                        thisMessage.setMessageType("CreateMovelist");
                        thisMessage.setIncomingStatus(1); //default value
                        thisMessage.setIncomingMessage(msg);
                        thisMessage.setOutgoingStatus(0);   //default value
                        thisMessage.setOutgoingMessage("");
                        thisMessage.setInsertedTimeStamp(today);
                        thisMessage.setTTL(100);    //default value

                        if (moveTask != null) {
                            moveTask.cancel(true);
                        }
                        moveTask = new MoveQrytask();
                        moveTask.execute(thisMessage);  //executes -> Send webservice -> To our msg Queue
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        //LogEntry log = new LogEntry(1L, ApplicationID, this.getClass().getSimpleName() + " - ButtonClicked - onCreate", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        LogEntry log = new LogEntry(1L, ApplicationID, ((Object) this).getClass().getSimpleName() + " - ButtonClicked - onCreate", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.log(log);
                    }
                } else {
                    //warn that there is no matching product of that description
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) ActReplenCreateMiniMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(2000);
                    String mMsg = "There is no such product in this Bin \nPlease re-scan";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    refreshActivity();
                                }
                            });
                    builder.show();
                }

            }else {
                //Warn and do nothing
                appContext.playSound(2);
                Vibrator vib = (Vibrator) ActReplenCreateMiniMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                vib.vibrate(2000);
                String mMsg = "Unable to continue move because bin quantity is zero \nPlease go back and choose another product";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
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

    private int receiveQuantityFromDialog() {
        int ret = 0;
        if (moveItem != null) {
            ret = moveItem.getQtyToMove();
        }
        return ret;
    }

    private void showQuantityDialog() {
        FragmentManager fm = getSupportFragmentManager();
        QuantityDialogFragment dialog = new QuantityDialogFragment();
        dialog.show(fm, "QuantityDialog");
    }

    @Override
    public void onDialogMessage_ICommunicator(int buttonClicked) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                if (moveItem.getQtyToMove() == 1) {
                    this.lblIntro.setText(String.format("Moving %s item To: %s", moveItem.getQtyToMove(), getScanInput()));
                } else {
                    this.lblIntro.setText(String.format("Moving %s item To: %s", moveItem.getQtyToMove(), getScanInput()));
                }
                commitMiniMove();       // Finally commit our move
                break;
            case R.integer.MSG_NO:
                break;
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
            btnEnterBin.setText(byHand);
        } else {
            btnEnterBin.setText(finish);
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

    public void refreshActivity() {
        if (moveTask != null) {
            moveTask.cancel(true);
            moveTask = null;
        }
        if (!mReception.getText().toString().equalsIgnoreCase("")) mReception.setText("");
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KEY_SCAN) {
            if (event.getRepeatCount() == 0) {
                boolean bContinuous = true;
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

    private class MoveQrytask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActReplenCreateMiniMove.this);
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
            PartialBinMoveResponse qryResponse = new PartialBinMoveResponse();
            HttpResponseHelper response = null;

            //String response = resolver.resolveMessageQuery(msg[0]);
            response = resolver.resolveHttpMessage(msg[0]);
            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
//                //throw new RuntimeException("Network Error has occurred that resulted in package loss. Please check Wi-Fi");
//                Message message = new Message();
//                message.what = 1;
//                message.obj = new AbstractMap.SimpleEntry<String, HttpResponseHelper>(ymsg, response);
//                taskErrorHandler.sendMessage(message);
            }
//            if (response != null && !response.equalsIgnoreCase("")) {
            if (response.getResponse().toString().contains("not recognised")) {
                //manually error trap this error
                String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage(iMsg);
//                Message message1 = new Message();
//                message1.what = 1;
//                message1.obj = new AbstractMap.SimpleEntry<String, HttpResponseHelper>(iMsg, response);
//                taskErrorHandler.sendMessage(message1);
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
//                    Message message = new Message();
//                    message.what = 1;
//                    message.obj = new AbstractMap.SimpleEntry<String, HttpResponseHelper>(ex.getMessage(), response);
//                    taskErrorHandler.sendMessage(message);
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }

            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActReplenCreateMiniMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
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
                    if (response.getResponse().getClass().equals(PartialBinMoveResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
                        String msg = "Success: BinMove completed!";
                        builder.setMessage(msg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //Go back and return ReplenMiniMove
                                        ReplenMiniMove miniMove = new ReplenMiniMove();
                                        miniMove.setDestination(currentDetination);
                                        miniMove.setQuantity(moveItem.getQtyToMove());
                                        Intent i = new Intent();
                                        i.putExtra("RETURN_EXTRA", miniMove);
                                        i.putExtra("MOVE_EXTRA", moveItem);
                                        setResult(RESULT_OK, i);
                                        ActReplenCreateMiniMove.this.finish();
                                    }
                                });
                        builder.show();
                    } else { //unnecessary but just to make sure...
                        /**--------------------------- Failed because of Bad scan -------------------------**/
                        Vibrator vib = (Vibrator) ActReplenCreateMiniMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
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
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActReplenCreateMiniMove.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
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
//            if (response != null) {
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
//                String msg = "Success: BinMove completed!";
//                builder.setMessage(msg)
//                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                //Go back and return ReplenMiniMove
//                                ReplenMiniMove miniMove = new ReplenMiniMove();
//                                miniMove.setDestination(currentDetination);
//                                miniMove.setQuantity(moveItem.getQtyToMove());
//                                Intent i = new Intent();
//                                i.putExtra("RETURN_EXTRA", miniMove);
//                                i.putExtra("MOVE_EXTRA", moveItem);
//                                setResult(RESULT_OK, i);
//                                ActReplenCreateMiniMove.this.finish();
//                            }
//                        });
//                builder.show();
//            } else {
//                //Response is null the disable Yes button:
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenCreateMiniMove.this);
//                String msg = "Failed: BinMove NOT Completed because of network error, please contact IT for help";
//                builder.setMessage(msg)
//                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                //Go back and return ReplenMiniMove
//                                ReplenMiniMove miniMove = new ReplenMiniMove();
//                                miniMove.setDestination(currentDetination);
//                                miniMove.setQuantity(moveItem.getQtyToMove());
//                                Intent i = new Intent();
//                                i.putExtra("RETURN_EXTRA", miniMove);
//                                i.putExtra("MOVE_EXTRA", moveItem);
//                                setResult(RESULT_OK, i);
//                                ActReplenCreateMiniMove.this.finish();
//                            }
//                        });
//                builder.show();
//            }
        }
    }
}