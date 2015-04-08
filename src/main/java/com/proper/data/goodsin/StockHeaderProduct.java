package com.proper.data.goodsin;

import com.proper.data.binmove.Bin;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 15/10/2014.
 */
public class StockHeaderProduct  implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ProductId;
    private String SupplierCat;
    private String Artist;
    private String Title;
    private String Barcode;
    private String Format;
    private String EAN;
    private int StockAmount;
    private int DeletionType;
    private String FullArtist;
    private String FullTilte;
    private String PackshotURL;
    private List<Bin> Bins;
    private int QtyOrdered;
    private int QtyReceived;
    private int NumStockLines;
    private List<StockLineTransaction> Transactions;

    public StockHeaderProduct() {
    }

    public StockHeaderProduct(int productId, String supplierCat, String artist, String title, String barcode, String format, String EAN, int stockAmount, int deletionType, String fullArtist, String fullTilte, String packshotURL, List<Bin> bins, int qtyOrdered, int qtyReceived, int numStockLines, List<StockLineTransaction> transactions) {
        ProductId = productId;
        SupplierCat = supplierCat;
        Artist = artist;
        Title = title;
        Barcode = barcode;
        Format = format;
        this.EAN = EAN;
        StockAmount = stockAmount;
        DeletionType = deletionType;
        FullArtist = fullArtist;
        FullTilte = fullTilte;
        PackshotURL = packshotURL;
        Bins = bins;
        QtyOrdered = qtyOrdered;
        QtyReceived = qtyReceived;
        NumStockLines = numStockLines;
        Transactions = transactions;
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

    @JsonProperty("FullTilte")
    public String getFullTilte() {
        return FullTilte;
    }

    public void setFullTilte(String fullTilte) {
        FullTilte = fullTilte;
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

    @JsonProperty("QtyOrdered")
    public int getQtyOrdered() {
        return QtyOrdered;
    }

    public void setQtyOrdered(int qtyOrdered) {
        QtyOrdered = qtyOrdered;
    }

    @JsonProperty("QtyReceived")
    public int getQtyReceived() {
        return QtyReceived;
    }

    public void setQtyReceived(int qtyReceived) {
        QtyReceived = qtyReceived;
    }

    @JsonProperty("NumStockLines")
    public int getNumStockLines() {
        return NumStockLines;
    }

    public void setNumStockLines(int numStockLines) {
        NumStockLines = numStockLines;
    }

    @JsonProperty("Transactions")
    public List<StockLineTransaction> getTransactions() {
        return Transactions;
    }

    public void setTransactions(List<StockLineTransaction> transactions) {
        Transactions = transactions;
    }
}
