package com.proper.warehousetools.replen.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.proper.data.core.IReplenCommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.ReplenDialogHelper;
import com.proper.data.replen.ReplenMoveListLinesItemResponse;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenManageWork;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by Lebel on 10/12/2014.
 */
//public class UpdateLineFragment extends DialogFragment implements View.OnClickListener, IReplenUpdateLineCommunicator {
public class UpdateLineFragment extends DialogFragment {
    private String TAG = UpdateLineFragment.class.getSimpleName();
    private EditText txtQtyConfirmed, txtSrcBin, txtDstBin;
    private Button btnEditQty, btnEditSrcBin, btnEditDstBin, btnUpdate;
    private com.proper.data.core.ICommunicator ICommunicator;
    private IReplenCommunicator IReplenCommunicator;
    private int inputByHand = 0, confirmedQty = 0;
    private String srcBin = "", dstBin = "";
    private ReplenMoveListLinesItemResponse moveline;
    private UpdateLineAsync updateLineAsyncTask = null;
    private ActReplenManageWork mActivity = null;
    private Message iMessage = null;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private int ConvertPixelsToDp(float pixelValue)
    {
        int dp = (int) ((pixelValue)/ getResources().getDisplayMetrics().density);
        return dp;
    }

    public int getConfirmedQty() {
        return confirmedQty;
    }

    public void setConfirmedQty(int confirmedQty) {
        pcs.firePropertyChange("confirmedQty", this.confirmedQty, confirmedQty);
        this.confirmedQty = confirmedQty;
    }

    public String getSrcBin() {
        return srcBin;
    }

    public void setSrcBin(String srcBin) {
        pcs.firePropertyChange("srcBin", this.srcBin, srcBin);
        this.srcBin = srcBin;
    }

    public String getDstBin() {
        return dstBin;
    }

