package com.proper.warehousetools.binmove.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.proper.data.binmove.*;
import com.proper.data.binmove.adapters.BinResponseSelectionAdapter;
import com.proper.data.core.ICommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.helpers.DialogHelper;
import com.proper.messagequeue.Message;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.binmove.fragments.ProductDetailsDialogFragment;
import com.proper.warehousetools.binmove.fragments.QuantityDialogFragment;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 27/08/2014.
 */
//public class ActBinItemSelection extends BaseFragmentActivity implements ICommunicator {
public class ActBinItemSelection extends BaseScanActivity implements ICommunicator {
    private ListView lvProducts;
    private TextView txtTile;
    private Button btnContinue;
    private Button btnExit;
    private String sourceBin = "";
    private String destinationBin = "";
    private static final String menuItem1 = "Exclude From Move List";
    private static final String menuItem2 = "Exclude All Except this line";
    private static final String menuItem3 = "Show Details";
    private static final String menuItem4 = "Enter Quantity";
    private String ApplicationID = "BinMove";
    private int ctxId;
    private int currentSelectedIndex = -1;
    private ProductBinSelection currentBinSelection = null;
    private BinResponse thisBinResponse =  new BinResponse();
    private ProductBinResponse selectedProduct = new ProductBinResponse();
    private List<ProductBinSelection> moveList = new ArrayList<ProductBinSelection>();
    private BinResponseSelectionAdapter adapter;
    private MoveQrytask moveTask = null;
    //private int mDownPosition = 0;

