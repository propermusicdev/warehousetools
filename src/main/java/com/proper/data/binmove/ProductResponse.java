package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 11/04/2014.
 */
public class ProductResponse implements Serializable {
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
    private String PackshotURL;
    private List<Bin> Bins;

    public ProductResponse() {
    }

    public ProductResponse(int productId, String supplierCat, String artist, String title, String barcode,
                           String format, String EAN, String suppCode, int stockAmount, int deletionType,
                           String fullArtist, String fullTitle, String packshotURL, List<Bin> bins) {
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
        PackshotURL = packshotURL;
        Bins = bins;
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

    @JsonProperty("Format")
    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    @JsonProperty("EAN")
    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    @JsonProperty("SuppCode")
    public String getSuppCode() {
        return SuppCode;
    }

    public void setSuppCode(String suppCode) {
        SuppCode = suppCode;
    }

    @JsonProperty("StockAmount")
    public int getStockAmount() {
        return StockAmount;
    }

    public void setStockAmount(int stockAmount) {
        StockAmount = stockAmount;
    }

    @JsonProperty("DeletionType")
    public int getDeletionType() {
        return DeletionType;
    }

    public void setDeletionType(int deletionType) {
        DeletionType = deletionType;
    }

    @JsonProperty("FullArtist")
    public String getFullArtist() {
        return FullArtist;
    }

    public void setFullArtist(String fullArtist) {
        FullArtist = fullArtist;
    }

    @JsonProperty("FullTitle")
    public String getFullTitle() {
        return FullTitle;
    }

    public void setFullTitle(String fullTitle) {
        FullTitle = fullTitle;
    }

    @JsonProperty("PackshotURL")
    public String getPackshotURL() {
        return PackshotURL;
    }

    public void setPackshotURL(String packshotURL) {
        PackshotURL = packshotURL;
    }

    @JsonProperty("Bins")
    public List<Bin> getBins() {
        return Bins;
    }

    public void setBins(List<Bin> bins) {
        Bins = bins;
    }
}