    public void setDstBin(String dstBin) {
        pcs.firePropertyChange("dstBin", this.dstBin, dstBin);
        this.dstBin = dstBin;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        //ICommunicator = (ICommunicator) activity;
        IReplenCommunicator = (com.proper.data.core.IReplenCommunicator) activity;
        mActivity = (ActReplenManageWork) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }
        this.addPropertyChangeListener(new UpdateLineChangedListener()); // add property change listener
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int widthInDp = ConvertPixelsToDp(metrics.widthPixels);
        int heightInDp = ConvertPixelsToDp(metrics.heightPixels);
        getDialog().getWindow().setLayout(widthInDp, heightInDp);
        // Get data from activity
        moveline = ((ActReplenManageWork) getActivity()).getSelectedLine(); //get data
        View view = inflater.inflate(R.layout.lyt_replen_fgm_update_line, null);
        LinearLayout lytMain = (LinearLayout) view.findViewById(R.id.lyt_ReplenULOutterWrapper);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) lytMain.getLayoutParams();
        TextView txtArtist = (TextView) view.findViewById(R.id.txtvReplenULArtist);
        TextView txtTitle = (TextView) view.findViewById(R.id.txtvReplenULTitle);
        TextView txtInsertTimeStamp = (TextView) view.findViewById(R.id.txtvReplenULInsertTimeStamp);
        TextView txtCatalog = (TextView) view.findViewById(R.id.txtvReplenULCatalog);
        TextView txtEAN = (TextView) view.findViewById(R.id.txtvReplenULEAN);
        TextView txtQty = (TextView) view.findViewById(R.id.txtvReplenULQuantity);
        txtQtyConfirmed = (EditText) view.findViewById(R.id.txtvReplenULQtyConfirmed);
        txtQtyConfirmed.addTextChangedListener(new TextChanged(txtQtyConfirmed));
        txtSrcBin = (EditText) view.findViewById(R.id.txtvReplenULSrcBin);
        txtSrcBin.addTextChangedListener(new TextChanged(txtSrcBin));
        txtDstBin = (EditText) view.findViewById(R.id.txtvReplenULDstBin);
        txtDstBin.addTextChangedListener(new TextChanged(txtDstBin));
        btnEditQty = (Button) view.findViewById(R.id.bnReplenULEditQtyConfirmed);
        btnEditQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnEditSrcBin = (Button) view.findViewById(R.id.bnReplenULEditSrcBin);
        btnEditSrcBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnEditDstBin = (Button) view.findViewById(R.id.bnReplenULEditDstBin);
        btnEditDstBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnUpdate = (Button) view.findViewById(R.id.bnReplenULUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        //Populate controls
        txtArtist.setText(moveline.getArtist()); txtTitle.setText(moveline.getTitle());
        txtInsertTimeStamp.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(moveline.getInsertTimeStamp()));
        txtCatalog.setText(moveline.getCatNumber());
        txtEAN.setText(moveline.getEAN());
        txtQty.setText(String.format("%s", moveline.getQty()));
        txtQtyConfirmed.setText(String.format("%s", confirmedQty));
        txtSrcBin.setText(moveline.getSrcBinCode());
        txtDstBin.setText(moveline.getDstBinCode());
        disableAll();
        enableEditButtons();
        return view;
    }

    private void buttonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnReplenULEditQtyConfirmed:
                //do
                manageInputByHand(btnEditQty);
                break;
            case R.id.bnReplenULEditSrcBin:
                //do
                manageInputByHand(btnEditSrcBin);
                break;
            case R.id.bnReplenULEditDstBin:
                //do
                manageInputByHand(btnEditDstBin);
                break;
            case R.id.bnReplenULUpdate:
                //do
                //manageInputByHand(btnUpdate);
                updateLineAsyncTask = new UpdateLineAsync();
                buildMessage();
                updateLineAsyncTask.execute(iMessage);
                break;
        }
    }

    private void buildMessage() {
        iMessage = new Message();
        if (mActivity != null && mActivity.getCurrentUser() != null) {
            if (mActivity.getSelectedMove() != null) {
                String msg = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"ProductId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\"}",
                        mActivity.getSelectedMove().getItem().getMovelistId(), mActivity.getSelectedLine().getMovelistLineId(), mActivity.getSelectedLine().getProductId(),
                        mActivity.getCurrentUser().getUserCode(), mActivity.getCurrentUser().getUserId());
                mActivity.setToday(new Timestamp(mActivity.getUtilDate().getTime()));
                iMessage = new Message();
                iMessage.setSource(mActivity.getDeviceIMEI());
                iMessage.setMessageType("UpdateMoveListLine");
                iMessage.setIncomingStatus(1); //default value
                iMessage.setIncomingMessage(msg);
                iMessage.setOutgoingStatus(0);   //default value
                iMessage.setOutgoingMessage("");
                iMessage.setInsertedTimeStamp(mActivity.getToday());
                iMessage.setTTL(100);    //default value
            } else {
                Log.e("**************************ERROR", "getSelectedMove is current null *********************************");
            }
        }
    }

    private void updateQtyControls() {
        txtQtyConfirmed.setText(String.format("%s", confirmedQty));

        if (moveline.getQty() > confirmedQty) {
            txtQtyConfirmed.setTextColor(Color.parseColor("#ff0000"));
        }
        if (moveline.getQty() == confirmedQty) {
            txtQtyConfirmed.setTextColor(Color.parseColor("#ff0000")); //green
        }
    }

    private void showSoftKeyboard() {
        InputMethodManager imm =(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
        }
    }

    private void manageInputByHand(View view) {
        switch (view.getId()) {
            case R.id.bnReplenULEditDstBin:
                //do disable all, enable txtDstBin, select all
                disableAll();
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    if (!txtDstBin.isEnabled()) {
                        txtDstBin.setEnabled(true);
                    }
                    paintByHandButtons(btnEditDstBin);
                    txtDstBin.selectAll();
                    showSoftKeyboard();
                    txtDstBin.requestFocus();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons(btnEditDstBin);
                    //setQtyToSplitInput(Integer.parseInt(moveQty.getText().toString()));
//                    if (getQtyToSplitInput() != 0) {
//                        moveQty.setText(String.format("%s", getQtyToSplitInput()));     // just to trigger text changed
//                        //paintByHandButtons(btnEditDstBin);
//                    }
                }
                break;
            case R.id.bnReplenULEditQtyConfirmed:
                //do
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    if (!txtQtyConfirmed.isEnabled()) {
                        txtQtyConfirmed.setEnabled(true);
                    }
                    paintByHandButtons(btnEditQty);
                    txtQtyConfirmed.selectAll();
                    showSoftKeyboard();
                    txtQtyConfirmed.requestFocus();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons(btnEditQty);
                    //setQtyToSplitInput(Integer.parseInt(moveQty.getText().toString()));
//                    if (getQtyToSplitInput() != 0) {
//                        moveQty.setText(String.format("%s", getQtyToSplitInput()));     // just to trigger text changed
//                        //paintByHandButtons(btnEditDstBin);
//                    }
                }
                break;
            case R.id.bnReplenULEditSrcBin:
                //do
                disableAll();
                if (inputByHand == 0) {
                    turnOnInputByHand();
                    if (!txtSrcBin.isEnabled()) {
                        txtSrcBin.setEnabled(true);
                    }
                    paintByHandButtons(btnEditSrcBin);
                    txtSrcBin.selectAll();
                    showSoftKeyboard();
                    txtSrcBin.requestFocus();
                } else {
                    turnOffInputByHand();
                    paintByHandButtons(btnEditSrcBin);
                }
                break;
        }
        enableEditButtons();
    }

    private void turnOnInputByHand(){
        this.inputByHand = 1;    //Turn On Input by Hand
    }

    private void turnOffInputByHand(){
        this.inputByHand = 0;    //Turn On Input by Hand
    }

    private void paintByHandButtons(Button view) {
        final String byHand = "Edit";
        final String finish = "Finish";
        if (inputByHand == 0) {
            view.setText(byHand);
        } else {
            view.setText(finish);
        }
    }

    private void selectAll(EditText view){
        view.selectAll();
    }

    private void disableAll(){
        if (txtQtyConfirmed.isEnabled())txtQtyConfirmed.setEnabled(false);
        if (txtSrcBin.isEnabled())txtSrcBin.setEnabled(false);
        if (txtDstBin.isEnabled())txtDstBin.setEnabled(false);
        if (btnEditQty.isEnabled()) {
            btnEditQty.setEnabled(false);
            btnEditQty.setPaintFlags(btnEditQty.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (btnEditSrcBin.isEnabled()) {
            btnEditSrcBin.setEnabled(false);
            btnEditSrcBin.setPaintFlags(btnEditSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        if (btnEditDstBin.isEnabled()) {
            btnEditDstBin.setEnabled(false);
            btnEditDstBin.setPaintFlags(btnEditDstBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
    }

    private void disableAllButThis(Button view) {
        switch (view.getId()) {
            case R.id.bnReplenULEditDstBin:
                //disable buttons(confirmQty, src)
                if (btnEditQty.isEnabled()) {
                    btnEditQty.setEnabled(false);
                    btnEditQty.setPaintFlags(btnEditQty.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (btnEditSrcBin.isEnabled()) {
                    btnEditSrcBin.setEnabled(false);
                    btnEditSrcBin.setPaintFlags(btnEditSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (!btnEditDstBin.isEnabled()) {
                    btnEditDstBin.setEnabled(true);
                    btnEditDstBin.setPaintFlags(btnEditDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                break;
            case R.id.bnReplenULEditQtyConfirmed:
                //disable buttons (srcBin, DstBin)
                if (btnEditSrcBin.isEnabled()) {
                    btnEditSrcBin.setEnabled(false);
                    btnEditSrcBin.setPaintFlags(btnEditSrcBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (btnEditDstBin.isEnabled()) {
                    btnEditDstBin.setEnabled(false);
                    btnEditDstBin.setPaintFlags(btnEditDstBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (!btnEditQty.isEnabled()) {
                    btnEditQty.setEnabled(true);
                    btnEditQty.setPaintFlags(btnEditQty.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                break;
            case R.id.bnReplenULEditSrcBin:
                //do disable buttons (confirmQty, DstBin)
                if (btnEditQty.isEnabled()) {
                    btnEditQty.setEnabled(false);
                    btnEditQty.setPaintFlags(btnEditQty.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (btnEditDstBin.isEnabled()) {
                    btnEditDstBin.setEnabled(false);
                    btnEditDstBin.setPaintFlags(btnEditDstBin.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                }
                if (!btnEditQty.isEnabled()) {
                    btnEditQty.setEnabled(true);
                    btnEditQty.setPaintFlags(btnEditQty.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                break;
        }
    }

    private void enableEditButtons() {
        if (!btnEditQty.isEnabled()) {
            btnEditQty.setEnabled(true);
            btnEditQty.setPaintFlags(btnEditQty.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnEditSrcBin.isEnabled()) {
            btnEditSrcBin.setEnabled(true);
            btnEditSrcBin.setPaintFlags(btnEditSrcBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
        if (!btnEditDstBin.isEnabled()) {
            btnEditDstBin.setEnabled(true);
            btnEditDstBin.setPaintFlags(btnEditDstBin.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = getActivity().getSupportFragmentManager();

        //DialogHelper dialog = new DialogHelper(severity, dialogType, message, title);
        //DialogHelper dialog = new DialogHelper();
        ReplenDialogHelper dialog = new ReplenDialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message", message);
        args.putString("Title_ARG", title);
        args.putString("Originated_ARG", UpdateLineFragment.class.getSimpleName());
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

//    @Override
//    public void onDialogMessage(int buttonClicked) {
//        switch (buttonClicked) {
//            case R.integer.MSG_CANCEL:
//                break;
//            case R.integer.MSG_YES:
//                break;
//            case R.integer.MSG_OK:
//                //check if the dialog came from a warning
//                if (getUserVisibleHint()) {
//                    Log.e(TAG, "******************************  Is Visible *********************************");
////                    if (DISPLAY_UPDATE_INFO) {
////                        showUpdateLineDialog();
////                    }
//                    moveline.restoreDefaultQtyValue(); //default value
//                    updateQtyControls();
//                    if (txtQtyConfirmed.isEnabled()) {
//                        txtQtyConfirmed.setEnabled(false);
//                    }
//                    inputByHand = 0;
//                    //TODO - change text button text on EditQty
//                } else {
//                    Log.e(TAG, "******************************  Not Visible *********************************");
//                }
//                break;
//            case R.integer.MSG_NO:
//                break;
//        }
//    }

    private class TextChanged implements TextWatcher {
        private View view = null;

        private TextChanged(View v) {
            this.view = v;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String val = s.toString().trim();
            switch (view.getId()) {
                case R.id.txtvReplenULQtyConfirmed:
                    //do it for quantity
                    if (!val.isEmpty() && com.proper.utils.StringUtils.isNumeric(val)) {
                        int value = Integer.parseInt(s.toString());
                        if (value <= moveline.getQty()) {
                            //confirmedQty = value;
                            setConfirmedQty(value);     //set value
                        } else {
                            //Alert that input cannot be larger than rowTotal, default value, updateControls, disable editText, manageInput to default
                            String mMsg = "Move Quantity cannot be larger than the total found in bin";
                            AlertDialog.Builder builder = new AlertDialog.Builder(UpdateLineFragment.this.getActivity());
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            moveline.restoreDefaultQtyValue(); //default value
                                            updateQtyControls();
                                            if (txtQtyConfirmed.isEnabled()) {
                                                txtQtyConfirmed.setEnabled(false);
                                            }
                                            inputByHand = 0;
                                            //TODO - change text button text on EditQty
                                        }
                                    });
                            builder.show();
                        }
                    }
                    break;
                default:
                    //do others (scr, dst etc..)
                    if (!s.toString().isEmpty()) {
                        String value = s.toString().trim();
                        if (value.length() >= 5) {
                            if (view == txtSrcBin) {
                                moveline.setSrcBinCode(value.toUpperCase());
                                setSrcBin(value.toUpperCase());     //set value
                            }
                            if (view == txtDstBin) {
                                moveline.setSrcBinCode(value.toUpperCase());
                                setDstBin(value.toUpperCase());     //set value
                            }
                        } else {
                            String msg = "Please enter the right BinCode";
                            showDialog(R.integer.MSG_SEVERITY_FAILURE, R.integer.MSG_TYPE_NOTIFICATION, msg, "Invalid BinCode");
                        }
                    }
                    break;
            }
            disableAll();
            enableEditButtons();
        }
    }

    public class UpdateLineChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            // If property changes notify other controls
            String propertyName = e.getPropertyName();
            if ("confirmedQty".equalsIgnoreCase(propertyName)) {
                paintByHandButtons(btnEditDstBin);
                enableEditButtons();
            }
            if ("srcBin".equalsIgnoreCase(propertyName)) {
                paintByHandButtons(btnEditDstBin);
                enableEditButtons();
            }
            if ("dstBin".equalsIgnoreCase(propertyName)) {
                paintByHandButtons(btnEditDstBin);
                enableEditButtons();
            }
            //enableEditButtons();
        }
    }

    @Override
    public void setUserVisibleHint(boolean visible) {

        super.setUserVisibleHint(visible);
        if (visible && isResumed()) {
            //Only manually call onResume if fragment is already visible
            //Otherwise allow natural fragment lifecycle to call onResume
            onResume();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint())
        {
            return;
        }
        if (updateLineAsyncTask != null) {
            buildMessage();
            updateLineAsyncTask.execute(iMessage);
        } else {
            buildMessage();
            updateLineAsyncTask = new UpdateLineAsync();
            updateLineAsyncTask.execute(iMessage);
        }
    }


    private class UpdateLineAsync extends AsyncTask<Message, Void, Object> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            mDialog = new ProgressDialog(mActivity);
            CharSequence title = "Please Wait";
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage("Working hard...Updating Line...");
            mDialog.setTitle(title);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected Object doInBackground(Message... params) {
            Object myResp = null;
            String response = "";
            try {
                //Activity.setMoveListReponseString(mActivity.getResolver().resolveMessageQueue(params[0]));
                response = mActivity.getResolver().resolveMessageQueue(params[0]);
                if (!mActivity.getMoveListReponseString().isEmpty()) {
                    if (mActivity.getMoveListReponseString().contains("Error")) {
                        //manually error trap this error
                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                        mActivity.setToday(new Timestamp(mActivity.getUtilDate().getTime()));
                        LogEntry log = new LogEntry(1L, mActivity.getApplicationID(), "ActReplenResume - queryTask - Line:231", mActivity.getDeviceIMEI(), RuntimeException.class.getSimpleName(), iMsg, mActivity.getToday());
                        mActivity.getLogger().log(log);
                        throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
                    } else {
                        //Process it manually
                        myResp = response;
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                mActivity.setToday(new Timestamp(mActivity.getUtilDate().getTime()));
                LogEntry log = new LogEntry(1L, mActivity.getApplicationID(), "ActReplenResume - doInBackground", mActivity.getDeviceIMEI(), ex.getClass().getSimpleName(), ex.getMessage(), mActivity.getToday());
                mActivity.getLogger().log(log);
            }
            return myResp;
        }

        @Override
        protected void onPostExecute(Object result) {
            //super.onPostExecute(result);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            if (result != null) {
                //TODO - return some important value to the activity and then dismiss this dialog
            }
        }
    }
}
