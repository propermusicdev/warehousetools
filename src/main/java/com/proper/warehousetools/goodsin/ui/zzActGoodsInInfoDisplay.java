package com.proper.warehousetools.goodsin.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.proper.data.goodsin.BoardScanResult;
import com.proper.data.goodsin.GoodsInImage;
import com.proper.utils.PhotoHelper;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

import java.util.AbstractMap;

/**
 * Created by Lebel on 06/10/2014.
 */
public class zzActGoodsInInfoDisplay extends PlainActivity {
    private static final int PROVIDE_INFO_REQUEST_CODE = 1000;
    private static final int TAKE_PICTURE_REQUEST_CODE = 1010;
    private Button btnProvideInfo;
    private Button btnTakePicture;
    private ImageView imgThumb;
    private TextView txtGoodsinID;
    private TextView txtProductID;
    private TextView txtSupplier;
    private TextView txtOrderNumber;
    private TextView txtQuantity;
    private TextView txtPerson;
    private Button btnSend;
    private Uri mPhotoPathUri = null;
    private GoodsInImage img = null;
    private UploadPhotoAsync photoTask;
    private BoardScanResult boardScanResult = new BoardScanResult();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsininfodisplay);
        Bundle extras = getIntent().getExtras();
        boardScanResult = (BoardScanResult) extras.getSerializable("BOARDSCAN_EXTRA");
        if (extras == null || boardScanResult == null) {
            throw new RuntimeException("boardScanResult Extras cannot be Empty");
        }
        btnProvideInfo = (Button) this.findViewById(R.id.bnGoodsinInfoDisplayProvideInfo);
        btnTakePicture = (Button) this.findViewById(R.id.bnGoodsinInfoDisplayTakePicture);
        imgThumb = (ImageView) this.findViewById(R.id.GoodsinInfoDisplayThumbnailImage);
        txtSupplier = (TextView) this.findViewById(R.id.txtGoodsinInfoDisplaySupplier);
        txtOrderNumber = (TextView) this.findViewById(R.id.txtGoodsinInfoDisplayOrderNumber);
        txtProductID = (TextView) this.findViewById(R.id.txtGoodsinInfoDisplayProductID);
        txtQuantity = (TextView) this.findViewById(R.id.txtGoodsinInfoDisplayQuantity);
        txtGoodsinID = (TextView) this.findViewById(R.id.txtGoodsinInfoDisplayProvideID);
        btnSend = (Button) this.findViewById(R.id.bnExitActGoodsinInfoDisplay);

        btnProvideInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonClicked(v);
            }
        });

        //Populate text
        txtSupplier.setText(boardScanResult.getDelivery().getSupplier());
        txtGoodsinID.setText(String.format("%s", boardScanResult.getDelivery().getGoodsInId()));

        txtOrderNumber.setText(boardScanResult.getGoodsIn().getOrderNumber());
        txtQuantity.setText(String.format("%s", boardScanResult.getGoodsIn().getUnitsOrdered()));
    }

    private void ButtonClicked(View v) {
        switch (v.getId()) {
            case R.id.bnExitActGoodsinInfoDisplay:
                if (img != null) {
                    if (!mPhotoPathUri.toString().isEmpty()) {
                        if (img != null) {
                            photoTask = new UploadPhotoAsync();
                            photoTask.execute(img);
                        }else {
                            String mMsg = "Unable to upload a Goods-In Image over the network.\nPlease contact IT if error continues";
                            AlertDialog.Builder builder = new AlertDialog.Builder(zzActGoodsInInfoDisplay.this);
                            builder.setMessage(mMsg)
                                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            //do nothing
                                        }
                                    });
                            builder.show();
                        }
                    }
                    else{
                        String mMsg = "Please take a picture as proof of this delivery";
                        AlertDialog.Builder builder = new AlertDialog.Builder(zzActGoodsInInfoDisplay.this);
                        builder.setMessage(mMsg)
                                .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //do nothing
                                    }
                                });
                        builder.show();
                    }
                } else {
                    String mMsg = "Please provide information about this Goods-in";
                    AlertDialog.Builder builder = new AlertDialog.Builder(zzActGoodsInInfoDisplay.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
//                Intent i = new Intent();
//                setResult(RESULT_OK, i);
//                this.finish();
                break;
            case R.id.bnGoodsinInfoDisplayProvideInfo:
                //do provide info
                Intent provideIntent = new Intent(this, zzActGoodsInProvideInfo.class);
                startActivityForResult(provideIntent, PROVIDE_INFO_REQUEST_CODE);
                break;
            case R.id.bnGoodsinInfoDisplayTakePicture:
                //do take picture
                mPhotoPathUri = PhotoHelper.generateTimeStampPhotoFileUri();

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoPathUri);
                startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
                break;
        }
    }

    private void handleTakePictureResult(int resultCode, Intent resultIntent) {
        if (resultCode == RESULT_OK) {
            String photoPathName = mPhotoPathUri.getPath();
            PhotoHelper.addPhotoToMediaStoreAndDisplayThumbnail(
                    photoPathName, this, imgThumb);
            //Build Good
//            if (mPhotoPathUri != null && !mPhotoPathUri.toString().isEmpty()) {
//                if (img != null) {
//                    //Already got Info
//                    File file = new File(mPhotoPathUri.getPath());
//                    try {
//                        img.setName(file.getName());    img.setDataStream(new FileInputStream(file));
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                        String iMsg = "Unable to create a GoodsInImage POJO object.";
//                        today = new java.sql.Timestamp(utilDate.getTime());
//                        LogEntry log = new LogEntry(1L, ApplicationID, "zzActGoodsInInfoDisplay - ButtonClicked", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                        logger.log(log);
//                        img = null;
//                    }
//                } else {
//                    //Not Yet Provided Info
//                    File file = new File(mPhotoPathUri.getPath());
//                    try {
//                        String userDetail = currentUser.getUserFirstName() + currentUser.getUserLastName().substring(0, 1).toUpperCase(Locale.getDefault()) + ".";
//                        img = new GoodsInImage(file.getName(), "", 0, 0, "", 0, userDetail, new FileInputStream(file));
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                        String iMsg = "Unable to create a GoodsInImage POJO object.";
//                        today = new java.sql.Timestamp(utilDate.getTime());
//                        LogEntry log = new LogEntry(1L, ApplicationID, "zzActGoodsInInfoDisplay - ButtonClicked", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                        logger.log(log);
//                        img = null;
//                    }
//                }
//            }

        }
        else {
            mPhotoPathUri = null;
            Toast.makeText(this, "User Canceled", Toast.LENGTH_LONG).show();
        }
    }

    private void handleProvideInfoResult(int resultCode, Intent resultIntent) {
        if (resultCode == RESULT_OK) {

            int goodsinID = resultIntent.getIntExtra("ID_EXTRA", 0);
            int productID = resultIntent.getIntExtra("PRODUCTID_EXTRA", 0);
            String supplier = resultIntent.getStringExtra("SUPPLIER_EXTRA");
            String orderNumba = String.format("%s", resultIntent.getIntExtra("ORDERNUMBER_EXTRA", 0));
            int qty = resultIntent.getIntExtra("QUANTITY_EXTRA", 0);

            txtGoodsinID.setText(String.format("%s", goodsinID));
            txtProductID.setText(String.format("%s", productID));
            txtOrderNumber.setText(String.format("%s", orderNumba));
            txtSupplier.setText(supplier);
            txtQuantity.setText(String.format("%s", qty));

            //Provide info
//            if (mPhotoPathUri != null && !mPhotoPathUri.toString().isEmpty()) {
//                //Already taken Photo
//                File file = new File(mPhotoPathUri.getPath());
//                try {
//                    String userDetail = currentUser.getUserFirstName() + currentUser.getUserLastName().substring(0, 1).toUpperCase(Locale.getDefault()) + ".";
//                    img = new GoodsInImage(file.getName(), supplier, goodsinID, productID, orderNumba, qty, userDetail, new FileInputStream(file));
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                    String iMsg = "Unable to create a GoodsInImage POJO object.";
//                    today = new java.sql.Timestamp(utilDate.getTime());
//                    LogEntry log = new LogEntry(1L, ApplicationID, "zzActGoodsInInfoDisplay - ButtonClicked", deviceIMEI, RuntimeException.class.getSimpleName(), iMsg, today);
//                    logger.log(log);
//                    img = null;
//                }
//            } else {
//                // Photo not yet taken !
//                String userDetail = currentUser.getUserFirstName() + currentUser.getUserLastName().substring(0, 1).toUpperCase(Locale.getDefault()) + ".";
//                img = new GoodsInImage("", supplier, goodsinID, productID, orderNumba, qty, userDetail, null);
//            }
        }
        else {
            Toast.makeText(this, "User Canceled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        switch (requestCode) {
            case PROVIDE_INFO_REQUEST_CODE:
                handleProvideInfoResult(resultCode, resultIntent);
                break;
            case TAKE_PICTURE_REQUEST_CODE:
                handleTakePictureResult(resultCode, resultIntent);
                break;
        }
    }

    private class UploadPhotoAsync extends AsyncTask<GoodsInImage, Void, AbstractMap.SimpleEntry<Boolean, String>> {
        protected ProgressDialog xDialog;

        @Override
        protected void onPreExecute() {
            //startTime = new Date().getTime(); //get start time
            xDialog = new ProgressDialog(zzActGoodsInInfoDisplay.this);
            CharSequence message = "Working hard...contacting webservice...";

            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected AbstractMap.SimpleEntry<Boolean, String> doInBackground(GoodsInImage... images) {
            //Boolean response = resolver.uploadImage(images[0]);       //NOT THROUGH WEBSERVICE
            //AbstractMap.SimpleEntry<Boolean, String> response = resolver.uploadImageFTP(images[0], mPhotoPathUri);
            AbstractMap.SimpleEntry<Boolean, String> response = resolver.uploadImagesFTP(images[0]);
            return response;
        }

        @Override
        protected void onPostExecute(AbstractMap.SimpleEntry<Boolean, String> response) {
            if (xDialog != null && xDialog.isShowing()) {
                xDialog.dismiss();
            }
            if (response != null) {
                if (response.getKey() == true) {
                    Intent i = new Intent();
                    setResult(Activity.RESULT_OK, i);
                    zzActGoodsInInfoDisplay.this.finish();
                } else {
                    String mMsg = "Unable to upload a Goods-In Image over the network.\nPlease contact IT if error continues";
                    AlertDialog.Builder builder = new AlertDialog.Builder(zzActGoodsInInfoDisplay.this);
                    builder.setMessage(mMsg)
                            .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //do nothing
                                }
                            });
                    builder.show();
                }
            }
        }
    }
}