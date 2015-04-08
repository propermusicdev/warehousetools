package com.proper.data.stocktake;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Knight on 26/01/2015.
 */
public class StockTakeLineProduct implements Serializable{
    private static final long serialVersionUID = 1L;
    private int StockTakeLineId;
    private String BinCode;
    private int StockTakeId;
    private int ProductId;
    private String SupplierCat;
    private String EAN;
    private String Barcode;
    private String Artist;
    private String Title;
    private int Qty;
    private int ActualQty;
    private int Status;
    private int AdjustedByUserId;
    private String AdjustedByName;
    private int BinCheckerFlag;
    private String Format = "";
    private int QtyScanned = 0;

    public StockTakeLineProduct() {
    }

    public StockTakeLineProduct(int stockTakeLineId, String binCode, int stockTakeId, int productId, String supplierCat, String EAN, String barcode, String artist, String title, int qty, int actualQty, int status, int adjustedByUserId, String adjustedByName, int binCheckerFlag) {
        StockTakeLineId = stockTakeLineId;
        BinCode = binCode;
        StockTakeId = stockTakeId;
        ProductId = productId;
        SupplierCat = supplierCat;
        this.EAN = EAN;
        Barcode = barcode;
        Artist = artist;
        Title = title;
        Qty = qty;
        ActualQty = actualQty;
        Status = status;
        AdjustedByUserId = adjustedByUserId;
        AdjustedByName = adjustedByName;
        BinCheckerFlag = binCheckerFlag;
        Format = "";
        QtyScanned = 0; //added property
    }

    public StockTakeLineProduct(StockTakeLine line) {
        StockTakeLineId = line.getStockTakeLineId();
        BinCode = line.getBinCode();
        StockTakeId = line.getStockTakeId();
        ProductId = line.getProductId();
        SupplierCat = line.getSupplierCat();
        this.EAN = line.getEAN();
        Barcode = line.getBarcode();
        Artist = line.getArtist();
        Title = line.getTitle();
        Qty = line.getQty();
        ActualQty = line.getActualQty();
        Status = line.getStatus();
        AdjustedByUserId = line.getAdjustedByUserId();
        AdjustedByName = line.getAdjustedByName();
        BinCheckerFlag = line.getBinCheckerFlag();
        Format = "";
        QtyScanned = 0; //added property
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

    @JsonProperty("BinCode")
    public String getBinCode() {
        return BinCode;
    }

    public void setBinCode(String binCode) {
        BinCode = binCode;
    }

    @JsonProperty("StockTakeId")
    public int getStockTakeId() {
        return StockTakeId;
    }

    public void setStockTakeId(int stockTakeId) {
        StockTakeId = stockTakeId;
    }

    @JsonProperty("ProductId")
    public int getProductId() {
        return ProductId;
    }

    public void setProductId(int productId) {
        ProductId = productId;
    }

    @JsonProperty("SupplierCat")
    public String getSupplierCat() {
        return SupplierCat;
    }

    public void setSupplierCat(String supplierCat) {
        SupplierCat = supplierCat;
    }

    @JsonProperty("EAN")
    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    @JsonProperty("Barcode")
    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    @JsonProperty("Artist")
    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    @JsonProperty("Title")
    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }

    @JsonProperty("ActualQty")
    public int getActualQty() {
        return ActualQty;
    }

    public void setActualQty(int actualQty) {
        ActualQty = actualQty;
    }

    @JsonProperty("Status")
    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @JsonProperty("AdjustedByUserId")
    public int getAdjustedByUserId() {
        return AdjustedByUserId;
    }

    public void setAdjustedByUserId(int adjustedByUserId) {
        AdjustedByUserId = adjustedByUserId;
    }

    @JsonProperty("AdjustedByName")
    public String getAdjustedByName() {
        return AdjustedByName;
    }

    public void setAdjustedByName(String adjustedByName) {
        AdjustedByName = adjustedByName;
    }

    @JsonProperty("BinCheckerFlag")
    public int getBinCheckerFlag() {
        return BinCheckerFlag;
    }

    public void setBinCheckerFlag(int binCheckerFlag) {
        BinCheckerFlag = binCheckerFlag;
    }

    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    public int getQtyScanned() {
        return QtyScanned;
    }

    public void setQtyScanned(int qtyScanned) {
        QtyScanned = qtyScanned;
    }

    public void addToQtyScanned(int howMuch) {
        this.QtyScanned = this.QtyScanned + howMuch;
    }

    public void removeFromQtyScanned(int howMuch) {
        if (this.QtyScanned >= howMuch) {
            this.QtyScanned = this.QtyScanned - howMuch;
        }
    }

    public StockTakeLine toStockTakeLine() {
        StockTakeLine line = null;
        if (this.getProductId() > 0 && this.getStockTakeLineId() > 0 && getStockTakeId() > 0) {
            line = new StockTakeLine(this.StockTakeLineId, this.BinCode, this.StockTakeId, this.ProductId,
                    this.SupplierCat, this.EAN, this.Barcode, this.Artist, this.Title, this.Qty, this.ActualQty,
            this.Status, this.AdjustedByUserId, this.AdjustedByName,this.BinCheckerFlag);
        }
        return line;
    }
}
