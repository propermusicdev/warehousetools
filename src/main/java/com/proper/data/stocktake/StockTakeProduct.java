package com.proper.data.stocktake;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 23/01/2015.
 */
public class StockTakeProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ProductId;
    private String SupplierCat;
    private String Artist;
    private String Title;
    private String Barcode;
    private String EAN;
    private String Format;
    private int QtyInBin;
    private int QtyScanned;

    public StockTakeProduct() {
    }

    public StockTakeProduct(int productId, String supplierCat, String artist, String title, String barcode, String EAN, String format, int qtyInBin, int qtyScanned) {
        ProductId = productId;
        SupplierCat = supplierCat;
        Artist = artist;
        Title = title;
        Barcode = barcode;
        this.EAN = EAN;
        Format = format;
        QtyInBin = qtyInBin;
        QtyScanned = qtyScanned;
    }

    public StockTakeProduct(StockTakeProductResponse response, int qtyInBin) {
        ProductId = response.getProductId();
        SupplierCat = response.getSupplierCat();
        Artist = response.getArtist();
        Title = response.getTitle();
        Barcode = response.getBarcode();
        this.EAN = response.getEAN();
        Format = response.getFormat();
        QtyInBin = qtyInBin;
        QtyScanned = 0;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
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

    @JsonProperty("Barcode")
    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    @JsonProperty("EAN")
    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    @JsonProperty("Format")
    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    @JsonProperty("QtyInBin")
    public int getQtyInBin() {
        return QtyInBin;
    }

    public void setQtyInBin(int qtyInBin) {
        QtyInBin = qtyInBin;
    }

    @JsonProperty("QtyScanned")
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
        if (QtyScanned >= howMuch) {
            this.QtyScanned = this.QtyScanned - howMuch;
        }
    }
}
