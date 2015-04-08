package com.proper.data.replen.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.proper.data.replen.ReplenMoveListItemResponse;
import com.proper.warehousetools.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Lebel on 27/11/2014.
 */
public class ReplenMoveListAdapter extends BaseExpandableListAdapter {
    private Context context;
    protected List<ReplenMoveListItemResponse> moves;
    private LayoutInflater inflater = null;

    public ReplenMoveListAdapter(Context context, List<ReplenMoveListItemResponse> moves) {
        this.context = context;
        this.moves = moves;
    }

    @Override
    public int getGroupCount() {
        return moves.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //return moves.size();
        return 1; // it doesn't matter since we're only faking it
    }

    @Override
    public ReplenMoveListItemResponse getGroup(int groupPosition) {
        return moves.get(groupPosition);
    }

    @Override
    public ReplenMoveListItemResponse getChild(int groupPosition, int childPosition) {
        return moves.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return moves.get(groupPosition).getMovelistId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return moves.get(groupPosition).getMovelistId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPos, boolean isExpanded, View view, ViewGroup parent) {
        GroupViewHolder holder = new GroupViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_replen_movelist_item_group1, parent, false);
            holder.txtMoveId = (TextView) view.findViewById(R.id.txtvReplenMLMoveId);
            holder.txtMoveQty = (TextView) view.findViewById(R.id.txtvReplenMLMoveTotalQty);
            //SeekBar seekBar = (SeekBar) myView.findViewById(R.id.seekBarReplenMLTotalLines);
            view.setTag(holder);
        }else{
            holder = (GroupViewHolder) view.getTag();
        }
        ReplenMoveListItemResponse move = moves.get(groupPos);
        holder.txtMoveId.setText(String.format("ID :   %s", move.getMovelistId()));
        holder.txtMoveQty.setText(String.format("Qty:  %s", move.getTotalQty()));
        //seekBar.setProgress(Math.round((move.getTotalLines() * 100) / 30));
        holder.position = groupPos;
        return view;
    }

    @Override
    public View getChildView(int groupPos, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ChildViewHolder holder = new ChildViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_replen_movelist_item_child, parent, false);
            holder.txtMoveStatus = (TextView) convertView.findViewById(R.id.txtvReplenMLStatus);
            holder.txtMoveTotalLines = (TextView) convertView.findViewById(R.id.txtvReplenMLTotalLines);
            holder.txtMoveTimeStamp = (TextView) convertView.findViewById(R.id.txtvReplenMLTimeStamp);
            convertView.setTag(holder);
        }else{
            holder = (ChildViewHolder) convertView.getTag();
        }
        ReplenMoveListItemResponse move = moves.get(groupPos);
        holder.txtMoveStatus.setText(String.format("%s", move.getStatus()));
        holder.txtMoveTotalLines.setText(String.format("%s", move.getTotalLines()));
        holder.txtMoveTimeStamp.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(move.getInsertTimeStamp()));
        holder.position = groupPos;
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView txtMoveId;
        TextView txtMoveQty;
        int position;
    }

    static class ChildViewHolder {
        TextView txtMoveStatus;
        TextView txtMoveTotalLines;
        TextView txtMoveTimeStamp;
        int position;
    }
}
