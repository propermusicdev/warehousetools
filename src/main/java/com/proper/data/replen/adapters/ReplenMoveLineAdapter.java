package com.proper.data.replen.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import com.proper.data.customcontrols.LetterSpacingTextView;
import com.proper.data.replen.ReplenMoveListLinesItemResponse;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Lebel on 02/12/2014.
 */
public class ReplenMoveLineAdapter extends BaseExpandableListAdapter {
    private Context context;
    protected List<ReplenMoveListLinesItemResponse> lines;
    private LayoutInflater inflater = null;

    public ReplenMoveLineAdapter(Context context, List<ReplenMoveListLinesItemResponse> lines) {
        this.context = context;
        this.lines = lines;
    }

    @Override
    public int getGroupCount() {
        return lines.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        //return lines.size();
        return 1; // it doesn't matter since we're only faking it
    }

    @Override
    public ReplenMoveListLinesItemResponse getGroup(int groupPosition) {
        return lines.get(groupPosition);
    }

    @Override
    public ReplenMoveListLinesItemResponse getChild(int groupPosition, int childPosition) {
        return lines.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return lines.get(groupPosition).getMovelistLineId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return lines.get(groupPosition).getMovelistLineId();
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
            view = inflater.inflate(R.layout.list_replen_moveline_item_group, parent, false);
            holder.txtCatNumber = (TextView) view.findViewById(R.id.txtvReplenMLCatalog);
            holder.txtQty = (TextView) view.findViewById(R.id.txtvReplenMLQty);
            holder.txtMoveId = (TextView) view.findViewById(R.id.txtvReplenMLLineId);
            holder.txtCompleted = (TextView) view.findViewById(R.id.txtvReplenMLMoveCompleted);
            view.setTag(holder);
        }else{
            holder = (GroupViewHolder) view.getTag();
        }

        //Catalog, Qty - ID, Completed
        ReplenMoveListLinesItemResponse line = lines.get(groupPos);
        //SeekBar seekBar = (SeekBar) myView.findViewById(R.id.seekBarReplenMLTotalLines);
        holder.txtCatNumber.setText(String.format("Cat: %s", line.getCatNumber()));
        holder.txtQty.setText(String.format("Qty:  %s", line.getQty()));
        holder.txtMoveId.setText(String.format("ID :   %s", line.getMovelistLineId()));
        if (line.isCompleted()) {
            holder.txtCompleted.setText(String.format("Completed"));
            holder.txtCompleted.setTextColor(Color.GREEN);
        } else {
            holder.txtCompleted.setText(String.format("Incomplete"));
            holder.txtCompleted.setTextColor(Color.RED);
        }
        holder.txtCompleted.setTypeface(null, Typeface.BOLD);
        holder.position = groupPos;
        //seekBar.setProgress(Math.round((line.getTotalLines() * 100) / 30));
        return view;
    }

    @Override
    public View getChildView(int groupPos, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        ChildViewHolder holder = new ChildViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_replen_moveline_item_child, parent, false);
            //albumPic = (ImageView) myView.findViewById(R.id.imgAlbum);
            holder.txtArtist = (TextView) view.findViewById(R.id.txtv_ReplenMLIArtist);
            holder.txtTitle = (TextView) view.findViewById(R.id.txtv_ReplenMLITitle);
            holder.txtEAN = (LetterSpacingTextView) view.findViewById(R.id.lblReplenMLIEAN);
            holder.lblSrcBinCode = (TextView) view.findViewById(R.id.lblReplenMLISrcBin);
            holder.txtSrcBinCode = (TextView) view.findViewById(R.id.txtvReplenMLISrcBin);
            holder.lblDstBin = (TextView) view.findViewById(R.id.lblReplenMLIDstBin);
            holder.txtDstBin = (TextView) view.findViewById(R.id.txtvReplenMLIDstBin);
            holder.lblQtyConfirmed = (TextView) view.findViewById(R.id.lblReplenMLIQtyConfirmed);
            holder.txtQtyConfirmed = (TextView) view.findViewById(R.id.txtvReplenMLIQtyConfirmed);
            view.setTag(holder);
        }else{
            holder = (ChildViewHolder) view.getTag();
        }
        //artist, Title, EAN, SrcBin, DstBin, Qty
        ReplenMoveListLinesItemResponse line = lines.get(groupPos);

        holder.txtArtist.setText(line.getArtist());
        holder.txtTitle.setText(line.getTitle());
        holder.txtEAN.setLetterSpacing(31);
        holder.txtEAN.setText(line.getEAN());
        holder.txtEAN.setTypeface(null, Typeface.BOLD);
        holder.lblSrcBinCode.setText("Src Bin:");
        holder.txtSrcBinCode.setText(line.getSrcBinCode());
        holder.lblDstBin.setText("Dst Bin:");
        holder.txtDstBin.setText(line.getDstBinCode());
        holder.lblQtyConfirmed.setText("Qty Confirmed:");
        holder.txtQtyConfirmed.setText(String.format("%s", line.getQty()));
        if (line.isQtyConfirmed()) {
            holder.txtQtyConfirmed.setText("Yes");
            holder.txtQtyConfirmed.setTextColor(Color.GREEN);
        } else{
            holder.txtQtyConfirmed.setText("No");
            holder.txtQtyConfirmed.setTextColor(Color.RED);
        }
        holder.txtQtyConfirmed.setTypeface(null, Typeface.BOLD);
        holder.position = groupPos;
        //barcodePic.setImageBitmap(generateBarCode(prod.getBarcode()));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class GroupViewHolder {
        TextView txtCatNumber;
        TextView txtQty;
        TextView txtMoveId;
        TextView txtCompleted;
        int position;
    }

    static  class ChildViewHolder {
        TextView txtArtist;
        TextView txtTitle;
        LetterSpacingTextView txtEAN;
        TextView lblSrcBinCode;
        TextView txtSrcBinCode;
        TextView lblDstBin;
        TextView txtDstBin;
        TextView lblQtyConfirmed;
        TextView txtQtyConfirmed;
        int position;
    }
}
