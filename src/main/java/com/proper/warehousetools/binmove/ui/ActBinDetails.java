package com.proper.warehousetools.binmove.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import com.proper.data.binmove.BinMoveResponse;
import com.proper.data.core.ICommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.messagequeue.Message;
import com.proper.security.TransactionHistory;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 27/08/2014.
 */
//public class ActBinDetails extends BaseFragmentActivity implements ICommunicator {
public class ActBinDetails extends BaseScanActivity implements ICommunicator {
    private static final String QUEUE_NAME = "BinMove";
    private String sourceBin = "";
    private String destinationBin = "";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private Timestamp today = null;
    private wsSendQueue sendQueueTask;
    private int transactionNumber = 0;
    private Button btnContinue = null;
    private ScrollView screen;
    private BinMoveResponse binMoveResponse = new BinMoveResponse();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_bindetails);
        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();
        deviceIMEI = deviceUtils.getIMEI();
        deviceID = deviceUtils.getDeviceID();

        screen = (ScrollView) this.findViewById(R.id.detailsScroll);
        populateUiControls(savedInstanceState);
        if (!btnContinue.isEnabled()) {
            btnContinue.setEnabled(true);
            btnContinue.setPaintFlags(btnContinue.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnContinue.isEnabled()) btnContinue.setEnabled(true);
    }

    private void populateUiControls(Bundle form) {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            //Yell, Blue murder !
            return;
        }
        sourceBin = extras.getString("SOURCE_EXTRA");
        destinationBin = extras.getString("DESTINATION_EXTRA");
        deviceIMEI = extras.getString("DEVICEIMEI_EXTRA");

        //Do we want to display the bin the source bin item content and quantity?
        //If we need to
        String intro = String.format("Are you sure, You want to authorise this BinMove\n" +
                "From:   %s\nTo:   %s", sourceBin, destinationBin);
        TextView lblIntro = (TextView) this.findViewById(R.id.LabelIntro);
        lblIntro.setText(intro);
        lblIntro.setVisibility(View.VISIBLE);
        btnContinue = (Button) this.findViewById(R.id.bnDetContinue);
        Button btnExit = (Button) this.findViewById(R.id.bnDetClose);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnContinue_Clicked();
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnClose_Clicked();
            }
        });

        Animation animSlideInLow = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
        Animation animFadeIn = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in);
        screen.startAnimation(animSlideInLow);
        btnContinue.startAnimation(animFadeIn);
        btnExit.startAnimation(animFadeIn);
    }

    private void btnClose_Clicked() {
        Intent resultIntent = new Intent();
        this.setResult(0, resultIntent);
        this.finish();
    }

    private void btnContinue_Clicked() {
        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
        if (currentUser != null) {
            today = new Timestamp(utilDate.getTime());
            //BinMove move = new BinMove(1L, sourceBin, destinationBin, 0, null, today);
            String msg = String.format("{\"SrcBin\":\"%s\", \"DstBin\":\"%s\", \"UserId\":\"%s\", \"UserCode\":\"%s\"}",
                    sourceBin, destinationBin, currentUser.getUserId(), currentUser.getUserCode());

            thisMessage.setSource(deviceIMEI);
            thisMessage.setMessageType("BinMove");
            thisMessage.setIncomingStatus(1); //default value
            thisMessage.setIncomingMessage(msg);
            thisMessage.setOutgoingStatus(0);   //default value
            thisMessage.setOutgoingMessage("");
            thisMessage.setInsertedTimeStamp(today);
            thisMessage.setTTL(100);    //default value
            sendQueueTask = new wsSendQueue();
            sendQueueTask.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
        } else {
            appContext.playSound(2);
            Vibrator vib = (Vibrator) ActBinDetails.this.getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            vib.vibrate(2000);
            String mMsg = "User not Authenticated \nPlease login";
            AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
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
    public void onBackPressed() {
        super.onBackPressed();

        if (sendQueueTask != null && sendQueueTask.isCancelled() == false) {
            sendQueueTask.cancel(true);
        }
        Intent resultIntent = new Intent();
        if (backPressedParameter != null && !backPressedParameter.equalsIgnoreCase("")) {
            setResult(1, resultIntent);
        } else {
            setResult(0, resultIntent);
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(R.anim.activity_open_scale,R.anim.activity_close_translate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Animation aniSlideInLow = AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom);
        screen.startAnimation(aniSlideInLow);
    }

    @Override
    protected void onDestroy() {
        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("HISTORY_EXTRA", new TransactionHistory(1L, transactionNumber, deviceIMEI, today, false));
            ActBinDetails.this.setResult(transactionNumber, resultIntent);
            ActBinDetails.this.finish();
        }
    }

    @Override
    public void onDialogMessage_ICommunicator(int buttonClicked) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

    private void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = getSupportFragmentManager();
        //DialogHelper dialog = new DialogHelper(severity, dialogType, message, title);
        DialogHelper dialog = new DialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message", message);
        args.putString("Title_ARG", title);
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    private class wsSendQueue extends AsyncTask<Message, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActBinDetails.this);
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
        protected HttpResponseHelper doInBackground(Message... msg) {

            //String response = resolver.resolveMessageQuery(msg[0]); not needed bcoz doesn't look at network issues
            //response = responseHelper.refineOutgoingMessage(response);    Not need to add columns like: PackshotURL etc...
            HttpResponseHelper response = null;
            response = resolver.resolveHttpMessage(msg[0]);

            try {
                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                if (response.getResponse().toString().contains("not recognised")) { //response.getResponse().toString().contains("Error") ||
                    //manually error trap this error
                    String iMsg = "The Response object returns null due to improper request.";
                    response.setResponseMessage(iMsg);
                } else {
                    JSONObject resp = new JSONObject(response.getResponse().toString());
                    JSONArray messages = resp.getJSONArray("Messages");
                    JSONArray actions = resp.getJSONArray("MessageObjects");
                    String RequestedSrcBin = resp.getString("RequestedSrcBin");
                    String RequestedDstBin = resp.getString("RequestedDstBin");
                    String Result = resp.getString("Result");
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
                    binMoveResponse.setRequestedSrcBin(RequestedSrcBin);
                    binMoveResponse.setRequestedDstBin(RequestedDstBin);
                    binMoveResponse.setResult(Result);
                    binMoveResponse.setMessages(messageList);
                    binMoveResponse.setMessageObjects(actionList);
                    response.setResponse(binMoveResponse);
                }
            } catch (Exception ex){
                ex.printStackTrace();
                response.setResponseMessage(ex.getMessage());
                response.setExceptionClass(ex.getClass());
            }
            return response;
        }

        @Override
        protected void onPostExecute(final HttpResponseHelper response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }

            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActBinDetails.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // Attempt to reload Activity
                                        if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(BinMoveResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        BinMoveResponse resp = (BinMoveResponse) response.getResponse();
                        int warnings = 0;
                        String msg = "";
                        ArrayList<BinMoveMessage> errMsg = new ArrayList<BinMoveMessage>();
                        if (resp.getMessages() != null) {
                            for (BinMoveMessage m : resp.getMessages()) {
                                if (m.getMessageName().equalsIgnoreCase("warning")) {
                                    warnings++;
                                }
                            }
                        }
                        if (resp.getResult().equalsIgnoreCase("Success")) {
                            if (warnings > 0) {
                                msg = String.format("Success: BinMove Completed with %s warnings", warnings);
                            }else{
                                msg = "Success: BinMove Completed";
                            }
                        }
                        if (resp.getResult().equalsIgnoreCase("Failure")) {
                            for (BinMoveMessage message : resp.getMessages()) {
                                if (message.getMessageName().equalsIgnoreCase("Error")) {
                                    errMsg.add(message);
                                }
                            }
                            if (warnings > 0) {
                                msg = String.format("Failed: %s because %s with %s warnings", warnings);
                            }else{
                                msg = String.format("Failed: %s because %s", errMsg.get(0).getMessageText(),
                                        errMsg.get(1).getMessageText());
                            }
                        }
                        /************************   Report  ****************************/
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
                        builder.setMessage(msg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent resultIntent = new Intent();
                                        ActBinDetails.this.setResult(RESULT_OK, resultIntent);
                                        ActBinDetails.this.finish();
                                    }
                                }).show();

                        if (ActBinDetails.this.btnContinue.isEnabled()) {
                            ActBinDetails.this.btnContinue.setEnabled(false);
                            btnContinue.setPaintFlags(btnContinue.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        }
                    }
                }else {
                    /**-------------------- Failed because of Bad query construction ----------------------**/
                    Vibrator vib = (Vibrator) ActBinDetails.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
                    builder.setMessage("This BinMove has been blocked\nPlease contact an IT staff")
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // Attempt to reload Activity
                                    if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                }
            }
