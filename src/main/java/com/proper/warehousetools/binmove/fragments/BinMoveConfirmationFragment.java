package com.proper.warehousetools.binmove.fragments;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import com.proper.data.binmove.BinMoveResponse;
import com.proper.data.core.IViewPagerFragmentSwitcher;
import com.proper.data.diagnostics.LogEntry;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.ui.chainway_c4000.ActManageBinMove;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 05/09/2014.
 */
public class BinMoveConfirmationFragment extends Fragment {
//    private String sourceBin = "";
//    private String destinationBin = "";
    private wsSendQueue sendQueueTask;
    private int transactionNumber = 0;
    private TextView lblIntro;
    private Button btnContinue = null;
    private ScrollView screen;
    private BinMoveResponse binMoveResponse = new BinMoveResponse();
    private ActManageBinMove activity;
    private View view;

    public BinMoveConfirmationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.lyt_binmove_bindetails, container, false);
        activity  = ((ActManageBinMove)getActivity());
        screen = (ScrollView) view.findViewById(R.id.detailsScroll);

        activity.backPressedParameter = "";
        String intro = String.format("Are you sure, You want to authorise this BinMove\n" + "From:   %s\nTo:   %s", activity.getCurrentSourceBin(), activity.getCurrentDestinationBin());
        //intro = intro + "From: " + activity.currentSourceBin1 + " To: " + activity.currentDestinationBin1;
        lblIntro = (TextView) view.findViewById(R.id.LabelIntro);
        lblIntro.setText(intro);
        lblIntro.setVisibility(View.VISIBLE);
        btnContinue = (Button) view.findViewById(R.id.bnDetContinue);
        Button btnExit = (Button) view.findViewById(R.id.bnDetClose);
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

        Animation animSlideInLow = AnimationUtils.loadAnimation(activity, R.anim.abc_slide_in_bottom);
        Animation animFadeIn = AnimationUtils.loadAnimation(activity, R.anim.abc_fade_in);
        screen.startAnimation(animSlideInLow);
        btnContinue.startAnimation(animFadeIn);
        btnExit.startAnimation(animFadeIn);

        if (!btnContinue.isEnabled()) {
            btnContinue.setEnabled(true);
            btnContinue.setPaintFlags(btnContinue.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnContinue.isEnabled()) btnContinue.setEnabled(true);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String intro = String.format("Are you sure, You want to authorise this BinMove\n" + "From:   %s\nTo:   %s", activity.getCurrentSourceBin(), activity.getCurrentDestinationBin());
        lblIntro.setText(intro);
    }

    private void btnClose_Clicked() {
        activity.backPressedParameter = activity.paramTaskCompleted;
        IViewPagerFragmentSwitcher pageSwitcher =  (IViewPagerFragmentSwitcher) activity;
        pageSwitcher.switchFragment(0);     //Switch back to scan page
        activity.getSupportActionBar().setSelectedNavigationItem(0);
    }

    private void btnContinue_Clicked() {
        if (activity.currentUser != null) {
            activity.today = new Timestamp(activity.utilDate.getTime());
            String msg = String.format("{\"SrcBin\":\"%s\", \"DstBin\":\"%s\", \"UserId\":\"%s\", \"UserCode\":\"%s\"}",
                    activity.getCurrentSourceBin(), activity.getCurrentDestinationBin(), activity.currentUser.getUserId(), activity.currentUser.getUserCode());
            activity.thisMessage.setSource(activity.deviceIMEI);
            activity.thisMessage.setMessageType("BinMove");
            activity.thisMessage.setIncomingStatus(1); //default value
            activity.thisMessage.setIncomingMessage(msg);
            activity.thisMessage.setOutgoingStatus(0);   //default value
            activity.thisMessage.setOutgoingMessage("");
            activity.thisMessage.setInsertedTimeStamp(activity.today);
            activity.thisMessage.setTTL(100);    //default value
            sendQueueTask = new wsSendQueue();
            sendQueueTask.execute(activity.thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
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

    private class wsSendQueue extends AsyncTask<Message, Void, BinMoveResponse> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPostExecute(final BinMoveResponse response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            if (response != null) {
                String msg = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                if (response.getResult().equalsIgnoreCase("Success")) {
                    //****************  Report all warnings ********************
                    int warnings = 0;
                    if (response.getMessages() != null) {
                        for (BinMoveMessage m : response.getMessages()) {
                            if (m.getMessageName().equalsIgnoreCase("warning")) {
                                warnings ++;
                            }
                        }
                    }
                    msg = String.format("Success: BinMove Completed with %s warnings", warnings);

                    builder.setMessage(msg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                *****************     Requested to be removed by Scott       ***********************
//                                    backPressedParameter = paramTaskCompleted;
//                                    Intent i = new Intent(ActBinDetails.this, ActInfo.class);
//                                    i.putExtra("RESPONSE_EXTRA", response);
//                                    i.putExtra("ACTION_EXTRA", R.integer.ACTION_BINMOVE);
//                                    startActivityForResult(i, 0);
                                    btnClose_Clicked();
                                }
                            }).show();
                } else {
                    msg = "Failed: BinMove NOT Completed because it broke many rules";

                    builder.setMessage(msg)
                            .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
//                *****************     Requested to be removed by Scott       ***********************
//                                    backPressedParameter = paramTaskCompleted;
//                                    Intent i = new Intent(ActBinDetails.this, ActInfo.class);
//                                    i.putExtra("RESPONSE_EXTRA", response);
//                                    i.putExtra("ACTION_EXTRA", R.integer.ACTION_BINMOVE);
//                                    startActivityForResult(i, 0);
                                    btnClose_Clicked();
                                }
                            }).show();
                }

                builder.show();
                if (btnContinue.isEnabled()) {
                    btnContinue.setEnabled(false);
                    btnContinue.setPaintFlags(btnContinue.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
            } else {
                //Response is null the disable Yes button:
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                String msg = "Failed: BinMove NOT Completed because of network error, please contact IT for help";
                builder.setMessage(msg)
                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Simulate onBackPressed and pass parameter for next task
                                //TODO - CORRECT THIS! Whatever it's for i forgotten already.
                                activity.backPressedParameter = activity.paramTaskIncomplete;
                                if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                            }
                        }).show();
            }
        }

        @Override
        protected BinMoveResponse doInBackground(Message... msg) {

            String response = activity.resolver.resolveMessageQuery(msg[0]);
            //response = responseHelper.refineOutgoingMessage(response);    Not need to add columns like: PackshotURL etc...

            if (response != null && !response.equalsIgnoreCase("")) {
                //ObjectMapper mapper = new ObjectMapper();
                try {
                    //binMoveResponse = mapper.readValue(response.getBytes(), BinMoveResponse.class);
                    JSONObject resp = new JSONObject(response);
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
                } catch (JSONException e) {
                    e.printStackTrace();
                    activity.today = new Timestamp(activity.utilDate.getTime());
                    LogEntry log = new LogEntry(1L, activity.ApplicationID, "ActBinDetail [sendQueueTask] - doInBackground", activity.deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), activity.today);
                    activity.logger.log(log);
                }
            }
            return binMoveResponse;
        }

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
        protected void onCancelled() {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            sendQueueTask.cancel(true);
            super.onCancelled();
        }
    }

}