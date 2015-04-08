package com.proper.warehousetools.replen.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ViewFlipper;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.replen.ReplenMoveListItemResponse;
import com.proper.data.replen.ReplenMoveListResponse;
import com.proper.data.replen.adapters.ReplenMoveListAdapter;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 13/11/2014.
 */
public class ActReplenResume extends PlainActivity {
    //private TextView txtTitle;
    private static final String menuItem1 = "Continue this MoveList";
    private static final String menuItem2 = "Show Details";
    private int groupPos;
    private ViewFlipper flipper;
    private ExpandableListView lvWork;
    private RetrieveWorkAsync getWorkAsync;
    private ReplenMoveListAdapter adapter = null;
    private SharedPreferences prefs = null;
    private String moveListReponseString = "";
    private ReplenMoveListResponse moveListResponse = null;
    private ReplenMoveListItemResponse SelectedMove;
    private int currentSelectedIndex = -1;

    public int getCurrentSelectedIndex() {
        return currentSelectedIndex;
    }

    public void setCurrentSelectedIndex(int currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }

    public ReplenMoveListItemResponse getSelectedMove() {
        return SelectedMove;
    }

    public void setSelectedMove(ReplenMoveListItemResponse selectedMove) {
        SelectedMove = selectedMove;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_resume);
        //txtTitle = this.findViewById(R.id.)
        flipper = (ViewFlipper) this.findViewById(R.id.vfReplenResume);
        lvWork = (ExpandableListView) this.findViewById(R.id.lvReplenResume);
        lvWork.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
                onListViewGroupClicked(parent, view, groupPosition, id);
                return false;
            }
        });

        lvWork.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        registerForContextMenu(lvWork);

        // retrieve any outstanding work from the database
        String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\"}", currentUser.getUserId(), currentUser.getUserCode());
        //String msg = "{\"UserId\":\"263\", \"UserCode\":\"B97C51\"}";       //test only
        today = new Timestamp(utilDate.getTime());
        thisMessage = new Message();
        thisMessage.setSource(deviceIMEI);
        thisMessage.setMessageType("GetUserMovelists");
        thisMessage.setIncomingStatus(1); //default value
        thisMessage.setIncomingMessage(msg);
        thisMessage.setOutgoingStatus(0);   //default value
        thisMessage.setOutgoingMessage("");
        thisMessage.setInsertedTimeStamp(today);
        thisMessage.setTTL(100);    //default value
        getWorkAsync = new RetrieveWorkAsync();
        getWorkAsync.execute(thisMessage);  //executes both -> Send Queue Directly AND Send queue to Service
    }

    private void removeSharedPreferences() {
        prefs = getSharedPreferences(getString(R.string.preference_replenmovelist), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("ApplicationID");
        editor.remove("IMEI");
        editor.remove("Device");
        editor.remove("UserToken");
        editor.remove("Movelist");
        editor.apply();
    }

    private void saveSharedPreferences() {
        SharedPreferences credPref = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        prefs = getSharedPreferences(getString(R.string.preference_replenmovelist), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("ApplicationID", ApplicationID);
        editor.putString("IMEI", deviceIMEI);
        editor.putString("Device", deviceID);
        editor.putString("UserToken", credPref.getString("UserToken", ""));
        editor.putString("Movelist", moveListReponseString);
        editor.commit();
    }

    private void loadSharedPreferences() {
        SharedPreferences credPref = getSharedPreferences(getString(R.string.preference_credentials), Context.MODE_PRIVATE);
        prefs = getSharedPreferences(getString(R.string.preference_replenmovelist), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        prefs.getString("ApplicationID", ApplicationID);
        prefs.getString("IMEI", deviceIMEI);
        prefs.getString("Device", deviceID);
        prefs.getString("UserToken", credPref.getString("UserToken", ""));
        prefs.getString("Movelist", moveListReponseString);
    }

    private void onListViewGroupClicked(ExpandableListView parent, View view, int groupPosition, long id) {
        this.setSelectedMove(adapter.getGroup(groupPosition)); //current selection
        this.lvWork.setItemChecked(groupPosition, true);
        view.setSelected(true);
        this.setCurrentSelectedIndex(groupPosition);
        groupPos = groupPosition;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
        int type =
                ExpandableListView.getPackedPositionType(info.packedPosition);
        int group =
                ExpandableListView.getPackedPositionGroup(info.packedPosition);
        int child =
                ExpandableListView.getPackedPositionChild(info.packedPosition);

        menu.add(menuItem1);
        menu.add(menuItem2);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        //AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        ExpandableListView.ExpandableListContextMenuInfo info =
                (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
        int childPos = 0;
        int type = ExpandableListView.getPackedPositionType(info.packedPosition);
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);
        }
        if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
            childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
        }

        this.setSelectedMove(moveListResponse.getMovelists().get(this.groupPos));
        this.setCurrentSelectedIndex(this.groupPos);
        lvWork.setSelected(true);
        this.lvWork.setItemChecked(this.groupPos, true);
        if (item.getTitle().toString().equalsIgnoreCase(menuItem1)) {
            final String msg = String.format("Are you sure you want to process this entry number (%s) from move list", this.groupPos);
            AlertDialog.Builder alert = new AlertDialog.Builder(ActReplenResume.this);
            alert.setTitle("This Move Line ?");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO - Proceed with the currently selected move list
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }
        if (item.getTitle().toString().equalsIgnoreCase(menuItem2)) {
            final String msg = String.format("Do you want to see more details for this entry", this.groupPos);
            AlertDialog.Builder alert = new AlertDialog.Builder(ActReplenResume.this);
            alert.setTitle("View Details?");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO - Show show more details for this entry
                    Intent nav = new Intent(ActReplenResume.this, zzActReplenManageConfig.class);
                    startActivityForResult(nav, RESULT_OK);
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }
            return super.onContextItemSelected(item);
    }

     class RetrieveWorkAsync extends AsyncTask<Message, Void, HttpResponseHelper> {
        private ProgressDialog vDialog;

        @Override
        protected void onPreExecute() {
            vDialog = new ProgressDialog(ActReplenResume.this);
            CharSequence message = "Working hard...sending queue [directly] [to webservice]...";
            CharSequence title = "Please Wait";
            vDialog.setCancelable(true);
            vDialog.setCanceledOnTouchOutside(false);
            vDialog.setMessage(message);
            vDialog.setTitle(title);
            vDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            vDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(Message... inputMsg) {
            HttpResponseHelper response = null;
            ReplenMoveListResponse qryResponse = null;

            response = resolver.resolveHttpMessage(inputMsg[0]);
            moveListReponseString = response.getResponse().toString();

            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("not recognised")) {
                String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage(iMsg);
            }else {
                try {
                    if (moveListReponseString != null && !moveListReponseString.isEmpty()) {
                        JSONObject resp = new JSONObject(moveListReponseString);
                        int requestedUserId = Integer.parseInt(resp.getString("RequestedUserId"));
                        int movelistsReturned = Integer.parseInt(resp.getString("MovelistsReturned"));
                        JSONArray moveList = resp.getJSONArray("Movelists");

                        List<ReplenMoveListItemResponse> moveListItemResponseList = new ArrayList<ReplenMoveListItemResponse>();
                        //get move list
                        for (int i = 0; i < moveList.length(); i++) {
                            JSONObject move = moveList.getJSONObject(i);
                            int movelistId = Integer.parseInt(move.getString("MovelistId"));
                            String description = move.getString("Description");
                            String notes = move.getString("Notes");
                            Timestamp insertTimeStamp = Timestamp.valueOf(move.getString("InsertTimeStamp"));
                            int assignedTo = Integer.parseInt(move.getString("AssignedTo"));
                            int status = Integer.parseInt(move.getString("Status"));
                            String statusName = move.getString("StatusName");
                            int listType = Integer.parseInt(move.getString("ListType"));
                            String listTypeName = move.getString("ListTypeName");
                            int totalLines = Integer.parseInt(move.getString("TotalLines"));
                            int totalQty = Integer.parseInt(move.getString("TotalQty"));
                            ReplenMoveListItemResponse moveListItemResponse = new ReplenMoveListItemResponse(movelistId, description, notes, insertTimeStamp,
                                    assignedTo, status, statusName, listType, listTypeName, totalLines, totalQty);
                            moveListItemResponseList.add(moveListItemResponse);
                        }
                        qryResponse =  new ReplenMoveListResponse(requestedUserId, moveListItemResponseList, movelistsReturned);
                        response.setResponse(qryResponse);
                        moveListResponse = qryResponse;
                    } else {
                        // TODO - Report response is empty
                        String iMsg = "The response object returns null due to ReplenMoveListResponse being empty";
                        response.setResponseMessage(iMsg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response.setExceptionClass(ex.getClass());
                }
            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            if (vDialog != null && vDialog.isShowing()) {
                vDialog.dismiss();
            }

            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActReplenResume.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenResume.this);
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
                    if (response.getResponse().getClass().equals(ReplenMoveListResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        ReplenMoveListResponse resp = (ReplenMoveListResponse) response.getResponse();
                        adapter = new ReplenMoveListAdapter(ActReplenResume.this, resp.getMovelists());
                        lvWork.setAdapter(adapter);
                        flipper.setDisplayedChild(1);
                    }else { //just to make sure
                        Vibrator vib = (Vibrator) ActReplenResume.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenResume.this);
                        builder.setMessage("The product query has return no result because response is empty")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {//do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActReplenResume.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActReplenResume.this);
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