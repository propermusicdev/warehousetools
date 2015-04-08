package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.proper.data.core.IReplenSplitLineCommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.replen.ReplenLineFeedBackResponse;
import com.proper.data.replen.ReplenLinesItemResponseSelection;
import com.proper.data.replen.ReplenMoveListLinesItemResponse;
import com.proper.data.replen.adapters.ReplenAddMoveLineAdapter;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.BaseReplenPlainFragmentActivity;
import com.proper.warehousetools.replen.fragments.movelist.SplitLineQuantityFragment;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 08/01/2015.
 * Subtract line from an existing one, only requires (SrcBin, DstBin) if it differs from the existing one
 * passed activity Properties: MoveListId, UserId, UserCode, ProductId, SrcBin, DstBin, Qty, MovelistLineId
 */
public class ActReplenSplitLine extends BaseReplenPlainFragmentActivity implements IReplenSplitLineCommunicator {
    private String TAG = ActReplenSplitLine.class.getSimpleName();
    private TextView txtArtist, txtTitle, txtLineID, txtCatalog, txtSrcBin, txtDstBin, txtQuantity;
    private Button btnSplit, btnAdd;
    private ListView lvLines;
    private int passedMovelistId = 0, passedQuantity = 0, splitMoveLineSelectionChangedCount = 0;
    private boolean hasSplitMoveLineSelectionChanged = false;
    private com.proper.data.core.IReplenCommunicator IReplenCommunicator;
    private ReplenMoveListLinesItemResponse moveline = null, currentSplitLine = null;
    //private ReplenSelectedMoveWrapper selectedMove = null;
    //private List<ReplenLinesItemResponseSelection> movelineList = null;
    private SplitLineAsync splitLineAsyncTask = null;
    //private ActReplenManageWork mActivity = null;
    private Message aMessage = null;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ReplenAddMoveLineAdapter adapter = null;
    private int originalQty, currentQty, loadedCount;
    private ReplenAddMoveLineAdapter splitLineAdapter = null;
    //private ReplenLinesItemResponseSelection split
    private ReplenLinesItemResponseSelection splitMoveLineSelection = null;
    //private List<ReplenMoveListLinesItemResponse> splitMoveLineSelectionList = null;
    private List<ReplenLinesItemResponseSelection> splitMoveLineSelectionList = null, splitMoveLineSelectionListOld = null;

    public ReplenMoveListLinesItemResponse getMoveline() {
        return moveline;
    }

    public void setMoveline(ReplenMoveListLinesItemResponse moveline) {
        this.moveline = moveline;
    }

    public List<ReplenLinesItemResponseSelection> getSplitMoveLineSelectionList() {
        return splitMoveLineSelectionList;
    }

    public void setSplitMoveLineSelectionList(List<ReplenLinesItemResponseSelection> splitMoveLineSelectionList) {
        this.splitMoveLineSelectionList = splitMoveLineSelectionList;
    }

    public List<ReplenLinesItemResponseSelection> getSplitMoveLineSelectionListOld() {
        return splitMoveLineSelectionListOld;
    }

    public void setSplitMoveLineSelectionListOld(List<ReplenLinesItemResponseSelection> splitMoveLineSelectionListOld) {
        this.splitMoveLineSelectionListOld = splitMoveLineSelectionListOld;
    }

    public ReplenLinesItemResponseSelection getSplitMoveLineSelection() {
        return splitMoveLineSelection;
    }

    public void setSplitMoveLineSelection(ReplenLinesItemResponseSelection splitMoveLineSelection) {
        this.splitMoveLineSelection = splitMoveLineSelection;
        pcs.firePropertyChange("splitMoveLineSelection", this.splitMoveLineSelection, splitMoveLineSelection);
    }

    public ReplenAddMoveLineAdapter getSplitLineAdapter() {
        return splitLineAdapter;
    }

    public void setSplitLineAdapter(ReplenAddMoveLineAdapter splitLineAdapter) {
        this.splitLineAdapter = splitLineAdapter;
    }

