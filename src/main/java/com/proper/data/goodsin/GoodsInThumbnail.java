package com.proper.data.goodsin;

import android.graphics.Bitmap;
import android.net.Uri;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 31/10/2014.
 */
public class GoodsInThumbnail implements Serializable {
    private static final long serialVersionUID = 1L;
    private String URL;
    private Bitmap ThumbNail;
    private String fileName;

    public GoodsInThumbnail() {
    }

//    public GoodsInThumbnail(Uri URL, Bitmap thumbNail, String fileName) {
//        this.URL = URL;
//        ThumbNail = thumbNail;
//        this.fileName = fileName;
//    }

    public GoodsInThumbnail(String URL, String fileName) {
        this.URL = URL;
        this.fileName = fileName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("URL")
    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    @JsonProperty("ThumbNail")
    public Bitmap getThumbNail() {
        return ThumbNail;
    }

    public void setThumbNail(Bitmap thumbNail) {
        ThumbNail = thumbNail;
    }

    @JsonProperty("FileName")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
