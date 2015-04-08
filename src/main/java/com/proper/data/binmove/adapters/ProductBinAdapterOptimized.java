package com.proper.data.binmove.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.proper.data.binmove.ProductBinResponse;
import com.proper.data.customcontrols.LetterSpacingTextView;
import com.proper.warehousetools.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Lebel on 25/07/2014.
 */
public class ProductBinAdapterOptimized extends BaseAdapter {
    private static final String ApplicationID = "BinMove";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private String deviceIMEI;
    private Context kontext;
    //protected LayoutInflater inflater;
    protected List<ProductBinResponse> products;
    private static final int MSG_BCODE_STARTING = 22;
    private static final int MSG_DONE = 11;
    //private Handler codeImageHandler = null;
    protected ProgressDialog bcDialog;
    private boolean hasBcRan = false;
    private int bcRunCount = 0;
    private ImageView barcodePic;
    private ImageView albumPic;
    private Bitmap mBitmap = null;
    private LetterSpacingTextView txtBarcode;

    public ProductBinAdapterOptimized(Context context, List<ProductBinResponse> products, String deviceIMEI) {
        this.kontext = context;
        this.products = products;
        this.deviceIMEI = deviceIMEI;
    }

    @Override
    public int getCount() {
        return products.size();
    }

    @Override
    public ProductBinResponse getItem(int i) {
        return products.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        View myView = view;
        ViewHolder holder = new ViewHolder();
        if (myView == null) {
            LayoutInflater inflater = (LayoutInflater) kontext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myView = inflater.inflate(R.layout.list_binmove_qryview_item1, viewGroup, false);
            holder.albumPic = (ImageView) myView.findViewById(R.id.imgAlbum);
            holder.txtArtist = (TextView) myView.findViewById(R.id.txtv_Artist);
            holder.txtTitle = (TextView) myView.findViewById(R.id.txtv_Title);
            //barcodePic = (ImageView) myView.findViewById(R.id.imgBarcode);
            holder.txtBarcode = (LetterSpacingTextView) myView.findViewById(R.id.lblBarcode);
            holder.lblFormat = (TextView) myView.findViewById(R.id.lblFormat);
            holder.txtFormat = (TextView) myView.findViewById(R.id.txtvFormat);
            holder.lblSupplierCat = (TextView) myView.findViewById(R.id.lblSupplierCat);
            holder.txtSupplierCat = (TextView) myView.findViewById(R.id.txtvSupplierCat);
            holder.lblQuantity = (TextView) myView.findViewById(R.id.lblQtyInBin);
            holder.txtQuantity = (TextView) myView.findViewById(R.id.txtvQtyInBin);
            myView.setTag(holder);
        }else{
            holder = (ViewHolder) myView.getTag();
        }

        ProductBinResponse prod = products.get(pos);

        holder.txtArtist.setText(prod.getArtist());
        holder.txtTitle.setText(prod.getTitle());
        holder.txtBarcode.setText(prod.getBarcode());
        holder.txtBarcode.setLetterSpacing(21);
        holder.lblFormat.setText("Format:  ");
        holder.txtFormat.setText(prod.getFormat());
        holder.lblSupplierCat.setText("Supplier Catalog:");
        holder.txtSupplierCat.setText(prod.getSupplierCat());
        holder.lblQuantity.setText("Qty In Bin");
        holder.txtQuantity.setText(String.format("%s", prod.getQtyInBin()));
        holder.position = pos;

        // retrieve album pictures here, if url is present
        //barcodePic.setImageBitmap(generateBarCode(prod.getBarcode()));
        //barcodePic.setScaleType(ImageView.ScaleType.FIT_XY);
        return myView;
    }

//    public Bitmap generateBarCode(String data) {
//
//        switch (data.length()) {
//            case 12:    //UPC-A
//                com.google.zxing.oned.UPCAWriter upc = new com.google.zxing.oned.UPCAWriter();
//                try {
//                    BitMatrix bm = upc.encode(data, BarcodeFormat.UPC_A, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 8:     //EAN-8
//                com.google.zxing.oned.EAN8Writer ean8 = new com.google.zxing.oned.EAN8Writer();
//                try {
//                    BitMatrix bm = ean8.encode(data, BarcodeFormat.EAN_8, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 14:    //UPC-14
//                //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
//                com.google.zxing.oned.ITFWriter itf = new com.google.zxing.oned.ITFWriter();
//                try {
//                    //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
//                    BitMatrix bm = itf.encode(data, BarcodeFormat.ITF, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            case 13:    //EAN-13
//                com.google.zxing.oned.EAN13Writer ean13 = new com.google.zxing.oned.EAN13Writer();
//                try {
//                    BitMatrix bm = ean13.encode(data, BarcodeFormat.EAN_13, 360, 108);
//                    mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
//                    for (int i = 0; i < 360; i++) {
//                        for (int j = 0; j < 108; j++) {
//
//                            mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
//                        }
//                    }
//                } catch (WriterException e) {
//                    e.printStackTrace();
//                }
//                break;
//            default:    //Error - throw dead kittens
//                LogHelper logger = new LogHelper();
//                String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
//                today = new java.sql.Timestamp(utilDate.getTime());
//                LogEntry log = new LogEntry(1L, ApplicationID, "QueryView - generateBarcode - Line:306", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                logger.log(log);
//                WriterException ex = new WriterException(iMsg);
//                ex.printStackTrace();
//                throw new RuntimeException(ex.getMessage());
//        }
//        return mBitmap;
//    }

//    public Drawable getAlbumPicture(String url) {
//        try {
//            InputStream is = (InputStream) new URL(url).getContent();
//            Drawable d = Drawable.createFromStream(is, "src name");
//            return d;
//        } catch (Exception e) {
//            return null;
//        }
//    }
    static class ViewHolder {
        ImageView albumPic;
        TextView txtArtist;
        TextView txtTitle;
        //barcodePic = (ImageView) myView.findViewById(R.id.imgBarcode);
        LetterSpacingTextView txtBarcode;
        TextView lblFormat;
        TextView txtFormat;
        TextView lblSupplierCat;
        TextView txtSupplierCat;
        TextView lblQuantity;
        TextView txtQuantity;
        int position;
    }
}
