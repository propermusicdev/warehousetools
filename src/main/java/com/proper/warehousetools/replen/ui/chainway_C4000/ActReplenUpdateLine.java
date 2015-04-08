package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.helpers.ReplenDialogHelper;
import com.proper.data.replen.ReplenLinesItemResponseSelection;
import com.proper.data.replen.ReplenMoveLineResponse;
import com.proper.data.replen.ReplenMoveListLinesItemResponse;
import com.proper.data.replen.ReplenMoveListLinesResponse;
import com.proper.messagequeue.Message;
import com.proper.utils.StringUtils;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.BaseReplenPlainFragmentActivity;
import com.proper.warehousetools.replen.fragments.movelist.zzUpdateLineFragment;
import org.json.JSONArray;
import org.json.JSONObject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Knight on 12/01/2015.
 */
public class ActReplenUpdateLine extends BaseReplenPlainFragmentActivity {
    private String TAG = ActReplenUpdateLine.class.getSimpleName();
    private TextView txtArtist, txtTitle, txtInsertTimeStamp, txtCatalog, txtEAN, txtQty, txtComplete;
    private EditText txtQtyConfirmed, txtSrcBin, txtDstBin;
    private CheckBox chkComplete;
    private Button btnEditQty, btnEditSrcBin, btnEditDstBin, btnUpdate;
    private com.proper.data.core.IReplenCommunicator IReplenCommunicator;
    private int inputByHand = 0, confirmedQty = 0;
    private String srcBin = "", dstBin = "", originalSrcBin = "", originalDstBin= "";
    private boolean qtyConfirmedHasChanged = false, srcBinHasChanged = false, dstBinHasChanged = false, onEditMode = false;
    private ReplenMoveListLinesItemResponse moveline;
    private UpdateLineAsync updateLineAsyncTask = null;
    //private ActReplenManageWork mActivity = null;
    private Message iMessage = null;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private int passedMovelistId = 0;
    private int loopCount = 0;
    private int loadedCount = 0;
    private ReplenLinesItemResponseSelection splitMoveLineSelection = null;
    //private List<ReplenMoveListLinesItemResponse> splitMoveLineSelectionList = null;
    private List<ReplenLinesItemResponseSelection> splitMoveLineSelectionList = null;
    private String moveLinesRepsponseString = "";
    private ReplenMoveListLinesResponse currentListLines = null;

    public int getConfirmedQty() {
        return confirmedQty;
    }

    public void setConfirmedQty(int confirmedQty) {
        pcs.firePropertyChange("confirmedQty", this.confirmedQty, confirmedQty);
        if (confirmedQty > 0) {
            if (onEditMode) {
                qtyConfirmedHasChanged = true;
                onEditMode = false;
            }
        }
        this.confirmedQty = confirmedQty;
    }

    public String getSrcBin() {
        return srcBin;
    }

    public void setSrcBin(String srcBin) {
        pcs.firePropertyChange("srcBin", this.srcBin, srcBin);
        if (!srcBin.equalsIgnoreCase(originalSrcBin)) {
            if (onEditMode) {
                srcBinHasChanged = true;
                onEditMode = false;
            }
        }
        this.srcBin = srcBin;
    }

    public String getDstBin() {
        return dstBin;
    }

    public void setDstBin(String dstBin) {
        pcs.firePropertyChange("dstBin", this.dstBin, dstBin);
        if (!dstBin.equalsIgnoreCase(originalDstBin)) {
            if (onEditMode) {
                dstBinHasChanged = true;
                onEditMode = false;
            }
        }
        this.dstBin = dstBin;
    }

    public List<ReplenLinesItemResponseSelection> getSplitMoveLineSelectionList() {
        return splitMoveLineSelectionList;
    }

    public void setSplitMoveLineSelectionList(List<ReplenLinesItemResponseSelection> splitMoveLineSelectionList) {
        this.splitMoveLineSelectionList = splitMoveLineSelectionList;
    }

    public ReplenLinesItemResponseSelection getSplitMoveLineSelection() {
        return splitMoveLineSelection;
    }

