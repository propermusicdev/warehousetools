package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 23/06/2014.
 */
public class MoveRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private String UserCode;
    private String UserId;
    private String SrcBin;
    private String DstBin;
    private List<MoveRequestItem> Products;

    public MoveRequest() {
    }

    public MoveRequest(String userCode, String userId, String srcBin, String dstBin, List<MoveRequestItem> products) {
        UserCode = userCode;
        UserId = userId;
        SrcBin = srcBin;
        DstBin = dstBin;
        Products = products;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("UserCode")
    public String getUserCode() {
        return UserCode;
    }

    public void setUserCode(String userCode) {
        UserCode = userCode;
    }

    @JsonProperty("UserId")
    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    @JsonProperty("SrcBin")
    public String getSrcBin() {
        return SrcBin;
    }

    public void setSrcBin(String srcBin) {
        SrcBin = srcBin;
    }

    @JsonProperty("DstBin")
    public String getDstBin() {
        return DstBin;
    }

    public void setDstBin(String dstBin) {
        DstBin = dstBin;
    }

    @JsonProperty("Products")
    public List<MoveRequestItem> getProducts() {
        return Products;
    }

    public void setProducts(List<MoveRequestItem> products) {
        Products = products;
    }
}