    public ProductBinResponse getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(ProductBinResponse selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public BinResponse getThisBinResponse() {
        return thisBinResponse;
    }

    public int getCurrentSelectedIndex() {
        return currentSelectedIndex;
    }

    public void setCurrentSelectedIndex(int currentSelectedIndex) {
        this.currentSelectedIndex = currentSelectedIndex;
    }

    public ProductBinSelection getCurrentBinSelection() {
        return currentBinSelection;
    }

    public void setCurrentBinSelection(ProductBinSelection currentBinSelection) {
        this.currentBinSelection = currentBinSelection;
    }

    public void setThisBinResponse(BinResponse thisBinResponse) {
        this.thisBinResponse = thisBinResponse;
    }

    public List<ProductBinSelection> getMoveList() {
        return moveList;
    }

    public void setMoveList(List<ProductBinSelection> moveList) {
        this.moveList = moveList;
    }

    public BinResponseSelectionAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(BinResponseSelectionAdapter adapter) {
        this.adapter = adapter;
        this.adapter.notifyDataSetChanged();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_binitem_selection);
        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();

        try {
            Bundle extras = getIntent().getExtras();
            if (extras == null) throw new ExceptionInInitializerError("onCreate failed because variable [extras] has returned null");
            sourceBin = extras.getString("SOURCE_EXTRA");
            destinationBin = extras.getString("DESTINATION_EXTRA");
            //deviceIMEI = extras.getString("DEVICEIMEI_EXTRA");
            //currentUser = (UserLoginResponse) extras.getSerializable("LOGIN_EXTRA");
            thisBinResponse = (BinResponse) extras.getSerializable("RESPONSE_EXTRA");
            if (thisBinResponse == null) {
                throw new ExceptionInInitializerError("onCreate failed because variable [thisBinResponse] has returned null");
            } else {
                for (ProductBinResponse prod : getThisBinResponse().getProducts()) {
                    ProductBinSelection sel = new ProductBinSelection(prod);
                    moveList.add(sel);  //initialize moveList
                }
            }
            txtTile = (TextView) this.findViewById(R.id.txtvMoveSelectionTitle);
            btnContinue = (Button) this.findViewById(R.id.bnMoveSelectionContinue);
            btnContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ButtonClicked(view);
                }
            });
            btnExit = (Button) this.findViewById(R.id.bnExitActBinItemSelection);
            btnExit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ButtonClicked(view);
                }
            });
            lvProducts = (ListView) this.findViewById(R.id.lvMoveSelectionProducts);
            lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    onListItemClick(adapterView, view, i, l);
                }
            });
            lvProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    onListItemLongClick(parent, view, position, id);
                    return false;
                }
            });

            //Change title to show sourceBin & Destination bin - Req. by Scott: << Task: #737 >>
            this.setTitle(String.format("Move - From: %s To: %s", sourceBin, destinationBin));   //Req.#737
            adapter = new BinResponseSelectionAdapter(ActBinItemSelection.this, moveList);
            lvProducts.setAdapter(adapter);
            lvProducts.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            registerForContextMenu(lvProducts);
        } catch (Exception ex) {
            ex.printStackTrace();
            today = new Timestamp(utilDate.getTime());
            LogEntry log = new LogEntry(1L, ApplicationID, "ActBinItemSelection - onCreate", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
            logger.log(log);
        }

        overridePendingTransition(R.anim.bottom_in, R.anim.top_out);
    }

    private void onListItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
        this.setSelectedProduct(thisBinResponse.getProducts().get(pos));
        this.setCurrentBinSelection(this.getMoveList().get(pos));       //current selection
        this.setCurrentSelectedIndex(pos);
        this.lvProducts.setItemChecked(pos, true);
        view.setSelected(true);
        long i = id;
        updateTitleBar();
    }

    private void onListItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
        this.setSelectedProduct(thisBinResponse.getProducts().get(pos));
        this.setCurrentBinSelection(this.getMoveList().get(pos));       //current selection
        this.setCurrentSelectedIndex(pos);
        this.lvProducts.setItemChecked(pos, true);
        view.setSelected(true);
        long i = id;
        updateTitleBar();
    }

    private void ButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.bnExitActBinItemSelection:
                Intent i = new Intent();
                setResult(RESULT_OK, i);
                this.finish();
                break;
            case R.id.bnMoveSelectionContinue:
                //Get All the row modified and put them in a list
                BinResponseSelectionAdapter adapter = (BinResponseSelectionAdapter) lvProducts.getAdapter();
                moveList = adapter.getThisSelection();
                MoveRequest req = new MoveRequest();
                List<MoveRequestItem> list = new ArrayList<MoveRequestItem>();
                req.setUserCode(currentUser.getUserCode());
                req.setUserId(String.format("%s", currentUser.getUserId()));
                req.setSrcBin(sourceBin);
                req.setDstBin(destinationBin);
                for (ProductBinSelection sel : moveList) {
                    if (sel.getQtyToMove() > 0) {
                        MoveRequestItem item = new MoveRequestItem();
                        item.setProductID(sel.getProductId());
                        item.setSuppliercat(sel.getSupplierCat());
                        item.setQty(sel.getQtyToMove());
                        list.add(item);
                    }
                }
                if (!list.isEmpty()) {  //Verify that there are products to be moved
                    currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();  //Gets currently authenticated user
                    if (currentUser != null) {
                        try {
                            //Build message request
                            req.setProducts(list);
                            ObjectMapper mapper = new ObjectMapper();
                            String msg = mapper.writeValueAsString(req);
                            today = new Timestamp(utilDate.getTime());
                            thisMessage = new Message();

                            thisMessage.setSource(deviceIMEI);
                            thisMessage.setMessageType("CreateMovelist");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value
                            moveTask = new MoveQrytask();
                            moveTask.execute(thisMessage);  //executes -> Send webservice -> To our msg Queue
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            today = new Timestamp(utilDate.getTime());
                            //LogEntry log = new LogEntry(1L, ApplicationID, this.getClass().getSimpleName() + " - ButtonClicked - onCreate", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                            LogEntry log = new LogEntry(1L, ApplicationID, ((Object) this).getClass().getSimpleName() + " - ButtonClicked - onCreate", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                            logger.log(log);
                        }
                    } else {
                        //soundPool.play(errorSoundId, 1, 1, 0, 0, 1);
                        appContext.playSound(2);
                        Vibrator vib = (Vibrator) ActBinItemSelection.this.getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 500 milliseconds
                        vib.vibrate(2000);
                        String mMsg = "User not Authenticated \nPlease login";
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActBinItemSelection.this);
                        builder.setMessage(mMsg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder.show();
                    }
                }
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(menuItem4);
        menu.add(menuItem1);
        menu.add(menuItem2);
        menu.add(menuItem3);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ctxId = info.position;  //get position of item clicked
        this.setSelectedProduct(thisBinResponse.getProducts().get(ctxId));
        this.setCurrentSelectedIndex(ctxId);
        lvProducts.setSelected(true);
        this.lvProducts.setItemChecked(ctxId, true);
        if  (item.getTitle().toString().equalsIgnoreCase(menuItem1)) {
            final String msg = String.format("Are you sure you want to Exclude this entry number (%s) from move list", ctxId);
            AlertDialog.Builder alert = new AlertDialog.Builder(ActBinItemSelection.this);
            alert.setTitle("Exclude?");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    adapter.remove(ctxId);
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }
        if (item.getTitle().toString().equalsIgnoreCase(menuItem2)) {
            final String msg = String.format("Are you sure you want to Exclude ALL but this entry number (%s) from move list", ctxId);
            AlertDialog.Builder alert = new AlertDialog.Builder(ActBinItemSelection.this);
            alert.setTitle("Exclude?");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    adapter.removeAllExcept(ctxId);
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }
        if (item.getTitle().toString().equalsIgnoreCase(menuItem3)) {
            showProductDetailsDialog();
        }
        if (item.getTitle().toString().equals(menuItem4)) {
            showQuantityDialog();
        }

        updateTitleBar();
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //soundPool.release();
    }

    private void showProductDetailsDialog() {
        FragmentManager fm = getSupportFragmentManager();
        ProductDetailsDialogFragment dialog = new ProductDetailsDialogFragment();
        dialog.show(fm, "ProductDetailsDialog");
    }

    private void showDialog(int severity, int dialogType, String message, String title) {
        FragmentManager fm = getSupportFragmentManager();
        //DialogHelper dialog = new DialogHelper(subjectReferenceType, dialogType, message, title);
        DialogHelper dialog = new DialogHelper();
        Bundle args = new Bundle();
        args.putInt("DialogType_ARG", dialogType);
        args.putInt("Severity_ARG", severity);
        args.putString("Message_ARG", message);
        args.putString("Title_ARG", title);
        dialog.setArguments(args);
        dialog.show(fm, "Dialog");
    }

    private void showQuantityDialog() {
        FragmentManager fm = getSupportFragmentManager();
        QuantityDialogFragment dialog = new QuantityDialogFragment();
        dialog.show(fm, "QuantityDialog");
    }

    private void updateTitleBar() {
        txtTile.setText(getSelectedProduct().getArtist() + " - " + getSelectedProduct().getTitle());
    }

    @Override
    public void onDialogMessage_ICommunicator(int buttonClicked) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                int totalMove = this.getAdapter().getTotalMoveCollection();
                if (totalMove == 1) {
                    this.setTitle(String.format("Moving %s item From: %s To: %s", this.getAdapter().getTotalMoveCollection(),
                            this.sourceBin, this.destinationBin));
                } else {
                    this.setTitle(String.format("Moving %s items From: %s To: %s", this.getAdapter().getTotalMoveCollection(),
                            this.sourceBin, this.destinationBin));
                }
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

    private class MoveQrytask extends AsyncTask<Message, Void, PartialBinMoveResponse> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            wsDialog = new ProgressDialog(ActBinItemSelection.this);
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
        protected PartialBinMoveResponse doInBackground(Message... msg) {
            PartialBinMoveResponse qryResponse = new PartialBinMoveResponse();

            //HttpMessageResolver resolver = new HttpMessageResolver();
            String response = resolver.resolveMessageQuery(msg[0]);
            if (response != null && !response.equalsIgnoreCase("")) {
                if (response.contains("not recognised")) {
                    //manually error trap this error
                    String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                    today = new Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActBinItemSelection - MoveQryTask - Line:253", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                    logger.log(log);
                    throw new RuntimeException("Warehouse Support webservice is currently down. Please contact the IT department");
                }else {
                    //Manually process this response
                    try {
                        JSONObject resp = new JSONObject(response);
                        JSONArray messages = resp.getJSONArray("Messages");
                        JSONArray actions = resp.getJSONArray("MessageObjects");
                        String RequestedSrcBin = resp.getString("RequestedSrcBin");
                        String RequestedDstBin = resp.getString("RequestedDstBin");
                        //String Result = resp.getString("Result");
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
                        qryResponse.setRequestedSrcBin(RequestedSrcBin);
                        qryResponse.setRequestedDstBin(RequestedDstBin);
                        //qryResponse.setResult(Result);
                        qryResponse.setMessages(messageList);
                        qryResponse.setMessageObjects(actionList);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        today = new Timestamp(utilDate.getTime());
                        LogEntry log = new LogEntry(1L, ApplicationID, this.getClass().getSimpleName() + " - MoveQrytask - doInBackground", deviceIMEI, ex.getClass().getSimpleName(), ex.getMessage(), today);
                        logger.log(log);
                    }
                }
            }
            return qryResponse;
        }

        @Override
        protected void onPostExecute(PartialBinMoveResponse response) {
            if (wsDialog != null && wsDialog.isShowing()) {
                wsDialog.dismiss();
            }
            if (response != null) {
//                *****************     Requested to be removed by Scott       ***********************
//                Intent i = new Intent(ActBinItemSelection.this, ActInfo.class);
//                i.putExtra("RESPONSE_EXTRA", response);
//                i.putExtra("ACTION_EXTRA", R.integer.ACTION_PARTIALMOVE);
//                startActivityForResult(i, RESULT_OK);

                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinItemSelection.this);
                String msg = "Success: BinMove completed!";
                builder.setMessage(msg)
                        .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Attempt to reload Activity
                                if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                Intent i = new Intent();
                                setResult(RESULT_OK, i);
                                finish();
                            }
                        });
                builder.show();
            } else {
                //Response is null the disable Yes button:
                AlertDialog.Builder builder = new AlertDialog.Builder(ActBinItemSelection.this);
                String msg = "Failed: BinMove NOT Completed because of network error, please contact IT for help";
                builder.setMessage(msg)
                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // Attempt to reload Activity
                                if (btnContinue.isEnabled()) btnContinue.setEnabled(false);
                                Intent i = new Intent();
                                setResult(RESULT_OK, i);
                                finish();
                            }
                        });
                builder.show();
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
//            if (wsTask != null && wsTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
//                wsTask.cancel(true);
//            }
//            wsTask = null;
//            if(xDialog != null && xDialog.isShowing() == true){ xDialog.dismiss(); }
//            mReception.setText("");
//            Toast.makeText(ActSingleMain.this, "The product scanned does not exist in our database", Toast.LENGTH_LONG).show();
        }
    }
}