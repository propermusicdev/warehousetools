package com.proper.data.replen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import com.proper.data.replen.ReplenMoveListLinesItemResponse;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Lebel on 18/12/2014.
 */
public class ReplenAddMoveLineAdapter extends BaseAdapter {
    private Context context;
    protected List<ReplenMoveListLinesItemResponse> moves;
    //private LayoutInflater inflater = null;

    public ReplenAddMoveLineAdapter(Context context, List<ReplenMoveListLinesItemResponse> moves) {
        this.context = context;
        this.moves = moves;
    }

    @Override
    public int getCount() {
        return moves.size();
    }

    @Override
    public ReplenMoveListLinesItemResponse getItem(int position) {
        return moves.get(position);
    }

    @Override
    public long getItemId(int position) {
        return moves.get(position).getMovelistLineId();
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_replen_addline_item, parent, false);
//            holder.txtLineID = (TextView) myView.findViewById(R.id.txtvReplenALID);
//            holder.txtCatalog = (TextView) myView.findViewById(R.id.txtvReplenALCatalog);
            holder.txtSrcBin = (EditText) convertView.findViewById(R.id.txtvReplenALISrcBin);
            holder.txtDstBin = (EditText) convertView.findViewById(R.id.txtvReplenALIDstBin);
            holder.txtQty = (TextView) convertView.findViewById(R.id.txtvReplenALIQty);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        ReplenMoveListLinesItemResponse move = moves.get(pos);
        //populate
//        holder.txtLineID.setText(String.format("%s", move.getMovelistLineId()));
//        holder.txtCatalog.setText(move.getCatNumber());
        holder.txtSrcBin.setText(move.getSrcBinCode());
        holder.txtDstBin.setText(move.getDstBinCode());
        holder.txtQty.setText(String.format("%s", move.getQty()));
        holder.position = pos;
        return convertView;
    }

    static class ViewHolder {
//        TextView txtLineID;
//        TextView txtCatalog;
        EditText txtSrcBin;
        EditText txtDstBin;
        TextView txtQty;
        int position;
    }
}
