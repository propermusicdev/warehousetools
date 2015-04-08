package com.proper.warehousetools.binmove.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.proper.data.diagnostics.LogEntry;
import com.proper.logger.LogHelper;
import com.proper.security.TransactionHistory;
import com.proper.warehousetools.R;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 27/08/2014.
 */
public class ActBinMoveMonitor extends Activity {
    private String deviceIMEI = "";
    private static final String ApplicationID = "BinMove";
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private List<TransactionHistory> transactionList = new ArrayList<TransactionHistory>();
    private LogHelper logger = new LogHelper();
    private TransactionHistory currentHistory =  new TransactionHistory();
    private boolean hasChanged = false;
    private Button btnExit;
    private int RESULT_DONE = 44;
    private Timestamp today = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_monitor);
        setupControls(savedInstanceState);
    }

    private void setupControls(Bundle bundle) {

        btnExit = (Button) findViewById(R.id.bnExiActBinMoveMonitor);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnExit_Clicked();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            //Yell, Blue murder !
            return;
        }
        int newEntry = extras.getInt("NewEntry");
        currentHistory = (TransactionHistory) extras.getParcelable("HISTORY_EXTRA");

        //Get history from a rest web service
        AsyncCheckStatusChanged statusChangedTask = new AsyncCheckStatusChanged();
        statusChangedTask.execute();
    }

    private void btnExit_Clicked() {
        Intent resultIntent = new Intent();
        ActBinMoveMonitor.this.setResult(0, resultIntent);
        ActBinMoveMonitor.this.finish();
    }

    private void checkStatusChanged() {
        try {
            String uri = String.format("http://192.168.10.248:9080/warehouse.support/api/v1/message/checkchangedstatus/%s",
                    currentHistory.getMessageId());
            URL url = new URL(uri);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            ObjectInputStream ois = new ObjectInputStream(conn.getInputStream());
            hasChanged = ois.readBoolean();
            ois.close();
            conn.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
            today = new Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "onCreate", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
            logger.log(log);
        } catch(Exception ex) {
            ex.printStackTrace();
            today = new Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "onCreate", deviceIMEI, ex.getClass().toString(), ex.getMessage(), today);
            logger.log(log);
        }
    }

    private class AsyncCheckStatusChanged extends AsyncTask<Void, Void, Void> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            mDialog = new ProgressDialog(ActBinMoveMonitor.this);
            CharSequence message = "Working hard...checking status [contacting webservice]...";
            CharSequence title = "Please Wait";
            mDialog.setCancelable(false);  //not cancellable period!
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage(message);
            mDialog.setTitle(title);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            checkStatusChanged();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            //if status has changed then remove entry from our local session
            if (hasChanged) {
                String msg = String.format("The message status has successfully changed for the current bin move");
                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMoveMonitor.this);
                builder.setMessage(msg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
            } else {
                String msg = String.format("The message status hasn't changed for the current bin move");
                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinMoveMonitor.this);
                builder.setMessage(msg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                builder.show();
            }
        }
    }

}