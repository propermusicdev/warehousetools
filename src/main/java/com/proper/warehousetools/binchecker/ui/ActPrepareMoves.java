package com.proper.warehousetools.binchecker.ui;

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
import com.proper.data.binchecker.BinCheckerResponse;
import com.proper.data.binchecker.CheckerProduct;
import com.proper.data.binchecker.IndividualMoveLine;
import com.proper.data.binchecker.IndividualMoveRequest;
import com.proper.data.binchecker.adapters.CheckerProductAdapter;
import com.proper.data.binmove.*;
import com.proper.data.core.IBinCheckerQtyCommunicator;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.enums.HttpResponseCodes;
import com.proper.data.helpers.HttpResponseHelper;
import com.proper.warehousetools.BaseScanActivity;
import com.proper.warehousetools.R;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Lebel on 30/03/2015.
 */
public class ActPrepareMoves extends BaseScanActivity implements IBinCheckerQtyCommunicator {
    private RadioGroup radioGroup;
    private RadioButton radSingleMode, radBulkMode;
    private Button btnAbort, btnScan, btnUpdate; //btnEnterBarcode
    private EditText txtBarcode;
    private ExpandableListView lvProducts;
    private BarcodeQueryTask barcodeQryTask = null;
    private UpdateBinCheckerTask updateBinTask = null;
    private BinResponse currentBinData = null;
    private LinkedList<CheckerProduct> currentLines = null;
    private CheckerProduct selectedLine = null;
    private List<BarcodeResponse> currentFoundStock = null;
    private CheckerProductAdapter productAdapter;
    private LinkedList<IndividualMoveLine> moves = null;
    private IndividualMoveLine currentMove = null;
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

    public BinResponse getCurrentBinData() {
        return currentBinData;
    }

    public void setCurrentBinData(BinResponse currentBinData) {
        this.currentBinData = currentBinData;
    }

    public LinkedList<CheckerProduct> getCurrentLines() {
        return currentLines;
    }

    public void setCurrentLines(LinkedList<CheckerProduct> currentLines) {
        this.currentLines = currentLines;
    }

    public CheckerProduct getSelectedLine() {
        return selectedLine;
    }

    public void setSelectedLine(CheckerProduct selectedLine) {
        this.selectedLine = selectedLine;
    }

    public List<BarcodeResponse> getCurrentFoundStock() {
        return currentFoundStock;
    }

    public void setCurrentFoundStock(List<BarcodeResponse> currentFoundStock) {
        this.currentFoundStock = currentFoundStock;
    }

    public CheckerProductAdapter getProductAdapter() {
        return productAdapter;
    }

    public void setProductAdapter(CheckerProductAdapter productAdapter) {
        this.productAdapter = productAdapter;
    }

    public int getSelectedMode() {
        return selectedMode;
    }

    public void setSelectedMode(int selectedMode) {
        this.selectedMode = selectedMode;
    }

    public IndividualMoveLine getCurrentMove() {
        return currentMove;
    }

    public void setCurrentMove(IndividualMoveLine currentMove) {
        this.currentMove = currentMove;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binchecker_preparemoves);
        Bundle extras = getIntent().getExtras();
        currentBinData = (BinResponse) extras.getSerializable("RESPONSE_EXTRA");
        selectedMode = extras.getInt("LAST_MODE", -33) == 0? -33 : extras.getInt("LAST_MODE", -33);
        if (currentBinData == null) {
            throw new NullPointerException("currentBinData cannot be null");
        }
        currentLines = new LinkedList<CheckerProduct>();
        for (ProductBinResponse prod: currentBinData.getProducts()) {
            if (prod.getQtyInBin() < 0) {
                ProductBinResponse newProd = prod;
                newProd.setQtyInBin(0);  /**  Adjust negative number to zero  **/
                currentLines.add(new CheckerProduct(newProd));
            } else{
                currentLines.add(new CheckerProduct(prod));
            }
        }

