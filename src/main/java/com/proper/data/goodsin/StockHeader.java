package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 14/10/2014.
 */
public class StockHeader implements Serializable {
    private static final long serialVersionUID = 1L;
    private int StockHeaderId;
    private String SupplierCode;
    private String OrderNumber;
    private String Notes;
    private String StatusName;
    private int Status;
    private int AssignedUserId;
    private String AssignedUserName;
    private int CreatedByUserId;
    private String CreatedByUserName;
    private int GoodsInBoardId;

    public StockHeader() {
    }

    public StockHeader(int stockHeaderId, String supplierCode, String orderNumber, String notes, String statusName, int status, int assignedUserId, String assignedUserName, int createdByUserId, String createdByUserName, int goodsInBoardId) {
        StockHeaderId = stockHeaderId;
        SupplierCode = supplierCode;
        OrderNumber = orderNumber;
        Notes = notes;
        StatusName = statusName;
        Status = status;
        AssignedUserId = assignedUserId;
        AssignedUserName = assignedUserName;
        CreatedByUserId = createdByUserId;
        CreatedByUserName = createdByUserName;
        GoodsInBoardId = goodsInBoardId;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("StockHeaderId")
    public int getStockHeaderId() {
        return StockHeaderId;
    }

    public void setStockHeaderId(int stockHeaderId) {
        StockHeaderId = stockHeaderId;
    }

    @JsonProperty("SupplierCode")
    public String getSupplierCode() {
        return SupplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        SupplierCode = supplierCode;
    }

    public String getOrderNumber() {
        return OrderNumber;
    }

    @JsonProperty("OrderNumber")
    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
    }

    @JsonProperty("Notes")
    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    @JsonProperty("StatusName")
    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String statusName) {
        StatusName = statusName;
    }

    @JsonProperty("Status")
    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @JsonProperty("AssignedUserId")
    public int getAssignedUserId() {
        return AssignedUserId;
    }

    public void setAssignedUserId(int assignedUserId) {
        AssignedUserId = assignedUserId;
    }

    @JsonProperty("AssignedUserName")
    public String getAssignedUserName() {
        return AssignedUserName;
    }

    public void setAssignedUserName(String assignedUserName) {
        AssignedUserName = assignedUserName;
    }

    @JsonProperty("CreatedByUserId")
    public int getCreatedByUserId() {
        return CreatedByUserId;
    }

    public void setCreatedByUserId(int createdByUserId) {
        CreatedByUserId = createdByUserId;
    }

    @JsonProperty("CreatedByUserName")
    public String getCreatedByUserName() {
        return CreatedByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        CreatedByUserName = createdByUserName;
    }

    @JsonProperty("GoodsInBoardId")
    public int getGoodsInBoardId() {
        return GoodsInBoardId;
    }

    public void setGoodsInBoardId(int goodsInBoardId) {
        GoodsInBoardId = goodsInBoardId;
    }
}
