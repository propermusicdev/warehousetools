package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 03/11/2014.
 */
public class GoodsInImagePath implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ImagePath;

    public GoodsInImagePath() {
    }

    public GoodsInImagePath(String imagePath) {
        ImagePath = imagePath;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("ImagePath")
    public String getImagePath() {
        return ImagePath;
    }

    public void setImagePath(String imagePath) {
        ImagePath = imagePath;
    }
}