        btnUpdate = (Button) this.findViewById(R.id.bnBCPMUpdateBin);
        btnScan = (Button) this.findViewById(R.id.bnBCPMBinScan);
        radioGroup = (RadioGroup) this.findViewById(R.id.rgBCPMScanMode);
        radSingleMode = (RadioButton) this.findViewById(R.id.rdBCPMSingle);
        radBulkMode = (RadioButton) this.findViewById(R.id.rdBCPMBulk);
        btnAbort = (Button) this.findViewById(R.id.bnExitActPrepareMoves);
        txtBarcode = (EditText) this.findViewById(R.id.etxtBCPMInvisibleBarcode);
        lvProducts = (ExpandableListView) this.findViewById(R.id.lvBCPMLines);
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
        txtBarcode.addTextChangedListener(new TextChanged());
        TextView lblBin = (TextView) this.findViewById(R.id.txtvBCPMBinTitle);
        lblBin.setText(currentBinData.getRequestedBinCode());
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
            case R.id.rdBCPMSingle:
                setSelectedMode(MODE_SINGLE);
                break;
            case R.id.rdBCPMBulk:
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

        if (v.getId() == R.id.lvBCPMLines) {
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
                    alert.setTitle("Update Bin ?");
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
                            thisMessage.setMessageType("BarcodeQuery");
                            thisMessage.setIncomingStatus(1); //default value
                            thisMessage.setIncomingMessage(msg);
                            thisMessage.setOutgoingStatus(0);   //default value
                            thisMessage.setOutgoingMessage("");
                            thisMessage.setInsertedTimeStamp(today);
                            thisMessage.setTTL(100);    //default value


                            //TODO -- findProductInLine if found tally, if not then asyncTask
                            CheckerProduct exists = findProductInLine(eanCode);
                            if (exists != null) {
                                //TODO - >>>>>>>    manipulate adapter line: tally, color   <<<<<<<<
                                setAlreadyFired(false);
                                refreshDataScreen();
                            } else {
                                barcodeQryTask = new BarcodeQueryTask();
                                barcodeQryTask.execute(thisMessage);
                            }
                        } else {
                            appContext.playSound(2);
                            Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                            // Vibrate for 500 milliseconds
                            vib.vibrate(2000);
                            String mMsg = "User not Authenticated \nPlease login";
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDialogMessage_IBinCheckerQtyCommunicator(int buttonClicked, int passedIndex, boolean editMode) {
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

    private void refreshDataScreen() {
        productAdapter = null;
        productAdapter = new CheckerProductAdapter(currentLines, ActPrepareMoves.this);
        lvProducts.setAdapter(productAdapter);
        setAlreadyFired(false);
    }

    /** Returns True if Positive else then assumes Negative **/
    private Boolean isNumberPositive(int number) {
        boolean ret = false;
        float i = (float) number;
        float determinant = Math.signum(i);
        if (determinant > 0) {
            ret = true;
        }
        return ret;
    }

    /** Search current lines for a match **/
    private CheckerProduct findProductInLine(String barcode) {
        String newEAN = barcodeHelper.formatBarcode(barcode);
        CheckerProduct prod = null;
        LinkedList<CheckerProduct> productlines = currentLines;
        int found = 0;
        if (currentLines != null && !currentLines.isEmpty()) {
            for (int i = 0; i < productlines.size(); i++) {
                CheckerProduct line = productlines.get(i);
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
        CheckerQuantityFragment dialog = new CheckerQuantityFragment();
        FragmentManager fm = getSupportFragmentManager(); //declare bundles here
        Bundle bundle = new Bundle();
        bundle.putInt("INDEX_EXTRA", listIndex);
        bundle.putBoolean("MODE_EXTRA", editMode);
        dialog.setArguments(bundle);
        dialog.show(fm, "StockTakeProductQuantityFragment");
    }

    private CheckerProduct findProductLineByScan(ProductResponse qryProduct) {
        //TODO - if response = 1 then add, if more or less than 1 found product ignore...
        CheckerProduct prod = null;
        LinkedList<CheckerProduct> productlines = currentLines;
        String thisUser = currentUser.getUserFirstName() + " " + currentUser.getUserLastName();
        int found = 0;
        if (productlines != null && !productlines.isEmpty()) {
            for (int i = 0; i < productlines.size(); i++) {
                CheckerProduct line = productlines.get(i);
                if (line.getProductId() == qryProduct.getProductId() && line.getEAN().equalsIgnoreCase(qryProduct.getEAN())) {
                    found ++;
                    //TODO - confirm Quantity and Update details
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
                Bin foundBin = null;
                for (Bin bin : qryProduct.getBins()) {
                    if (bin.getBinCode().equalsIgnoreCase(currentBinData.getRequestedBinCode())) {
                        foundBin = bin;
                    }
                }
                CheckerProduct newProd = new CheckerProduct(qryProduct, foundBin.getQty());
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
                prod = newProd;
            }
        }
        return prod;
    }

    private IndividualMoveRequest createUpdateRequest() {
        final int ADD = 5, SUB = 3, PAR = 0;
        int METHOD = -1;
        IndividualMoveRequest updateRequest = null;
        List<IndividualMoveLine> updateLineList = new ArrayList<IndividualMoveLine>();
        if (currentLines != null && !currentLines.isEmpty()) {
            for (CheckerProduct prod: currentLines) {
                //if(prod.getQty()-)
                if (prod.getQty() < prod.getQtyScanned()) {
                    METHOD = ADD;
                }
                if (prod.getQty() > prod.getQtyScanned()) {
                    METHOD = SUB;
                }
                if (prod.getQty() == prod.getQtyScanned()) {
                    METHOD = PAR;
                }
                switch (METHOD) {
                    case SUB:
                        int trueQty = prod.getQtyScanned() - (prod.getQty()<0?0:prod.getQty());
                        IndividualMoveLine line2UpdateSub = new IndividualMoveLine(prod);
                        line2UpdateSub.setSrcBin(currentBinData.getRequestedBinCode());
                        if (isNumberPositive(trueQty)) {
                            line2UpdateSub.setQty(trueQty);
                        } else {
                            line2UpdateSub.setQty(Math.abs(trueQty));
                        }
                        updateLineList.add(line2UpdateSub);
                        break;
                    case PAR:
                        //do nothing for now...
                        break;
                    case ADD:
                        int trueQty1 = prod.getQtyScanned() - (prod.getQty()<0?0:prod.getQty());
                        IndividualMoveLine line2UpdateAdd = new IndividualMoveLine(prod);
                        line2UpdateAdd.setDstBin(currentBinData.getRequestedBinCode());
                        if (prod.getQty() < 0 && prod.getQtyScanned() >= 0) {
                            if (prod.getQtyScanned() > 0) {
                                line2UpdateAdd.setQty(Math.abs(prod.getQty() + prod.getQtyScanned()));
                            }else{
                                line2UpdateAdd.setQty(Math.abs(prod.getQty()));
                            }
                        } else{
                            if (isNumberPositive(trueQty1)) {
                                line2UpdateAdd.setQty(trueQty1);
                            } else {
                                line2UpdateAdd.setQty(Math.abs(prod.getQty()) + prod.getQtyScanned());
                            }
                        }
                        updateLineList.add(line2UpdateAdd);
                        break;
                }
            }
            updateRequest = new IndividualMoveRequest("Correction", updateLineList, currentUser.getUserCode(),
                    currentUser.getUserId());
        }
        return updateRequest;
    }

    private boolean UpdateBin() {
        // Build request based on list item changed, if succeeded then attempt an update request
        boolean ret = false;
        IndividualMoveRequest request = createUpdateRequest();
        if (request != null) {
            if (!request.getProducts().isEmpty()) {
                try {
                    currentUser = currentUser != null ? currentUser : authenticator.getCurrentUser();   //Gets currently authenticated user
                    if (currentUser != null) {
                        ret = true;

                        updateBinTask = new UpdateBinCheckerTask();
                        updateBinTask.execute(request);
                    } else {
                        appContext.playSound(2);
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        // Vibrate for 500 milliseconds
                        vib.vibrate(2000);
                        String mMsg = "User not Authenticated \nPlease login";
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                        builder.setMessage(mMsg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {//do nothing
                                    }
                                });
                        builder.show();
                    }
                } catch (Exception ex){
                    ex.printStackTrace();
                    String iMsg = "Unable to Update Bin";
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "ActPrepareMoves - UpdateBin", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                    //logger.log(log);
                }
            } else {
                final String msg = "This bin checks out fine";
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle("All is Well");
                alert.setMessage(msg);
                alert.setPositiveButton("Ok, I'm done", new AlertDialog.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exitActivity();
                    }
                });
                alert.show();
            }
        } else {
            final String msg = "Failed to create an Update Request, please start again";
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Unexpected Result");
            alert.setMessage(msg);
            alert.setPositiveButton("Restart Process", new AlertDialog.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    exitActivity();
                }
            });
            alert.show();
        }
        return ret;
    }