    public int getCurrentQty() {
        return currentQty;
    }

    public void setCurrentQty(int currentQty) {
        this.currentQty = currentQty;
        pcs.firePropertyChange("currentQty", this.currentQty, currentQty);
    }

    public int getPassedMovelistId() {
        return passedMovelistId;
    }

    public void setPassedMovelistId(int passedMovelistId) {
        this.passedMovelistId = passedMovelistId;
    }

    private int ConvertPixelsToDp(float pixelValue)
    {
        int dp = (int) ((pixelValue)/ getResources().getDisplayMetrics().density);
        return dp;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_fgm_split_line);
        //mActivity = getParentActivityIntent();
        this.addPropertyChangeListener(new UpdateLineChangedListener()); // add property change listener
        txtArtist = (TextView) this.findViewById(R.id.txtvReplenSLArtist);
        txtTitle = (TextView) this.findViewById(R.id.txtvReplenSLTitle);
        txtLineID = (TextView) this.findViewById(R.id.txtvReplenSLID);
        txtCatalog = (TextView) this.findViewById(R.id.txtvReplenSLCatalog);
        txtSrcBin = (TextView) this.findViewById(R.id.txtvReplenSLSrcBin);
        txtDstBin = (TextView) this.findViewById(R.id.txtvReplenSLDstBin);
        txtQuantity = (TextView) this.findViewById(R.id.txtvReplenSLQty);
        btnSplit = (Button) this.findViewById(R.id.bnReplenSLSplit);
        btnSplit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnAdd = (Button) this.findViewById(R.id.bnReplenSLAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        lvLines = (ListView) this.findViewById(R.id.lvReplenSLLines);
        lvLines.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(parent, view, position, id);
            }
        });
        //TODO - Retrieve values from ActReplenManageWork to allow splitting
        Bundle extras = getIntent().getExtras();
        setSplitMoveLineSelectionListOld((List<ReplenLinesItemResponseSelection>) extras.getSerializable("SplitMoveLineSelectionList_Extra"));
        setMoveline((ReplenMoveListLinesItemResponse) extras.getSerializable("SelectedLine_Extra"));
        passedMovelistId = extras.getInt("MovelistId_Extra", 0);
        setSplitMoveLineSelection(new ReplenLinesItemResponseSelection(getMoveline()));
        //Populate
        setSplitMoveLineSelectionList(new ArrayList<ReplenLinesItemResponseSelection>()); //old
        if (getMoveline() != null && getMoveline().getMovelistLineId() > 0) {
            passedQuantity = moveline.getQty();
            txtArtist.setText(moveline.getArtist());
            txtTitle.setText(moveline.getTitle());
            txtLineID.setText(String.format("%s", moveline.getMovelistLineId()));
            txtCatalog.setText(moveline.getCatNumber());
            txtSrcBin.setText(String.format(moveline.getSrcBinCode()));
            txtDstBin.setText(String.format(moveline.getDstBinCode()));
            txtQuantity.setText(String.format("%s", passedQuantity));
        }
    }

    private String buildParam() {
        String ret = "";
        boolean srcBinHasChanged = (!getSplitMoveLineSelection().getSrcBinCode().equalsIgnoreCase(getSplitMoveLineSelection().getDefaultSrcBin()));
        boolean dstBinHasChanged = (!getSplitMoveLineSelection().getDstBinCode().equalsIgnoreCase(getSplitMoveLineSelection().getDefaultDstBin()));

        if (srcBinHasChanged || dstBinHasChanged) {
            ret = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\", \"ProductId\":\"%s\", \"Qty\":\"%s\"",
                    passedMovelistId, getSplitMoveLineSelection().getMovelistLineId(), currentUser.getUserCode(), currentUser.getUserId(),
                    getSplitMoveLineSelection().getProductId(), getSplitMoveLineSelection().getQtyToSplit());
            if (dstBinHasChanged) {
                ret = ret + String.format(", \"DstBin\":\"%s\"", getSplitMoveLineSelection().getDstBinCode());
            }
            if (srcBinHasChanged) {
                ret = ret + String.format(", \"SrcBin\":\"%s\"", getSplitMoveLineSelection().getSrcBinCode());
            }
//            if (moveline.isCompleted()) {
//                ret = ret + String.format(", \"LineComplete\":\"%s\"", 1);
//            }
            ret = ret + "}";
        } else {
            ret  = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\", \"ProductId\":\"%s\", \"SrcBin\":\"%s\", \"DstBin\":\"%s\", \"Qty\":\"%s\"}",
                    passedMovelistId, getSplitMoveLineSelection().getMovelistLineId(), currentUser.getUserCode(), currentUser.getUserId(),
                    getSplitMoveLineSelection().getProductId(), getSplitMoveLineSelection().getSrcBinCode(), getSplitMoveLineSelection().getDstBinCode(),
                    getSplitMoveLineSelection().getQtyToSplit());
        }
        return ret;
    }

    private void buildMessage() {
        if (currentUser != null) {
            if (getSplitMoveLineSelection() != null) {
                String msg = buildParam();
                today = new Timestamp(utilDate.getTime());
                aMessage = new Message();
                aMessage.setSource(deviceIMEI);
                aMessage.setMessageType("AddMovelistLine");
                aMessage.setIncomingStatus(1); //default value
                aMessage.setIncomingMessage(msg);
                aMessage.setOutgoingStatus(0);   //default value
                aMessage.setOutgoingMessage("");
                aMessage.setInsertedTimeStamp(today);
                aMessage.setTTL(100);    //default value
            } else {
                Log.e("**************************ERROR", "getSelectedMove is current null *********************************");
            }
        }
    }

    private void showQuantityDialog() {
        if (moveline != null) {
            FragmentManager fm = getSupportFragmentManager();
            SplitLineQuantityFragment dialog = new SplitLineQuantityFragment();
//            Bundle args = new Bundle();
//            args.putSerializable("LINE_EXTRA", moveline);
//            dialog.setArguments(args);
            dialog.show(fm, "SplitLineQuantityFragment");
        }
    }

    private void buttonClicked(View v) {
        if (v == btnAdd) {
            //TODO - Commit all pending lines (splitMoveLineSelectionList) and return to ManageMoveLineFragment
            final String msg = "Split Completed !";
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Done");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent intent = new Intent();
                    intent.putExtra("SplitMoveLineSelectionList_Return", (Serializable) getSplitMoveLineSelectionList());
                    setResult(ActReplenManageWork.MOVE_SPLIT_SCREEN, intent);
                    ActReplenSplitLine.this.finish();
                }
            });
            alert.show();
        }
        if (v == btnSplit) {
            //TODO - create a new line by subtracting from the total quantity
            showQuantityDialog();
        }
    }

    private void splitThisLine() {
        if (moveline != null) {
            buildMessage();
            splitLineAsyncTask = new SplitLineAsync();
            splitLineAsyncTask.execute(aMessage);
            //splitLineAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, aMessage); //v4.0 +
        }
    }

    private void listItemClicked(AdapterView<?> parent, View view, int position, long id) {
        //do nothing for now...
    }

    @Override
    public void onDialogMessage_IReplenSplitLineCommunicator(int buttonClicked) {
        //TODO - Complete this *****
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                List<ReplenMoveListLinesItemResponse> list = new ArrayList<ReplenMoveListLinesItemResponse>();
                if (getSplitMoveLineSelection() != null && getSplitMoveLineSelection().getProductId() > 0) {
                    //movelineList = new ArrayList<ReplenLinesItemResponseSelection>();
                    int foundCount = 0;
                    if (splitMoveLineSelectionList != null && !splitMoveLineSelectionList.isEmpty()) {
                        for (ReplenLinesItemResponseSelection sel : splitMoveLineSelectionList) {
                            if (sel.getProductId() ==  getSplitMoveLineSelection().getProductId()) {
                                foundCount ++;
                            }
                        }
                        if (foundCount == 0) {
                            splitMoveLineSelectionList.add(getSplitMoveLineSelection());
                            splitThisLine(); //TODO - watch this method carefully
                        }
                    } else {
                        splitMoveLineSelectionList.add(getSplitMoveLineSelection());
                        splitThisLine();
                    }
                }
                setCurrentQty(getSplitMoveLineSelection().getQty());
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

    private void updateDisplayQuantity() {
        if (getSplitMoveLineSelection() != null && getSplitMoveLineSelection().getQtyToSplit() > 0) {
            txtQuantity.setText(String.format("%s", this.getSplitMoveLineSelection().getQty()));
        }else{
            txtQuantity.setText(String.format("%s", this.currentQty));
        }
    }

    public class UpdateLineChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            // If property changes notify other controls
            String propertyName = e.getPropertyName();
            if ("confirmedQty".equalsIgnoreCase(propertyName)) {
                //paintByHandButtons(btnEditDstBin);
                //enableEditButtons();
                Toast.makeText(ActReplenSplitLine.this, "confirmedQty has changed", Toast.LENGTH_SHORT);
            }
            if ("srcBin".equalsIgnoreCase(propertyName)) {
                //paintByHandButtons(btnEditDstBin);
                //enableEditButtons();
                Toast.makeText(ActReplenSplitLine.this, "Source Bin has changed", Toast.LENGTH_SHORT);
            }
            if ("dstBin".equalsIgnoreCase(propertyName)) {
                //paintByHandButtons(btnEditDstBin);
                //enableEditButtons();
                Toast.makeText(ActReplenSplitLine.this, "Destination Bin has changed", Toast.LENGTH_SHORT);
            }
            if ("currentQty".equalsIgnoreCase(propertyName)) {
                updateDisplayQuantity();
            }
            if ("splitMoveLineSelection".equalsIgnoreCase(propertyName)) {
                if (e.getOldValue() != e.getNewValue()) {
                    hasSplitMoveLineSelectionChanged = true;
                    splitMoveLineSelectionChangedCount ++;
                }
            }
            //enableEditButtons();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    private class SplitLineAsync extends AsyncTask<Message, Void, HttpResponseHelper> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            mDialog = new ProgressDialog(ActReplenSplitLine.this);
            CharSequence title = "Please Wait";
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage("Working hard...Splitting Line...");
            mDialog.setTitle(title);
            mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(Message... params) {
            HttpResponseHelper response = null;
            ReplenLineFeedBackResponse myResp = null;
            //String response = "";
            response = resolver.resolveHttpMessage(params[0]);

            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage(iMsg);
            }else {
                try {
                    myResp = replenResponseHelper.refineSplitResponse(response.getResponse().toString());
                    response.setResponse(myResp);
                } catch(Exception ex) {
                    ex.printStackTrace();
                    response.setExceptionClass(ex.getClass());
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            //super.onPostExecute(result);
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }

            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActReplenSplitLine.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSplitLine.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {//do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(ReplenLineFeedBackResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        ReplenLineFeedBackResponse resp = (ReplenLineFeedBackResponse) response.getResponse();
                        List<ReplenMoveListLinesItemResponse> list = new ArrayList<ReplenMoveListLinesItemResponse>();
                        list.add(new ReplenMoveListLinesItemResponse(getMoveline(), resp));
                        setSplitLineAdapter(new ReplenAddMoveLineAdapter(ActReplenSplitLine.this, list));
                        lvLines.setAdapter(getSplitLineAdapter());
                        //mActivity.setSplitLineConfig(mActivity.SPLITLINE_CONFIG_HALT);
                    }else{ //unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActReplenSplitLine.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSplitLine.this);
                        builder.setMessage("Unable to convert result to object required [ReplenLineFeedBackResponse] \n" +
                        response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {//do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActReplenSplitLine.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenSplitLine.this);
                    builder.setMessage("The product query has return no result\nPlease verify then re-scan")
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