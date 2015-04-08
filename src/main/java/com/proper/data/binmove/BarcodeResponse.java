package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 11/04/2014.
 */
public class BarcodeResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<ProductResponse> Products;

    public BarcodeResponse() {
    }

    public BarcodeResponse(List<ProductResponse> products) {
        Products = products;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("Products")
    public List<ProductResponse> getProducts() {
        return Products;
    }

    public void setProducts(List<ProductResponse> products) {
        Products = products;
    }
}
