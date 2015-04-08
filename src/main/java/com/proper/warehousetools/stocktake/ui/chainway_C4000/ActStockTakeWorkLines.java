package com.proper.warehousetools.stocktake.ui.chainway_C4000;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.*;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.proper.data.binmove.BarcodeResponse;
import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import com.proper.data.core.IStockTakeQtyCommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.data.helpers.ReplenResponseHelper;
import com.proper.data.stocktake.*;
import com.proper.data.stocktake.adapters.StockTakeProductLineAdapter;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Created by Lebel on 22/01/2015.
 * Will have 2 lists, decor list (that we decorate and subtract- foreach item we list total vs scanned) and worked list (just a copy for comparison)
 */
//public class ActStockTakeWorkLines extends BaseFragmentActivity implements IStockTakeQtyCommunicator {
public class ActStockTakeWorkLines extends BaseScanActivity implements IStockTakeQtyCommunicator {
    private RadioGroup radioGroup;
    private RadioButton radSingleMode, radBulkMode;
    private Button btnAbort, btnScan, btnUpdate; //btnEnterBarcode
    private EditText txtBarcode;
    private ExpandableListView lvProducts;
    private StockTakeBarcodeQueryTask barcodeQryTask = null;
    private UpdateStockTakeBinTask updateBinTask = null;
    private StockTakeBinResponse currentBinData = null;
    private LinkedList<StockTakeLineProduct> currentLines = null;
    private StockTakeLineProduct selectedLine = null;
    private List<BarcodeResponse> currentFoundStock = null;
    private StockTakeProductLineAdapter productAdapter;
    private String scanInput;
    private int groupPos= -1, enteredQty = 0, selectedMode;
    private static final String menuItemUpdateBin = "Update Bin", menuItemEditQty = "Edit Scanned Qty";
    private static final boolean EDITMODE_ON = true, EDITMODE_OFF = false;
    private static final int MODE_SINGLE = -33, MODE_BULK = -44;
    private boolean alreadyFired = false;
    private boolean dialogShowing = false;

    public boolean isDialogShowing() {
        return dialogShowing;
    }

    public void setDialogShowing(boolean dialogShowing) {
        this.dialogShowing = dialogShowing;
    }

    public boolean hasAlreadyFired() {
        return alreadyFired;
    }

    public void setAlreadyFired(boolean alreadyFired) {
        this.alreadyFired = alreadyFired;
    }

    public String getScanInput() {
        return scanInput;
    }

    public void setScanInput(String scanInput) {
        this.scanInput = scanInput;
    }

    public int getEnteredQty() {
        return enteredQty;
    }

    public void setEnteredQty(int enteredQty) {
        this.enteredQty = enteredQty;
    }

    public StockTakeBinResponse getCurrentBinData() {
        return currentBinData;
    }

    public void setCurrentBinData(StockTakeBinResponse currentBinData) {
        this.currentBinData = currentBinData;
    }

    public LinkedList<StockTakeLineProduct> getCurrentLines() {
        return currentLines;
    }

    public void setCurrentLines(LinkedList<StockTakeLineProduct> currentLines) {
        this.currentLines = currentLines;
    }

    public StockTakeLineProduct getSelectedLine() {
        return selectedLine;
    }

    public void setSelectedLine(StockTakeLineProduct selectedLine) {
        this.selectedLine = selectedLine;
    }

    public List<BarcodeResponse> getCurrentFoundStock() {
        return currentFoundStock;
    }

    public void setCurrentFoundStock(List<BarcodeResponse> currentFoundStock) {
        this.currentFoundStock = currentFoundStock;
    }

    public StockTakeProductLineAdapter getProductAdapter() {
        return productAdapter;
    }

    public void setProductAdapter(StockTakeProductLineAdapter productAdapter) {
        this.productAdapter = productAdapter;
    }

