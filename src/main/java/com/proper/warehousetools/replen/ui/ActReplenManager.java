package com.proper.warehousetools.replen.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.proper.data.binmove.Bin;
import com.proper.data.binmove.ProductBinResponse;
import com.proper.data.binmove.ProductBinSelection;
import com.proper.data.replen.ReplenMiniMove;
import com.proper.data.replen.adapters.ReplenMiniMoveAdapter;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;
import com.proper.warehousetools.replen.ui.chainway_C4000.ActReplenCreateMiniMove;
import org.apache.commons.collections4.IteratorUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Lebel on 04/09/2014.
 */
public class ActReplenManager extends PlainActivity {
    private SharedPreferences prefs = null;
    private LinearLayout mainLayout;
    private TextView txtProductDetails, txtPalate, txtQty, txtListTile;
    private Button btnNewMove, btnExit;
    private ListView lvRepelen;
    private List<ProductBinResponse> inputList;
    private List<ReplenMiniMove> moveList = new ArrayList<ReplenMiniMove>();
    private ReplenMiniMoveAdapter adapter;
    private List<Bin> primaryList = new ArrayList<Bin>(), populatedBins = new ArrayList<Bin>();
    private String currentSource = "";
    private int tot = 0;
    private int backParam = 0;
    private boolean qtyChanged = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_replen_manager);
        getSupportActionBar().setLogo(R.drawable.ic_launcher);
        getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.flat_button_palebrown));
        getSupportActionBar().setTitle("Manage Replen");

        Bundle extra = getIntent().getExtras();
        if (extra == null) throw new RuntimeException("onCreate: Bundled Extra cannot be null!, Line: 44");
        prefs = getSharedPreferences("Proper_Replen", Context.MODE_PRIVATE);        //get preferences

        inputList = new ArrayList<ProductBinResponse>();
        inputList = (List<ProductBinResponse>) extra.getSerializable("PRODUCT_EXTRA");
        currentSource = extra.getString("SOURCE_EXTRA");
        primaryList = (List<Bin>) extra.getSerializable("PRIMARY_EXTRA");

        mainLayout = (LinearLayout) this.findViewById(R.id.lytReplenManager);
        txtProductDetails = (TextView) this.findViewById(R.id.txtvReplenManagerProductTitle);
        txtPalate = (TextView) this.findViewById(R.id.txtvReplenManagerPalate);
        txtQty = (TextView) this.findViewById(R.id.txtvReplenManagerTotalQuantity);
        btnNewMove = (Button) this.findViewById(R.id.bnReplenNewMove);
        btnExit = (Button) this.findViewById(R.id.bnExitActReplenManager);
        txtListTile = (TextView) this.findViewById(R.id.txtvReplenManListTitle);
        lvRepelen = (ListView) this.findViewById(R.id.lvReplenManagerMoves);

        btnNewMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClicked(v);
            }
        });
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClicked(v);
            }
        });

        for (ProductBinResponse prod : inputList) {
            tot = tot + prod.getQtyInBin();
        }

        txtProductDetails.setText(inputList.get(0).getArtist() + " - " + inputList.get(0).getTitle());
        txtPalate.setText(currentSource);
        txtQty.setText(String.format("%s", tot));
        adapter = new ReplenMiniMoveAdapter(ActReplenManager.this, moveList);
        lvRepelen.setAdapter(adapter);

        if (adapter.isEmpty()) {
            txtListTile.setVisibility(View.GONE);
        }
        saveQuantityData();
    }

    private void exitActivity(){
        Intent i = new Intent();    //If we have zero then leave automatically since we don't have anything to move
        i.putExtra("BIN_EXTRA", currentSource);
        i.putExtra("QTYCHANGED_EXTRA", qtyChanged);
        setResult(RESULT_OK, i);
        finish();
    }

    private void saveQuantityData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("From", currentSource);
        editor.putInt("Quantity", tot);
        editor.commit();
    }

    private void buildPrimaryLocations() {
        //TODO - Fix this method when you can find some time
        if (!adapter.isEmpty()) {
            List<Bin> bins = new ArrayList<Bin>();
            List<ReplenMiniMove> newMoveList = new ArrayList<ReplenMiniMove>();
            List<ReplenMiniMove> newMoveListPrep = new ArrayList<ReplenMiniMove>();
            List<ReplenMiniMove> moveSearch = new ArrayList<ReplenMiniMove>();
            List<ReplenMiniMove> moveDup = new ArrayList<ReplenMiniMove>();
            ListIterator<ReplenMiniMove> moveIterator = moveList.listIterator();
            List<AbstractMap.SimpleEntry<String, int[]>> refined = null;

            int found = 0;
            for (ReplenMiniMove move : moveList) {
                if (moveSearch.isEmpty()) {
                    moveSearch.add(move);
                } else {
                    if (!moveSearch.isEmpty()) {
                        for (int i = 0; i < moveSearch.size(); i++) {
                            ReplenMiniMove xMove = moveSearch.get(i);
                            if (move.getDestination().equalsIgnoreCase(xMove.getDestination())) {
                                //Find duplicates ....
                                found++;
                                moveDup.add(xMove);
                            }
                        }
                    }
                }
            }

            if (moveList.size() > 1) {
                for (ReplenMiniMove move : moveList) {
                    while(moveIterator.hasNext()){
                        ReplenMiniMove item = moveIterator.next();
                        if(item.getDestination().equals(move.getDestination())){
                            item.setQuantity(item.getQuantity() + move.getQuantity());
                            moveIterator.set(item);
                        }
                    }
                }
            }
            moveList = new ArrayList<ReplenMiniMove>();
            moveList = IteratorUtils.toList(moveIterator);

            for (ReplenMiniMove move : moveList) {
                Bin bin = new Bin(move.getDestination(), move.getQuantity());

                if (bins.isEmpty()) {
                    bins.add(bin);
                }else {
                    if (!bins.contains(bin)) {
                        bins.add(bin);
                    }
                }
            }
            for (int i = 0; i < bins.size(); i ++) {
                if (bins.get(i).getBinCode().substring(0, 1).equalsIgnoreCase("1")) {
                    primaryList.add(bins.get(i));
                }
            }
        }

    }

    private void updateInputListQuantity(ProductBinSelection moveItem) {
        if (moveItem != null && !inputList.isEmpty()) {
            inputList.get(0).setQtyInBin(moveItem.getQtyInBin()); //TODO - updates values manually <<< Find a better way to minimise RISK >>>
        }
    }

    private void onButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnExitActReplenManager:
                exitActivity();
                break;
            case R.id.bnReplenNewMove:
                //Generate new primaryList & then navigate to the scanning screen to acquire a move (dst + qty)
                int qtyParam = 0;
                //int qty = prefs.getInt("NewQuantity", 0);
                int qty = tot;
                if (qty > 0) {
                    qtyParam = qty;
                }else {
                    qtyParam = inputList.get(0).getQtyInBin();
                }
                backParam ++;
                //if (primaryList.size() == 0)
                buildPrimaryLocations();
                Intent i = new Intent(ActReplenManager.this, ActReplenCreateMiniMove.class);
                i.putExtra("QUANTITY_EXTRA", qtyParam);
                i.putExtra("DATA_EXTRA", (java.io.Serializable) inputList);
                i.putExtra("SOURCE_EXTRA", currentSource);
                i.putExtra("PRIMARY_EXTRA", (java.io.Serializable) primaryList);
                startActivityForResult(i, 13);
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Return ReplenMiniMove so that we can subtract from the total and add the move to the list
        if (data != null) {
            Bundle extra = data.getExtras();
            ReplenMiniMove miniMove = (ReplenMiniMove) extra.getSerializable("RETURN_EXTRA");
            ProductBinSelection moveItemResp = (ProductBinSelection) extra.getSerializable("MOVE_EXTRA");
            if (moveItemResp != null) {
                qtyChanged = inputList.get(0).getQtyInBin() != moveItemResp.getQtyInBin();
                tot = moveItemResp.getQtyInBin();
                txtQty.setText(String.format("%s", tot));       //update total
                updateInputListQuantity(moveItemResp);  //update input list
            }
            if (miniMove != null) {
                moveList.add(miniMove); //updates the listView adapter as well
                //adapter.add(miniMove);
                //adapter.notifyDataSetChanged();
                adapter = new ReplenMiniMoveAdapter(this, moveList);
                lvRepelen.setAdapter(adapter);
            }
        }
        if (tot < 1) {
            exitActivity();
        }
    }
}