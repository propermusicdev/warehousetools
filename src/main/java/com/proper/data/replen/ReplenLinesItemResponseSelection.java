package com.proper.data.replen;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Knight on 18/12/2014.
 */
public class ReplenLinesItemResponseSelection implements Serializable {
    private static final long serialVersionUID = 1L;
    private int MovelistLineId;
    private Timestamp InsertTimeStamp;
    private int ProductId;
    private String CatNumber;
    private String Artist;
    private String Title;
    private String EAN;
    private String SrcBinCode;
    private String DstBinCode;
    private int Qty;
    private int RemoveLink;
    private int MovementId;
    private String SortOrder;
    private int defaultQty;
    private String defaultSrcBin;
    private String defaultDstBin;
    private int QtyToSplit;
    private int QtyTotal;

    public ReplenLinesItemResponseSelection(int movelistLineId, Timestamp insertTimeStamp, int productId, String catNumber, String artist, String title, String EAN, String srcBinCode, String dstBinCode, int qty, int removeLink, int movementId, String sortOrder, int defaultQty, String defaultSrcBin, String defaultDstBin, int qtyToSplit, int qtyTotal) {
        MovelistLineId = movelistLineId;
        InsertTimeStamp = insertTimeStamp;
        ProductId = productId;
        CatNumber = catNumber;
        Artist = artist;
        Title = title;
        this.EAN = EAN;
        SrcBinCode = srcBinCode;
        DstBinCode = dstBinCode;
        Qty = qty;
        RemoveLink = removeLink;
        MovementId = movementId;
        SortOrder = sortOrder;
        this.defaultQty = defaultQty;
        this.defaultSrcBin = defaultSrcBin;
        this.defaultDstBin = defaultDstBin;
        QtyToSplit = qtyToSplit;
        QtyTotal = qtyTotal;
    }

