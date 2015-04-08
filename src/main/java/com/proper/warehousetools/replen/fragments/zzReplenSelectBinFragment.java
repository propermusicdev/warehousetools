package com.proper.warehousetools.replen.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.*;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.proper.data.binmove.BinResponse;
import com.proper.data.core.IViewPagerFragmentSwitcher;
import com.proper.data.diagnostics.LogEntry;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.zzActReplenManageConfig;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Date;

/**
 * Created by Lebel on 03/09/2014.
 */
public class zzReplenSelectBinFragment extends Fragment {
    private EditText mReception;
    private TextView txtInto;
    private Button btnScan;
    private Button btnExit;
    private Button btnEnterBinCode;
    private String scanInput;
    private WebServiceTask wsTask;
    private Handler handler = null;
    private zzActReplenManageConfig activity;
    private View view;

    public zzReplenSelectBinFragment() {
    }

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.lyt_replen_selectbin, container, false);
        view = inflater.inflate(R.layout.lyt_replen_selectbin, container, false);
        activity = ((zzActReplenManageConfig)getActivity());

        txtInto = (TextView) view.findViewById(R.id.txtvReplenScanIntro);
        btnScan = (Button) view.findViewById(R.id.bnReplenScanPerformScan);
        btnExit = (Button) view.findViewById(R.id.bnExitActReplenScan);
        btnEnterBinCode = (Button) view.findViewById(R.id.bnEnterBinReplenScan);
        mReception = (EditText) view.findViewById(R.id.etxtReplenScanBinCode);

        mReception.addTextChangedListener(new TextChanged());
        btnScan.setOnClickListener(new ClickEvent());
        btnExit.setOnClickListener(new ClickEvent());
        btnEnterBinCode.setOnClickListener(new ClickEvent());

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
                    activity.appContext.playSound(1);
                    btnScan.setEnabled(true);
                }
            }
        };
        return view;
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
                if (activity.inputByHand == 0) {
                    String eanCode = s.toString().trim();
                    if (eanCode.length() > 0 && eanCode.length() == 5) {
                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                        activity.currentUser = activity.currentUser != null ? activity.currentUser : activity.authenticator.getCurrentUser();   //Gets currently authenticated user
                        if (activity.currentUser != null) {
                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"BinCode\":\"%s\"}",
                                    activity.currentUser.getUserId(), activity.currentUser.getUserCode(), eanCode);
                            activity.originalEAN = eanCode;
                            activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                            activity.thisMessage.setSource(activity.deviceIMEI);
                            activity.thisMessage.setMessageType("BinQuery");
                            activity.thisMessage.setIncomingStatus(1); //default value
                            activity.thisMessage.setIncomingMessage(msg);
                            activity.thisMessage.setOutgoingStatus(0);   //default value
                            activity.thisMessage.setOutgoingMessage("");
                            activity.thisMessage.setInsertedTimeStamp(activity.today);
                            activity.thisMessage.setTTL(100);    //default value

                            wsTask = new WebServiceTask();
                            wsTask.execute(activity.thisMessage);
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
                    } else {
                        new AlertDialog.Builder(activity).setTitle(R.string.DIA_ALERT).setMessage(R.string.DEV_EAN_ERR).setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
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
            boolean bContinuous = true;
            int iBetween = 0;
            if (v == btnExit) {
//                if (activity.readThread != null && activity.readThread.isInterrupted() == false) {
//                    activity.readThread.interrupt();
//                }
//                Intent resultIntent = new Intent();
//                if (activity.backPressedParameter != null && !activity.backPressedParameter.equalsIgnoreCase("")) {
//                    setResult(1, resultIntent);
//                } else {
//                    setResult(RESULT_OK, resultIntent);
//                }
//                ActReplenSelectBin.this.finish();
            }
            else if(v == btnScan)
            {
                btnScan.setEnabled(false);
                mReception.requestFocus();
                if (activity.threadStop) {
                    Log.i("Reading", "My Barcode " + activity.readerStatus);
                    activity.readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                    activity.readThread.setName("Single Barcode ReadThread");
                    activity.readThread.start();
                }else {
                    activity.threadStop = true;
                }
                btnScan.setEnabled(true);
            } else if(v == btnEnterBinCode) {
                if (activity.inputByHand == 0) {
                    turnOnInputByHand();
                    if (!mReception.isEnabled()) mReception.setEnabled(true);
                    mReception.setText("");
                    showSoftKeyboard();
                } else {
                    turnOffInputByHand();
                    //mReception.removeTextChangedListener();
                }
            }
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void turnOnInputByHand(){
        activity.inputByHand = 1;    //Turn On Input by Hand
        this.btnScan.setEnabled(false);
        paintByHandButtons();
    }

    private void turnOffInputByHand(){
        activity.inputByHand = 0;    //Turn On Input by Hand
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
        if (activity.inputByHand == 0) {
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

                handler.sendMessage(msg);

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

    public void refreshActivity() {
        if (!mReception.getText().toString().equalsIgnoreCase("")) mReception.setText("");
        if (!btnScan.isEnabled()) btnScan.setEnabled(true);
    }

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == activity.KEY_SCAN) {
//            if (event.getRepeatCount() == 0) {
//                boolean bContinuous = true;
//                int iBetween = 0;
//                mReception.requestFocus();
//                if (activity.threadStop) {
//                    Log.i("Reading", "My Barcode " + activity.readerStatus);
//                    activity.readThread = new Thread(new GetBarcode(bContinuous, iBetween));
//                    activity.readThread.setName("Single Barcode ReadThread");
//                    activity.readThread.start();
//                }else {
//                    activity.threadStop = true;
//                }
//            }
//        }
//
//        //return super.onKeyDown(keyCode, event);
//        //return true;
//        return true;
//    }

//    @Override
//    protected void onPause() {
//        threadStop = true;
//        if (readThread != null && readThread.isInterrupted() == false) {
//            readThread.interrupt();
//        }
//        Log.d(TAG, "onPause");
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        Log.d(TAG, "onResume");
//        super.onResume();
//    }
//
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d(TAG, "onDestroy");
//        if (isBarcodeOpened) {
//            if (readThread != null && readThread.isInterrupted() == false) {
//                readThread.interrupt();
//            }
//            //soundPool.release();
//            mInstance.close();
//        }
//    }

//    @Override
//    public void onBackPressed() {
//        if (readThread != null && readThread.isInterrupted() == false) {
//            readThread.interrupt();
//        }
//        Intent resultIntent = new Intent();
//        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
//            setResult(1, resultIntent);
//        } else {
//            setResult(RESULT_OK, resultIntent);
//        }
//        ActReplenSelectBin.this.finish();
//    }

    private class WebServiceTask extends AsyncTask<com.proper.messagequeue.Message, Void, BinResponse> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            activity.startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(activity.getApplicationContext());
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
        protected BinResponse doInBackground(com.proper.messagequeue.Message... msg) {
            BinResponse ret = new BinResponse();
            try {
                String response = activity.resolver.resolveMessageQuery(activity.thisMessage);
                response = activity.responseHelper.refineResponse(response);
                if (response.contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                    activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                    LogEntry log = new LogEntry(1L, activity.ApplicationID, "ActReplenSelectBin - WebServiceTask - Line:655", activity.deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, activity.today);
                    activity.logger.log(log);
                    throw new RuntimeException("The bin you have scanned have not been recognised. Please check and scan again");
                }else {
                    ObjectMapper mapper = new ObjectMapper();
                    ret = mapper.readValue(response, BinResponse.class);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                LogEntry log = new LogEntry(1L, activity.ApplicationID, "ActReplenSelectBin - doInBackground", activity.deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), activity.today);
                activity.logger.log(log);
            } catch (Exception ex) {
                ex.printStackTrace();
                activity.today = new java.sql.Timestamp(activity.utilDate.getTime());
                LogEntry log = new LogEntry(1L, activity.ApplicationID, "ActReplenSelectBin - doInBackground", activity.deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), activity.today);
                activity.logger.log(log);
            }
            return ret;
        }

        @Override
        protected void onPostExecute(BinResponse response) {
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            //Finally end turn by locking all input controls
            //lockAllControls();
            if (response != null) {
//                Intent i = new Intent(activity, ActReplenSelectProduct.class);
//                i.putExtra("RESPONSE_EXTRA", response);
//                startActivityForResult(i, activity.RESULT_OK);
                activity.thisBinResponse = response;
                IViewPagerFragmentSwitcher pageSwitcher =  (IViewPagerFragmentSwitcher) activity;
                pageSwitcher.switchFragment(1);     //Switch to zzReplenSelectProductFragment
            } else {
                String mMsg = "Incorrect BinCode \nPlease re-scan";
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(mMsg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do nothing
                            }
                        });
                builder.show();
            }
            refreshActivity();
        }

        @Override
        protected void onCancelled() {
            mReception.setText("");
        }
    }
}