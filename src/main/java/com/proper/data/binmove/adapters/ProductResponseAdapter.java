package com.proper.data.binmove.adapters;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.data.binmove.Bin;
import com.proper.data.binmove.ProductResponse;
import com.proper.warehousetools.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 05/06/2014.
 */
public class ProductResponseAdapter extends BaseAdapter {
    private Context thisContext;
    private List<ProductResponse> thisResponse = new ArrayList<ProductResponse>();

    public ProductResponseAdapter(Context thisContext, List<ProductResponse> productResponses) {
        this.thisContext = thisContext;
        this.thisResponse = productResponses;
    }

    @Override
    public int getCount() {
        return thisResponse.size();
    }

    @Override
    public Object getItem(int i) {
        return thisResponse.get(i);
    }

    @Override
    public long getItemId(int i) {
        return thisResponse.get(i).getProductId();
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View iView = view;
        if (iView == null) {
            LayoutInflater inflater = (LayoutInflater) thisContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            iView = inflater.inflate(R.layout.list_binmove_product_item, viewGroup, false);
        }

        ProductResponse prod = thisResponse.get(pos);

        int totalQty = 0;
        for (Bin bin : prod.getBins()) {
            totalQty += bin.getQty();
        }

        TextView txtProd = (TextView) iView.findViewById(R.id.txtvProdItem);
        txtProd.setText(String.format("Catalog: %s\nQty: %s", prod.getSupplierCat(), totalQty));
//        if (totalQty > 0) {
//            txtProd.setText(String.format("Catalog: %s\nQty: %s", prod.getSupplierCat(), totalQty));
//        } else {
//            txtProd.setText(String.format("Catalog: %s", prod.getSupplierCat()));
//        }
        txtProd.setGravity(Gravity.CENTER);
        txtProd.setMinHeight(30);
        return iView;
    }
}
