package com.proper.warehousetools.binmove.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.*;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.proper.data.binmove.BinResponse;
import com.proper.data.core.IScanKeyDown;
import com.proper.data.core.IViewPagerFragmentSwitcher;
import com.proper.data.diagnostics.LogEntry;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.chainway_c4000.ActManageBinMove;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Created by Lebel on 05/09/2014.
 */
public class BinMoveScanFragment extends Fragment implements IScanKeyDown {
    private Button btnExitSF;
    private Button btnScanSourceSF;
    private Button btnContinueSF;
    private Button btnScanDestinationSF;
    private Button btnEnterSourceSF;
    private Button btnEnterDestinationSF;
    private EditText txtSourceReceptionSF;
    private EditText txtDestinationReception;
    private TextView lblSource;
    private TextView lblDestination;
    private static final String TAG = "BinMoveScanFragment";
    //private TextView txtCurrentUser;
    //private String backPressedParameter = "";
    //private BinResponse qryResponse = new BinResponse();
    private queryTask binQryTask;
    private View view;
    private ActManageBinMove activity;

    public BinMoveScanFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.lyt_binmove_binmain, container, false);
        activity = ((ActManageBinMove)getActivity());

        //txtCurrentUser = (TextView) view.findViewById(R.id.txtvHeaderTitle);
        txtSourceReceptionSF = (EditText) view.findViewById(R.id.txtvSourceText);
        txtSourceReceptionSF.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtSourceReceptionSF.addTextChangedListener(new TextChanged(this.txtSourceReceptionSF));
        txtDestinationReception = (EditText) view.findViewById(R.id.txtvDestinationText);
        txtDestinationReception.setImeOptions(EditorInfo.IME_ACTION_DONE);
        txtDestinationReception.addTextChangedListener(new TextChanged(this.txtDestinationReception));
        //TextView txtHeader = (TextView) this.findViewById(R.id.txtvHeaderTitle);
        btnExitSF = (Button) view.findViewById(R.id.bnExitActBinMain);
        btnExitSF.setOnClickListener(new ClickEvent());
        btnScanSourceSF = (Button) view.findViewById(R.id.bnScanSource);
        btnScanSourceSF.setOnClickListener(new ClickEvent());
        btnScanDestinationSF = (Button) view.findViewById(R.id.bnScanDestination);
        btnScanDestinationSF.setOnClickListener(new ClickEvent());
        btnContinueSF = (Button) view.findViewById(R.id.bnContinue);
        btnContinueSF.setOnClickListener(new ClickEvent());
        btnEnterSourceSF = (Button) view.findViewById(R.id.bnEnterSourceBinMain);
        btnEnterSourceSF.setOnClickListener(new ClickEvent());
        btnEnterDestinationSF = (Button) view.findViewById(R.id.bnEnterDestinationBinMain);
        btnEnterDestinationSF.setOnClickListener(new ClickEvent());
        lblSource = (TextView) view.findViewById(R.id.txtvSourceLabel);
        lblSource.setText("SrcBin:");
        lblDestination = (TextView) view.findViewById(R.id.txtvDestinationLabel);
        lblDestination.setText("DstBin:");

