package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 15/10/2014.
 */
public class StockLineTransaction implements Serializable {
    private static final long serialVersionUID = 1L;
    private int StockLineTransactionId;
    private int ProductId;
    private String BinCode;
    private int QtyPlaced;
    private int Status;
    private String StatusName;

    public StockLineTransaction() {
    }

    public StockLineTransaction(int stockLineTransactionId, int productId, String binCode, int qtyPlaced, int status, String statusName) {
        StockLineTransactionId = stockLineTransactionId;
        ProductId = productId;
        BinCode = binCode;
        QtyPlaced = qtyPlaced;
        Status = status;
        StatusName = statusName;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("StockLineTransactionId")
    public int getStockLineTransactionId() {
        return StockLineTransactionId;
    }

    public void setStockLineTransactionId(int stockLineTransactionId) {
        StockLineTransactionId = stockLineTransactionId;
    }

    @JsonProperty("ProductId")
    public int getProductId() {
        return ProductId;
    }

    public void setProductId(int productId) {
        ProductId = productId;
    }

    @JsonProperty("BinCode")
    public String getBinCode() {
        return BinCode;
    }

    public void setBinCode(String binCode) {
        BinCode = binCode;
    }

    @JsonProperty("QtyPlaced")
    public int getQtyPlaced() {
        return QtyPlaced;
    }

    public void setQtyPlaced(int qtyPlaced) {
        QtyPlaced = qtyPlaced;
    }

    @JsonProperty("Status")
    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @JsonProperty("StatusName")
    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String statusName) {
        StatusName = statusName;
    }
}
