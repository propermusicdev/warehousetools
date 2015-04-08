package com.proper.data.stocktake.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.proper.data.customcontrols.LetterSpacingTextView;
import com.proper.data.stocktake.StockTakeLineProduct;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Knight on 26/01/2015.
 */
public class StockTakeProductLineAdapter extends BaseExpandableListAdapter {
    private Context context = null;
    private List<StockTakeLineProduct> products;

    public StockTakeProductLineAdapter(Context context, List<StockTakeLineProduct> products) {
        this.context = context;
        this.products = products;
    }

    @Override
    public int getGroupCount() {
        return products.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1; // it doesn't matter since we're only faking it
    }

    @Override
    public StockTakeLineProduct getGroup(int groupPosition) {
        return products.get(groupPosition);
    }

    @Override
    public StockTakeLineProduct getChild(int groupPosition, int childPosition) {
        return products.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return products.get(groupPosition).getProductId();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return products.get(groupPosition).getProductId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupViewHolder holder = new GroupViewHolder();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_stocktake_product_group, parent, false);
            holder.lytGroupMain = (LinearLayout) convertView.findViewById(R.id.lytLSTPGroupMain);
            holder.txtCatNumber = (TextView) convertView.findViewById(R.id.txtvReplenLSTPGroupCatalog);
            holder.txtQty = (TextView) convertView.findViewById(R.id.txtvReplenLSTPGroupQty);
            holder.txtScannedQty = (TextView) convertView.findViewById(R.id.txtvReplenLSTPGroupLineId);
            holder.txtStatus = (TextView) convertView.findViewById(R.id.txtvReplenLSTPGroupStatus);
            convertView.setTag(holder);
        }else{
            holder = (GroupViewHolder) convertView.getTag();
        }

        //Catalog, Qty - ID, Completed
        StockTakeLineProduct line = products.get(groupPos);
        //Resources res = context.getResources();
        //SeekBar seekBar = (SeekBar) myView.findViewById(R.id.seekBarReplenMLTotalLines);
        holder.txtCatNumber.setText(String.format("Cat: %s", line.getSupplierCat()));
        holder.txtQty.setText(String.format("BinQty:  %s", line.getQty()));
        holder.txtScannedQty.setText(String.format("ScannedQty:  %s", line.getQtyScanned()));
        //if (line.getStatus() > 0) {
        if (line.getBinCheckerFlag() > 0) {
            holder.txtStatus.setText(String.format("Completed"));
            holder.txtStatus.setTextColor(Color.GREEN);
        } else {
            holder.txtStatus.setText(String.format("Incomplete"));
            holder.txtStatus.setTextColor(Color.RED);
        }
        if (line.getQtyScanned() > 0) {
            //if (line.getQty() == line.getQtyScanned() && line.getQtyScanned() > 1) {
            if (line.getQty() == line.getQtyScanned()) {
                //color green
                //holder.lytGroupMain.setBackgroundColor(Color.GREEN);
                holder.lytGroupMain.setBackgroundResource(R.drawable.button_green);
                holder.txtCatNumber.setTextColor(Color.WHITE);
                holder.txtScannedQty.setTextColor(Color.WHITE);
                //if (line.getStatus() > 0) {
                if (line.getBinCheckerFlag() > 0) {
                    holder.txtStatus.setText(String.format("Completed"));
                    holder.txtStatus.setTextColor(Color.BLUE);
                } else {
                    holder.txtStatus.setText(String.format("Incomplete"));
                    holder.txtStatus.setTextColor(Color.RED);
                }
            }
            if (line.getQtyScanned() < line.getQty()) {
                //color yellow
                holder.lytGroupMain.setBackgroundResource(R.drawable.button_yellow);
                holder.txtCatNumber.setTextColor(Color.parseColor("#2d143d"));
                holder.txtScannedQty.setTextColor(Color.parseColor("#2d143d"));
                //if (line.getStatus() > 0) {
                if (line.getBinCheckerFlag() > 0) {
                    holder.txtStatus.setText(String.format("Completed"));
                    holder.txtStatus.setTextColor(Color.GREEN);
                } else {
                    holder.txtStatus.setText(String.format("Incomplete"));
                    holder.txtStatus.setTextColor(Color.RED);
                }
            }
            if (line.getQtyScanned() > line.getQty()) {
                //color golden yellow
                //holder.lytGroupMain.setBackgroundColor(Color.parseColor("#957700"));
                holder.lytGroupMain.setBackgroundResource(R.drawable.button_blue);
                holder.txtCatNumber.setTextColor(Color.WHITE); //#f29f00
                holder.txtScannedQty.setTextColor(Color.WHITE);
                holder.txtQty.setTextColor(Color.WHITE);
                //if (line.getStatus() > 0) {
                if (line.getBinCheckerFlag() > 0) {
                    holder.txtStatus.setText(String.format("Completed"));
                    holder.txtStatus.setTextColor(Color.GREEN);
                } else {
                    holder.txtStatus.setText(String.format("Incomplete"));
                    holder.txtStatus.setTextColor(Color.RED);
                }
            }
        } else {
            holder.lytGroupMain.setBackgroundResource(R.drawable.button_black);
            holder.txtCatNumber.setTextColor(Color.parseColor("#aeb404"));
            holder.txtScannedQty.setTextColor(Color.parseColor("#aeb404"));
            holder.txtQty.setTextColor(Color.parseColor("#0080ff"));
            //if (line.getStatus() > 0) {
            if (line.getBinCheckerFlag() > 0) {
                holder.txtStatus.setText(String.format("Completed"));
                holder.txtStatus.setTextColor(Color.GREEN);
            } else {
                holder.txtStatus.setText(String.format("Incomplete"));
                holder.txtStatus.setTextColor(Color.RED);
            }
        }
        holder.txtStatus.setTypeface(null, Typeface.BOLD);
        holder.position = groupPos;
        //seekBar.setProgress(Math.round((line.getTotalLines() * 100) / 30));
        return convertView;
    }

    @Override
    public View getChildView(int groupPos, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        //Artist,Tile,ProductId,SupplierCat,Artist,Title,Barcode,EAN,Format,QtyInBin,QtyScanned
        ChildViewHolder vcHolder = new ChildViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_stocktake_product, parent, false);
            //albumPic = (ImageView) myView.findViewById(R.id.imgAlbum);
            vcHolder.lytMain = (RelativeLayout) view.findViewById(R.id.lytLSTPMain);
            vcHolder.txtArtist = (TextView) view.findViewById(R.id.txtv_ReplenLSTPArtist);
            vcHolder.txtTitle = (TextView) view.findViewById(R.id.txtv_ReplenLSTPTitle);
            vcHolder.txtEAN = (LetterSpacingTextView) view.findViewById(R.id.lblReplenLSTPEAN);
            vcHolder.lblSuppCat = (TextView) view.findViewById(R.id.lblReplenLSTPSuppCat);
            vcHolder.txtSuppCat = (TextView) view.findViewById(R.id.txtvReplenLSTPSuppCat);
            vcHolder.lblFormat = (TextView) view.findViewById(R.id.lblReplenLSTPFormat);
            vcHolder.txtFormat = (TextView) view.findViewById(R.id.txtvReplenLSTPFormat);
            vcHolder.lblQtyInBin = (TextView) view.findViewById(R.id.lblReplenLSTPQtyInBin);
            vcHolder.txtQtyInBin = (TextView) view.findViewById(R.id.txtvReplenLSTPQtyInBin);
            vcHolder.lblQtyScanned = (TextView) view.findViewById(R.id.lblReplenLSTPQtyScanned);
            vcHolder.txtQtyScanned = (TextView) view.findViewById(R.id.txtvReplenLSTPQtyScanned);
            view.setTag(vcHolder);
        } else{
            vcHolder = (ChildViewHolder) view.getTag();
        }
        StockTakeLineProduct prod = products.get(groupPos);     //get data
        vcHolder.txtArtist.setText(prod.getArtist());
        vcHolder.txtTitle.setText(prod.getTitle());
        vcHolder.txtEAN.setLetterSpacing(31);
        vcHolder.txtEAN.setText(prod.getEAN());
        vcHolder.txtEAN.setTypeface(null, Typeface.BOLD);
        vcHolder.lblSuppCat.setText("Catalog:");
        vcHolder.txtSuppCat.setText(prod.getSupplierCat());
        vcHolder.lblFormat.setText("Format:");
        vcHolder.txtFormat.setText(prod.getFormat());
        vcHolder.lblQtyInBin.setText("Qty In Bin:");
        vcHolder.txtQtyInBin.setText(String.format("%s", prod.getQty()));
        vcHolder.lblQtyScanned.setText("Qty Scanned:");
        vcHolder.txtQtyScanned.setText(String.format("%s", prod.getQtyScanned()));
