package com.proper.data.stocktake.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.proper.data.customcontrols.LetterSpacingTextView;
import com.proper.data.stocktake.StockTakeProduct;
import com.proper.warehousetools.R;

import java.util.List;

/**
 * Created by Knight on 26/01/2015.
 */
public class StockTakeProductAdapter extends BaseAdapter {
    private Context context = null;
    private List<StockTakeProduct> products = null;

    public StockTakeProductAdapter(Context context, List<StockTakeProduct> products) {
        this.context = context;
        this.products = products;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public StockTakeProduct getItem(int position) {
        return products.get(position);
    }

    @Override
    public long getItemId(int position) {
        return products.get(position).getProductId();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        //Artist,Tile,ProductId,SupplierCat,Artist,Title,Barcode,EAN,Format,QtyInBin,QtyScanned
        ViewHolder holder = new ViewHolder();
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_binmove_binitem1, parent, false);
            //albumPic = (ImageView) myView.findViewById(R.id.imgAlbum);
            holder.lytMain = (RelativeLayout) view.findViewById(R.id.lytLSTPMain);
            holder.txtArtist = (TextView) view.findViewById(R.id.txtv_ReplenLSTPArtist);
            holder.txtTitle = (TextView) view.findViewById(R.id.txtv_ReplenLSTPTitle);
            holder.txtEAN = (LetterSpacingTextView) view.findViewById(R.id.lblReplenLSTPEAN);
            holder.lblSuppCat = (TextView) view.findViewById(R.id.lblReplenLSTPSuppCat);
            holder.txtSuppCat = (TextView) view.findViewById(R.id.txtvReplenLSTPSuppCat);
            holder.lblFormat = (TextView) view.findViewById(R.id.lblReplenLSTPFormat);
            holder.txtFormat = (TextView) view.findViewById(R.id.txtvReplenLSTPFormat);
            holder.lblQtyInBin = (TextView) view.findViewById(R.id.lblReplenLSTPQtyInBin);
            holder.txtQtyInBin = (TextView) view.findViewById(R.id.txtvReplenLSTPQtyInBin);
            holder.lblQtyScanned = (TextView) view.findViewById(R.id.lblReplenLSTPQtyScanned);
            holder.txtQtyScanned = (TextView) view.findViewById(R.id.txtvReplenLSTPQtyScanned);
            view.setTag(holder);
        }
        StockTakeProduct prod = products.get(position);     //get data
        holder.txtArtist.setText(prod.getArtist());
        holder.txtTitle.setText(prod.getTitle());
        holder.txtEAN.setLetterSpacing(31);
        holder.txtEAN.setText(prod.getEAN());
        holder.txtEAN.setTypeface(null, Typeface.BOLD);
        holder.lblSuppCat.setText("Catalog:");
        holder.txtSuppCat.setText(prod.getSupplierCat());
        holder.lblFormat.setText("Format:");
        holder.txtFormat.setText(prod.getFormat());
        holder.lblQtyInBin.setText("Qty In Bin:");
        holder.txtQtyInBin.setText(String.format("%s", prod.getQtyInBin()));
        holder.lblQtyScanned.setText("Qty Scanned:");
        holder.txtQtyInBin.setText(String.format("%s", prod.getQtyScanned()));
//        if (prod.getQtyInBin() == prod.getQtyScanned()) {
//            holder.lytMain.setBackgroundColor(Color.GREEN);
//        }
        if (prod.getQtyScanned() > 0) {
            if (prod.getQtyInBin() == prod.getQtyScanned() && prod.getQtyScanned() > 1) {
                //color green
                holder.lytMain.setBackgroundColor(Color.GREEN);
            }
            if (prod.getQtyScanned() < prod.getQtyInBin()) {
                //color yellow
                holder.lytMain.setBackgroundColor(Color.YELLOW);
            }
            if (prod.getQtyScanned() > prod.getQtyInBin()) {
                //color golden yellow
                holder.lytMain.setBackgroundColor(Color.parseColor("#957700"));
            }
        }
        holder.position = position;
        //barcodePic.setImageBitmap(generateBarCode(prod.getBarcode()));
        return view;
    }

    static  class ViewHolder {
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
