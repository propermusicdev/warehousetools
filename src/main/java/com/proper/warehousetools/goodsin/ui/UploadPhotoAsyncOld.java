package com.proper.warehousetools.goodsin.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import com.proper.data.goodsin.GoodsInImage;
import com.proper.messagequeue.HttpMessageResolver;
import com.proper.warehousetools.AppContext;
import com.proper.warehousetools.R;

import java.util.AbstractMap;

/**
 * Created by Lebel on 17/10/2014.
 */
public class UploadPhotoAsyncOld extends AsyncTask<GoodsInImage, Void, AbstractMap.SimpleEntry<Boolean, String>> {
    protected ProgressDialog xDialog;
    protected Context context;
    protected HttpMessageResolver resolver;
    private String mPhotoPathUri;

    public UploadPhotoAsyncOld(AppContext context, String mPhotoPathUri) {
        this.context = context;
        this.mPhotoPathUri = mPhotoPathUri;
        this.resolver = new HttpMessageResolver(context);
    }

    @Override
    protected void onPreExecute() {
        //startTime = new Date().getTime(); //get start time
        xDialog = new ProgressDialog(this.context);
        CharSequence message = "Working hard...contacting webservice...";
//            if (instruction == R.integer.TURN_DESTINATION) {
//                message = "Working hard...Moving Product...";
//            }
//            if (instruction == R.integer.TURN_SOURCE) {
//                message = "Working hard...Searching Bin...";
//            }

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
        return null;
    }

    @Override
    protected void onPostExecute(AbstractMap.SimpleEntry<Boolean, String> response) {
        if (xDialog != null && xDialog.isShowing()) {
            xDialog.dismiss();
        }
        if (response != null) {
            if (response.getKey() == true) {
                Intent i = new Intent();
                //setResult(Activity.RESULT_OK, i);
               // zzActGoodsInInfoDisplay.this.finish();
            } else {
                String mMsg = "Unable to upload a Goods-In Image over the network.\nPlease contact IT if error continues";
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
