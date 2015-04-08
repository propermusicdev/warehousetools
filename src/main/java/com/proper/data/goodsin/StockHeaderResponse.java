package com.proper.data.goodsin;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 15/10/2014.
 */
public class StockHeaderResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private StockHeader StockHeader;
    private List<StockHeaderProduct> Products;

    public StockHeaderResponse() {
    }

    public StockHeaderResponse(com.proper.data.goodsin.StockHeader stockHeader, List<StockHeaderProduct> products) {
        StockHeader = stockHeader;
        Products = products;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("StockHeader")
    public StockHeader getStockHeader() {
        return StockHeader;
    }

    public void setStockHeader(StockHeader stockHeader) {
        StockHeader = stockHeader;
    }

    @JsonProperty("Products")
    public List<StockHeaderProduct> getProducts() {
        return Products;
    }

    public void setProducts(List<StockHeaderProduct> products) {
        Products = products;
    }
}
