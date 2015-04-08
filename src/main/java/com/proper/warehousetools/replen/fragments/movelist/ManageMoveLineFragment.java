package com.proper.warehousetools.replen.fragments.movelist;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.*;
import android.widget.ExpandableListView;
import android.widget.ViewFlipper;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.DialogHelper;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.helpers.ReplenDialogHelper;
import com.proper.data.replen.*;
import com.proper.data.replen.adapters.ReplenMoveLineAdapter;
import com.proper.messagequeue.Message;
import com.proper.utils.StringUtils;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenManageWork;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 01/12/2014.
 */
//public class ManageMoveLineFragment extends Fragment implements ICommunicator {
public class ManageMoveLineFragment extends Fragment {
   private String TAG = ManageMoveLineFragment.class.getSimpleName();
    private static final String menuItemMoveLine = "Update MoveLine";
    private static final String menuItemMoveLineAdd = "Add MoveLine";
    private static final String menuItemMoveLineSplit = "Split MoveLine";
    private int groupPos;
    private boolean DISPLAY_UPDATE_INFO = false, DISPLAY_ADD_INFO = false;
    private RetrieveWorkLineAsync getWorkLineAsync = null;
    private ActReplenManageWork mActivity = null;
    private ViewFlipper flipper;
    private ExpandableListView lvWorkLines;
    private Message xMessage = null;

    public ManageMoveLineFragment() {
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }

        // retrieve possible moves from move line from the database

        if (mActivity.getMoveListResponse() != null && mActivity.getSelectedMove() != null) {
            buildMessage();
            getWorkLineAsync = new RetrieveWorkLineAsync();
            getWorkLineAsync.execute(xMessage);  //executes both -> Send Queue Directly AND Send queue to Service
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        if (mActivity == null) {
            mActivity = (ActReplenManageWork) getActivity();
        }

        View view = inflater.inflate(R.layout.lyt_replen_movelines, container, false);
        flipper = (ViewFlipper) view.findViewById(R.id.vfReplenMoveLines);
        lvWorkLines = (ExpandableListView) view.findViewById(R.id.lvReplenMoveLines);
        lvWorkLines.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
                onListViewGroupClicked(parent, view, groupPosition, id);
                return false;
            }
        });

        lvWorkLines.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        registerForContextMenu(lvWorkLines);

        // retrieve possible moves from move line from the database

//        if (mActivity.getMoveListResponse() != null && mActivity.getSelectedMove() != null) {
//            buildMessage();
//            getWorkLineAsync = new RetrieveWorkLineAsync();
//            getWorkLineAsync.execute(xMessage);  //executes both -> Send Queue Directly AND Send queue to Service
//        }
        return view;
    }

    private void buildMessage() {
        if (mActivity != null && mActivity.getCurrentUser() != null) {
            if (mActivity.getSelectedMove() != null) {
                String msg = String.format("{\"MovelistId\":\"%s\", \"UserCode\":\"%s\", \"UserId\":\"%s\"}",
                        mActivity.getSelectedMove().getItem().getMovelistId(), mActivity.getCurrentUser().getUserCode(), mActivity.getCurrentUser().getUserId());
                mActivity.setToday(new Timestamp(mActivity.getUtilDate().getTime()));
                xMessage = new Message();
                xMessage.setSource(mActivity.getDeviceIMEI());
                xMessage.setMessageType("GetMoveListLines");
                xMessage.setIncomingStatus(1); //default value
                xMessage.setIncomingMessage(msg);
                xMessage.setOutgoingStatus(0);   //default value
                xMessage.setOutgoingMessage("");
                xMessage.setInsertedTimeStamp(mActivity.getToday());
                xMessage.setTTL(100);    //default value
            } else {
                Log.e("**************************ERROR","getSelectedMove is current null *********************************");
            }
        }
    }