    private AbstractMap.SimpleEntry<Boolean, Integer> hasOutstandingWork(){
        AbstractMap.SimpleEntry<Boolean, Integer> ret = new AbstractMap.SimpleEntry<Boolean, Integer>(false, 0);
        if (currentLines != null && !currentLines.isEmpty()) {
            int lineScannedCount = 0;
            int lineWithQtyGreaterThanZero = 0;
            for (CheckerProduct prod : currentLines) {
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
            Toast.makeText(ActPrepareMoves.this, "No Product has been scanned from the bin", Toast.LENGTH_LONG);
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

    private class BarcodeQueryTask extends AsyncTask<com.proper.messagequeue.Message, Void, HttpResponseHelper> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            //startTime = new Date().getTime(); //get start time
            txtBarcode.setText("");     //empty control
            xDialog = new ProgressDialog(ActPrepareMoves.this);
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
            Thread.currentThread().setName("BinCheckerBarcodeQueryAsyncTask");
            BarcodeResponse productResponse = null;
            response = resolver.resolveHttpMessage(msg[0]);
            response.setResponse(responseHelper.refineProductResponse(response.getResponse().toString()));
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
                    productResponse = new BarcodeResponse();
                    ObjectMapper mapper = new ObjectMapper();
                    productResponse = mapper.readValue(response.getResponse().toString(), BarcodeResponse.class);
                    response.setResponse(new AbstractMap.SimpleEntry<String, BarcodeResponse>(response.getResponse().toString(), productResponse));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response.setResponseMessage(ex.getMessage());
                    response.setExceptionClass(ex.getClass());
                }
            }
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
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
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
                        AbstractMap.SimpleEntry<String, BarcodeResponse> resp =
                                (AbstractMap.SimpleEntry<String, BarcodeResponse>) response.getResponse();
                        //TODO -************************************* REPORT - SUCCESS ! ****************************************
                        //TODO - >> if 1 found report as found if > 1 report null : report null    manipulate adapter line: tally, color   <<<<<<
                        CheckerProduct exists = null;
                        //boolean matched = false;
                        if (resp.getValue().getProducts().size() == 1) {
                            for (Bin bin : resp.getValue().getProducts().get(0).getBins()) {
                                if (bin.getBinCode().equalsIgnoreCase(currentBinData.getRequestedBinCode())) {
                                    //matched = true;
                                    exists = findProductLineByScan(resp.getValue().getProducts().get(0));
                                }
                            }
                        }
                        if (exists != null) {
                            refreshDataScreen();
                        }else{
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
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
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
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
                    Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                    vib.vibrate(2000);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
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
            if (barcodeQryTask != null) {
                if (barcodeQryTask.getStatus() != null && barcodeQryTask.getStatus() != Status.FINISHED) barcodeQryTask.cancel(true);
                barcodeQryTask = null;
            }
        }
    }

