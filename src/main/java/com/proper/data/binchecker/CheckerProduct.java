package com.proper.data.binchecker;

import com.proper.data.binmove.ProductBinResponse;
import com.proper.data.binmove.ProductResponse;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by lebel on 07/04/2015.
 */
public class CheckerProduct implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ProductId;
    private String SupplierCat;
    private String Artist;
    private String Title;
    private String Barcode;
    private String Format;
    private String EAN;
    private String SuppCode;
    private int Qty;
    private int QtyScanned;
    private int Status;
    private int AdjustedByUserId;
    private String AdjustedByName;
    private int BinCheckerFlag;

    public CheckerProduct() {
    }

    public CheckerProduct(int productId, String supplierCat, String artist, String title, String barcode, String format, String EAN, String suppCode, int qty, int qtyScanned, int status, int adjustedByUserId, String adjustedByName, int binCheckerFlag) {
        ProductId = productId;
        SupplierCat = supplierCat;
        Artist = artist;
        Title = title;
        Barcode = barcode;
        Format = format;
        this.EAN = EAN;
        SuppCode = suppCode;
        Qty = qty;
        QtyScanned = qtyScanned;
        Status = status;
        AdjustedByUserId = adjustedByUserId;
        AdjustedByName = adjustedByName;
        BinCheckerFlag = binCheckerFlag;
    }

    public CheckerProduct(ProductResponse prod, int qtyInBin) {
        ProductId = prod.getProductId();
        SupplierCat = prod.getSupplierCat();
        Artist = prod.getArtist();
        Title = prod.getTitle();
        Barcode = prod.getBarcode();
        Format = prod.getFormat();
        this.EAN = prod.getEAN();
        SuppCode = prod.getSuppCode();
        Qty = qtyInBin;
        QtyScanned = 0; //default
        Status = 0; //default
        AdjustedByUserId = 0; //default
        AdjustedByName = ""; //default
        BinCheckerFlag = 0; //default
    }

    public CheckerProduct(ProductBinResponse prod) {
        ProductId = prod.getProductId();
        SupplierCat = prod.getSupplierCat();
        Artist = prod.getArtist();
        Title = prod.getTitle();
        Barcode = prod.getBarcode();
        Format = prod.getFormat();
        this.EAN = prod.getEAN();
        SuppCode = prod.getSuppCode();
        Qty = prod.getQtyInBin();
        QtyScanned = 0; //default
        Status = 0; //default
        AdjustedByUserId = 0; //default
        AdjustedByName = ""; //default
        BinCheckerFlag = 0; //default
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

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }

    @JsonProperty("QtyScanned")
    public int getQtyScanned() {
        return QtyScanned;
    }

    public void setQtyScanned(int qtyScanned) {
        QtyScanned = qtyScanned;
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
}
