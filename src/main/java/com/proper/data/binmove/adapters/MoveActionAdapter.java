package com.proper.data.binmove.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.data.binmove.BinMoveObject;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Lebel on 02/06/2014.
 */
public class MoveActionAdapter extends BaseAdapter {
    private Context context;
    private List<BinMoveObject> theseActions;

    public MoveActionAdapter(Context context,  List<BinMoveObject> actions) {
        this.context = context;
        this.theseActions = actions;
    }

    @Override
    public int getCount() {
        return theseActions.size();
    }

    @Override
    public BinMoveObject getItem(int i) {
        return theseActions.get(i);
    }

    @Override
    public long getItemId(int i) {
        return theseActions.get(i).getProductId();
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View myView = view;
        ViewHolder holder = new ViewHolder();
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmove_binaction_item, viewGroup, false);
            holder.lblAction = (TextView) myView.findViewById(R.id.lblAction);
            holder.txtAction = (TextView) myView.findViewById(R.id.txtvAction);
            holder.lblProductId = (TextView) myView.findViewById(R.id.lblProductId);
            holder.txtProductId = (TextView) myView.findViewById(R.id.txtvProductId);
            holder.lblSupplierCat = (TextView) myView.findViewById(R.id.lblSupplierCat);
            holder.txtSupplierCat = (TextView) myView.findViewById(R.id.txtvSupplierCat);
            holder.lblEAN = (TextView) myView.findViewById(R.id.lblEAN);
            holder.txtEAN = (TextView) myView.findViewById(R.id.txtvEAN);
            holder.lblQty = (TextView) myView.findViewById(R.id.lblQty);
            holder.txtQty = (TextView) myView.findViewById(R.id.txtvQty);
            myView.setTag(holder);
        }else {
            holder = (ViewHolder) myView.getTag();
        }
        BinMoveObject action = theseActions.get(pos);
        //assign control values and display
        holder.lblAction.setText("Action:  "); holder.lblProductId.setText("ProductID:    ");
        holder.lblSupplierCat.setText("Catalog");  holder.lblEAN.setText("EAN:    ");
        holder.lblQty.setText("Quantity:    ");

        holder.txtAction.setText(action.getAction());  holder.txtProductId.setText(String.format("%s", action.getProductId()));
        holder.txtSupplierCat.setText(action.getSupplierCat());    holder.txtEAN.setText(action.getEAN());
        holder.txtQty.setText(String.format("%s", action.getQty()));
        return myView;
    }

    static class ViewHolder {
        TextView lblAction;
        TextView txtAction;
        TextView lblProductId;
        TextView txtProductId;
        TextView lblSupplierCat;
        TextView txtSupplierCat;
        TextView lblEAN;
        TextView txtEAN;
        TextView lblQty;
        TextView txtQty;
    }
}
