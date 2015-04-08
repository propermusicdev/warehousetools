package com.proper.data.binmove.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.proper.data.binmove.BarcodeResponse;
import com.proper.data.binmove.Bin;
import com.proper.data.binmove.ProductResponse;
import com.proper.warehousetools.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 30/05/2014.
 */
public class BarcodeResponseAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<ProductResponse> thisResponse =  new ArrayList<ProductResponse>();

    public BarcodeResponseAdapter(Context context, BarcodeResponse barcodeResponse) {
        this.context = context;
        thisResponse = barcodeResponse.getProducts();
    }

    @Override
    public int getGroupCount() {
        return thisResponse.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return thisResponse.get(i).getBins().size();
    }

    @Override
    public ProductResponse getGroup(int i) {
        return thisResponse.get(i);
    }

    @Override
    public Bin getChild(int i, int i2) {
        return thisResponse.get(i).getBins().get(i2);
    }

    @Override
    public long getGroupId(int i) {
        return thisResponse.get(i).getProductId();
    }

    @Override
    public long getChildId(int i, int i2) {
        //return thisResponse.get(i).getBins().get(i2);
        return i2;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        View myView = view;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmove_qryview_item2, viewGroup, false);
        }

        ProductResponse response = thisResponse.get(i);
//          Populate views candidate code below :
//        String displayValue = String.format("Artist:     %s\nTitle:     %s", response.getFullArtist(), response.getFullTitle());
//        TextView txtChild = new TextView(context);
//        txtChild.setText(displayValue);
//        txtChild.setTextSize(12);
//        txtChild.setTypeface(null, Typeface.BOLD);
//        txtChild.setTextColor(Color.parseColor("#695cf5"));
//        return txtChild;
        //Populate views Alternative code below :
//        TextView txtArtist = (TextView) myView.findViewById(R.id.txtv_Artist);
//        TextView txtTitle = (TextView) myView.findViewById(R.id.txtv_Title);
//
//        //TextView lblShortDesc = (TextView) myView.findViewById(R.id.lblShortDesc);
//        TextView lblISBN = (TextView) myView.findViewById(R.id.lblISBN);
//        TextView lblFormat = (TextView) myView.findViewById(R.id.lblFormat);
//        TextView lblBinNo = (TextView) myView.findViewById(R.id.lblBinNumber);
//        TextView lblOutOfStock = (TextView) myView.findViewById(R.id.lblOutOfStock);
//        TextView lblOnHand = (TextView) myView.findViewById(R.id.lblOnHand);
//        TextView lblPrice = (TextView) myView.findViewById(R.id.lblPrice);
//        TextView txtShortDesc = (TextView) myView.findViewById(R.id.txtvShortDesc);
//        TextView txtISBN = (TextView) myView.findViewById(R.id.txtvISBN);
//        TextView txtFormat = (TextView) myView.findViewById(R.id.txtvFormat);
//        TextView txtBinNo = (TextView) myView.findViewById(R.id.txtvBinNumber);
//        TextView txtOutOfStock = (TextView) myView.findViewById(R.id.txtvOutOfStock);
//        TextView txtOnHand = (TextView) myView.findViewById(R.id.txtvOnHand);
//        TextView txtPrice = (TextView) myView.findViewById(R.id.txtvPrice);
//
//        //txtArtist.setText(prod.getArtist()) ; txtTitle.setText(prod.getTitle());
//        //lblShortDesc.setText("Short Description:") ; lblISBN.setText("EAN:");
//        lblFormat.setText("Format:") ; lblBinNo.setText("Bin Number:");
//        lblOutOfStock.setText("Out of Stock:") ; lblOnHand.setText("Stock On Hand:");
//        lblPrice.setText("Price:") ; //lblTime.setText("Time Elapsed:");
//        //lblOriginatedBin.setText("Originated Bin:");
//        //txtShortDesc.setText(response.) ;
//        txtISBN.setText(response.getBarcode());
//        txtFormat.setText(response.getFormat()) ; txtBinNo.setText(response.getFormat());
//        txtOutOfStock.setText(String.format("%s", response.getDeletionType()));
//        txtOnHand.setText(String.format("%s", response.getStockAmount()));
        //txtPrice.setText(String.format("£    %s", response.get));
        //txtTime.setText(String.format("%s", prevTaskElapsedTime));
        TextView txtArtist = (TextView) myView.findViewById(R.id.txtvArtist_qryView2);
        TextView txtTitle = (TextView) myView.findViewById(R.id.txtvTitle_qryView2);
        txtArtist.setText(response.getSupplierCat());
        txtTitle.setText("Format: " + response.getFormat());
        return myView;
    }

    @Override
    public View getChildView(int i, int i2, boolean b, View view, ViewGroup viewGroup) {
//        Bin bin = thisResponse.get(i).getBins().get(i2);
//        String displayValue = String.format("Selected Bin:     %s,         Qty:     %s", bin.getBinCode(), bin.getQty());
//        TextView txtChild = new TextView(context);
//        txtChild.setText(displayValue);
//        txtChild.setTextSize(14);
//        txtChild.setTypeface(null, Typeface.BOLD);
//        txtChild.setTextColor(Color.parseColor("#695cf5"));
//        return txtChild;
        View myView = view;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmove_binitem1, viewGroup, false);
        }
        Bin bin = thisResponse.get(i).getBins().get(i2);
        TextView txtBincode = (TextView) myView.findViewById(R.id.binCode);
        TextView txtQty = (TextView) myView.findViewById(R.id.quantity);
        txtBincode.setText("Bin: " + bin.getBinCode());
        txtQty.setText(String.format("Qty: %s", bin.getQty()));
        return myView;
    }

    @Override
    public boolean isChildSelectable(int i, int i2) {
        return true;    //return false;
    }
}