    public int getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(int selectedMode) {
        this.selectedMode = selectedMode;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_stocktake_worklines);
        Bundle extras = getIntent().getExtras();
        currentBinData = (StockTakeBinResponse) extras.getSerializable("DATA_EXTRA");
        selectedMode = extras.getInt("LAST_MODE", -33) == 0? -33 : extras.getInt("LAST_MODE", -33);
        if (currentBinData == null) {
            throw new NullPointerException("currentBinData cannot be null");
        }
        if (currentBinData.getBinAlreadyChecked() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
            String msg = "This Bin has already been checked\nDo you want to Stay?";
            builder.setTitle("Already Checked !!!");
            builder.setMessage(msg)
                    .setNegativeButton("No, Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            exitActivity();
                        }
                    })
                    .setPositiveButton("Yes, Stay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do nothing
                        }
                    }).show();
        }
        currentLines = new LinkedList<StockTakeLineProduct>();
        for (StockTakeLine line : currentBinData.getStockTakeLines()) {
            if (line.getQty() < 0) {
                StockTakeLine newLine = line;
                newLine.setQty(0);  /**  Adjust negative number to zero  **/
                currentLines.add(new StockTakeLineProduct(newLine));
            } else {
                currentLines.add(new StockTakeLineProduct(line));
            }
        }
        btnUpdate = (Button) this.findViewById(R.id.bnSTWUpdateBin);
        btnScan = (Button) this.findViewById(R.id.bnSTWBinScan);
        radioGroup = (RadioGroup) this.findViewById(R.id.rgSTWScanMode);
        radSingleMode = (RadioButton) this.findViewById(R.id.rdSTWSingle);
        radBulkMode = (RadioButton) this.findViewById(R.id.rdSTWBulk);
        //btnEnterBarcode = (Button) this.findViewById(R.id.bnSTWEnterBarcode);
        btnAbort = (Button) this.findViewById(R.id.bnSTWExitActStockTakeLines);
        txtBarcode = (EditText) this.findViewById(R.id.etxtSTWInvisibleBarcode);
        lvProducts = (ExpandableListView) this.findViewById(R.id.lvSTWLines);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        btnAbort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioGroupButtonChanged(group, checkedId);
            }
        });
//        btnEnterBarcode.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                buttonClicked(v);
//            }
//        });
        txtBarcode.addTextChangedListener(new TextChanged());
        TextView lblBin = (TextView) this.findViewById(R.id.txtvSTWBinTitle);
        lblBin.setText(String.format("%s", currentBinData.getBinCode()));
//        lvProducts.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                listItemClicked(parent, view, position, id);
//            }
//        });
        lvProducts.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View view, int groupPosition, long id) {
                onListViewGroupClicked(parent, view, groupPosition, id);
                return false;
            }
        });

        lvProducts.setChoiceMode(ExpandableListView.CHOICE_MODE_SINGLE);
        refreshDataScreen();
        registerForContextMenu(lvProducts);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int sound = 0; //defaults to error
                if(msg.what == 1){
                    setScanInput(msg.obj.toString().trim());   //>>>>>>>>>>>>>>>   set scanned object  <<<<<<<<<<<<<<<<
                    //if (!txtBarcode.getText().toString().equalsIgnoreCase("")) {
                    int acceptable[] = {12, 13, 14};
                    if (!getScanInput().isEmpty() && !(Arrays.binarySearch(acceptable, getScanInput().length()) == -1)) {
                        setAlreadyFired(true);
                        txtBarcode.setText("");
                        txtBarcode.setText(getScanInput());
                        sound = 1;  //success
                    }else {
                        alreadyFired = false;
                    }
                }else {
                    alreadyFired = false;
                }
                appContext.playSound(sound);
                btnScan.setEnabled(true);
            }
        };
        //radSingleMode.performClick();   // radioGroup defaults to
        effectRadioButton(selectedMode);
    }

    private void effectRadioButton(int mode) {
        switch (mode) {
            case MODE_BULK:
                radBulkMode.performClick();
                break;
            case MODE_SINGLE:
                radSingleMode.performClick();
                break;
        }
    }

    private void radioGroupButtonChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rdSTWSingle:
                setSelectedMode(MODE_SINGLE);
                break;
            case R.id.rdSTWBulk:
                setSelectedMode(MODE_BULK);
                break;
        }
    }

    private void onListViewGroupClicked(ExpandableListView parent, View view, int groupPosition, long id) {
        if (productAdapter != null) {
            setSelectedLine(productAdapter.getGroup(groupPosition)); //current selection
            lvProducts.setItemChecked(groupPosition, true);
            view.setSelected(true);
            //setCurrentLineSelectedIndex(groupPosition);
            groupPos = groupPosition;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (v.getId() == R.id.lvSTWLines) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
            int type =
                    ExpandableListView.getPackedPositionType(info.packedPosition);
            int group =
                    ExpandableListView.getPackedPositionGroup(info.packedPosition);
            int child =
                    ExpandableListView.getPackedPositionChild(info.packedPosition);

            menu.add(menuItemUpdateBin);
            menu.add(menuItemEditQty);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (productAdapter != null) {
            ExpandableListView.ExpandableListContextMenuInfo info =
                    (ExpandableListView.ExpandableListContextMenuInfo) item.getMenuInfo();
            int childPos = 0;
            int type = ExpandableListView.getPackedPositionType(info.packedPosition);
            if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                childPos = ExpandableListView.getPackedPositionChild(info.packedPosition);
            }
            if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
                groupPos = ExpandableListView.getPackedPositionGroup(info.packedPosition);

                setSelectedLine(productAdapter.getGroup(groupPos)); //current selection
                //setCurrentLineSelectedIndex(groupPos);
                lvProducts.setSelected(true);
                lvProducts.setItemChecked(groupPos, true);
                if (item.getTitle().toString().equalsIgnoreCase(menuItemUpdateBin)) {
                    //final String msg = String.format("Are you sure you want to update this line entry (%s)", groupPos + 1);
                    final String msg = "Are you sure you want to update the whole Bin ?";
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);
                    alert.setTitle("Update Line ?");
                    alert.setMessage(msg);
                    alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //TODO - Proceed Updating the StockTake Bin
                            proceedWithUpdate();
                            //Toast.makeText(ActStockTakeWorkLines.this, "sss", Toast.LENGTH_LONG).show();
                        }
                    });
                    alert.setNegativeButton("Cancel", null);
                    alert.show();
                }
                if (item.getTitle().toString().equalsIgnoreCase(menuItemEditQty)) {
                    showQuantityDialog(groupPos, EDITMODE_ON);
                }
            }
        }
        return super.onContextItemSelected(item);
    }

    private void exitActivity() {
        Intent i = new Intent();
        i.putExtra("PREVIOUS_MODE", selectedMode);
        setResult(RESULT_FIRST_USER, i);
        finish();
    }

    private void buttonClicked(View v) {
        if (v == btnAbort) {
            //Find outstanding work and prompt to save
            AbstractMap.SimpleEntry<Boolean, Integer> hasAny = hasOutstandingWork();
            if (hasAny.getKey()) {
                final String msg = String.format("You did some scanning on this StockTake Bin that needs to be updated. Do you want to update Bin now?");
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("Update Line ?");
                alert.setMessage(msg);
                alert.setPositiveButton("Yes, Save then Exit", new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //TODO - Proceed Updating the StockTake Bin
                        UpdateBin();
                    }
                });
                alert.setNegativeButton("No, Don't save just Exit", new AlertDialog.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exitActivity();
                    }
                });
                alert.show();
            } else {
                exitActivity();
            }
        }