    public void setSplitMoveLineSelection(ReplenLinesItemResponseSelection splitMoveLineSelection) {
        this.splitMoveLineSelection = splitMoveLineSelection;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public String getMoveLinesRepsponseString() {
        return moveLinesRepsponseString;
    }

    public void setMoveLinesRepsponseString(String moveLinesRepsponseString) {
        this.moveLinesRepsponseString = moveLinesRepsponseString;
    }

    public ReplenMoveListLinesResponse getCurrentListLines() {
        return currentListLines;
    }

    public void setCurrentListLines(ReplenMoveListLinesResponse currentListLines) {
        this.currentListLines = currentListLines;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.lyt_replen_fgm_update_line);
        LinearLayout lytMain = (LinearLayout) this.findViewById(R.id.lyt_ReplenULOutterWrapper);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) lytMain.getLayoutParams();
        txtArtist = (TextView) this.findViewById(R.id.txtvReplenULArtist);
        txtTitle = (TextView) this.findViewById(R.id.txtvReplenULTitle);
        txtInsertTimeStamp = (TextView) this.findViewById(R.id.txtvReplenULInsertTimeStamp);
        txtCatalog = (TextView) this.findViewById(R.id.txtvReplenULCatalog);
        txtEAN = (TextView) this.findViewById(R.id.txtvReplenULEAN);
        txtQty = (TextView) this.findViewById(R.id.txtvReplenULQuantity);
        txtQtyConfirmed = (EditText) this.findViewById(R.id.txtvReplenULQtyConfirmed);
        txtQtyConfirmed.addTextChangedListener(new TextChanged(txtQtyConfirmed));
        txtSrcBin = (EditText) this.findViewById(R.id.txtvReplenULSrcBin);
        txtSrcBin.addTextChangedListener(new TextChanged(txtSrcBin));
        txtDstBin = (EditText) this.findViewById(R.id.txtvReplenULDstBin);
        txtDstBin.addTextChangedListener(new TextChanged(txtDstBin));
        txtComplete = (TextView) this.findViewById(R.id.txtvReplenULComplete);
        chkComplete = (CheckBox) this.findViewById(R.id.chkReplenULComplete);
        chkComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chkComplete_Clicked(v);
            }
        });
        btnEditQty = (Button) this.findViewById(R.id.bnReplenULEditQtyConfirmed);
        btnEditQty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnEditSrcBin = (Button) this.findViewById(R.id.bnReplenULEditSrcBin);
        btnEditSrcBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnEditDstBin = (Button) this.findViewById(R.id.bnReplenULEditDstBin);
        btnEditDstBin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnUpdate = (Button) this.findViewById(R.id.bnReplenULUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        //if (getMoveListResponse() != null && getSelectedMove() != null) {
        loadedCount ++;

        Bundle extras = getIntent().getExtras();
        moveline = (ReplenMoveListLinesItemResponse) extras.getSerializable("SelectedLine_Extra");
        passedMovelistId = extras.getInt("MovelistId_Extra", 0);
        //moveLinesRepsponseString = extras.getString("MoveLinesRepsponseString_Extra");
        if (moveline == null) {
            throw new NullPointerException("SelectedLine_Extra cannot be null");    // Get data from activity
        }

        if (moveline != null) {

            //Populate controls
            txtArtist.setText(moveline.getArtist());
            txtTitle.setText(moveline.getTitle());
            txtInsertTimeStamp.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(moveline.getInsertTimeStamp()));
            txtCatalog.setText(moveline.getCatNumber());
            txtEAN.setText(moveline.getEAN());
            txtQty.setText(String.format("%s", moveline.getQty()));
            txtQtyConfirmed.setText(String.format("%s", confirmedQty));
            txtSrcBin.setText(moveline.getSrcBinCode());
            txtDstBin.setText(moveline.getDstBinCode());

            //Record original values for future comparision
            originalDstBin = moveline.getDstBinCode();
            originalSrcBin = moveline.getSrcBinCode();
        }
        
        disableAll();
        enableEditButtons();
    }

    private void chkComplete_Clicked(View v) {
        if (((CheckBox) v).isChecked()) {
            txtComplete.setText("Complete");
            txtComplete.setTextColor(Color.GREEN);
            new AlertDialog.Builder(this)
                    .setTitle("ATTENTION !!!")
                    .setMessage("Make sure that you've move the item(s) before marking this line as COMPLETED")
                    .setPositiveButton(R.string.DIA_CHECK, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //do nothing for now ...;
                        }
                    }).show();
        } else {
            txtComplete.setText("InComplete");
            txtComplete.setTextColor(Color.RED);
        }
        moveline.setCompleted(((CheckBox) v).isChecked());
    }

    private void buttonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnReplenULEditQtyConfirmed:
                // edit qty
                manageInputByHand(btnEditQty);
                break;
            case R.id.bnReplenULEditSrcBin:
                //edit Source
                manageInputByHand(btnEditSrcBin);
                break;
            case R.id.bnReplenULEditDstBin:
                //edit Destination
                manageInputByHand(btnEditDstBin);
                break;
            case R.id.bnReplenULUpdate:
                //update line
