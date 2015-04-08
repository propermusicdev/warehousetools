package com.proper.data.binmove.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.proper.data.binmove.Bin;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Lebel on 14/04/2014.
 */
public class BinListAdapter extends BaseAdapter {
    private Context context;
    protected List<Bin> bins;
    private LayoutInflater inflater = null;

    public BinListAdapter(Context context, List<Bin> bins) {
        this.context = context;
        this.bins = bins;
    }

    public BinListAdapter(Context context, LayoutInflater inflater, List<Bin> bins) {
        this.context = context;
        this.bins = bins;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return bins.size();
    }

    @Override
    public Bin getItem(int index) {
        return bins.get(index);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View myView = view;
        if (myView == null) {
            if (inflater != null) {
                myView = inflater.inflate(R.layout.list_binmove_binitem1, viewGroup, false);
            } else {
                LayoutInflater thisInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                myView = thisInflater.inflate(R.layout.list_binmove_binitem1, viewGroup, false);
            }
        }

        Bin bin = bins.get(pos);
        TextView txtBinCode = (TextView) myView.findViewById(R.id.binCode);
        TextView txtQuantity = (TextView) myView.findViewById(R.id.quantity);
        ImageView imgIcon = (ImageView) myView.findViewById(R.id.icon); // so far we don't want to generate bin barcode images too long.

        txtBinCode.setText(String.format("Bin:  %s", bin.getBinCode()));
        txtQuantity.setText(String.format("Qty: %s", bin.getQty()));

        return myView;
    }
}
