package com.proper.data.replen;
import java.sql.Timestamp;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Knight on 13/01/2015.
 */
public class ReplenLineFeedBack implements Serializable {
    private static final long serialVersionUID = 1L;
    private int MovelistId;
    private int MovelistLineId;
    private Timestamp InsertTimeStamp;
    private int ProductId;
    private String SrcBinCode;
    private String DstBinCode;
    private int Qty;
    private int RemoveLink;
    private int MovementId;
    private String SortOrder;
    private boolean QtyConfirmed;
    private boolean Completed;

    public ReplenLineFeedBack() {
    }

    public ReplenLineFeedBack(int movelistId, int movelistLineId, Timestamp insertTimeStamp, int productId, String srcBinCode, String dstBinCode, int qty, int removeLink, int movementId, String sortOrder, boolean qtyConfirmed, boolean completed) {
        MovelistId = movelistId;
        MovelistLineId = movelistLineId;
        InsertTimeStamp = insertTimeStamp;
        ProductId = productId;
        SrcBinCode = srcBinCode;
        DstBinCode = dstBinCode;
        Qty = qty;
        RemoveLink = removeLink;
        MovementId = movementId;
        SortOrder = sortOrder;
        QtyConfirmed = qtyConfirmed;
        Completed = completed;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("MovelistId")
    public int getMovelistId() {
        return MovelistId;
    }

    public void setMovelistId(int movelistId) {
        MovelistId = movelistId;
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

    @JsonProperty("MovementId(")
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
}
