package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 20/06/2014.
 */
public class ProductBinSelection implements Serializable {
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
    private String PackshotURL;
    private int QtyInBin;
    private int QtyToMove;
    private int QtyTotal;

    public ProductBinSelection() {
    }

    public ProductBinSelection(int productId, String supplierCat, String artist, String title, String barcode, String format, String EAN, String suppCode, int stockAmount, int deletionType, String packshotURL, int qtyInBin, int qtyToMove) {
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
        PackshotURL = packshotURL;
        QtyInBin = qtyInBin;
        QtyToMove = qtyToMove;
        QtyTotal = qtyToMove + qtyInBin;
    }

    public ProductBinSelection(ProductBinResponse products) {
        ProductId = products.getProductId();
        SupplierCat = products.getSupplierCat();
        Artist = products.getArtist();
        Title = products.getTitle();
        Barcode = products.getBarcode();
        Format = products.getFormat();
        this.EAN = products.getEAN();
        SuppCode = products.getSuppCode();
        StockAmount = products.getStockAmount();
        DeletionType = products.getDeletionType();
        PackshotURL = products.getPackshotURL();
        QtyInBin = 0;
        QtyToMove = products.getQtyInBin();
        QtyTotal = products.getQtyInBin();
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

    @JsonProperty("PackshotURL")
    public String getPackshotURL() {
        return PackshotURL;
    }

    public void setPackshotURL(String packshotURL) {
        PackshotURL = packshotURL;
    }

    @JsonProperty("QtyInBin")
    public int getQtyInBin() {
        return QtyInBin;
    }

    public void setQtyInBin(int qtyInBin) {
        QtyInBin = qtyInBin;
    }

    @JsonProperty("QtyToMove")
    public int getQtyToMove() {
        return QtyToMove;
    }

    public void setQtyToMove(int qtyToMove) {
        QtyToMove = qtyToMove;
    }

    public boolean add10ToMove() {
        boolean done = false;
        if (this.getQtyInBin() >= 10) {
            this.setQtyToMove(this.getQtyToMove() + 10);
            this.setQtyInBin(this.getQtyInBin() - 10);
            done = true;
        }
        return done;
    }

    public boolean take10FromMove() {
        boolean done = false;
        if (this.getQtyToMove() >= 10) {
            this.setQtyToMove(this.getQtyToMove() - 10);
            this.setQtyInBin(this.getQtyInBin() + 10);
            done = true;
        }
        return done;
    }

    public boolean add10ToBin() {
        boolean done = false;
        if (this.getQtyToMove() >= 10) {
            this.setQtyInBin(this.getQtyInBin() + 10);
            this.setQtyToMove(this.getQtyToMove() - 10);
            done = true;
        }
        return done;
    }

    public boolean take10FromBin() {
        boolean done = false;
        if (this.getQtyInBin() >= 10) {
            this.setQtyInBin(this.getQtyInBin() - 10);
            this.setQtyToMove(this.getQtyToMove() + 10);
            done = true;
        }
        return done;
    }

    public boolean incrementMove() {
        boolean done = false;
        if (this.getQtyInBin() > 0) {
            this.setQtyToMove(getQtyToMove() + 1);
            this.setQtyInBin(getQtyInBin() - 1);
            done = true;
        }
        return done;
    }

    public boolean incrementBin() {
        boolean done = false;
        if (this.getQtyToMove() > 0) {
            this.setQtyInBin(getQtyInBin() + 1);
            this.setQtyToMove(getQtyToMove() - 1);
            done = true;
        }
        return done;
    }

    public boolean purgeMove() {
        boolean done = true;
        if (this.getQtyToMove() > 0) {
            this.setQtyInBin(this.getQtyToMove() + this.getQtyInBin());
            this.setQtyToMove(0);
        }
        return done;
    }

    public boolean purgeBin() {
        boolean done = true;
        if (this.getQtyInBin() > 0) {
            this.setQtyToMove(this.getQtyInBin() + this.getQtyToMove());
            this.setQtyInBin(0);
        }
        return done;
    }

    public boolean incrementMoveBy(int thisMuch) {
        boolean done = false;
        if (this.getQtyInBin() > 0 && thisMuch <= this.getQtyInBin()) {
            this.setQtyToMove(getQtyToMove() + thisMuch);
            this.setQtyInBin(getQtyInBin() - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean incrementBinBy(int thisMuch) {
        boolean done = false;
        if (this.getQtyToMove() > 0 && thisMuch <= this.getQtyToMove()) {
            this.setQtyInBin(getQtyInBin() + thisMuch);
            this.setQtyToMove(getQtyToMove() - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean changeMoveTo(int thisMuch) {
        boolean done = false;
        if (thisMuch <= this.QtyTotal) {
            this.setQtyToMove(thisMuch);
            this.setQtyInBin(QtyTotal - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean changeBinTo(int thisMuch) {
        boolean done = false;
        if (thisMuch <= this.QtyTotal) {
            this.setQtyInBin(thisMuch);
            this.setQtyToMove(QtyTotal - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean restoreDefaultValue() {
        boolean done = false;
        if (this.QtyTotal > 0) {
            this.setQtyToMove(0);
            this.setQtyInBin(this.QtyTotal);
            done = true;
        }
        return done;
    }
}