    public ReplenLinesItemResponseSelection(ReplenMoveListLinesItemResponse line) {
        MovelistLineId = line.getMovelistLineId();
        InsertTimeStamp = line.getInsertTimeStamp();
        ProductId = line.getProductId();
        CatNumber = line.getCatNumber();
        Artist = line.getArtist();
        Title = line.getTitle();
        this.EAN = line.getEAN();
        SrcBinCode = line.getSrcBinCode();
        DstBinCode = line.getDstBinCode();
        Qty = line.getQty();
        RemoveLink = line.getRemoveLink();
        MovementId = line.getMovementId();
        SortOrder = line.getSortOrder();
        defaultQty = line.getQty();
        defaultSrcBin = line.getSrcBinCode();
        defaultDstBin = line.getDstBinCode();
        QtyToSplit = 0;
        QtyTotal = line.getQty() + QtyToSplit;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("MovelistLineId")
    public int getMovelistLineId() {
        return MovelistLineId;
    }

    public void setMovelistLineId(int movelistLineId) {
        MovelistLineId = movelistLineId;
    }

    @JsonProperty("InsertTimeStamp")
    public Timestamp getInsertTimeStamp() {
        return InsertTimeStamp;
    }

    public void setInsertTimeStamp(Timestamp insertTimeStamp) {
        InsertTimeStamp = insertTimeStamp;
    }

    @JsonProperty("ProductId")
    public int getProductId() {
        return ProductId;
    }

    public void setProductId(int productId) {
        ProductId = productId;
    }

    @JsonProperty("CatNumber")
    public String getCatNumber() {
        return CatNumber;
    }

    public void setCatNumber(String catNumber) {
        CatNumber = catNumber;
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

    @JsonProperty("EAN")
    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    @JsonProperty("SrcBinCode")
    public String getSrcBinCode() {
        return SrcBinCode;
    }

    public void setSrcBinCode(String srcBinCode) {
        SrcBinCode = srcBinCode;
    }

    @JsonProperty("DstBinCode")
    public String getDstBinCode() {
        return DstBinCode;
    }

    public void setDstBinCode(String dstBinCode) {
        DstBinCode = dstBinCode;
    }

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }

    @JsonProperty("RemoveLink")
    public int getRemoveLink() {
        return RemoveLink;
    }

    public void setRemoveLink(int removeLink) {
        RemoveLink = removeLink;
    }

    @JsonProperty("MovementId")
    public int getMovementId() {
        return MovementId;
    }

    public void setMovementId(int movementId) {
        MovementId = movementId;
    }

    @JsonProperty("SortOrder")
    public String getSortOrder() {
        return SortOrder;
    }

    public void setSortOrder(String sortOrder) {
        SortOrder = sortOrder;
    }

    @JsonProperty("QtyToSplit")
    public int getQtyToSplit() {
        return QtyToSplit;
    }

    public void setQtyToSplit(int qtyToSplit) {
        QtyToSplit = qtyToSplit;
    }

    @JsonProperty("QtyTotal")
    public int getQtyTotal() {
        return QtyTotal;
    }

    public void setQtyTotal(int qtyTotal) {
        QtyTotal = qtyTotal;
    }

    @JsonProperty("DefaultDstBin")
    public String getDefaultDstBin() {
        return defaultDstBin;
    }

    public void setDefaultDstBin(String defaultDstBin) {
        this.defaultDstBin = defaultDstBin;
    }

    @JsonProperty("DefaultSrcBin")
    public String getDefaultSrcBin() {
        return defaultSrcBin;
    }

    public void setDefaultSrcBin(String defaultSrcBin) {
        this.defaultSrcBin = defaultSrcBin;
    }

    @JsonProperty("DefaultQty")
    public int getDefaultQty() {
        return defaultQty;
    }

    public void setDefaultQty(int defaultQty) {
        this.defaultQty = defaultQty;
    }

    public boolean add10ToMove() {
        boolean done = false;
        if (this.getQty() >= 10) {
            this.setQtyToSplit(this.getQtyToSplit() + 10);
            this.setQty(this.getQty() - 10);
            done = true;
        }
        return done;
    }

    public boolean take10FromMove() {
        boolean done = false;
        if (this.getQtyToSplit() >= 10) {
            this.setQtyToSplit(this.getQtyToSplit() - 10);
            this.setQty(this.getQty() + 10);
            done = true;
        }
        return done;
    }

    public boolean add10ToBin() {
        boolean done = false;
        if (this.getQtyToSplit() >= 10) {
            this.setQty(this.getQty() + 10);
            this.setQtyToSplit(this.getQtyToSplit() - 10);
            done = true;
        }
        return done;
    }

    public boolean take10FromBin() {
        boolean done = false;
        if (this.getQty() >= 10) {
            this.setQty(this.getQty() - 10);
            this.setQtyToSplit(this.getQtyToSplit() + 10);
            done = true;
        }
        return done;
    }

    public boolean incrementMove() {
        boolean done = false;
        if (this.getQty() > 0) {
            this.setQtyToSplit(getQtyToSplit() + 1);
            this.setQty(getQty() - 1);
            done = true;
        }
        return done;
    }

    public boolean incrementBin() {
        boolean done = false;
        if (this.getQtyToSplit() > 0) {
            this.setQty(getQty() + 1);
            this.setQtyToSplit(getQtyToSplit() - 1);
            done = true;
        }
        return done;
    }

    public boolean purgeMove() {
        boolean done = true;
        if (this.getQtyToSplit() > 0) {
            this.setQty(this.getQtyToSplit() + this.getQty());
            this.setQtyToSplit(0);
        }
        return done;
    }

    public boolean purgeBin() {
        boolean done = true;
        if (this.getQty() > 0) {
            this.setQtyToSplit(this.getQty() + this.getQtyToSplit());
            this.setQty(0);
        }
        return done;
    }

    public boolean incrementMoveBy(int thisMuch) {
        boolean done = false;
        if (this.getQty() > 0 && thisMuch <= this.getQty()) {
            this.setQtyToSplit(getQtyToSplit() + thisMuch);
            this.setQty(getQty() - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean incrementBinBy(int thisMuch) {
        boolean done = false;
        if (this.getQtyToSplit() > 0 && thisMuch <= this.getQtyToSplit()) {
            this.setQty(getQty() + thisMuch);
            this.setQtyToSplit(getQtyToSplit() - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean changeMoveTo(int thisMuch) {
        boolean done = false;
        if (thisMuch <= this.QtyTotal) {
            this.setQtyToSplit(thisMuch);
            this.setQty(QtyTotal - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean changeBinTo(int thisMuch) {
        boolean done = false;
        if (thisMuch <= this.QtyTotal) {
            this.setQty(thisMuch);
            this.setQtyToSplit(QtyTotal - thisMuch);
            done = true;
        }
        return done;
    }

    public boolean restoreDefaultValue() {
        boolean done = false;
        if (this.QtyTotal > 0) {
            this.setQtyToSplit(0);
            this.setQty(this.QtyTotal);
            done = true;
        }
        return done;
    }

    public ReplenMoveListLinesItemResponse toReplenMoveListLinesItemResponse() {
        ReplenMoveListLinesItemResponse line = new ReplenMoveListLinesItemResponse(MovelistLineId, InsertTimeStamp,
                ProductId, CatNumber, Artist, Title, EAN, SrcBinCode, DstBinCode, Qty, RemoveLink, MovementId,
                false, false, SortOrder);
        return line;
    }
}