//            if (response != null) {
//                String msg = "";
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
//                if (response.getResult().equalsIgnoreCase("Success")) {
//                    //****************  Report all warnings ********************
//                    int warnings = 0;
//                    if (response.getMessages() != null) {
//                        for (BinMoveMessage m : response.getMessages()) {
//                            if (m.getMessageName().equalsIgnoreCase("warning")) {
//                                warnings ++;
//                            }
//                        }
//                    }
//                    msg = String.format("Success: BinMove Completed with %s warnings", warnings);
//
//                    builder.setMessage(msg)
//                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    Intent resultIntent = new Intent();
//                                    ActBinDetails.this.setResult(RESULT_OK, resultIntent);
//                                    ActBinDetails.this.finish();
//                                }
//                            }).show();
//                } else {
//                    msg = "Failed: BinMove NOT Completed because it broke many rules";
//
//                    builder.setMessage(msg)
//                            .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    Intent resultIntent = new Intent();
//                                    ActBinDetails.this.setResult(RESULT_CANCELED, resultIntent);
//                                    ActBinDetails.this.finish();
//                                }
//                            }).show();
//                }
//
//                builder.show();
//                if (ActBinDetails.this.btnContinue.isEnabled()) {
//                    ActBinDetails.this.btnContinue.setEnabled(false);
//                    btnContinue.setPaintFlags(btnContinue.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//                }
//            } else {
//                //Response is null the disable Yes button:
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinDetails.this);
//                String msg = "Failed: BinMove NOT Completed because of network error, please contact IT for help";
//                builder.setMessage(msg)
//                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                // Simulate onBackPressed and pass parameter for next task
//                                backPressedParameter = paramTaskCompleted;
//                                if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
//                            }
//                        }).show();
//            }
        }

        @Override
        protected void onCancelled() {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            sendQueueTask.cancel(true);
            super.onCancelled();
        }
    }
}