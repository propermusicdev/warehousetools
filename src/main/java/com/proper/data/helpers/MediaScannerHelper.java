package com.proper.data.helpers;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

/**
 * Created by Lebel on 05/02/2015.
 */
public class MediaScannerHelper implements MediaScannerConnection.MediaScannerConnectionClient {
    private MediaScannerConnection conn;
    private String path;

    public MediaScannerHelper(Context context, String path) {
        this.path = path;
        conn = new MediaScannerConnection(context, this);
        conn.connect();
    }

    @Override
    public void onMediaScannerConnected() {
        conn.scanFile(path, null);
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        conn.disconnect();
    }
}