//                if (getUserVisibleHint()) {
//                }
                if (qtyConfirmedHasChanged || srcBinHasChanged || dstBinHasChanged) {
                    loopCount++;
                    updateLineAsyncTask = new UpdateLineAsync();
                    buildMessage();
                    updateLineAsyncTask.execute(iMessage);
                }
                break;
        }
    }

    private String buildParam() {
        String ret = "";
        if (qtyConfirmedHasChanged || srcBinHasChanged || dstBinHasChanged) {
            ret = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"ProductId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\"",
                    passedMovelistId, moveline.getMovelistLineId(), moveline.getProductId(),
                    currentUser.getUserCode(), currentUser.getUserId());
            if (dstBinHasChanged) {
                ret = ret + String.format(", \"DstBin\":\"%s\"", getDstBin());
            }
            if (srcBinHasChanged) {
                ret = ret + String.format(", \"SrcBin\":\"%s\"", getSrcBin());
            }
            if (qtyConfirmedHasChanged) {
                ret = ret + String.format(", \"UpdateQtyFlag\":\"1\", \"UpdateQty\":\"%s\", \"ConfirmQtyFlag\":\"1\", \"ConfirmQty\":\"%s\"", getConfirmedQty(), getConfirmedQty());
            }
            if (moveline.isCompleted()) {
                ret = ret + String.format(", \"LineComplete\":\"%s\"", 1);
            }
            ret = ret + "}";
        } else {
            ret  = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"ProductId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\"}",
                    passedMovelistId, moveline.getMovelistLineId(), moveline.getProductId(),
                    currentUser.getUserCode(), currentUser.getUserId());
        }
        return ret;
    }

    private void buildMessage() {
        iMessage = new Message();
        if (currentUser != null) {
            if (moveline != null) {
//                String msg = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"ProductId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\"}",
//                        getSelectedMove().getItem().getMovelistId(), getSelectedLine().getMovelistLineId(), getSelectedLine().getProductId(),
//                        getCurrentUser().getUserCode(), getCurrentUser().getUserId());
                String msg = buildParam();
                today = new Timestamp(utilDate.getTime());
                iMessage = new Message();
                iMessage.setSource(deviceIMEI);
                iMessage.setMessageType("UpdateMoveListLine");
                iMessage.setIncomingStatus(1); //default value
                iMessage.setIncomingMessage(msg);
                iMessage.setOutgoingStatus(0);   //default value
                iMessage.setOutgoingMessage("");
                iMessage.setInsertedTimeStamp(today);
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
        InputMethodManager imm =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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
        FragmentManager fm = getSupportFragmentManager();

        //DialogHelper dialog = new DialogHelper(severity, dialogType, message, title);
        //DialogHelper dialog = new DialogHelper();
        ReplenDialogHelper dialog = new ReplenDialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message_ARG", message);
        args.putString("Title_ARG", title);
        args.putString("Originated_ARG", zzUpdateLineFragment.class.getSimpleName());
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

//    @Override
//    public void onDialogMessage_IReplenUpdateLineCommunicator(int buttonClicked) {
//        switch (buttonClicked) {
//            case R.integer.MSG_CANCEL:
//                break;
//            case R.integer.MSG_YES:
//                break;
//            case R.integer.MSG_OK:
//                List<ReplenMoveListLinesItemResponse> list = new ArrayList<ReplenMoveListLinesItemResponse>();
//                if (getSplitMoveLineSelection() != null && getSplitMoveLineSelection().getProductId() > 0) {
//                    //movelineList = new ArrayList<ReplenLinesItemResponseSelection>();
//                    int foundCount = 0;
//                    if (splitMoveLineSelectionList != null && !splitMoveLineSelectionList.isEmpty()) {
//                        for (ReplenLinesItemResponseSelection sel : splitMoveLineSelectionList) {
//                            if (sel.getProductId() ==  getSplitMoveLineSelection().getProductId()) {
//                                foundCount ++;
//                            }
//                        }
//                        if (foundCount == 0) {
//                            splitMoveLineSelectionList.add(getSplitMoveLineSelection());
//                            splitThisLine(); //TODO - check this
//                        }
//                    } else {
//                        splitMoveLineSelectionList.add(getSplitMoveLineSelection());
//                        splitThisLine();
//                    }
//                }
////                for (ReplenLinesItemResponseSelection sel : splitMoveLineSelectionList) {
////                    list.add(sel.toReplenMoveListLinesItemResponse());
////                }
////                //adapter = new ReplenAddMoveLineAdapter(zzSplitLineFragment.this.getActivity(), list);
////                splitLineAdapter = new ReplenAddMoveLineAdapter(this, list);
//                setCurrentQty(getSplitMoveLineSelection().getQty());
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
                    if (!val.isEmpty() && StringUtils.isNumeric(val)) {
                        int value = Integer.parseInt(s.toString());
                        if (value > 0) {
                            if (value <= moveline.getQty()) {
                                //confirmedQty = value;
                                onEditMode = true;
                                setConfirmedQty(value);     //set value
                            } else {
                                //Alert that input cannot be larger than rowTotal, default value, updateControls, disable editText, manageInput to default
                                String mMsg = "Move Quantity cannot be larger than the total found in bin";
                                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenUpdateLine.this);
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
                    }
                    break;
                default:
                    //do others (scr, dst etc..)
                    if (!s.toString().isEmpty()) {
                        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
                        String value = s.toString().trim();
                        boolean hasSpecialChar = p.matcher(s).find();
                        if (value.length() == 5) {
                            if (hasSpecialChar == false) {
                                if (view == txtSrcBin) {
                                    if (!value.equalsIgnoreCase(moveline.getSrcBinCode())) {
                                        onEditMode = true;
                                        moveline.setSrcBinCode(value.toUpperCase());
                                        setSrcBin(value.toUpperCase());     //set value
                                    }

                                }
                                if (view == txtDstBin) {
                                    if (!value.equalsIgnoreCase(moveline.getDstBinCode())) {
                                        onEditMode = true;
                                        moveline.setSrcBinCode(value.toUpperCase());
                                        setDstBin(value.toUpperCase());     //set value
                                    }
                                }
                                disableAll();
                                enableEditButtons();
                            } else {
                                String msg = "Please enter the right BinCode";
                                showDialog(R.integer.MSG_SEVERITY_FAILURE, R.integer.MSG_TYPE_NOTIFICATION, msg, "Invalid BinCode");
                            }
                        }
                    }
                    break;
            }
//            disableAll();
//            enableEditButtons();
        }
    }

    /** Should be called on a background thread **/
    private boolean processUpdateResponse(String updateServerReponse){
        boolean ret = false;
        String baseResponse = getMoveLinesRepsponseString();
        try {
            JSONObject updateResponse = new JSONObject(updateServerReponse);
            JSONObject updateResp = new JSONObject(updateResponse.getString("UpdatedMovelistLine"));
            JSONObject base = new JSONObject(baseResponse);

            int requestedUserId = Integer.parseInt(base.getString("RequestedUserId"));
            int requestedMovelistId = Integer.parseInt(base.getString("RequestedMovelistId"));
            JSONObject move = new JSONObject(base.getString("MoveList"));
            JSONArray moveListLines = base.getJSONArray("MoveListLines");
            //JSONArray moveListLines2 = base.getJSONArray("MoveListLines");  // copy for retaining new value
            List<ReplenMoveListLinesItemResponse> moveListItemResponseList = new ArrayList<ReplenMoveListLinesItemResponse>();

            //get current move list item
            int movelistId = Integer.parseInt(move.getString("MovelistId"));
            String description = move.getString("Description");
            String notes = move.getString("Notes");
            Timestamp insertTimeStamp = Timestamp.valueOf(move.getString("InsertTimeStamp"));
            int assignedTo = Integer.parseInt(move.getString("AssignedTo"));
            int status = Integer.parseInt(move.getString("Status"));
            String statusName = move.getString("StatusName");
            int listType = Integer.parseInt(move.getString("ListType"));
            String listTypeName = move.getString("ListTypeName");
            ReplenMoveLineResponse moveLineItem = new ReplenMoveLineResponse(movelistId, description, notes, insertTimeStamp,
                    assignedTo, status, statusName, listType, listTypeName);
            //Look up move list lines
            int updatedProductId = StringUtils.toInt(updateResp.getString("ProductId"), 0);
            int updatedIndex = -1;

            for (int i = 0; i < moveListLines.length(); i++) {
                JSONObject moveListLine = moveListLines.getJSONObject(i);
                int movelistLineId;
                Timestamp insertTimeStamp2;
                String catNumber = "";
                String artist = "";
                String title = "";
                String ean = "";
                String srcBinCode = "";
                String dstBinCode = "";
                int qty;
                int removeLink;
                int movementId;
                boolean qtyConfirmed = false;
                boolean completed = false;
                String sortOrder = "";

                int productId = StringUtils.toInt(moveListLine.getString("ProductId"), 0);
                if (productId == updatedProductId) {
                    movelistLineId = StringUtils.toInt(moveListLine.getString("MovelistLineId"), 0);
                    insertTimeStamp2 = Timestamp.valueOf(moveListLine.getString("InsertTimeStamp"));
                    catNumber = moveListLine.getString("CatNumber");
                    artist = moveListLine.getString("Artist");
                    title = moveListLine.getString("Title");
                    ean = moveListLine.getString("EAN");
                    srcBinCode = moveListLine.getString("SrcBinCode");
                    dstBinCode = moveListLine.getString("DstBinCode");
                    qty = StringUtils.toInt(moveListLine.getString("Qty"), 0);
                    removeLink = StringUtils.toInt(moveListLine.getString("RemoveLink"), 0);
                    movementId = StringUtils.toInt(moveListLine.getString("MovementId"), 0);
                    qtyConfirmed = StringUtils.toBool(StringUtils.toInt(moveListLine.getString("QtyConfirmed"), 0));
                    completed = StringUtils.toBool(StringUtils.toInt(moveListLine.getString("Completed"), 0));
                    sortOrder = moveListLine.getString("SortOrder");
                } else {
                    movelistLineId = StringUtils.toInt(moveListLine.getString("MovelistLineId"), 0);
                    insertTimeStamp2 = Timestamp.valueOf(moveListLine.getString("InsertTimeStamp"));
                    catNumber = moveListLine.getString("CatNumber");
                    artist = moveListLine.getString("Artist");
                    title = moveListLine.getString("Title");
                    ean = moveListLine.getString("EAN");
                    srcBinCode = moveListLine.getString("SrcBinCode");
                    dstBinCode = moveListLine.getString("DstBinCode");
                    qty = StringUtils.toInt(moveListLine.getString("Qty"), 0);
                    removeLink = StringUtils.toInt(moveListLine.getString("RemoveLink"), 0);
                    movementId = StringUtils.toInt(moveListLine.getString("MovementId"), 0);
                    qtyConfirmed = StringUtils.toBool(StringUtils.toInt(moveListLine.getString("QtyConfirmed"), 0));
                    completed = StringUtils.toBool(StringUtils.toInt(moveListLine.getString("Completed"), 0));
                    sortOrder = moveListLine.getString("SortOrder");
                }

                //Determine if that's what we're looking for
                if (productId == updatedProductId) {
                    updatedIndex = i;
                }
                ReplenMoveListLinesItemResponse item = new ReplenMoveListLinesItemResponse(movelistLineId, insertTimeStamp2, productId,
                        catNumber, artist, title, ean, srcBinCode, dstBinCode, qty, removeLink,movementId, qtyConfirmed, completed, sortOrder);
                moveListItemResponseList.add(item);
            }

            //Modify MoveLinesRepsponseString
            JSONArray newLines = new JSONArray();
            int total = moveListLines.length();
            total --;
            total ++;
            for (int i = 0; i < moveListLines.length(); i++) {
                JSONObject moveListLine = moveListLines.getJSONObject(i);
                int productId = StringUtils.toInt(moveListLine.getString("ProductId"), 0);
                JSONObject line = null;
                if (productId == updatedProductId) {
                    //JSONObject line = new JSONObject()
                    line = new JSONObject()
                            .put("MovelistLineId", updateResp.getString("MovelistLineId"))
                            .put("InsertTimeStamp", updateResp.getString("InsertTimeStamp"))
                            .put("ProductId", updateResp.getString("ProductId"))
                            .put("CatNumber", moveListLine.getString("CatNumber"))
                            .put("Artist", moveListLine.getString("Artist"))
                            .put("Title", moveListLine.getString("Title"))
                            .put("EAN", moveListLine.getString("EAN"))
                            .put("SrcBinCode", updateResp.getString("SrcBinCode"))
                            .put("DstBinCode", updateResp.getString("DstBinCode"))
                            .put("Qty", updateResp.getString("Qty"))
                            .put("RemoveLink", updateResp.getString("RemoveLink").isEmpty() ? "0" : updateResp.getString("RemoveLink"))
                            .put("MovementId", updateResp.getString("MovementId").isEmpty() ? "0" : updateResp.getString("MovementId"))
                            .put("SortOrder", updateResp.getString("SortOrder").isEmpty() ? "0" : updateResp.getString("SortOrder"))
                            .put("QtyConfirmed", updateResp.getString("QtyConfirmed").isEmpty() ? "0" : updateResp.getString("QtyConfirmed"))
                            .put("Completed", updateResp.getString("Completed").isEmpty() ? "0" : updateResp.getString("Completed"));
                    //newLines.put(line);     //Line entry
                }else {
                    line = new JSONObject()
                            .put("MovelistLineId", moveListLine.getString("MovelistLineId"))
                            .put("InsertTimeStamp", moveListLine.getString("InsertTimeStamp"))
                            .put("ProductId", moveListLine.getString("ProductId"))
                            .put("CatNumber", moveListLine.getString("CatNumber"))
                            .put("Artist", moveListLine.getString("Artist"))
                            .put("Title", moveListLine.getString("Title"))
                            .put("EAN", moveListLine.getString("EAN"))
                            .put("SrcBinCode", moveListLine.getString("SrcBinCode"))
                            .put("DstBinCode", moveListLine.getString("DstBinCode"))
                            .put("Qty", moveListLine.getString("Qty"))
                            .put("RemoveLink", moveListLine.getString("RemoveLink").isEmpty() ? "0" : moveListLine.getString("RemoveLink"))
                            .put("MovementId", moveListLine.getString("MovementId").isEmpty() ? "0" : moveListLine.getString("MovementId"))
                            .put("SortOrder", moveListLine.getString("SortOrder").isEmpty() ? "0" : moveListLine.getString("SortOrder"))
                            .put("QtyConfirmed", moveListLine.getString("QtyConfirmed").isEmpty() ? "0" : moveListLine.getString("QtyConfirmed"))
                            .put("Completed", moveListLine.getString("Completed").isEmpty() ? "0" : moveListLine.getString("Completed"));
                    //newLines.put(moveListLine); //Line entry
                }
                newLines.put(line);
            }
            JSONObject result = new JSONObject()
                    .put("RequestedUserId", requestedUserId)
                    .put("RequestedMovelistId", requestedMovelistId)
                    .put("MoveList", move)
                    .put("MoveListLines", newLines);
            if (result.length() < 1) {
                Log.e(TAG, "getMoveLinesRepsponseString() doesn't have any MoveListLines");
                throw new RuntimeException("getMoveLinesRepsponseString() doesn't have any MoveListLines");
            }else {
                ret = true;
                setCurrentListLines(new ReplenMoveListLinesResponse(requestedUserId, requestedMovelistId, moveLineItem, moveListItemResponseList));   //new CurrentListLines
                setMoveLinesRepsponseString(result.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ManageMoveLineFragment - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }
        return ret;
    }

    public class UpdateLineChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            // If property changes notify other controls
            String propertyName = e.getPropertyName();
            if ("confirmedQty".equalsIgnoreCase(propertyName)) {
                paintByHandButtons(btnEditQty);
                enableEditButtons();
            }
            if ("srcBin".equalsIgnoreCase(propertyName)) {
                paintByHandButtons(btnEditSrcBin);
                enableEditButtons();
            }
            if ("dstBin".equalsIgnoreCase(propertyName)) {
                paintByHandButtons(btnEditDstBin);
                enableEditButtons();
            }
            //enableEditButtons();
        }
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        if (!getUserVisibleHint())
//        {
//            return;
//        } else {
//            if (loopCount > 0) {
//                loadData();
//                if (updateLineAsyncTask != null) {
//                    buildMessage();
//                    //updateLineAsyncTask.execute(iMessage);
//                } else {
//                    buildMessage();
//                    updateLineAsyncTask = new UpdateLineAsync();
//                    //updateLineAsyncTask.execute(iMessage);
//                }
//            } else {
//                if (loadedCount > 0) {
//                    loadData();
//                }
//            }
//        }
//    }

    private class UpdateLineAsync extends AsyncTask<Message, Void, HttpResponseHelper> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            mDialog = new ProgressDialog(ActReplenUpdateLine.this);
            CharSequence title = "Please Wait";
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage("Working hard...Updating Line...");
            mDialog.setTitle(title);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

//        @Override
//        protected Boolean doInBackground(Message... params) {
//            boolean result = false;
//            String response = "";
//            try {
//                if (qtyConfirmedHasChanged) {
//                }
//                response = resolver.resolveMessageQueue(params[0]);
//                //setMoveLinesRepsponseString(getResolver().resolveMessageQueue(params[0]));
//                //if (!getMoveListReponseString().isEmpty()) {
//                if (!response.isEmpty()) {
//                    //if (getMoveListReponseString().contains("Error")) {
//                    if (response.contains("Error")) {
//                        //manually error trap this error
//                        String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
//                        today = new Timestamp(utilDate.getTime());
//                        LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenResume - queryTask - Line:231", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                        logger.log(log);
//                        throw new RuntimeException("The barcode you have scanned have not been recognised. Please check and scan again");
//                    } else {
//                        //Process it manually
//                        result = processUpdateResponse(response);
//                    }
//                }
//            } catch(Exception ex) {
//                ex.printStackTrace();
//                today = new Timestamp(utilDate.getTime());
//                LogEntry log = new LogEntry(1L, ApplicationID, "ActReplenResume - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
//                logger.log(log);
//            }
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(Boolean result) {
//            if (mDialog != null && mDialog.isShowing()) {
//                mDialog.dismiss();
//            }
//            if (result) {
//                //TODO - return some important value to the activity and then dismiss this dialog
//                String mMsg = "Line Updated Successfully";
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenUpdateLine.this);
//                builder.setMessage(mMsg)
//                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
////                                PREVIOUS_TAB = MOVE_UPDATE_TAB;
////                                setCanDisplayUpdateInfo(false);
////                                getDisplayedFragment().switchFragment(MOVE_LINE_TAB);
//                                Intent i = new Intent();
//                                i.putExtra("CurrentListLines_Extra", currentListLines);
//                                i.putExtra("MoveLinesRepsponseString_Extra", moveLinesRepsponseString);
//                                setResult(ActReplenManageWork.MOVE_UPDATE_SCREEN, i);
//                                ActReplenUpdateLine.this.finish();
//                            }
//                        });
//                builder.show();
//            }
//        }

        @Override
        protected HttpResponseHelper doInBackground(Message... params) {
            HttpResponseHelper response = null;
            boolean result = false;
            //String response = "";

            response = resolver.resolveHttpMessage(params[0]);
            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error")) {
                String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage(iMsg);
            }else {
                result = true;
            }
            response.setResponse(result);
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper result) {
            //super.onPostExecute(aBoolean);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (!result.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(result.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActReplenUpdateLine.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenUpdateLine.this);
                        builder.setMessage(statusCode.toString() + ": - " + result.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {//do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }
            } else {
                if (result.getResponse().getClass().equals(Boolean.class)) {
                    /**--------------------------- Success -------------------------**/
                    String mMsg = "Line Updated Successfully";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenUpdateLine.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent i = new Intent();
//                                i.putExtra("CurrentListLines_Extra", currentListLines);
//                                i.putExtra("MoveLinesRepsponseString_Extra", moveLinesRepsponseString);
                                    setResult(ActReplenManageWork.MOVE_UPDATE_SCREEN, i);
                                    ActReplenUpdateLine.this.finish();
                                }
                            });
                    builder.show();
                }else { //not necessary but just to make sure...
                    Vibrator vib = (Vibrator) ActReplenUpdateLine.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenUpdateLine.this);
                    builder.setMessage("Unable to cast response to required object [Boolean]. " + result.getResponseMessage())
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {//do nothing
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                }
            }
        }
    }
}