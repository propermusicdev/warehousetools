package com.proper.data.binchecker;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 18/03/2015.
 */
public class IndividualMoveLine implements Serializable {
    private static final long serialVersionUID = 1L;
    private int ProductId;
    private String SupplierCat;
    private String SrcBin;
    private String DstBin;
    private int Qty;

    public IndividualMoveLine() {
    }

    public IndividualMoveLine(int productId, String supplierCat, String srcBin, String dstBin, int qty) {
        ProductId = productId;
        SupplierCat = supplierCat;
        SrcBin = srcBin;
        DstBin = dstBin;
        Qty = qty;
    }

    public IndividualMoveLine(CheckerProduct prod) {
        ProductId = prod.getProductId();
        SupplierCat = prod.getSupplierCat();
        SrcBin = "";
        DstBin = "";
        Qty = 0;
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

    @JsonProperty("SrcBin")
    public String getSrcBin() {
        return SrcBin;
    }

    public void setSrcBin(String srcBin) {
        SrcBin = srcBin;
    }

    @JsonProperty("DstBin")
    public String getDstBin() {
        return DstBin;
    }

    public void setDstBin(String dstBin) {
        DstBin = dstBin;
    }

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }
}