//        if (v == btnEnterBarcode) {
//            if (inputByHand == 0) {
//                turnOnInputByHand();
//                if (!txtBarcode.isEnabled()) txtBarcode.setEnabled(true);
//                txtBarcode.setText("");
//                showSoftKeyboard();
//            } else {
//                turnOffInputByHand();
//                //mReception.removeTextChangedListener();
//            }
//        }
        if (v == btnScan) {
            boolean bContinuous = false; //true;
            int iBetween = 0;
            btnScan.setEnabled(false);
            txtBarcode.requestFocus();
            if (threadStop) {
                Log.i("Reading", "My Barcode " + readerStatus);
                readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                readThread.setName("Single Barcode ReadThread");
                readThread.start();
            }else {
                threadStop = true;
            }
            btnScan.setEnabled(true);
        }
        if (v == btnUpdate) {
            //UpdateBin();
            final String msg = "Are you sure you want to update this Bin ?";
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Update Line ?");
            alert.setMessage(msg);
            alert.setPositiveButton("Yes", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //TODO - Proceed Updating the StockTake Bin
                    proceedWithUpdate();
                    //Toast.makeText(ActStockTakeWorkLines.this, "sss", Toast.LENGTH_LONG).show();
                }
            });
            alert.setNegativeButton("Cancel", null);
            alert.show();
        }
    }

    class TextChanged implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && !s.toString().equalsIgnoreCase("")) {
                if (inputByHand == 0) {
                    int acceptable[] = {12, 13, 14};
                    String eanCode = s.toString().trim();
                    if (eanCode.length() > 0 && !(Arrays.binarySearch(acceptable, eanCode.length()) == -1)) {
                        //Authenticate current user, Build Message only when all these conditions are right then proceed with asyncTask
                        currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                        if (currentUser != null) {
                            String msg = String.format("{\"UserId\":\"%s\", \"UserCode\":\"%s\",\"Barcode\":\"%s\"}",
                                    currentUser.getUserId(), currentUser.getUserCode(), eanCode);
                            today = new java.sql.Timestamp(utilDate.getTime());
                            thisMessage.setSource(deviceIMEI);
                            thisMessage.setMessageType("StockTakeBarcodeQuery");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value

                            //sort out quantity


                            //TODO -- findProductInLine if found tally, if not then asynctask
                            StockTakeLineProduct exists = findProductInLine(eanCode);
                            if (exists != null) {
                                //TODO - >>>>>>>    manipulate adapter line: tally, color   <<<<<<<<
                                setAlreadyFired(false);
                                refreshDataScreen();
                            } else {
                                barcodeQryTask = new StockTakeBarcodeQueryTask();
                                barcodeQryTask.execute(thisMessage);
                            }
//                            barcodeQryTask = new StockTakeBarcodeQueryTask();
//                            barcodeQryTask.execute(thisMessage);
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    } else {
                        //txtBarcode.setText("");
                    }
                }
            }
        }
    }

    @Override
    public void onDialogMessage_IStockTakeQtyCommunicator(int buttonClicked, int passedIndex, boolean editMode) {
        switch (buttonClicked) {
            case R.integer.MSG_CANCEL:
                break;
            case R.integer.MSG_YES:
                break;
            case R.integer.MSG_OK:
                if (editMode) {
                    currentLines.get(passedIndex).setQtyScanned(getEnteredQty()); //replace qty
                } else {
                    currentLines.get(passedIndex).setQtyScanned(currentLines.get(passedIndex).getQtyScanned() + getEnteredQty()); //tally qty
                }
                currentLines.get(passedIndex).setAdjustedByUserId(currentUser.getUserId());
                currentLines.get(passedIndex).setAdjustedByName(currentUser.getUserFirstName() + " " + currentUser.getUserLastName());
                setDialogShowing(false);
                setAlreadyFired(false);
                refreshDataScreen();
                break;
            case R.integer.MSG_NO:
                break;
        }
    }

