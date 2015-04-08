package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 03/11/2014.
 */
public class GoodsInImagesRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private int GoodsInId;
    private int UserId;
    private String UserCode;
    private List<GoodsInImagePathIn> Images;

    public GoodsInImagesRequest() {
    }

    public GoodsInImagesRequest(int goodsInId, int userId, String userCode, List<GoodsInImagePathIn> images) {
        GoodsInId = goodsInId;
        UserId = userId;
        UserCode = userCode;
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

    @JsonProperty("UserCode")
    public String getUserCode() {
        return UserCode;
    }

    public void setUserCode(String userCode) {
        UserCode = userCode;
    }

    @JsonProperty("Images")
    public List<GoodsInImagePathIn> getImages() {
        return Images;
    }

    public void setImages(List<GoodsInImagePathIn> images) {
        Images = images;
    }
}