    private class UpdateBinCheckerTask extends AsyncTask<IndividualMoveRequest, Void, HttpResponseHelper> {
        protected ProgressDialog wsDialog;

        @Override
        protected void onPreExecute() {
            //txtBin.setText("");     //empty control
            wsDialog = new ProgressDialog(ActPrepareMoves.this);
            CharSequence message = "Checking bin...";
            CharSequence title = "Please Wait";
            wsDialog.setCancelable(true);
            wsDialog.setCanceledOnTouchOutside(false);
            wsDialog.setMessage(message);
            wsDialog.setTitle(title);
            wsDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            wsDialog.show();
        }

        @Override
        protected HttpResponseHelper doInBackground(IndividualMoveRequest... req) {
            HttpResponseHelper response = null;
            Thread.currentThread().setName("StockTakeBinResponseAsyncTask");
            BinCheckerResponse qryResponse = null;
            thisMessage = new com.proper.messagequeue.Message();
            today = new java.sql.Timestamp(utilDate.getTime());
            ObjectMapper mapper = new ObjectMapper();
            String request = null;
            try {
                request = mapper.writeValueAsString(req[0]);
            } catch (IOException e) {
                e.printStackTrace();
                /** Escaped with returned because we could not create a proper request **/
                return null;
            }
            thisMessage.setSource(deviceIMEI);
            thisMessage.setMessageType("CreateNewMovelist");
            thisMessage.setIncomingStatus(1); //default value
            thisMessage.setIncomingMessage(request);
            thisMessage.setOutgoingStatus(0);   //default value
            thisMessage.setOutgoingMessage("");
            thisMessage.setInsertedTimeStamp(today);
            thisMessage.setTTL(100);    //default value
            response = resolver.resolveHttpMessage(thisMessage);
            if (!response.isSuccess()) {
                String ymsg = "Network Error has occurred that resulted in package loss. Please check Wi-Fi";
                Log.e("ERROR !!!", ymsg);
                response.setResponseMessage(ymsg);
            }
            if (response.getResponse().toString().contains("Error") || response.getResponse().toString().contains("not recognised")) {
                //manually error trap this error
                String iMsg = "Response returned null due to improper request.";
                response.setResponseMessage(iMsg);
            }else {
                //Manually process this response
                try {
                    JSONObject resp = new JSONObject(response.getResponse().toString());
                    JSONArray messages = resp.getJSONArray("Messages");
                    JSONArray actions = resp.getJSONArray("MessageObjects");
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
                    qryResponse = new BinCheckerResponse(messageList, actionList);
                    //qryResponse.setResult(Result);
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
            if (wsDialog != null && wsDialog.isShowing()) wsDialog.dismiss();
            alreadyFired = false;
            if (!response.isSuccess()) {
                /**--------------------------- Network Error -------------------------**/
                HttpResponseCodes statusCode = HttpResponseCodes.findCode(response.getHttpResponseCode());
                if (statusCode != null) {
                    if (statusCode != HttpResponseCodes.OK) {
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                        builder.setMessage(statusCode.toString() + ": - " + response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //exitActivity();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }
            } else {
                if (response.getResponse() != null) {
                    if (response.getResponse().getClass().equals(BinCheckerResponse.class)) {
                        /**TODO - -------------------------------------- Success ----------------------------------**/
                        BinCheckerResponse resp = (BinCheckerResponse) response.getResponse();
                        boolean foundResult = false;
                        String result = "";
                        for (BinMoveMessage msg: resp.getMessages()) {
                            if (msg.getMessageName().equals("Result")) {
                                foundResult = true;
                                result = msg.getMessageText();
                            }
                        }
                        if (foundResult && result.equalsIgnoreCase("Success")) {
                            Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(2000);
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                            builder.setTitle("Success")
                                    .setMessage("Corrected Successfully !!")
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //reloadActivity();
                                            exitActivity();
                                        }
                                    });
                            builder.show();
                            appContext.playSound(1);
                        }else {
                            //Error
                            Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                            vib.vibrate(2000);
                            AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                            builder.setTitle("Check Failed");
                            builder.setMessage("Error:->" + response.getResponse().toString())
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            exitActivity();
                                        }
                                    });
                            builder.show();
                            appContext.playSound(2);
                            btnScan.setEnabled(true);
                        }
                    } else { // Unnecessary but just to make sure...
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                        builder.setMessage(response.getResponseMessage() + "->" + response.getResponse().toString())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //reloadActivity();
                                        exitActivity();
                                    }
                                });
                        builder.show();
                        appContext.playSound(2);
                        btnScan.setEnabled(true);
                    }
                }else{
                    /**--------------------------- Failed because of Bad scan -------------------------**/
                    if (response.getResponseMessage().contains("invalid product")) {
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                        builder.setMessage(response.getResponseMessage())
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        exitActivity();
                                    }
                                });
                        builder.show();
                    }else {
                        Vibrator vib = (Vibrator) ActPrepareMoves.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vib.vibrate(2000);
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActPrepareMoves.this);
                        builder.setMessage("The product query has return no result\nPlease verify then re-scan")
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        exitActivity();
                                    }
                                });
                        builder.show();
                    }
                    appContext.playSound(2);
                    btnScan.setEnabled(true);
                }
            }
        }
    }
}