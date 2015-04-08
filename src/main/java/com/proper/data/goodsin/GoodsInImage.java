package com.proper.data.goodsin;

import android.net.Uri;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 08/10/2014.
 */
public class GoodsInImage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String Supplier;
    private int GoodsInBoardID;
    private int ProductID;
    private String OrderNumber;
    private int Quantity;
    private String UserName;
    private List<Uri> Files;
    public GoodsInImage() {
    }

    public GoodsInImage(String supplier, int goodsInBoardID, int productID, String orderNumber, int quantity, String userName, List<Uri> files) {
        Supplier = supplier;
        GoodsInBoardID = goodsInBoardID;
        ProductID = productID;
        OrderNumber = orderNumber;
        Quantity = quantity;
        UserName = userName;
        this.Files = files;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("Supplier")
    public String getSupplier() {
        return Supplier;
    }

    public void setSupplier(String supplier) {
        Supplier = supplier;
    }

    @JsonProperty("GoodsInBoardID")
    public int getGoogsinID() {
        return GoodsInBoardID;
    }

    public void setGoogsinID(int googsinID) {
        GoodsInBoardID = googsinID;
    }

    @JsonProperty("ProductID")
    public int getProductID() {
        return ProductID;
    }

    public void setProductID(int productID) {
        ProductID = productID;
    }

    @JsonProperty("OrderNumber")
    public String getOrderNumber() {
        return OrderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        OrderNumber = orderNumber;
    }

    @JsonProperty("Quantity")
    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    @JsonProperty("UserName")
    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    @JsonProperty("Files")
    public List<Uri> getFiles() {
        return Files;
    }

    public void setFiles(List<Uri> files) {
        Files = files;
    }
}
