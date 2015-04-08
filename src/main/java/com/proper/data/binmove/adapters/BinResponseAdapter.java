package com.proper.data.binmove.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.proper.data.binmove.BinResponse;
import com.proper.data.binmove.ProductBinResponse;
import com.proper.warehousetools.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 30/05/2014.
 */
public class BinResponseAdapter extends BaseAdapter {
    private Context context;
    private List<ProductBinResponse> thisResponse = new ArrayList<ProductBinResponse>();

    public BinResponseAdapter(Context context, BinResponse binResponse) {
        this.context = context;
        thisResponse = binResponse.getProducts();
    }

    @Override
    public int getCount() {
        return thisResponse.size();
    }

    @Override
    public ProductBinResponse getItem(int i) {
        return thisResponse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return thisResponse.get(i).getProductId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View thisView = view;
        View myView = view;
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmove_binitem1, viewGroup, false);
        }
        //TODO - Do stuff - Access data here

        return thisView;
    }
}
