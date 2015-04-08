package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 07/11/2014.
 */
public class GoodsInImagesResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int GoodsInId;
    private int UserId;
    private String Result;
    private List<GoodsInImagePathOut> Images;

    public GoodsInImagesResponse() {
    }

    public GoodsInImagesResponse(int goodsInId, int userId, String result, List<GoodsInImagePathOut> images) {
        GoodsInId = goodsInId;
        UserId = userId;
        Result = result;
        Images = images;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("GoodsInId")
    public int getGoodsInId() {
        return GoodsInId;
    }

    public void setGoodsInId(int goodsInId) {
        GoodsInId = goodsInId;
    }

    @JsonProperty("UserId")
    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    @JsonProperty("Result")
    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }

    @JsonProperty("Images")
    public List<GoodsInImagePathOut> getImages() {
        return Images;
    }

    public void setImages(List<GoodsInImagePathOut> images) {
        Images = images;
    }
}
