package com.proper.warehousetools.binmove.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.proper.data.binmove.*;
import com.proper.data.binmove.adapters.BarcodeResponseAdapter;
import com.proper.data.binmove.adapters.ProductBinAdapterOptimized;
import com.proper.data.diagnostics.LogEntry;
import com.proper.data.customcontrols.LetterSpacingTextView;
import com.proper.logger.LogHelper;
import com.proper.warehousetools.R;

import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 27/08/2014.
 */
public class QueryView extends Activity {
    private static int NAV_INSTRUCTION = 0;
    private String deviceIMEI = "";
    private static final String ApplicationID = "BinMove";
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private java.util.Date utilDate = java.util.Calendar.getInstance().getTime();
    private java.sql.Timestamp today = null;
    private LinearLayout lytHeader;
    private ListView lvQuery;
    private ExpandableListView lvxQuery;
    private ViewFlipper flipper;
    private Button btnExit;
    private List<ProductResponse> productsList = new ArrayList<ProductResponse>();
    private List<Bin> binList = new ArrayList<Bin>();
    private ProductResponse currentProduct = new ProductResponse();
    private Bin currentBin = new Bin();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_binmove_qryview);
        TelephonyManager mngr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        deviceIMEI = mngr.getDeviceId();
        //Define controls
        flipper = (ViewFlipper) this.findViewById(R.id.loginFlipper);
        lytHeader = (LinearLayout) this.findViewById(R.id.lytInnerTop);
        //ImageView imgDetails = (ImageView) this.findViewById(R.id.imgDetailsTop);
        TextView txtHeader = (TextView) this.findViewById(R.id.txtHeader_qryview);
        lvQuery = (ListView) this.findViewById(R.id.qryListView);
        lvxQuery = (ExpandableListView) this.findViewById(R.id.qryXpListView);
        btnExit = (Button) this.findViewById(R.id.bnExitQueryView);
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OnButtonClicked(view);
            }
        });

        LayoutInflater inflater = (LayoutInflater) this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Bundle extras = getIntent().getExtras();
        NAV_INSTRUCTION = extras.getInt("INSTRUCTION_EXTRA");

        switch (NAV_INSTRUCTION) {
            case R.integer.ACTION_BINQUERY:
                //  load QueryView with BinQuery properties
                BinResponse binResp = (BinResponse) extras.getSerializable("BINRESPONSE_EXTRA");

                if (binResp != null && !binResp.getProducts().isEmpty()) {
                    //ProductBinAdapter1 adapter = new ProductBinAdapter1(QueryView.this, binResp.getProducts());
                    ProductBinAdapterOptimized adapter = new ProductBinAdapterOptimized(QueryView.this, binResp.getProducts(), deviceIMEI);
                    if (lytHeader.getVisibility() != View.GONE) lytHeader.setVisibility(View.GONE);
                    if (lvxQuery.getVisibility() != View.GONE) lvxQuery.setVisibility(View.GONE);
                    if (lvQuery.getVisibility() != View.VISIBLE) lvQuery.setVisibility(View.VISIBLE);
                    if (binResp.getProducts().size() > 1) {
                        this.setTitle(binResp.getProducts().get(0).getTitle().isEmpty() ?
                                binResp.getProducts().get(0).getTitle() : binResp.getProducts().get(1).getTitle());
                    } else {
                        this.setTitle(binResp.getProducts().get(0).getTitle());
                    }
                    txtHeader.setText(String.format("Artist: %s", binResp.getProducts().get(0).getArtist() != null ?
                            binResp.getProducts().get(0).getArtist() : binResp.getProducts().get(1).getArtist()));
                    txtHeader.setTypeface(null, Typeface.BOLD);
                    lvQuery.setAdapter(adapter);
                    flipper.setDisplayedChild(1);
                }
                break;
            case R.integer.ACTION_BARCODEQUERY:
                //  load QueryView with BarcodeQuery properties
                final BarcodeResponse bcResp = (BarcodeResponse) extras.getSerializable("BARCODERESPONSE_EXTRA");

                if (bcResp != null && !bcResp.getProducts().isEmpty()) {
                    BarcodeResponseAdapter iAdapter = new BarcodeResponseAdapter(this, bcResp);
                    txtHeader.setText(String.format("Artist: %s", bcResp.getProducts().get(0).getArtist() != null ?
                            bcResp.getProducts().get(0).getArtist() : bcResp.getProducts().get(1).getArtist()));
                    txtHeader.setTypeface(null, Typeface.BOLD);
                    if (bcResp.getProducts().size() > 1) {
                        this.setTitle(!bcResp.getProducts().get(0).getTitle().isEmpty() ?
                                bcResp.getProducts().get(0).getTitle() : bcResp.getProducts().get(1).getTitle());
                    } else {
                        this.setTitle(bcResp.getProducts().get(0).getTitle());
                    }
                    if (lvQuery.getVisibility() != View.GONE) lvQuery.setVisibility(View.GONE);
                    if (lvxQuery.getVisibility() != View.VISIBLE) lvxQuery.setVisibility(View.VISIBLE);
                    lvxQuery.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
                    lvxQuery.setAdapter(iAdapter);
                    lvxQuery.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                        @Override
                        public boolean onGroupClick(ExpandableListView expandableListView, View view, int pos, long id) {
                            expandableListView.setItemChecked(pos, true);
                            productsList = bcResp.getProducts();
                            currentProduct = productsList.get(pos);
                            binList = bcResp.getProducts().get(pos).getBins();
                            return false;
                        }
                    });
                    lvxQuery.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                        @Override
                        public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i2, long id) {
                            view.setSelected(true);
                            currentBin = bcResp.getProducts().get(i).getBins().get(i2);
                            return false;
                        }
                    });
                    flipper.setDisplayedChild(1);
                    lvxQuery.expandGroup(0);    // Expand the first item
                }
                break;
            case R.integer.ACTION_BARCODE_BINQUERY:
                //  load QueryView with BarcodeBinQuery properties
                BarcodeBinResponse response = (BarcodeBinResponse) extras.getSerializable("PRODUCTBINRESPONSE_EXTRA");

                if (response != null && !response.getProducts().isEmpty()) {
                    ProductBinAdapterOptimized thisAdapter = new ProductBinAdapterOptimized(this, response.getProducts(), deviceIMEI);
                    if (lytHeader.getVisibility() != View.GONE) lytHeader.setVisibility(View.GONE);
                    if (lvxQuery.getVisibility() != View.GONE) lvxQuery.setVisibility(View.GONE);
                    if (lvQuery.getVisibility() != View.VISIBLE) lvQuery.setVisibility(View.VISIBLE);
                    if (response.getProducts().size() > 1) {
                        this.setTitle(response.getProducts().get(0).getTitle().isEmpty() ?
                                response.getProducts().get(0).getTitle() : response.getProducts().get(1).getTitle());
                    } else {
                        this.setTitle(response.getProducts().get(0).getTitle());
                    }
                    txtHeader.setText(String.format("Artist: %s", response.getProducts().get(0).getArtist() != null ?
                            response.getProducts().get(0).getArtist() : response.getProducts().get(1).getArtist()));
                    txtHeader.setTypeface(null, Typeface.BOLD);
                    lvQuery.setAdapter(thisAdapter);
                    flipper.setDisplayedChild(1);
                }
                break;
        }
    }

    private void OnButtonClicked(View view) {
        switch (view.getId()) {
            case R.id.bnExitQueryView:
                Intent i = new Intent();
                this.setResult(RESULT_OK, i);
                this.finish();
                break;
        }
    }

    public class ProductBinAdapter1 extends BaseAdapter {
        private Context kontext;
        protected List<ProductBinResponse> products;
        private static final int MSG_BCODE_STARTING = 22;
        private static final int MSG_DONE = 11;
        protected ProgressDialog bcDialog;
        private boolean hasBcRan = false;
        private int bcRunCount = 0;
        private ImageView barcodePic;
        private ImageView albumPic;
        private Bitmap mBitmap = null;
        private LetterSpacingTextView txtBarcode;

        public ProductBinAdapter1(Context context, List<ProductBinResponse> products) {
            this.kontext = context;
            this.products = products;
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
            if (myView == null) {
                LayoutInflater inflater = (LayoutInflater) kontext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                myView = inflater.inflate(R.layout.list_binmove_qryview_item1, viewGroup, false);
            }

            ProductBinResponse prod = products.get(pos);
            albumPic = (ImageView) myView.findViewById(R.id.imgAlbum);
            TextView txtArtist = (TextView) myView.findViewById(R.id.txtv_Artist);
            TextView txtTitle = (TextView) myView.findViewById(R.id.txtv_Title);
            barcodePic = (ImageView) myView.findViewById(R.id.imgBarcode);
            txtBarcode = (LetterSpacingTextView) myView.findViewById(R.id.lblBarcode);
            TextView lblFormat = (TextView) myView.findViewById(R.id.lblFormat);
            TextView txtFormat = (TextView) myView.findViewById(R.id.txtvFormat);
            TextView lblSupplierCat = (TextView) myView.findViewById(R.id.lblSupplierCat);
            TextView txtSupplierCat = (TextView) myView.findViewById(R.id.txtvSupplierCat);
            TextView lblQuantity = (TextView) myView.findViewById(R.id.lblQtyInBin);
            TextView txtQuantity = (TextView) myView.findViewById(R.id.txtvQtyInBin);

            txtArtist.setText(prod.getArtist());
            txtTitle.setText(prod.getTitle());
            txtBarcode.setText(prod.getBarcode());
            txtBarcode.setLetterSpacing(21);
            lblFormat.setText("Format:  ");
            txtFormat.setText(prod.getFormat());
            lblSupplierCat.setText("Supplier Catalog:");
            txtSupplierCat.setText(prod.getSupplierCat());
            lblQuantity.setText("Qty In Bin");
            txtQuantity.setText(String.format("%s", prod.getQtyInBin()));

            // retrieve album pictures here, if url is present
            barcodePic.setImageBitmap(generateBarCode(prod.getBarcode()));
            barcodePic.setScaleType(ImageView.ScaleType.FIT_XY);
            return myView;
        }

        public Bitmap generateBarCode(String data) {

            switch (data.length()) {
                case 12:    //UPC-A
                    com.google.zxing.oned.UPCAWriter upc = new com.google.zxing.oned.UPCAWriter();
                    try {
                        BitMatrix bm = upc.encode(data, BarcodeFormat.UPC_A, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                case 8:     //EAN-8
                    com.google.zxing.oned.EAN8Writer ean8 = new com.google.zxing.oned.EAN8Writer();
                    try {
                        BitMatrix bm = ean8.encode(data, BarcodeFormat.EAN_8, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                case 14:    //UPC-14
                    //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
                    com.google.zxing.oned.ITFWriter itf = new com.google.zxing.oned.ITFWriter();
                    try {
                        //BitMatrix bm = c9.encode(data,BarcodeFormat.CODE_128,380, 168);
                        BitMatrix bm = itf.encode(data, BarcodeFormat.ITF, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                case 13:    //EAN-13
                    com.google.zxing.oned.EAN13Writer ean13 = new com.google.zxing.oned.EAN13Writer();
                    try {
                        BitMatrix bm = ean13.encode(data, BarcodeFormat.EAN_13, 360, 108);
                        mBitmap = Bitmap.createBitmap(360, 108, Bitmap.Config.ARGB_8888);
                        for (int i = 0; i < 360; i++) {
                            for (int j = 0; j < 108; j++) {

                                mBitmap.setPixel(i, j, bm.get(i, j) ? Color.BLACK : Color.WHITE);
                            }
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                    break;
                default:    //Error - throw dead kittens
                    LogHelper logger = new LogHelper();
                    String iMsg = "The Response object return null due to msg queue not recognising your improper request.";
                    today = new java.sql.Timestamp(utilDate.getTime());
                    LogEntry log = new LogEntry(1L, ApplicationID, "QueryView - generateBarcode - Line:306", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
                    logger.log(log);
                    WriterException ex = new WriterException(iMsg);
                    ex.printStackTrace();
                    throw new RuntimeException(ex.getMessage());
            }
            return mBitmap;
        }

        public Drawable getAlbumPicture(String url) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                Drawable d = Drawable.createFromStream(is, "src name");
                return d;
            } catch (Exception e) {
                return null;
            }
        }
    }
}