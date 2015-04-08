package com.proper.data.stocktake.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.proper.data.stocktake.StockTakeBinResponse;

import java.util.List;

/**
 * Created by Lebel on 22/01/2015.
 */
public class StockTakeBinAdapter extends BaseAdapter {
    private Context context = null;
    private List<StockTakeBinResponse> binResponses = null;

    @Override
    public int getCount() {
        return binResponses.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
