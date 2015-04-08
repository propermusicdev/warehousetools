package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 23/06/2014.
 */
public class MoveRequestItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ProductID;
    private String Suppliercat;
    private int Qty;

    public MoveRequestItem() {
    }

    public MoveRequestItem(int productID, String suppliercat, int qty) {
        ProductID = productID;
        Suppliercat = suppliercat;
        Qty = qty;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("ProductID")
    public int getProductID() {
        return ProductID;
    }

    public void setProductID(int productID) {
        ProductID = productID;
    }

    @JsonProperty("SupplierCat")
    public String getSuppliercat() {
        return Suppliercat;
    }

    public void setSuppliercat(String suppliercat) {
        Suppliercat = suppliercat;
    }

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }
}
