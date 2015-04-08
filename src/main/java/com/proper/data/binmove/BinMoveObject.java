package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 30/05/2014.
 */
public class BinMoveObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String Action;
    private int ProductId;
    private String SupplierCat;
    private String EAN;
    private int Qty;

    public BinMoveObject() {
    }

    public BinMoveObject(String action, int productId, String supplierCat, String EAN, int qty) {
        Action = action;
        ProductId = productId;
        SupplierCat = supplierCat;
        this.EAN = EAN;
        Qty = qty;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("Action")
    public String getAction() {
        return Action;
    }

    public void setAction(String action) {
        Action = action;
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

    @JsonProperty("EAN")
    public String getEAN() {
        return EAN;
    }

    public void setEAN(String EAN) {
        this.EAN = EAN;
    }

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }
}
