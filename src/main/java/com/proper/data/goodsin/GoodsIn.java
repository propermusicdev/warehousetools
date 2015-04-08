package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 15/10/2014.
 */
public class GoodsIn implements Serializable {
    private static final long serialVersionUID = 1L;
    private int StockHeaderId;
    private String Supplier;
    private String SupplierName;
    private String Notes;
    private int Status;
    private String StatusName;
    private String OrderNumber;
    private int AssignedUserId;
    private String AssignedUserName;
    private int Lines;
    private int UnitsOrdered;

    public GoodsIn() {
    }

    public GoodsIn(int stockHeaderId, String supplier, String supplierName, String notes, int status, String statusName, String orderNumber, int assignedUserId, String assignedUserName, int lines, int unitsOrdered) {
        StockHeaderId = stockHeaderId;
        Supplier = supplier;
        SupplierName = supplierName;
        Notes = notes;
        Status = status;
        StatusName = statusName;
        OrderNumber = orderNumber;
        AssignedUserId = assignedUserId;
        AssignedUserName = assignedUserName;
        Lines = lines;
        UnitsOrdered = unitsOrdered;
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

    @JsonProperty("Supplier")
    public String getSupplier() {
        return Supplier;
    }

    public void setSupplier(String supplier) {
        Supplier = supplier;
    }

    @JsonProperty("SupplierName")
    public String getSupplierName() {
        return SupplierName;
    }

    public void setSupplierName(String supplierName) {
        SupplierName = supplierName;
    }

    @JsonProperty("Notes")
    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    @JsonProperty("Status")
    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    @JsonProperty("StatusName")
    public String getStatusName() {
        return StatusName;
    }

    public void setStatusName(String statusName) {
        StatusName = statusName;
    }

    @JsonProperty("OrderNumber")
    public String getOrderNumber() {
        return OrderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
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

    @JsonProperty("Lines")
    public int getLines() {
        return Lines;
    }

    public void setLines(int lines) {
        Lines = lines;
    }

    @JsonProperty("UnitsOrdered")
    public int getUnitsOrdered() {
        return UnitsOrdered;
    }

    public void setUnitsOrdered(int unitsOrdered) {
        UnitsOrdered = unitsOrdered;
    }
}
