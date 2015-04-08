package com.proper.warehousetools.replen.fragments.movelist;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.replen.ReplenLinesItemResponseSelection;
import com.proper.data.replen.ReplenMoveListLinesItemResponse;
import com.proper.data.replen.adapters.ReplenAddMoveLineAdapter;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenManageWork;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 15/12/2014.
 */
public class zzSplitLineFragment extends Fragment {
    private String TAG = zzSplitLineFragment.class.getSimpleName();
    private TextView txtArtist, txtTitle, txtLineID, txtCatalog, txtSrcBin, txtDstBin, txtQuantity;
    private Button btnSplit, btnAdd;
    private ListView lvLines;
    private com.proper.data.core.IReplenCommunicator IReplenCommunicator;
    private ReplenMoveListLinesItemResponse moveline = null, currentSplitLine = null;
    //private List<ReplenLinesItemResponseSelection> movelineList = null;
    private SplitLineAsync splitLineAsyncTask = null;
    private ActReplenManageWork mActivity = null;
    private Message aMessage = null;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ReplenAddMoveLineAdapter adapter = null;
    private int originalQty, currentQty, loadedCount;

    public int getCurrentQty() {
        return currentQty;
    }

    public void setCurrentQty(int currentQty) {
        pcs.firePropertyChange("currentQty", this.currentQty, currentQty);
        if (currentQty != originalQty) {
//            if (onEditMode) {
//                dstBinHasChanged = true;
//                onEditMode = false;
//            }
        }
        this.currentQty = currentQty;
    }

