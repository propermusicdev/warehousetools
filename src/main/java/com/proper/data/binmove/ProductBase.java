package com.proper.data.binmove;

import java.io.Serializable;

/**
 * Created by Lebel on 19/05/2014.
 */
public abstract class ProductBase implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ProductId;
    private String SupplierCat;
    private String Artist;
    private String Title;
    private String Barcode;
    private String Format;
    private String EAN;
    private String SuppCode;
    private int StockAmount;
    private int DeletionType;
    private String FullArtist;
    private String FullTitle;

    protected ProductBase() {
    }

    protected ProductBase(int productId, String supplierCat, String artist, String title, String barcode, String format, String EAN, String suppCode, int stockAmount, int deletionType, String fullArtist, String fullTitle) {
        ProductId = productId;
        SupplierCat = supplierCat;
        Artist = artist;
        Title = title;
        Barcode = barcode;
        Format = format;
        this.EAN = EAN;
        SuppCode = suppCode;
        StockAmount = stockAmount;
        DeletionType = deletionType;
        FullArtist = fullArtist;
        FullTitle = fullTitle;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getProductId() {
        return ProductId;
    }

    public void setProductId(int productId) {
        ProductId = productId;
    }

    public String getSupplierCat() {
        return SupplierCat;
    }

    public void setSupplierCat(String supplierCat) {
        SupplierCat = supplierCat;
    }

    public String getArtist() {
        return Artist;
    }

    public void setArtist(String artist) {
        Artist = artist;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    public String getSuppCode() {
        return SuppCode;
    }

    public void setSuppCode(String suppCode) {
        SuppCode = suppCode;
    }

    public int getStockAmount() {
        return StockAmount;
    }

    public void setStockAmount(int stockAmount) {
        StockAmount = stockAmount;
    }

    public int getDeletionType() {
        return DeletionType;
    }

    public void setDeletionType(int deletionType) {
        DeletionType = deletionType;
    }

    public String getFullArtist() {
        return FullArtist;
    }

    public void setFullArtist(String fullArtist) {
        FullArtist = fullArtist;
    }

    public String getFullTitle() {
        return FullTitle;
    }

    public void setFullTitle(String fullTitle) {
        FullTitle = fullTitle;
    }
}
