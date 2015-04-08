package com.proper.data.replen;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 02/12/2014.
 */
public class ReplenMoveListLinesItemResponse implements Serializable {
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
    private boolean QtyConfirmed;
    private boolean Completed;
    private String SortOrder;
    private int defaultQty;
    private String defaultSrcBin;
    private String defaultDstBin;

    public ReplenMoveListLinesItemResponse() {
    }

    public ReplenMoveListLinesItemResponse(int movelistLineId, Timestamp insertTimeStamp, int productId, String catNumber, String artist, String title, String EAN, String srcBinCode, String dstBinCode, int qty, int removeLink, int movementId, boolean qtyConfirmed, boolean completed, String sortOrder) {
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
        QtyConfirmed = qtyConfirmed;
        Completed = completed;
        SortOrder = sortOrder;
        defaultQty = qty;
        defaultSrcBin = srcBinCode;
        defaultDstBin = dstBinCode;
    }

    /** Overload 3 - From ActReplenSplitLine Response **/
    public ReplenMoveListLinesItemResponse(ReplenMoveListLinesItemResponse item, ReplenLineFeedBackResponse response) {
        if (response.getResult().equalsIgnoreCase("Success")) {
            MovelistLineId = response.getNewLine().getMovelistLineId();
            InsertTimeStamp = response.getNewLine().getInsertTimeStamp();
            ProductId = response.getNewLine().getProductId();
            CatNumber = item.getCatNumber();
            Artist = item.getArtist();
            Title = item.getTitle();
            this.EAN = item.getEAN();
            SrcBinCode = response.getNewLine().getSrcBinCode();
            DstBinCode = response.getNewLine().getDstBinCode();
            Qty = response.getNewLine().getQty();
            RemoveLink = item.getRemoveLink();
            MovementId = item.getMovementId();
            QtyConfirmed = item.isQtyConfirmed();
            Completed = item.isCompleted();
            SortOrder = response.getNewLine().getSortOrder();
            defaultQty = response.getNewLine().getQty();
            defaultSrcBin = response.getNewLine().getSrcBinCode();
            defaultDstBin = response.getNewLine().getDstBinCode();
        }
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

    @JsonProperty("QtyConfirmed")
    public boolean isQtyConfirmed() {
        return QtyConfirmed;
    }

    public void setQtyConfirmed(boolean qtyConfirmed) {
        QtyConfirmed = qtyConfirmed;
    }

    @JsonProperty("Completed")
    public boolean isCompleted() {
        return Completed;
    }

    public void setCompleted(boolean completed) {
        Completed = completed;
    }

    @JsonProperty("SortOrder")
    public String getSortOrder() {
        return SortOrder;
    }

    public void setSortOrder(String sortOrder) {
        SortOrder = sortOrder;
    }

    public boolean restoreDefaultQtyValue() {
        boolean done = false;
        if (!this.isQtyConfirmed()) {
            this.setQty(defaultQty);
            done = true;
        }
        return done;
    }
}
