package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 06/11/2014.
 */
public class GoodsInImagePathOut implements Serializable {
    private static final long serialVersionUID = 1L;
    private String ImagePath;
    private String NewImagePath;
    private String Result;

    public GoodsInImagePathOut() {
    }

    public GoodsInImagePathOut(String imagePath, String newImagePath, String result) {
        ImagePath = imagePath;
        NewImagePath = newImagePath;
        Result = result;
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

    @JsonProperty("NewImagePath")
    public String getNewImagePath() {
        return NewImagePath;
    }

    public void setNewImagePath(String newImagePath) {
        NewImagePath = newImagePath;
    }

    @JsonProperty("Result")
    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }
}