    public zzSplitLineFragment() {
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }
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
        setHasOptionsMenu(true);
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }
        this.addPropertyChangeListener(new UpdateLineChangedListener()); // add property change listener
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        loadedCount ++;
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }

        View view = inflater.inflate(R.layout.lyt_replen_fgm_split_line, null);
        txtArtist = (TextView) view.findViewById(R.id.txtvReplenSLArtist);
        txtTitle = (TextView) view.findViewById(R.id.txtvReplenSLTitle);
        txtLineID = (TextView) view.findViewById(R.id.txtvReplenSLID);
        txtCatalog = (TextView) view.findViewById(R.id.txtvReplenSLCatalog);
        txtSrcBin = (TextView) view.findViewById(R.id.txtvReplenSLSrcBin);
        txtDstBin = (TextView) view.findViewById(R.id.txtvReplenSLDstBin);
        txtQuantity = (TextView) view.findViewById(R.id.txtvReplenSLQty);
        btnSplit = (Button) view.findViewById(R.id.bnReplenSLSplit);
        btnSplit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnAdd = (Button) view.findViewById(R.id.bnReplenSLAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        lvLines = (ListView) view.findViewById(R.id.lvReplenSLLines);
        lvLines.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(parent, view, position, id);
            }
        });
        loadData();
        //savedInstanceState.putAll(savedInstanceState);
        return view;
    }

    private void loadData() {
        /** Initialise our list container **/
        mActivity.setSplitMoveLineSelectionList(new ArrayList<ReplenLinesItemResponseSelection>());
        if (mActivity.getSelectedLine() != null && mActivity.getSelectedLine().getMovelistLineId() > 0) {
            moveline = mActivity.getSelectedLine();
            txtArtist.setText(moveline.getArtist());
            txtTitle.setText(moveline.getTitle());
            txtLineID.setText(String.format("%s", moveline.getMovelistLineId()));
            txtCatalog.setText(moveline.getCatNumber());
            txtSrcBin.setText(String.format(moveline.getSrcBinCode()));
            txtDstBin.setText(String.format(moveline.getDstBinCode()));
            txtQuantity.setText(String.format("%s", moveline.getQty()));
        }
    }

    private void buildMessage() {
        if (mActivity != null && mActivity.getCurrentUser() != null) {
            if (mActivity.getSplitMoveLineSelection() != null) {
                String msg = String.format("{\"MovelistId\":\"%s\", \"MovelistLineId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\", \"ProductId\":\"%s\", \"SrcBin\":\"%s\", \"DstBin\":\"%s\", \"Qty\":\"%s\"}",
                        mActivity.getSelectedMove().getItem().getMovelistId(), moveline.getMovelistLineId(),mActivity.getCurrentUser().getUserCode(), mActivity.getCurrentUser().getUserId(),
                        moveline.getProductId(), moveline.getSrcBinCode(), moveline.getDstBinCode(), moveline.getQty());
                mActivity.setToday(new Timestamp(mActivity.getUtilDate().getTime()));
                aMessage = new Message();
                aMessage.setSource(mActivity.getDeviceIMEI());
                aMessage.setMessageType("AddMovelistLine");
                aMessage.setIncomingStatus(1); //default value
                aMessage.setIncomingMessage(msg);
                aMessage.setOutgoingStatus(0);   //default value
                aMessage.setOutgoingMessage("");
                aMessage.setInsertedTimeStamp(mActivity.getToday());
                aMessage.setTTL(100);    //default value
            } else {
                Log.e("**************************ERROR", "getSelectedMove is current null *********************************");
            }
        }
    }

    private void showQuantityDialog() {
        if (moveline != null) {
            FragmentManager fm = mActivity.getSupportFragmentManager();
            SplitLineQuantityFragment dialog = new SplitLineQuantityFragment();
            Bundle args = new Bundle();
            args.putSerializable("LINE_EXTRA", moveline);
            dialog.setArguments(args);
            dialog.show(fm, "SplitLineQuantityFragment");
            int zero = 12;
            zero ++;
            zero = (zero * 2 + (zero / 100));
            zero --;
        }
    }

    private void buttonClicked(View v) {
        if (v == btnAdd) {
            //TODO - Commit all pending lines (splitMoveLineSelectionList) and return to ManageMoveLineFragment
            final String msg = "Split Completed !";
            AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
            alert.setTitle("Done");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO - Dismiss this Dialog and return to ManageMoveLineFragment
                    mActivity.setCanNavigate(true); /** Tell it to reload data **/
                    mActivity.getDisplayedFragment().switchFragment(mActivity.MOVE_LINE_TAB);
                    if (mActivity.getSplitMoveLineSelectionList().isEmpty()) {
                        mActivity.setMovelinesHasChanged(false);
                    }else {
                        mActivity.setMovelinesHasChanged(true);
                    }
                }
            });
            alert.show();
        }
        if (v == btnSplit) {
            //TODO - create a new line by subtracting from the total quantity
            showQuantityDialog();
        }
    }

    public void SplitLine() {
        ////TODO - Retain state *****************************    - Retain state

        //if (moveline != null && moveline.getQty() > 1) {
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }
        if (mActivity.getSelectedLine() != null && mActivity.getSelectedLine().getQty() > 1) {
            if (moveline == null || moveline.getQty() < 1) {
                moveline = mActivity.getSelectedLine();
            }
            buildMessage();
            splitLineAsyncTask = new SplitLineAsync();
            splitLineAsyncTask.execute(aMessage);
        }
    }

    private void listItemClicked(AdapterView<?> parent, View view, int position, long id) {
        //do nothing for now...
    }

    public class UpdateLineChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            // If property changes notify other controls
            String propertyName = e.getPropertyName();
            if ("confirmedQty".equalsIgnoreCase(propertyName)) {
                //paintByHandButtons(btnEditDstBin);
                //enableEditButtons();
                Toast.makeText(mActivity, "confirmedQty has changed", Toast.LENGTH_SHORT);
            }
            if ("srcBin".equalsIgnoreCase(propertyName)) {
                //paintByHandButtons(btnEditDstBin);
                //enableEditButtons();
                Toast.makeText(mActivity, "Source Bin has changed", Toast.LENGTH_SHORT);
            }
            if ("dstBin".equalsIgnoreCase(propertyName)) {
                //paintByHandButtons(btnEditDstBin);
                //enableEditButtons();
                Toast.makeText(mActivity, "Destination Bin has changed", Toast.LENGTH_SHORT);
            }
            //enableEditButtons();
        }
    }