//    private void showSoftKeyboard() {
//        InputMethodManager imm =(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//        //inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_FORCED, 0);
//        if (imm != null){
//            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
//        }
//    }
//
//    private void turnOnInputByHand(){
//        inputByHand = 1;    //Turn On Input by Hand
//        btnScan.setEnabled(false);
//        paintByHandButtons();
//    }
//
//    private void turnOffInputByHand(){
//        inputByHand = 0;    //Turn On Input by Hand
//        btnScan.setEnabled(true);  //
//        setScanInput(txtBarcode.getText().toString());
//        if (!getScanInput().isEmpty()) {
//            txtBarcode.setText(getScanInput()); // just to trigger text changed
//        }
//        paintByHandButtons();
//    }
//
//    private void paintByHandButtons() {
//        final String byHand = "ByHand";
//        final String finish = "Finish";
//        if (inputByHand == 0) {
//            btnEnterBarcode.setText(byHand);
//        } else {
//            btnEnterBarcode.setText(finish);
//        }
//    }

    private void refreshDataScreen() {
        productAdapter = null;
        productAdapter = new StockTakeProductLineAdapter(ActStockTakeWorkLines.this, currentLines);
        lvProducts.setAdapter(productAdapter);
        setAlreadyFired(false);
    }

    /** Search current lines for a match **/
    private StockTakeLineProduct findProductInLine(String barcode) {
        String newEAN = barcodeHelper.formatBarcode(barcode);
        StockTakeLineProduct prod = null;
        LinkedList<StockTakeLineProduct> productlines = currentLines;
        int found = 0;
        if (currentLines != null && !currentLines.isEmpty()) {
            for (int i = 0; i < productlines.size(); i++) {
                StockTakeLineProduct line = productlines.get(i);
                if (line.getEAN().equalsIgnoreCase(newEAN)) {
                    found ++;
                    //TODO - Sort ot qty and Update details
                    //showQuantityDialog(i, EDITMODE_OFF);
                    //currentLines.get(i).setQtyScanned(currentLines.get(i).getQtyScanned() + 1); //tally scanned item
                    if (selectedMode == MODE_SINGLE) {
                        currentLines.get(i).setQtyScanned(currentLines.get(i).getQtyScanned() + 1); //tally scanned item
                    }
                    if (selectedMode == MODE_BULK) {
                        if (!dialogShowing) {
                            showQuantityDialog(i, EDITMODE_OFF);
                        }
                    }
                    currentLines.get(i).setAdjustedByUserId(currentUser.getUserId());
                    currentLines.get(i).setAdjustedByName(currentUser.getUserFirstName() + " " + currentUser.getUserLastName());
                    //currentLines.get(i).setBinCheckerFlag(1);
                    //currentLines.get(i).setFormat(qryProduct.getFormat());
                    prod = currentLines.get(i);
                }
                if (found > 0) {
                    break; //escape early to save time
                }
            }
        }
        txtBarcode.setText("");
        return prod;
    }

    private void showQuantityDialog(int listIndex, boolean editMode) {
        StockTakeProductQuantityFragment dialog = new StockTakeProductQuantityFragment();
        FragmentManager fm = getSupportFragmentManager(); //declare bundles here
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX_EXTRA", listIndex);
        bundle.putBoolean("MODE_EXTRA", editMode);
        dialog.setArguments(bundle);
        dialog.show(fm, "StockTakeProductQuantityFragment");
    }

    private StockTakeLineProduct findProductLineByScan(StockTakeProductResponse qryProduct) {
        StockTakeLineProduct prod = null;
        LinkedList<StockTakeLineProduct> productlines = currentLines;
        String thisUser = currentUser.getUserFirstName() + " " + currentUser.getUserLastName();
        int found = 0;
        if (productlines != null && !productlines.isEmpty()) {
            for (int i = 0; i < productlines.size(); i++) {
                StockTakeLineProduct line = productlines.get(i);
                if (line.getProductId() == qryProduct.getProductId() && line.getEAN().equalsIgnoreCase(qryProduct.getEAN())) {
                    found ++;
                    //TODO - confirm Quantity and Update details
                    //showQuantityDialog(i, EDITMODE_OFF);
                    //currentLines.get(i).setQtyScanned(currentLines.get(i).getQtyScanned() + 1); //tally scanned item
                    if (selectedMode == MODE_SINGLE) {
                        currentLines.get(i).setQtyScanned(currentLines.get(i).getQtyScanned() + 1); //tally scanned item
                    }
                    if (selectedMode == MODE_BULK) {
                        if (!dialogShowing) {
                            showQuantityDialog(i, EDITMODE_OFF);
                        }
                    }
                    currentLines.get(i).setAdjustedByUserId(currentUser.getUserId());
                    currentLines.get(i).setAdjustedByName(thisUser);
                    //currentLines.get(i).setBinCheckerFlag(1);
                    currentLines.get(i).setFormat(qryProduct.getFormat());
                    prod = currentLines.get(i);

                }
                if (found > 0) {
                    break; //escape early to save time
                }
            }
            if (found == 0) {
                StockTakeLineProduct newProd = new StockTakeLineProduct(0, currentBinData.getBinCode(), currentBinData.getStockTakeId(),
                        qryProduct.getProductId(), qryProduct.getSupplierCat(), qryProduct.getEAN(), qryProduct.getBarcode(),
                        qryProduct.getArtist(), qryProduct.getTitle(), 0, 0, 0, currentUser.getUserId(), thisUser, 0);
                //newProd.setQtyScanned(1); //tally scanned item
                if (selectedMode == MODE_SINGLE) {
                    newProd.setQtyScanned(1); //tally scanned item
                    newProd.setFormat(qryProduct.getFormat());
                    currentLines.add(newProd);
                }
                if (selectedMode == MODE_BULK) {
                    if (!dialogShowing) {
                        newProd.setFormat(qryProduct.getFormat());
                        currentLines.add(newProd);
                        showQuantityDialog(currentLines.size() - 1, EDITMODE_OFF);
                    }
                }
                //newProd.setFormat(qryProduct.getFormat());
                //currentLines.add(newProd);
                prod = newProd;
            }
        }
        return prod;
    }

    private StockTakeUpdateRequest createUpdateRequest() {
        StockTakeUpdateRequest updateRequest = null;
        List<StockTakeUpdateLine> updateLineList = new ArrayList<StockTakeUpdateLine>();
        if (currentLines != null && !currentLines.isEmpty()) {
            for (StockTakeLineProduct line: currentLines) {
                StockTakeUpdateLine line2Update = new StockTakeUpdateLine(line);
                if (line2Update.getStockTakeLineId() == 0) line2Update.setLineAdded(1); //identify new lines to be added
                updateLineList.add(line2Update);
            }
            updateRequest = new StockTakeUpdateRequest(currentBinData.getBinCode(), currentUser.getUserId(),
                    currentUser.getUserCode(), currentBinData.getStockTakeId(), updateLineList);
        }
        return updateRequest;
    }

    private boolean UpdateBin() {
        boolean ret = false;
        StockTakeUpdateRequest request = createUpdateRequest();
        if (request != null) {
            try {
                thisMessage = new com.proper.messagequeue.Message();
                currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                if (currentUser != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    String msg = mapper.writeValueAsString(request);
                    today = new java.sql.Timestamp(utilDate.getTime());
                    thisMessage.setSource(deviceIMEI);
                    thisMessage.setMessageType("UpdateStockTakeBin");
                    thisMessage.setIncomingStatus(1); //default value
                    thisMessage.setIncomingMessage(msg);
                    thisMessage.setOutgoingStatus(0);   //default value
                    thisMessage.setOutgoingMessage("");
                    thisMessage.setInsertedTimeStamp(today);
                    thisMessage.setTTL(100);    //default value
                    ret = true;

                    updateBinTask = new UpdateStockTakeBinTask();
                    updateBinTask.execute(thisMessage);
                } else {
                    appContext.playSound(2);
                    Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                    // Vibrate for 500 milliseconds
                    vib.vibrate(2000);
                    String mMsg = "User not Authenticated \nPlease login";
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {//do nothing
                                }
                            });
                    builder.show();
                }
            } catch (Exception ex){
                ex.printStackTrace();
                String iMsg = "Unable to Update StockTakeBin";
                today = new java.sql.Timestamp(utilDate.getTime());
                LogEntry log = new LogEntry(1L, ApplicationID, "ActStockTakeWorkLines - UpdateBin - Line:503", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                //logger.log(log);
            }
        }
        return ret;
    }

    private AbstractMap.SimpleEntry<Boolean, Integer> hasOutstandingWork(){
        AbstractMap.SimpleEntry<Boolean, Integer> ret = new AbstractMap.SimpleEntry<Boolean, Integer>(false, 0);
        if (currentLines != null && !currentLines.isEmpty()) {
            int lineScannedCount = 0;
            int lineWithQtyGreaterThanZero = 0;
            for (StockTakeLineProduct prod : currentLines) {
                if (prod.getQtyScanned() > 0) {
                    lineScannedCount ++;
                }
                if (prod.getQty() > 0) {
                    lineWithQtyGreaterThanZero ++;
                }
            }
            if (lineScannedCount > 0) {
                ret = new AbstractMap.SimpleEntry<Boolean, Integer>(true, lineScannedCount);
            } else {
                //Exception: zero scanned, no line with qty greater than one <<Requested by Sam>>
                if (currentLines.size() > 0 && lineWithQtyGreaterThanZero > 0) {
                    ret = new AbstractMap.SimpleEntry<Boolean, Integer>(true, currentLines.size());
                }
            }
        }
        return ret;
    }

    private synchronized void proceedWithUpdate(){
        AbstractMap.SimpleEntry<Boolean, Integer> hasAny = hasOutstandingWork();
        if (hasAny.getKey()) {
            UpdateBin();
        } else {
            Toast.makeText(ActStockTakeWorkLines.this, "No Product has been scanned from the bin", Toast.LENGTH_LONG);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KEY_SCAN) {
            //Short press
            if (event.getRepeatCount() == 0) {
                if (!alreadyFired) {
                    setAlreadyFired(true);
                    //if (event.getRepeatCount() == 0) {
                    boolean bContinuous = false;
                    int iBetween = 0;
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "My Barcode " + readerStatus);
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("Single Barcode ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                }
            } else {
                //Long press
                if (!alreadyFired) {
                    setAlreadyFired(true);
                    //if (event.getRepeatCount() == 0) {
                    boolean bContinuous = false;
                    int iBetween = 0;
                    txtBarcode.requestFocus();
                    if (threadStop) {
                        Log.i("Reading", "My Barcode " + readerStatus);
                        readThread = new Thread(new GetBarcode(bContinuous, iBetween));
                        readThread.setName("Single Barcode ReadThread");
                        readThread.start();
                    }else {
                        threadStop = true;
                    }
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed(); do nothing !!!!
    }

    @Override
    protected void onPause() {
        super.onPause();
        threadStop = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBarcodeOpened) {
            mInstance.close();
        }
    }

    private class GetBarcode implements Runnable {

        private boolean isContinuous = false;
        String barCode = "";
        private long sleepTime = 1000;
        Message msg = null;

        public GetBarcode(boolean isContinuous) {
            this.isContinuous = isContinuous;
        }

        public GetBarcode(boolean isContinuous, int sleep) {
            this.isContinuous = isContinuous;
            this.sleepTime = sleep;
        }

        @Override
        public void run() {

            do {
                barCode = mInstance.scan();

                Log.i("MY", "barCode " + barCode.trim());

                msg = new Message();

                if (barCode == null || barCode.isEmpty()) {
                    msg.arg1 = 0;
                    msg.obj = "";
                } else {
                    msg.what = 1;
                    msg.obj = barCode;
                }

                handler.sendMessage(msg);

                if (isContinuous) {
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (isContinuous && !threadStop);

        }
    }

    private class StockTakeBarcodeQueryTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            //startTime = new Date().getTime(); //get start time
            txtBarcode.setText("");     //empty control
            xDialog = new ProgressDialog(ActStockTakeWorkLines.this);
            CharSequence message = "Working hard...Checking Product...";
            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... msg) {
            HttpResponseHelper response = null;
            Thread.currentThread().setName("StockTakeProductResponseAsyncTask");
            StockTakeProductResponse productResponse = null;

            response = resolver.resolveHttpMessage(msg[0]);

            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                //String iMsg = "The Response object returns null due to improper request.";
                response.setResponseMessage("One or more of this product? Remove as Found Stock");
            }else {
                try {
                    productResponse = new StockTakeProductResponse();
                    ObjectMapper mapper = new ObjectMapper();
                    productResponse = mapper.readValue(response.getResponse().toString(), StockTakeProductResponse.class);
                    response.setResponse(new AbstractMap.SimpleEntry<String, StockTakeProductResponse>(response.getResponse().toString(), productResponse));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response.setResponseMessage(ex.getMessage());
                    response.setExceptionClass(ex.getClass());
                }
            }
            //return new AbstractMap.SimpleEntry<String, StockTakeProductResponse>(response, productResponse);
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            //super.onPostExecute(response);
            if (xDialog != null && xDialog.isShowing()) xDialog.dismiss();
            setAlreadyFired(false);
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        txtBarcode.setText("");
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(AbstractMap.SimpleEntry.class)) {
                        /**--------------------------- Success -------------------------**/
                        AbstractMap.SimpleEntry<String, StockTakeProductResponse> resp =
                                (AbstractMap.SimpleEntry<String, StockTakeProductResponse>) response.getResponse();
                        //TODO -************************************* REPORT - SUCCESS ! ****************************************
                        //TODO - >>>>>>>>>>>>>>>>>>>>>>>>>>>>    manipulate adapter line: tally, color   <<<<<<<<<<<<<<<<<<<<<<<<
                        StockTakeLineProduct exists = findProductLineByScan(resp.getValue());
                        if (exists != null) {
                            refreshDataScreen();
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                            String msg = "Failed: Product Search did NOT return any match. This is FOUND STOCK";
                            builder.setTitle("Found Stock !!!");
                            builder.setMessage(msg)
                                    .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            txtBarcode.setText("");
                                        }
                                    }).show();
                        }
                    } else { //unnecessary but just to make sure...
                        /**--------------------------- Failed because database returned Error  -------------------------**/
                        Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                        builder.setMessage(response.getResponseMessage()).setTitle("Found Stock !!!")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) { //do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                    builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    txtBarcode.setText("");
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                    btnScan.setEnabled(true);
                }
            }
