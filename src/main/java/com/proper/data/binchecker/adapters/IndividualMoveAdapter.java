package com.proper.data.binchecker.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.data.binchecker.IndividualMoveLine;
import com.proper.warehousetools.R;

import java.util.LinkedList;

/**
 * Created by Lebel on 31/03/2015.
 */
public class IndividualMoveAdapter extends BaseAdapter {
    private Context context = null;
    private LinkedList<IndividualMoveLine> moves = null;

    public IndividualMoveAdapter(Context context, LinkedList<IndividualMoveLine> moves) {
        this.context = context;
        this.moves = moves;
    }

    @Override
    public int getCount() {
        return moves.size();
    }

    @Override
    public IndividualMoveLine getItem(int position) {
        return moves.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_binchecker_individual, parent, false);
            holder.txtSupplierCat = (TextView) view.findViewById(R.id.txtvBCKLSupplierCat);
            holder.txtQty = (TextView) view.findViewById(R.id.txtvBCKLQuantity);

            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        IndividualMoveLine move = moves.get(pos);
        holder.txtSupplierCat.setText(move.getSupplierCat());
        holder.txtQty.setText(String.format("%s", move.getQty()));
        holder.position = pos;

        return view;
    }

    class ViewHolder {
        TextView txtSupplierCat;
//        TextView txtSrcBin;
//        TextView txtDstBin;
        TextView txtQty;
        int position;
    }
}
