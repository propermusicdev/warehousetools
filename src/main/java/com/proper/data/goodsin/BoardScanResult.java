package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 14/10/2014.
 */
public class BoardScanResult implements Serializable {
    private static final long serialVersionUID = 1L;
    private int RequestedGoodsInBoardId;
    private GoodsInDelivery Delivery;
    private GoodsIn GoodsIn;

    public BoardScanResult() {
    }

    public BoardScanResult(int requestedGoodsInBoardId, GoodsInDelivery delivery, com.proper.data.goodsin.GoodsIn goodsIn) {
        RequestedGoodsInBoardId = requestedGoodsInBoardId;
        Delivery = delivery;
        GoodsIn = goodsIn;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("RequestedGoodsInBoardId")
    public int getRequestedGoodsInBoardId() {
        return RequestedGoodsInBoardId;
    }

    public void setRequestedGoodsInBoardId(int requestedGoodsInBoardId) {
        RequestedGoodsInBoardId = requestedGoodsInBoardId;
    }

    @JsonProperty("Delivery")
    public GoodsInDelivery getDelivery() {
        return Delivery;
    }

    public void setDelivery(GoodsInDelivery delivery) {
        Delivery = delivery;
    }

    @JsonProperty("GoodsIn")
    public GoodsIn getGoodsIn() {
        return GoodsIn;
    }

    public void setGoodsIn(GoodsIn goodsIn) {
        GoodsIn = goodsIn;
    }
}