//    private void showUpdateLineDialog() {
//        //FragmentManager fm = getActivity().getSupportFragmentManager();
//        FragmentManager fm = mActivity.getSupportFragmentManager();
//        zzUpdateMoveLineFragment dialog = new zzUpdateMoveLineFragment();
//        dialog.show(fm, "UpdateMoveLineFragmentDialog");
//    }

    private void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = mActivity.getSupportFragmentManager();

        //DialogHelper dialog = new DialogHelper(severity, dialogType, message, title);
        DialogHelper dialog = new DialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message_ARG", message);
        args.putString("Title_ARG", title);
        args.putString("Originated_ARG", ManageMoveLineFragment.class.getSimpleName());
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    private void showNavDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = mActivity.getSupportFragmentManager();

        //DialogHelper dialog = new DialogHelper(severity, dialogType, message, title);
        ReplenDialogHelper dialog = new ReplenDialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message_ARG", message);
        args.putString("Title_ARG", title);
        args.putString("Originated_ARG", ManageMoveLineFragment.class.getSimpleName());
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    private void onListViewGroupClicked(ExpandableListView parent, View view, int groupPosition, long id) {
        if (mActivity.getMoveLineAdapter() != null) {
            mActivity.setSelectedLine(mActivity.getMoveLineAdapter().getGroup(groupPosition)); //current selection
            lvWorkLines.setItemChecked(groupPosition, true);
            view.setSelected(true);
            mActivity.setCurrentLineSelectedIndex(groupPosition);
            groupPos = groupPosition;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.lvReplenMoveLines) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int type =
                    ExpandableListView.getPackedPositionType(info.packedPosition);
            int group =
                    ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int child =
                    ExpandableListView.getPackedPositionChild(info.packedPosition);

            menu.add(menuItemMoveLine);
            menu.add(menuItemMoveLineAdd);
            menu.add(menuItemMoveLineSplit);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (getUserVisibleHint()) {
            if (mActivity.getMoveLineAdapter() != null) {
                ExpandableListView.ExpandableListContextMenuInfo info =
                        (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
                int childPos = 0;
                int type = ExpandableListView.getPackedPositionType(info.packedPosition);
                if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
                }
                if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP && TAG.equalsIgnoreCase(ManageMoveLineFragment.class.getSimpleName())) {
                    groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);

                    mActivity.setSelectedLine(mActivity.getMoveLineAdapter().getGroup(groupPos)); //current selection
                    mActivity.setCurrentLineSelectedIndex(groupPos);
                    lvWorkLines.setSelected(true);
                    lvWorkLines.setItemChecked(groupPos, true);
                    if (item.getTitle().toString().equalsIgnoreCase(menuItemMoveLine)) {
                        final String msg = String.format("Are you sure you want to process this line entry (%s) from move list", groupPos);
//                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//                alert.setTitle("This Move Line ?");
//                alert.setMessage(msg);
//                alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //TODO - Proceed with the currently selected move list
//                    }
//                });
//                alert.setNegativeButton("Cancel", null);
//                alert.show();
                        //mActivity.setCanNavigate(true);
                        DISPLAY_UPDATE_INFO = true;
                        //showUpdateLineDialog();
                        mActivity.setCanDisplayUpdateInfo(true);
                        showNavDialog(R.integer.MSG_SEVERITY_POSITIVE, R.integer.MSG_TYPE_NOTIFICATION, msg, "This Line?");
                    }
                    if (item.getTitle().toString().equalsIgnoreCase(menuItemMoveLineAdd)) {
//                final String msg = String.format("Do you want to see more details for this entry", groupPos);
//                AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
//                alert.setTitle("View Details?");
//                alert.setMessage(msg);
//                alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //TODO - Show show more details for this entry
//                        Intent nav = new Intent(getActivity(), zzActReplenManageConfig.class);
//                        startActivityForResult(nav, getActivity().RESULT_OK);
//                    }
//                });
//                alert.setNegativeButton("Cancel", null);
//                alert.show();
                        mActivity.setCanDisplayAddInfo(true);
                        showNavDialog(R.integer.MSG_SEVERITY_POSITIVE, R.integer.MSG_TYPE_NOTIFICATION, "Test Message 123", "Add New Line?");
                    }
                    if (item.getTitle().toString().equalsIgnoreCase(menuItemMoveLineSplit)) {
                        final String msg = "Are you sure you want to split this quantity to smaller move lines";
                        mActivity.setCanDisplaySplitInfo(true); //give permission to navigate
                        showNavDialog(R.integer.MSG_SEVERITY_POSITIVE, R.integer.MSG_TYPE_NOTIFICATION, msg, "Split This Line?");
                    }
                }
            }
        }
        return super.onContextItemSelected(item);
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
        //BEGIN TODO - Find a way of determining where we've just came from ie: last tab or page etc...
        int loop = 1;
        if (mActivity.PREVIOUS_TAB == mActivity.MOVE_UPDATE_TAB) {
            //do nothing for now, just let the list adapter handle it all
            loop = (loop + 10) - (loop--);
            loop++;
        } else if(mActivity.PREVIOUS_TAB <= 0) {
            /** Initialise Data - retrieve data for the first time **/
            buildMessage();
            getWorkLineAsync = new RetrieveWorkLineAsync();
            getWorkLineAsync.execute(xMessage);
        } else {
            if (mActivity.isMovelinesHasChanged()) {
                if (getWorkLineAsync != null) {
                    if (!getWorkLineAsync.isCancelled()) {
                        getWorkLineAsync.cancel(true);
                        getWorkLineAsync = null;
                    }
                    buildMessage();
                    getWorkLineAsync = new RetrieveWorkLineAsync();
                    getWorkLineAsync.execute(xMessage);
                }else {
                    buildMessage();
                    getWorkLineAsync = new RetrieveWorkLineAsync();
                    getWorkLineAsync.execute(xMessage);
                }
            }
        }
        //END TODO - **********************************************************************************
    }

    private class RetrieveWorkLineAsync extends AsyncTask<Message, Void, HttpResponseHelper> {
        private ProgressDialog iDialog;

        @Override
        protected void onPreExecute() {
            iDialog = new ProgressDialog(getActivity());
            CharSequence message = "Working hard...sending queue [directly] [to webservice]...";
            CharSequence title = "Please Wait";
            iDialog.setCancelable(true);
            iDialog.setCanceledOnTouchOutside(false);
            iDialog.setMessage(message);
            iDialog.setTitle(title);
            iDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            iDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(Message... inputMsg) {
            HttpResponseHelper response = null;
            ReplenMoveListLinesResponse qryResponse = null;

            response = mActivity.getResolver().resolveHttpMessage(inputMsg[0]);
            mActivity.setMoveLinesRepsponseString(response.getResponse().toString());  //TODO - Check this value

            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("not recognised") || response.getResponse().toString().contains("Error")) {
                //manually error trap this error
                String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage(iMsg);
            }else {
                try {
                    if (!mActivity.getMoveLinesRepsponseString().isEmpty()) {
                        JSONObject resp = new JSONObject(mActivity.getMoveLinesRepsponseString());
                        int requestedUserId = Integer.parseInt(resp.getString("RequestedUserId"));
                        int requestedMovelistId = Integer.parseInt(resp.getString("RequestedMovelistId"));
                        JSONObject move = new JSONObject(resp.getString("MoveList"));
                        JSONArray moveListLines = resp.getJSONArray("MoveListLines");
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

                        //get move list lines
                        for (int i = 0; i < moveListLines.length(); i++) {
                            JSONObject moveListLine = moveListLines.getJSONObject(i);
                            int movelistLineId = StringUtils.toInt(moveListLine.getString("MovelistLineId"), 0);
                            Timestamp insertTimeStamp2 = Timestamp.valueOf(moveListLine.getString("InsertTimeStamp"));
                            int productId = StringUtils.toInt(moveListLine.getString("ProductId"), 0);
                            String catNumber = moveListLine.getString("CatNumber");
                            String artist = moveListLine.getString("Artist");
                            String title = moveListLine.getString("Title");
                            String ean = moveListLine.getString("EAN");
                            String srcBinCode = moveListLine.getString("SrcBinCode");
                            String dstBinCode = moveListLine.getString("DstBinCode");
                            int qty = StringUtils.toInt(moveListLine.getString("Qty"), 0);
                            int removeLink = StringUtils.toInt(moveListLine.getString("RemoveLink"), 0);
                            int movementId = StringUtils.toInt(moveListLine.getString("MovementId"), 0);
                            boolean qtyConfirmed = false;
                            boolean completed = false;
                            String sortOrder = moveListLine.getString("SortOrder");
                            ReplenMoveListLinesItemResponse item = new ReplenMoveListLinesItemResponse(movelistLineId, insertTimeStamp2, productId,
                                    catNumber, artist, title, ean, srcBinCode, dstBinCode, qty, removeLink,movementId, qtyConfirmed, completed, sortOrder);
                            moveListItemResponseList.add(item);
                        }
                        qryResponse =  new ReplenMoveListLinesResponse(requestedUserId, requestedMovelistId, moveLineItem, moveListItemResponseList);
                    } else {
                        //TODO - Report that response is empty
                        String iMsg = "The Response object returns null due to improper request.";
                        response.setResponseMessage(iMsg);
                    }
                    response.setResponse(qryResponse);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response.setExceptionClass(ex.getClass());
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            if (iDialog != null && iDialog.isShowing()) {
                iDialog.dismiss();
            }
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
//                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
//                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        //do nothing
//                                    }
//                                });
//                        builder.show();
                        showDialog(R.integer.MSG_SEVERITY_FAILURE, R.integer.MSG_TYPE_NOTIFICATION,
                                statusCode.toString() + ": - " + response.getResponseMessage(), "Network Error");
                        mActivity.getAppContext().playSound(2);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(ReplenMoveListResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        ReplenMoveListLinesResponse resp = (ReplenMoveListLinesResponse) response.getResponse();
                        mActivity.setCurrentListLines(resp);
                        mActivity.setMoveLineAdapter(new ReplenMoveLineAdapter(getActivity(), resp.getMoveListLines()));      //this line is repeated when setCurrentListLines i9s changed
                        lvWorkLines.setAdapter(mActivity.getMoveLineAdapter());
                        flipper.setDisplayedChild(1);
                    } else {        //just to make sure
                        Vibrator vib = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        showDialog(R.integer.MSG_SEVERITY_WARNING, R.integer.MSG_TYPE_NOTIFICATION,
                                response.getResponseMessage(), "Bad HttpResponse");
                        mActivity.getAppContext().playSound(2);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
//                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
//                    builder.setMessage("The product query has return no result\nPlease verify then re-scan")
//                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    //do nothing
//                                }
//                            });
//                    builder.show();
                    showDialog(R.integer.MSG_SEVERITY_FAILURE, R.integer.MSG_TYPE_NOTIFICATION,
                            response.getResponseMessage(), "Bad Scan");
                    mActivity.getAppContext().playSound(2);
                }
            }
            getWorkLineAsync = null;
        }
    }
}