//    @Override
//    public void onDialogMessage(int buttonClicked) {
//        switch (buttonClicked) {
//            case R.integer.MSG_CANCEL:
//                break;
//            case R.integer.MSG_YES:
//                break;
//            case R.integer.MSG_OK:
//                List<ReplenMoveListLinesItemResponse> list = new ArrayList<ReplenMoveListLinesItemResponse>();
//                if (mActivity.getSplitMoveLineSelection() != null && mActivity.getSplitMoveLineSelection().getProductId() > 0) {
//                    movelineList = new ArrayList<ReplenLinesItemResponseSelection>();
//                    int foundCount = 0;
//                    if (moveline != null && !movelineList.isEmpty()) {
//                        for (ReplenLinesItemResponseSelection sel : movelineList) {
//                            if (sel.getProductId() ==  mActivity.getSplitMoveLineSelection().getProductId()) {
//                                foundCount ++;
//                            }
//                        }
//                        if (foundCount == 0) {
//                            movelineList.add(mActivity.getSplitMoveLineSelection());
//                            SplitLine();
//                        }
//                    } else {
//                        movelineList.add(mActivity.getSplitMoveLineSelection());
//                        SplitLine();
//                    }
//                }
//                for (ReplenLinesItemResponseSelection sel : movelineList) {
//                    list.add(sel.toReplenMoveListLinesItemResponse());
//                }
//                adapter = new ReplenAddMoveLineAdapter(zzSplitLineFragment.this.getActivity(), list);
//                break;
//            case R.integer.MSG_NO:
//                break;
//        }
//    }

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
//        if (mActivity.PREVIOUS_TAB == mActivity.MOVE_ADD_TAB) {
//        }
//        switch (mActivity.getSplitLineConfig()) {
//            case ActReplenManageWork.ADDLINE_CONFIG_NEWLINE:
//                if (mActivity.getSelectedLine() != null && mActivity.getSelectedLine().getMovelistLineId() > 0) {
//                    if (splitLineAsyncTask != null) {
//                        //buildMessage();
//                        splitLineAsyncTask.execute(aMessage);
//                    } else {
//                        //buildMessage();
//                        splitLineAsyncTask = new SplitLineAsync();
//                        splitLineAsyncTask.execute(aMessage);
//                    }
//                }
//                break;
//            case ActReplenManageWork.ADDLINE_CONFIG_SPLITLINE:
//                //do
//                break;
//        }

        if (mActivity.getSelectedLine() != null && mActivity.getSelectedLine().getMovelistLineId() > 0) {
            if (splitLineAsyncTask != null) {
                if (!splitLineAsyncTask.isCancelled()) {
                    splitLineAsyncTask.cancel(true);
                }
                loadData();
                splitLineAsyncTask = new SplitLineAsync();
                splitLineAsyncTask.execute(aMessage);
            } else {
                loadData();
//                splitLineAsyncTask = new SplitLineAsync();
//                splitLineAsyncTask.execute(aMessage);
            }
        }

        //TODO - if split finish then proceed with splitLine
        if (mActivity.getSplitLineConfig() == mActivity.SPLITLINE_CONFIG_PROCEED) {
            SplitLine();
        }
    }


    private class SplitLineAsync extends AsyncTask<Message, Void, Object> {
        private ProgressDialog mDialog;

        @Override
        protected void onPreExecute() {
            //super.onPreExecute();
            mDialog = new ProgressDialog(mActivity);
            CharSequence title = "Please Wait";
            mDialog.setCancelable(true);
            mDialog.setCanceledOnTouchOutside(false);
            mDialog.setMessage("Working hard...Splitting Line...");
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

                List<ReplenMoveListLinesItemResponse> list = new ArrayList<ReplenMoveListLinesItemResponse>();
//                int foundCount = 0;
//                if (mActivity.getSplitMoveLineSelectionList() != null && !mActivity.getSplitMoveLineSelectionList().isEmpty()) {
//                    for (ReplenLinesItemResponseSelection sel : mActivity.getSplitMoveLineSelectionList()) {
//                        if (sel.getProductId() == mActivity.getSplitMoveLineSelection().getProductId()) {
//                            foundCount++;
//                        }
//                    }
//                    if (foundCount == 0) {
//                        mActivity.getSplitMoveLineSelectionList().add(mActivity.getSplitMoveLineSelection());
//                    }
//                } else {
//                    mActivity.getSplitMoveLineSelectionList().add(mActivity.getSplitMoveLineSelection());
//                }
                for (ReplenLinesItemResponseSelection sel : mActivity.getSplitMoveLineSelectionList()) {
                    list.add(sel.toReplenMoveListLinesItemResponse());
                }
                mActivity.setSplitLineAdapter(new ReplenAddMoveLineAdapter(mActivity, list));
                mActivity.setSplitLineConfig(mActivity.SPLITLINE_CONFIG_HALT);
            }
        }
    }
}
