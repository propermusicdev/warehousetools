package com.proper.data.stocktake;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 27/01/2015.
 */
public class StockTakeUpdateLine implements Serializable {
    private static final long serialVersionUID = 1L;
    private int StockTakeLineId;
    private int ProductId;
    private int OriginalQty;
    private int CheckedQty;
    private String SupplierCat;
    private int LineAdded;

    public StockTakeUpdateLine() {
    }

    public StockTakeUpdateLine(int stockTakeLineId, int productId, int originalQty, int checkedQty, String supplierCat, int lineAdded) {
        StockTakeLineId = stockTakeLineId;
        ProductId = productId;
        OriginalQty = originalQty;
        CheckedQty = checkedQty;
        SupplierCat = supplierCat;
        LineAdded = lineAdded;
    }

    public StockTakeUpdateLine(StockTakeLineProduct line) {
        StockTakeLineId = line.getStockTakeLineId();
        ProductId = line.getProductId();
        OriginalQty = line.getQty();
        CheckedQty = line.getQtyScanned();
        SupplierCat = line.getSupplierCat();
        LineAdded = 0;  /** >> (1)for new Entries only <<**/
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("StockTakeLineId")
    public int getStockTakeLineId() {
        return StockTakeLineId;
    }

    public void setStockTakeLineId(int stockTakeLineId) {
        StockTakeLineId = stockTakeLineId;
    }

    @JsonProperty("ProductId")
    public int getProductId() {
        return ProductId;
    }

    public void setProductId(int productId) {
        ProductId = productId;
    }

    @JsonProperty("OriginalQty")
    public int getOriginalQty() {
        return OriginalQty;
    }

    public void setOriginalQty(int originalQty) {
        OriginalQty = originalQty;
    }

    @JsonProperty("CheckedQty")
    public int getCheckedQty() {
        return CheckedQty;
    }

    public void setCheckedQty(int checkedQty) {
        CheckedQty = checkedQty;
    }

    @JsonProperty("SupplierCat")
    public String getSupplierCat() {
        return SupplierCat;
    }

    public void setSupplierCat(String supplierCat) {
        SupplierCat = supplierCat;
    }

    @JsonProperty("LineAdded")
    public int getLineAdded() {
        return LineAdded;
    }

    public void setLineAdded(int lineAdded) {
        LineAdded = lineAdded;
    }
}