//        if (prod.getQtyInBin() == prod.getQtyScanned()) {
//            holder.lytMain.setBackgroundColor(Color.GREEN);
//        }
        if (prod.getQtyScanned() > 0) {
            if (prod.getQty() == prod.getQtyScanned()) {
                //color green
                vcHolder.lytMain.setBackgroundColor(Color.parseColor("#7bbf6a"));
            }
            if (prod.getQtyScanned() < prod.getQty()) {
                //color yellow
                vcHolder.lytMain.setBackgroundColor(Color.parseColor("#ecc038"));
            }
            if (prod.getQtyScanned() > prod.getQty()) {
                //color golden yellow
                vcHolder.lytMain.setBackgroundColor(Color.parseColor("#009acd"));
            }
        } else {
            vcHolder.lytMain.setBackgroundColor(Color.TRANSPARENT);
        }
        vcHolder.position = groupPos;
        //barcodePic.setImageBitmap(generateBarCode(prod.getBarcode()));
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    static class GroupViewHolder {
        LinearLayout lytGroupMain;
        TextView txtCatNumber;
        TextView txtQty;
        TextView txtScannedQty;
        TextView txtStatus;
        int position;
    }

    static  class ChildViewHolder {
        RelativeLayout lytMain;
        TextView txtArtist;
        TextView txtTitle;
        LetterSpacingTextView txtEAN;
        TextView lblSuppCat;
        TextView txtSuppCat;
        TextView lblFormat;
        TextView txtFormat;
        TextView lblQtyInBin;
        TextView txtQtyInBin;
        TextView lblQtyScanned;
        TextView txtQtyScanned;
        int position;
    }
}
