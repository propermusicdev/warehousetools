package com.proper.warehousetools.goodsin.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.proper.data.goodsin.GoodsInThumbnail;
import com.proper.data.goodsin.adapters.GalleryGridAdapter;
import com.proper.utils.PhotoHelper;
import com.proper.warehousetools.PlainActivity;
import com.proper.warehousetools.R;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lebel on 31/10/2014.
 */
public class ActGoodsInPhotoDisplay extends PlainActivity {
    private TextView txtHeader;
    private GridView gallery;
    private Button btnExit;
    private List<GoodsInThumbnail> thumbnails = null;
    private GoodsInThumbnail[] thumbnailz = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lyt_goodsin_photodisplay);
        txtHeader = (TextView) this.findViewById(R.id.txtvGoodsInPhotoDisplayHeader);
        gallery = (GridView) this.findViewById(R.id.dgvGoodsInPhotoDisplayGallery);
        btnExit = (Button) this.findViewById(R.id.bnExitActGoodsInPhotoDisplay);

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonClicked(v);
            }
        });

        //get photos in a given directory
        Bundle extras = getIntent().getExtras();
        String directory = "";
        if (extras == null) {
            throw new RuntimeException("ActGoodsInPhotoDisplay - Extras cannot be null");
        }else {
            directory = extras.getString("DIRECTORY_EXTRA");
        }
        if (directory.isEmpty()) {
            String mMsg = "There are no photo(s) to display \nPlease exit and take some photos";
            AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInPhotoDisplay.this);
            builder.setMessage(mMsg)
                    .setPositiveButton(R.string.but_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do nothing
                        }
                    });
            builder.show();
        } else {
            //String path = Environment.getExternalStorageDirectory().toString()+"/Pictures";
//            File dir = new File(directory);
//            File file[] = dir.listFiles();
//            if (file.length > 0) {
//                //do something profound here
//            }
            loadGalleryTask loadGalleryAsync = new loadGalleryTask();
            loadGalleryAsync.execute(directory);
        }
    }

    private void buttonClicked(View v) {
        if (v == btnExit) {
            Intent i = new Intent();
            if (thumbnails != null) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("THUMBNAILS_EXTRA", (Serializable) thumbnails);
                i.putExtras(bundle);
            }
            setResult(RESULT_OK, i);
            this.finish();
        }
    }

    private class loadGalleryTask extends AsyncTask<String, Void, List<GoodsInThumbnail>> {
        protected ProgressDialog xDialog;
        @Override
        protected void onPreExecute() {
            xDialog = new ProgressDialog(ActGoodsInPhotoDisplay.this);
            CharSequence message = "Loading Gallery...";
            CharSequence title = "Please Wait";
            xDialog.setCancelable(true);
            xDialog.setCanceledOnTouchOutside(false);
            xDialog.setMessage(message);
            xDialog.setTitle(title);
            xDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            xDialog.show();
        }

        @Override
        protected List<GoodsInThumbnail> doInBackground(String... input) {
            thumbnails = new ArrayList<GoodsInThumbnail>();
            PhotoHelper photoHelper = new PhotoHelper(appContext);
            thumbnails = photoHelper.retrieveThumbnailsFromMediaStore(input[0]);
//            for (int i = 0; i < thumbnails.size(); i++) {
//                thumbnailz.
//            }
            thumbnailz = thumbnails.toArray(new GoodsInThumbnail[thumbnails.size()]);
            return thumbnails;
        }

        @Override
        protected void onPostExecute(List<GoodsInThumbnail> result) {
            if (xDialog != null && xDialog.isShowing()) {
                xDialog.dismiss();
            }
            if (result != null) {
                GalleryGridAdapter adapter = new GalleryGridAdapter(ActGoodsInPhotoDisplay.this, result);
                gallery.setAdapter(adapter);
            } else {
                String mMsg = "There are no photo(s) to display \nPlease exit and take some photos";
                AlertDialog.Builder builder = new AlertDialog.Builder(ActGoodsInPhotoDisplay.this);
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