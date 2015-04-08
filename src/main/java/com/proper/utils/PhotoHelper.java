package com.proper.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import com.proper.data.goodsin.GoodsInThumbnail;
import com.proper.warehousetools.AppContext;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lebel on 06/10/2014.
 */
public class PhotoHelper {
    private static final String LOG_TAG = "PHOTO HELPER";
    private AppContext appContext;

    public PhotoHelper(AppContext appContext) {
        this.appContext = appContext;
    }

    /**
     * Create a pathname composed of the current time appropriate for storing
     * a photo. The location of the file is a subfolder named "Pluralsight"
     * within the appropriate folder for storing pictures for the current user
     * @return The full path to the file
     */
    public static Uri generateTimeStampPhotoFileUri() {
        Uri photoFileUri = null;

        // Request the path to the Pluralsight folder in the photo folder
        File outputDir = getPhotoDirectory();

        if (outputDir != null) {
            // Create a file name for the photo based on current date/time
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String photoFileName = "IMG_" + timeStamp + ".jpg";

            // Create File instance representing the photo file within th
            //  the Plurasight subfolder of the photos folder
            File photoFile = new File(outputDir, photoFileName);
            // Convert the File path to a URI
            photoFileUri = Uri.fromFile(photoFile);
        }
        return photoFileUri;
    }

    /**
     * The path to the "Pluralsight" subfolder within the appropriate folder
     * for storing photos for the current user. Assures that the external media
     * is mounted and creates the "Pluralsight" subfolder if it doesn't already
     * exist.
     * @return Path to the "Pluralsight" subfolder within the appropriate folder
     * for storing photos for this user
     */
    public static File getPhotoDirectory() {
        File outputDir = null;

        // Confirm that External Storage (SD Card) is mounted
        String externalStorageState = Environment.getExternalStorageState();
        if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) {

            // Request the Folder where photos are supposed to be stored
            //File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);


            // Create a subfolder named Pluralsight
            outputDir = new File(dcimDir, "Camera");
            if (!outputDir.exists()) {
                if (!outputDir.mkdirs()) {
                    Log.e(LOG_TAG, "Failed to create directory: " + outputDir.getAbsolutePath());
                    outputDir = null;
                }
            }
        }
//        else {
//            //Then there is no SD card present store in Camera temporarily then delete after the image copied to network
//            File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera");
//            File thumbnails = new File(dcim, "/.thumbnails");
//            File[] listOfImg = dcim.listFiles();
//            if (dcim.isDirectory()){
//                //for each child in DCIM directory
//                for (int i = 0; i < listOfImg.length; ++i){
//                    //no thumbnails
//                    if( !listOfImg[i].getAbsolutePath().equals(thumbnails.getAbsolutePath()) ){
//                        //only get the directory (100MEDIA, Camera, 100ANDRO, and others)
//                        if(listOfImg[i].isDirectory()) {
//                            //is a parent directory, get children
//                            File[] temp = listOfImg[i].listFiles();
//                            for(int j = 0; j < temp.length; ++j) {
//                                //f.add(temp[j].getAbsolutePath());
//                            }
//                        }else if(listOfImg[i].isFile()){
//                            //is not a parent directory, get files
//                            //f.add(listOfImg[i].getAbsolutePath());
//                        }
//                    }
//                }
//            }
//        }

        return outputDir;
    }

    /**
     * Adds the photo file to the MediaStore and displays it's thummbnail within the
     * passed ImageView
     * @param pathName Path to the photo file
     * @param activity Activity that contains the ImageView
     * @param imageView ImageView in which to display the thumbnail
     */
    public static void addPhotoToMediaStoreAndDisplayThumbnail(String pathName, Activity activity, ImageView imageView) {
        // Create final references so they can be passed in a cloujure to the callbacks
        final ImageView thumbnailImageView = imageView;
        final Activity thumbnailActivity = activity;

        String[] filesToScan = {pathName};

        // Request Media Scanner to add photo into the Media system
        MediaScannerConnection.scanFile(thumbnailActivity, filesToScan, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String filePath, Uri uri) {
                        // The MediaStore calls back into this method to let us know
                        //  the scan is complete - this call occurs on a non-UI thread
                        long id = ContentUris.parseId(uri);
                        ContentResolver contentResolver = thumbnailActivity.getContentResolver();

                        // Request a thumbnail of the picture from the MediaStore
                        final Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                                contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);

                        // Load the thumbnail into the imageview
                        // This call must occur on the UI thread
                        thumbnailActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                thumbnailImageView.setImageBitmap(thumbnail);
                            }
                        });
                    }
                });
    }

    public List<GoodsInThumbnail> retrieveThumbnailsFromMediaStore(String pathName) {
        List<GoodsInThumbnail> thumbs = new ArrayList<GoodsInThumbnail>();
        ContentResolver cr = this.appContext.getContentResolver();
        Bitmap thumbnail = null;
        //Bitmap thumbnail2 = null;

        File files[] = new File(pathName).getParentFile().listFiles();
        for (int i = 0; i < files.length; i++) {
            GoodsInThumbnail thumb = new GoodsInThumbnail();
            Uri thisURI = Uri.fromFile(files[i]);
            long id = ContentUris.parseId(thisURI);
            // Request a thumbnail of the picture from the MediaStore
            //thumbnail = MediaStore.Images.Thumbnails.getThumbnail(
                    //contentResolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
            String path = thisURI.getPath();
            try {
                //thumbnail = MediaStore.Images.Media.getBitmap(cr, thisURI); //***original line ***
                //thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id, thisURI, null);
                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
//                thumbnail = MediaStore.Images.Thumbnails.getThumbnail(cr, thisURI.getPathSegments().get(1),
//                        MediaStore.Images.Thumbnails.MINI_KIND, (BitmapFactory.Options) null);
                //thumbnail2 = getThumbnail(cr, pathName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //thumb.setURL(thisURI);
            //thumb.setURL(FilenameUtils.concat(files[i].getAbsolutePath(), files[i].getName()));
            thumb.setURL(files[i].getAbsolutePath());
            thumb.setThumbNail(thumbnail);
            thumb.setFileName(files[i].getName());
            thumbs.add(thumb);
        }
        return thumbs;
    }
    public List<Bitmap> getThumbnailsFromMediaStore(String path) {
        List<Bitmap> images = new ArrayList<Bitmap>();
        ContentResolver cr = this.appContext.getContentResolver();
        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        //if (ca != null && ca.moveToFirst()) {
//        if (ca != null) {
//            for (int i = 0; i < cr; i++) {
//
//            }
//            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
//            ca.close();
//            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
//        }

        ca.close();
        return images;
    }

    public static Bitmap getThumbnail(ContentResolver cr, String path) throws Exception {

        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MICRO_KIND, null );
        }

        ca.close();
        return null;

    }
}