//            if (response != null) {
//                //if (response.getProductId() > 0) {
//                if (response.getKey().contains("Error") || response.getKey().contains("not recognised")) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
//                    String msg = "One or more of this barcode? Remove as Found Stock";
//                    builder.setTitle("Found Stock !!!");
//                    builder.setMessage(msg)
//                            .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int id) {
//                                    txtBarcode.setText("");
//                                }
//                            }).show();
//                } else {
//                    //TODO -************************************* REPORT - SUCCESS ! ****************************************
//                    //TODO - >>>>>>>    manipulate adapter line: tally, color   <<<<<<<<
//                    StockTakeLineProduct exists = findProductLineByScan(response.getValue());
//                    if (exists != null) {
//                        refreshDataScreen();
//                    }else{
//                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
//                        String msg = "Failed: Product Search did NOT return any match. This is FOUND STOCK";
//                        builder.setTitle("Found Stock !!!");
//                        builder.setMessage(msg)
//                                .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        txtBarcode.setText("");
//                                    }
//                                }).show();
//                    }
//                }
//            } else {
//                //Response is null the disable Yes button:
//                AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
//                String msg = "Failed: Product Check did NOT Completed because of network error, if this continues then please contact IT for help";
//                builder.setMessage(msg)
//                        .setNegativeButton(R.string.but_ok, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                txtBarcode.setText("");
//                            }
//                        }).show();
//            }
            if (barcodeQryTask != null) {
                if (barcodeQryTask.getStatus() != null && barcodeQryTask.getStatus() != Status.FINISHED) barcodeQryTask.cancel(true);
                barcodeQryTask = null;
            }
        }
    }

    private class UpdateStockTakeBinTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper>{
        protected ProgressDialog uDialog;
        @Override
        protected void onPreExecute() {
            uDialog = new ProgressDialog(ActStockTakeWorkLines.this);
            CharSequence message = "Updating StockTake Bin...";
            CharSequence title = "Please Wait";
            uDialog.setCancelable(true);
            uDialog.setCanceledOnTouchOutside(false);
            uDialog.setMessage(message);
            uDialog.setTitle(title);
            uDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            uDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(com.proper.messagequeue.Message... msg) {
            HttpResponseHelper response = null;
            Thread.currentThread().setName("StockTakeUpdateResponseAsyncTask");
            StockTakeUpdateResponse updateResponse = null;
            ReplenResponseHelper responseHelper = new ReplenResponseHelper();

            response = resolver.resolveHttpMessage(msg[0]);

            try {
                JSONObject ret = new JSONObject(response.getResponse().toString());
                JSONArray messages = ret.getJSONArray("Messages");
                String result = ret.getString("Result");    //Success || Error
                List<BinMoveMessage> outMsgs = responseHelper.buildMessageList(messages);
                if (!response.isSuccess()) {
                    String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                    Log.e("ERROR !!!", ymsg);
                    response.setResponseMessage(ymsg);
                }
                updateResponse = new StockTakeUpdateResponse(outMsgs, new ArrayList<BinMoveObject>(), result);
                response.setResponse(updateResponse);
            } catch (Exception ex) {
                ex.printStackTrace();
                response.setResponseMessage(ex.getMessage());
                response.setExceptionClass(ex.getClass());
            }
//            try {
//                String response = resolver.resolveMessageQuery(msg[0]);
//                JSONObject ret = new JSONObject(response);
//                JSONArray messages = ret.getJSONArray("Messages");
//                String result = ret.getString("Result");    //Success || Error
//                List<BinMoveMessage> outMsgs = responseHelper.buildMessageList(messages);
//                if (response.contains("Error") || response.contains("not recognised")) {
//                    //manually error trap this error
//                    String error = ret.getString("Error");
//                    //ObjectMapper mapper = new ObjectMapper();
//                    /***********   http://stackoverflow.com/a/6349488/4266650  **********/
//                    //List<BinMoveMessage> outMsgs = mapper.readValue(msgs, mapper.getTypeFactory().constructCollectionType(List.class, BinMoveMessage.class));   // for specific list type
//                    //List<BinMoveMessage> outMsgs = mapper.readValue(msgs, new TypeReference<List<BinMoveMessage>>(){});
//                    String iMsg = String.format("Error: %s [%s]", error, outMsgs.get(0).getMessageText());
//                    today = new java.sql.Timestamp(utilDate.getTime());
//                    LogEntry log = new LogEntry(1L, ApplicationID, "ActStockTakeWorkLines - WebServiceTask - Line:640", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                }
//                //updateResponse = new StockTakeUpdateResponse(outMsgs, null, result);
//                updateResponse = new StockTakeUpdateResponse(outMsgs, new ArrayList<BinMoveObject>(), result);
//            } catch (Exception e) {
//                //TODO optimise this exception handling and logging
//                e.printStackTrace();
//                today = new java.sql.Timestamp(utilDate.getTime());
//                LogEntry log = new LogEntry(1L, ApplicationID, "ActStockTakeWorkLines - doInBackground", deviceIMEI, e.getClass().getSimpleName(), e.getMessage(), today);
//                //logger.log(log);
//                //if (uDialog != null && uDialog.isShowing()) uDialog.dismiss();
//                //throw new NullPointerException(e.getMessage());
//            }
            return response;
        }

        @Override
        protected void onPostExecute(HttpResponseHelper response) {
            if (uDialog != null && uDialog.isShowing()) uDialog.dismiss();
            setAlreadyFired(false);
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(StockTakeUpdateResponse.class)) {
                        /**--------------------------- Success -------------------------**/
                        StockTakeUpdateResponse resp = (StockTakeUpdateResponse) response.getResponse();
                        if (resp.getResult().equalsIgnoreCase("Success")) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                            String msg = "Success: StockTake Bin is Completed successfully";
                            builder.setMessage(msg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {exitActivity();
                                        }
                                    }).show();
                        }else {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                            String msg = "Failed: Unable to process this StockTake Bin";
                            builder.setMessage(msg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) { exitActivity();
                                        }
                                    }).show();
                        }
                    }else{ // Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                        builder.setMessage("Failed: " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) { //do nothing
                                    }
                                });
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    Vibrator vib = (Vibrator) ActStockTakeWorkLines.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActStockTakeWorkLines.this);
                    builder.setMessage("Failed: Unable to process bin because response is null. " +
                            response.getExceptionClass().getSimpleName() + ": " + response.getResponseMessage())
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {//do nothing
                                }
                            });
                    builder.show();
                    appContext.playSound(2);
                    btnScan.setEnabled(true);
                }
            }
            if (updateBinTask != null) {
                if (updateBinTask.getStatus()!= null && updateBinTask.getStatus() != Status.FINISHED) updateBinTask.cancel(true);
                updateBinTask = null;
            }
        }
    }
}
