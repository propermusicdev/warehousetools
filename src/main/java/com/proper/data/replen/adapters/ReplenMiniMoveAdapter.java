package com.proper.data.replen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.proper.data.binmove.Bin;
import com.proper.data.replen.ReplenMiniMove;
import com.proper.warehousetools.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 08/09/2014.
 */
public class ReplenMiniMoveAdapter extends BaseAdapter {
    private Context context;
    private List<ReplenMiniMove> moves;

    public ReplenMiniMoveAdapter(Context ctxt, List<ReplenMiniMove> moveList) {
        this.context = ctxt;
        this.moves = moveList;
    }

    @Override
    public int getCount() {
        return moves.size();
    }

    @Override
    public ReplenMiniMove getItem(int position) {
        return moves.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater)  context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_replen_minimove, parent, false);
            holder.txtBin = (TextView) view.findViewById(R.id.txtvReplenMiniMoveDestination);
            holder.txtQuantity = (TextView) view.findViewById(R.id.txtvReplenMiniMoveQuantity);
            view.setTag(holder);
        }else{
            holder = (ViewHolder) view.getTag();
        }
        ReplenMiniMove thisMove = moves.get(position);
        holder.txtBin.setText(String.format("Dst Bin: %s", thisMove.getDestination()));
        holder.txtQuantity.setText(String.format("Qty: %s", thisMove.getQuantity()));
        return view;
    }

    public void add(ReplenMiniMove miniMove) {
        this.moves.add(miniMove);
        this.notifyDataSetChanged();
    }

    public List<Bin> getAllBins() {
        List<Bin> bins = new ArrayList<Bin>();
        for (ReplenMiniMove move : moves) {
            Bin bin = new Bin(move.getDestination(), move.getQuantity());
            bins.add(bin);
        }
        return bins;
    }
    static class ViewHolder {
        TextView txtBin;
        TextView txtQuantity;
    }
}