//        if (currentUser != null) {
//            txtHeader.setText(String.format("Current User [%s]", currentUser.getUserFirstName()));
//            txtCurrentUser.setText(currentUser.getUserFirstName());
//        }

        if (!btnContinueSF.isEnabled()) btnContinueSF.setEnabled(true);

        activity.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg != null) {
                    if(msg.what == 1) {
                        activity.setScanInput(msg.obj.toString());
                        //TODO - Emulate the implementation made in ActQueryScan

                        if (activity.getScanInput().length() == 5) {
                            //******************************************BEGIN*****************************************************CONTINUE
                            switch (activity.NAV_TURN) {
                                case R.integer.TURN_DESTINATION:
                                    //do destination    ````````````````````````````````````````````````````````````````````
                                    if (!txtDestinationReception.getText().toString().equalsIgnoreCase("")) {
                                        txtDestinationReception.setText("");
                                        txtDestinationReception.setText(activity.getScanInput());
                                    } else {

                                        if (!activity.getScanInput().equalsIgnoreCase("")) {
                                            txtDestinationReception.setText(activity.getScanInput());
                                        }
                                    }
                                    activity.appContext.playSound(1);
                                    break;
                                case R.integer.TURN_SOURCE:
                                    //do source     ````````````````````````````````````````````````````````````````````````
                                    if (!txtSourceReceptionSF.getText().toString().equalsIgnoreCase("")) {
                                        txtSourceReceptionSF.setText("");
                                        txtSourceReceptionSF.setText(activity.getScanInput());
                                    } else {
                                        txtSourceReceptionSF.setText(activity.getScanInput());
                                    }
                                    activity.appContext.playSound(1);
                                    break;
                            }
                            //******************************************BEGIN*****************************************************CONTINUE
                        } else {
                            //Scanned wrong item, barcode etc...
                            Log.e("A bad scan has occured", "Please scan again");
                            activity.appContext.playSound(2);
                            String mMsg = "Bad scan occured \nPlease scan again";
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                            switch (activity.NAV_TURN) {
                                case R.integer.TURN_DESTINATION:
                                    //we've just scanned a destination but incorrectly
                                    txtDestinationReception.setEnabled(true);
                                    txtDestinationReception.setText("");
                                    btnScanDestinationSF.setEnabled(true);
                                    btnScanDestinationSF.setPaintFlags(btnScanDestinationSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                                    break;
                                case R.integer.TURN_SOURCE:
                                    //we've just scanned a source but incorrectly
                                    reloadActivity();
                                    break;
                            }
                        }
                    }
                }
            }
        };
        activity.NAV_TURN = R.integer.TURN_SOURCE;   // start with the default turn!
        formatControls();

        return view;
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void manageInputByHand() {
        switch (activity.NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (activity.inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons();
                    if (!btnEnterSourceSF.isEnabled()) {
                        btnEnterSourceSF.setEnabled(true);
                        btnEnterSourceSF.setPaintFlags(btnEnterSourceSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                    if (btnEnterDestinationSF.isEnabled()) {
                        btnEnterDestinationSF.setEnabled(false);
                        btnEnterDestinationSF.setPaintFlags(btnEnterDestinationSF.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    activity.setScanInput(txtDestinationReception.getText().toString());
                    if (!activity.getScanInput().isEmpty()) {
                        activity.fullTurnCount ++;
                        txtDestinationReception.setText(activity.getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        txtSourceReceptionSF.requestFocus();
                        lockDestinationControls();
                        //unlockSourceControls();
                    }
                }
                break;
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change NAV-Turn
                if (activity.inputByHand == 0) {
                    turnOnInputByHand();
                    showSoftKeyboard();
                    paintByHandButtons();
                } else {
                    turnOffInputByHand();
                    if (!btnEnterDestinationSF.isEnabled()) {
                        btnEnterDestinationSF.setEnabled(true);
                        btnEnterDestinationSF.setPaintFlags(btnEnterDestinationSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                    if (btnEnterSourceSF.isEnabled()) {
                        btnEnterSourceSF.setEnabled(false);
                        btnEnterSourceSF.setPaintFlags(btnEnterSourceSF.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    }
                    activity.setScanInput(txtSourceReceptionSF.getText().toString());
                    if (!activity.getScanInput().isEmpty()) {
                        activity.fullTurnCount ++;
                        txtSourceReceptionSF.setText(activity.getScanInput());     // just to trigger text changed
                        paintByHandButtons();
                        txtDestinationReception.requestFocus();
                        lockSourceControls();
                        unlockDestinationControls();
                    }
                    //NAV_TURN = R.integer.TURN_BIN;
                }
                break;
        }
    }

    private void turnOnInputByHand(){
        activity.inputByHand = 1;    //Turn On Input by Hand
        switch (activity.NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                this.btnScanDestinationSF.setEnabled(false);
                break;
            case R.integer.TURN_SOURCE:
                this.btnScanSourceSF.setEnabled(false);
                break;
        }
    }

    private void turnOffInputByHand(){
        activity.inputByHand = 0;    //Turn On Input by Hand
        switch (activity.NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                this.btnScanDestinationSF.setEnabled(false);
                break;
            case R.integer.TURN_SOURCE:
                this.btnScanSourceSF.setEnabled(false);
                break;
        }
    }

    private void paintByHandButtons() {
        final String byHand = "ByHand";
        final String finish = "Finish";
        switch (activity.NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                //1st time = turn on, 2nd FINISH
                if (activity.inputByHand == 0) {
                    //btnEnterDestinationSF.setText(byHand);
                } else {
                    btnEnterDestinationSF.setText(finish);
                }
                break;
            case R.integer.TURN_SOURCE:
                //1st time = turn on, 2nd change Navigation Turn
                if (activity.inputByHand == 0) {
                    btnEnterSourceSF.setText(byHand);
                } else {
                    btnEnterSourceSF.setText(finish);
                }
                break;
            case R.integer.TURN_END:
                btnEnterSourceSF.setText(finish);
                btnEnterDestinationSF.setText(finish);
        }
    }

    private void lockSourceControls() {
        //disable all corresponding controls
        if (txtSourceReceptionSF.isEnabled()) {
            txtSourceReceptionSF.setEnabled(false);
        }
        if (btnScanSourceSF.isEnabled()) {
            btnScanSourceSF.setEnabled(false);
            btnScanSourceSF.setPaintFlags(btnScanSourceSF.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (btnEnterSourceSF.isEnabled()) {
            btnEnterSourceSF.setEnabled(false);
            btnEnterSourceSF.setPaintFlags(btnEnterSourceSF.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void lockDestinationControls() {
        //disable all corresponding controls
        if (txtDestinationReception.isEnabled() == true) {
            txtDestinationReception.setEnabled(false);
        }
        if (btnScanDestinationSF.isEnabled() == true) {
            btnScanDestinationSF.setEnabled(false);
            btnScanDestinationSF.setPaintFlags(btnScanDestinationSF.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (btnEnterDestinationSF.isEnabled()) {
            btnEnterDestinationSF.setEnabled(false);
            btnEnterDestinationSF.setPaintFlags(btnEnterDestinationSF.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void unlockSourceControls() {
        if (txtSourceReceptionSF.isEnabled() == false) {
            txtSourceReceptionSF.setEnabled(true);
        }
        if (!btnScanSourceSF.isEnabled()) {
            btnScanSourceSF.setEnabled(true);
            btnScanSourceSF.setPaintFlags(btnScanSourceSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnEnterSourceSF.isEnabled()) {
            btnEnterSourceSF.setEnabled(true);
            btnEnterSourceSF.setPaintFlags(btnEnterSourceSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void unlockDestinationControls() {
        if (txtDestinationReception.isEnabled() == false) {
            txtDestinationReception.setEnabled(true);
        }
        if (btnScanDestinationSF.isEnabled() == false) {
            btnScanDestinationSF.setEnabled(true);
            btnScanDestinationSF.setPaintFlags(btnScanDestinationSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnEnterDestinationSF.isEnabled()) {
            btnEnterDestinationSF.setEnabled(true);
            btnEnterDestinationSF.setPaintFlags(btnEnterDestinationSF.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void formatControls() {
        switch (activity.NAV_TURN) {
            case R.integer.TURN_DESTINATION:
                lockSourceControls();
                unlockDestinationControls();
                break;
            case R.integer.TURN_SOURCE:
                lockDestinationControls();
                unlockSourceControls();
                break;
            case R.integer.TURN_END:
                lockTaskControls();
                paintByHandButtons();
                break;
        }
    }

    private void lockTaskControls() {
        //disable all corresponding controls
        lockSourceControls();
        lockDestinationControls();
    }

    private void reloadActivity() {
        activity.setCurrentDestinationBin("");
        activity.setCurrentSourceBin("");
        txtSourceReceptionSF.setText("");
        txtDestinationReception.setText("");
        activity.NAV_TURN = R.integer.TURN_SOURCE;
        formatControls();
    }

    @Override
    public void onPause() {
        activity.threadStop = true;
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        if (!activity.backPressedParameter.isEmpty() && activity.backPressedParameter.equalsIgnoreCase(activity.paramTaskIncomplete)) {
            reloadActivity();
        }
        if (!activity.backPressedParameter.isEmpty() && activity.backPressedParameter.equalsIgnoreCase(activity.paramTaskCompleted)) {
            reloadActivity();
        }
        super.onResume();
    }

    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (activity.isBarcodeOpened) {
            activity.mInstance.close();
        }
    }

    @Override
    public void onKeyScan(int keyCode, KeyEvent keyEvent) {
        if (this.txtDestinationReception != null) {
            this.txtDestinationReception.requestFocus();
            this.btnScanDestinationSF.performClick();       //Scan();
        }
    }
//    @Override
//    public void onKeyScan() {
//        //Do some scan
//        txtDestinationReception.requestFocus();
//        btnScanDestinationSF.performClick(); //Scan();
//    }

    public void myOnKeyDown(int keyCode) {
        if (this.txtDestinationReception != null) {
            this.txtDestinationReception.requestFocus();
            this.btnScanDestinationSF.performClick(); //Scan();
        }
    }

    private class TextChanged implements TextWatcher {
        private View myView;
        private TextChanged(View view) {
            this.myView = view;
        }

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

            // Go on with AsyncTask
            if (s != null && !s.toString().equalsIgnoreCase("")) {
                if (activity.inputByHand == 0) {
                    String binCode = s.toString().trim();
                    if (binCode.length() == 5) {
                        switch (activity.NAV_TURN) {
                            case R.integer.TURN_DESTINATION:
                                //currentDestinationBin = binCode;
                                //currentDestinationBin = "4BBB1"; for testing
                                //if (!currentDestinationBin.equalsIgnoreCase(currentSourceBin)) {
                                if (!binCode.equalsIgnoreCase(activity.getCurrentSourceBin())) {
                                    activity.setCurrentDestinationBin(binCode);
                                    activity.currentDestinationBin1 = binCode;
                                    activity.NAV_TURN = R.integer.TURN_END; //new state
                                    formatControls();
                                }else {
                                    //Source & destination are the same echo error
                                    String mMsg = "Destination BinCode must not be the same as Source BinCode \nPlease scan again";
                                    activity.appContext.playSound(2);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                    builder.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder.show();
                                    txtDestinationReception.setText("");
                                    if (txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(false); //disable the control
                                }

                                break;
                            case R.integer.TURN_SOURCE:
                                activity.setCurrentSourceBin(binCode);
                                activity.currentSourceBin1 = binCode;
                                //currentSourceBin = "4BBC1";   For testing only
                                activity.NAV_TURN = R.integer.TURN_DESTINATION; //new
                                formatControls();
                                break;
                        }
                    } else {
                        String mMsg = "Bad scan occured \nPlease scan again";
                        switch (activity.NAV_TURN) {
                            case R.integer.TURN_SOURCE:
                                activity.appContext.playSound(2);
                                //soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                builder.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder.show();
                                txtSourceReceptionSF.setText("");
                                if (txtSourceReceptionSF.isEnabled()) txtSourceReceptionSF.setEnabled(false); //disable the control
                                break;
                            case R.integer.TURN_DESTINATION:
                                activity.appContext.playSound(2);
                                //soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                                //soundPool.play(errorSoundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
                                builder1.setMessage(mMsg)
                                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                //do nothing
                                            }
                                        });
                                builder1.show();
                                txtDestinationReception.setText("");
                                if (txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(false); //disable the control
                                break;
                            case R.integer.TURN_END:
                                //New code to disallow additional scan after a successful one
                                if (binCode.length() > 5) {
                                    binCode = binCode.substring(0, 5);
                                    activity.setCurrentDestinationBin(binCode);
                                    activity.currentDestinationBin1 = binCode;
                                    txtDestinationReception.setText(binCode);
                                    if (txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(false); //disable the control
                                } else {
                                    mMsg = "The End of the road my friend!";
                                    activity.appContext.playSound(1);
                                    AlertDialog.Builder builder2 = new AlertDialog.Builder(activity);
                                    builder2.setMessage(mMsg)
                                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    //do nothing
                                                }
                                            });
                                    builder2.show();
                                }
                                break;
                        }
                    }
                }
            }
        }
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            boolean bContinuous = true;
            int iBetween = 0;
            switch (v.getId()) {
                case R.id.bnExitActBinMain:
                    //TODO - ActBinMain.this.finish();
                    Intent i = new Intent();
                    activity.setResult(Activity.RESULT_OK, i);
                    activity.finish();
                    break;
                case R.id.bnEnterSourceBinMain:
                    if (!txtSourceReceptionSF.isEnabled()) txtSourceReceptionSF.setEnabled(true);
                    txtSourceReceptionSF.requestFocus();
                    manageInputByHand();
                    break;
                case R.id.bnEnterDestinationBinMain:
                    if (!txtDestinationReception.isEnabled()) txtDestinationReception.setEnabled(true);
                    txtDestinationReception.requestFocus();
                    manageInputByHand();
                    break;
                case R.id.bnScanSource:
                    txtSourceReceptionSF.requestFocus();
                    if (activity.threadStop) {
                        Log.i("Reading", "Source Barcode " + activity.readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        activity.readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        activity.readThread.setName("Source Barcode ReadThread");
                        activity.readThread.start();
                    }else {
                        activity.threadStop = true;
                    }
                    break;
                case R.id.bnScanDestination:
                    txtDestinationReception.requestFocus();
                    if (activity.threadStop) {
                        Log.i("Reading", "Destination Barcode " + activity.readerStatus);
                        //init_barcode = et_init_barcode.getText().toString();
                        activity.readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        activity.readThread.setName("Destination Barcode ReadThread");
                        activity.readThread.start();
                    } else {
                        activity.threadStop = true;
                    }
                    break;
                case R.id.bnContinue:
                    //check if we already have both (src & dst) values
                    if (activity.NAV_TURN == R.integer.TURN_END) {
                        //UserAuthenticator authenticator = new UserAuthenticator(ActBinMain.this);
                        activity.currentUser = activity.currentUser != null ? activity.currentUser : activity.authenticator.getCurrentUser();  //Gets currently authenticated user
                        if (activity.currentUser != null) {
                            activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\", \"BinCode\":\"%s\"}",
                                    activity.currentUser.getUserId(), activity.currentUser.getUserCode(), activity.getCurrentSourceBin());

                            activity.thisMessage.setSource(activity.deviceIMEI);
                            activity.thisMessage.setMessageType("BinQuery");
                            activity.thisMessage.setIncomingStatus(1); //default value
                            activity.thisMessage.setIncomingMessage(msg);
                            activity.thisMessage.setOutgoingStatus(0);   //default value
                            activity.thisMessage.setOutgoingMessage("");
                            activity.thisMessage.setInsertedTimeStamp(activity.today);
                            activity.thisMessage.setTTL(100);    //default value
                            binQryTask = new queryTask();
                            binQryTask.execute(activity.thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
                        } else {
                            activity.appContext.playSound(2);
                            Vibrator vib = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    }
                    break;
            }
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
                barCode = activity.mInstance.scan();

                Log.i("MY", "barCode " + barCode.trim());

                msg = new Message();

                if (barCode == null || barCode.isEmpty()) {
                    msg.arg1 = 0;
                    msg.obj = "";
                } else {
                    msg.what = 1;
                    msg.obj = barCode;
                }

                activity.handler.sendMessage(msg);

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (isContinuous && !activity.threadStop);

        }
    }

    private class queryTask extends AsyncTask<com.proper.messagequeue.Message, Void, BinResponse> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(activity);
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
        protected BinResponse doInBackground(com.proper.messagequeue.Message... msg) {
            //qryResponse = new BinResponse();

            try {
                String response = activity.resolver.resolveMessageQuery(msg[0]);
                //String response = "{\"RequestedBinCode\" : \"1HCH5\",\"MatchedProducts\" : \"17\",\"Products\" : [{\"ProductId\" : \"25168976\",\"SupplierCat\" : \"SNAP273CD\",\"Artist\" : \"FUNKADELIC\",\"Title\" : \"UNCLE JAM WANTS YOU\",\"Barcode\" : \"803415127320\",\"Format\" : \"CD\",\"EAN\" : \"0803415127320\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"82\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Funkadelic\",\"Full_Title_Value1\" : \"Uncle Jam Wants You\",\"QtyInBin\" : \"10\"},{\"ProductId\" : \"153609737\",\"SupplierCat\" : \"093624948247PMI\",\"Artist\" : \"GREEN DAY\",\"Title\" : \"UNO!\",\"Barcode\" : \"093624948247\",\"Format\" : \"CD\",\"EAN\" : \"0093624948247\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"2\"},{\"ProductId\" : \"143271891\",\"SupplierCat\" : \"3325480644160PMI\",\"Artist\" : \"FRERES PITIGOI & TARA OASULU\",\"Title\" : \"MUSIQUE DE MARIAGE ET FETES RO\",\"Barcode\" : \"3325480644160\",\"Format\" : \"CD\",\"EAN\" : \"3325480644160\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"2\"},{\"ProductId\" : \"138150831\",\"SupplierCat\" : \"651249076723PMI\",\"Artist\" : \"DIGWEED,JOHN\",\"Title\" : \"VOL. 2-RENAISSANCE PRESENTS TR\",\"Barcode\" : \"651249076723\",\"Format\" : \"CD\",\"EAN\" : \"0651249076723\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"138150918\",\"SupplierCat\" : \"824363000121PMI\",\"Artist\" : \"CRANK YANKERS\",\"Title\" : \"VOL. 1-BEST CRANK CALLS\",\"Barcode\" : \"824363000121\",\"Format\" : \"CD\",\"EAN\" : \"0824363000121\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"4993383\",\"SupplierCat\" : \"ANG646662PMI\",\"Artist\" : \"FOLLIES / O.B.C.\",\"Title\" : \"FOLLIES / O.B.C.\",\"Barcode\" : \"077776466620\",\"Format\" : \"CD\",\"EAN\" : \"0077776466620\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Broadway Cast\",\"Full_Title_Value1\" : \"Follies / O.B.C.\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"5469159\",\"SupplierCat\" : \"BAPO39026B2PMI\",\"Artist\" : \"MCNALLY,SHANNON\",\"Title\" : \"NORTH AMERICAN GHOST MUSIC\",\"Barcode\" : \"094633902626\",\"Format\" : \"CD\",\"EAN\" : \"0094633902626\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"158859083\",\"SupplierCat\" : \"CDB56379480762PMI\",\"Artist\" : \"VALLDENEU,MAX\",\"Title\" : \"IT'S ABOUT LOVE (CDRP)\",\"Barcode\" : \"885767060326\",\"Format\" : \"CD\",\"EAN\" : \"0885767060326\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"129403511\",\"SupplierCat\" : \"FAPO803412PMI\",\"Artist\" : \"BURNSIDE,R.L.\",\"Title\" : \"MISSISSIPPI HILL COUNTRY BLUES\",\"Barcode\" : \"045778034123\",\"Format\" : \"CD\",\"EAN\" : \"0045778034123\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"R.L. Burnside\",\"Full_Title_Value1\" : \"Mississippi Country Blues\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"133803673\",\"SupplierCat\" : \"KSCOPE216\",\"Artist\" : \"ULVER\",\"Title\" : \"CHILDHOOD'S END\",\"Barcode\" : \"802644821627\",\"Format\" : \"CD\",\"EAN\" : \"0802644821627\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"46\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Ulver\",\"Full_Title_Value1\" : \"Childhood's End\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"173678827\",\"SupplierCat\" : \"RCA1961202PMI\",\"Artist\" : \"WALKER,HEZEKIAH\",\"Title\" : \"AZUSA THE NEXT GENERATION\",\"Barcode\" : \"886919612028\",\"Format\" : \"CD\",\"EAN\" : \"0886919612028\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Hezekiah Walker\",\"Full_Title_Value1\" : \"Azusa The Next Generation\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"4974588\",\"SupplierCat\" : \"RCA763792PMI\",\"Artist\" : \"ATKINS,CHET / PAUL,LES\",\"Title\" : \"CHESTER & LESTER (BONUS TRACKS\",\"Barcode\" : \"828767637921\",\"Format\" : \"CD\",\"EAN\" : \"0828767637921\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"2\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"2497707\",\"SupplierCat\" : \"ROUCD9009\",\"Artist\" : \"BRAVE COMBO\",\"Title\" : \"DELETED - POLKATHARSIS\",\"Barcode\" : \"011661900929\",\"Format\" : \"CD\",\"EAN\" : \"0011661900929\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"4\",\"Full_Artist_Value1\" : \"Brave Combo\",\"Full_Title_Value1\" : \"Polkatharsis\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"86158133\",\"SupplierCat\" : \"WMCD1294\",\"Artist\" : \"MENON,JAY\",\"Title\" : \"24/01THROUGH MY EYES\",\"Barcode\" : \"5016700129427\",\"Format\" : \"CD\",\"EAN\" : \"5016700129427\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"26\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Jay Menon\",\"Full_Title_Value1\" : \"Through My Eyes\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"67978413\",\"SupplierCat\" : \"FATCD62PMI\",\"Artist\" : \"BROSSEAU,TOM\",\"Title\" : \"CAVALIER\",\"Barcode\" : \"000030251304\",\"Format\" : \"CD\",\"EAN\" : \"0000030251304\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"1\",\"DeletionType\" : \"0\",\"QtyInBin\" : \"1\"},{\"ProductId\" : \"27369877\",\"SupplierCat\" : \"FV12\",\"Artist\" : \"MARTIN,JUAN\",\"Title\" : \"SOLO\",\"Barcode\" : \"5023100081224\",\"Format\" : \"CD\",\"EAN\" : \"5023100081224\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"9\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Juan Martin\",\"Full_Title_Value1\" : \"Solo\",\"QtyInBin\" : \"9\"},{\"ProductId\" : \"2498218\",\"SupplierCat\" : \"SHCD3813\",\"Artist\" : \"RANCH ROMANCE\",\"Title\" : \"FLIP CITY\",\"Barcode\" : \"015891381329\",\"Format\" : \"CD\",\"EAN\" : \"0015891381329\",\"SuppCode\" : \"PROP\",\"StockAmount\" : \"3\",\"DeletionType\" : \"0\",\"Full_Artist_Value1\" : \"Ranch Romance\",\"Full_Title_Value1\" : \"Flip City\",\"QtyInBin\" : \"1\"}]}";
                response = activity.responseHelper.refineResponse(response);
                if (response != null && !response.equalsIgnoreCase("")) {
                    if (response.contains("not recognised")) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                        LogEntry log = new LogEntry(1L, activity.ApplicationID, "ActBinMain - queryTask - Line:1192", activity.deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, activity.today);
                        activity.logger.log(log);
                        throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
                    }else {
                        ObjectMapper mapper = new ObjectMapper();
                        activity.thisBinResponse = mapper.readValue(response, BinResponse.class);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                LogEntry log = new LogEntry(1L, activity.ApplicationID, "ActBinMain - doInBackground", activity.deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), activity.today);
                activity.logger.log(log);
            }
            return activity.thisBinResponse;
        }

        @Override
        protected void onPostExecute(BinResponse response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            if (response != null) {
                if (activity.getCurrentSourceBin() != null && !activity.getCurrentSourceBin().equalsIgnoreCase("") &&
                        activity.getCurrentDestinationBin() != null && !activity.getCurrentDestinationBin().equalsIgnoreCase("")) {

                    switch (activity.NAV_INSTRUCTION) {
                        case R.integer.ACTION_PARTIALMOVE:
//                            TODO -- Intent i = new Intent(ActBinMain.this, ActBinItemSelection.class);
//                            i.putExtra("SOURCE_EXTRA", currentSourceBin);
//                            i.putExtra("DESTINATION_EXTRA", currentDestinationBin);
//                            i.putExtra("DEVICEIMEI_EXTRA", deviceIMEI);
//                            i.putExtra("LOGIN_EXTRA", currentUser);
//                            i.putExtra("RESPONSE_EXTRA", response);
//                            startActivityForResult(i, RESULT_OK);
//                            reloadActivity();
                            activity.backPressedParameter = activity.paramTaskCompleted;
                            IViewPagerFragmentSwitcher pageSwitcher =  (IViewPagerFragmentSwitcher) activity;
                            pageSwitcher.switchFragment(1);     //Switch back to scan page
                            activity.viewPager.setCurrentItem(1);
                            activity.getSupportActionBar().setSelectedNavigationItem(1);
                            break;
                        case R.integer.ACTION_BINMOVE:
//                            TODO - Intent i2 = new Intent(ActBinMain.this, ActBinDetails.class);
//                            i2.putExtra("SOURCE_EXTRA", currentSourceBin);
//                            i2.putExtra("DESTINATION_EXTRA", currentDestinationBin);
//                            i2.putExtra("DEVICEIMEI_EXTRA", deviceIMEI);
//                            i2.putExtra("LOGIN_EXTRA", currentUser);
//                            i2.putExtra("RESPONSE_EXTRA", response);
//                            startActivityForResult(i2, RESULT_OK);
//                            reloadActivity();
                            activity.backPressedParameter = activity.paramTaskCompleted;
                            IViewPagerFragmentSwitcher pageSwitcha =  (IViewPagerFragmentSwitcher) activity;
                            pageSwitcha.switchFragment(1);     //Switch back to scan page
                            activity.viewPager.setCurrentItem(1);
                            activity.getSupportActionBar().setSelectedNavigationItem(1);
                            break;
                    }
                }
            } else {
                //Response is null the disable Yes button:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                String msg = "Failed: BinMove NOT Completed because of network error, please contact IT for help";
                builder.setMessage(msg)
                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Attempt to reload Activity
                                if (btnContinueSF.isEnabled()) btnContinueSF.setEnabled(false);
                            }
                        });
            }
        }
    }
}