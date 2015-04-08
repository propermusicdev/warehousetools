package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 10/04/2014.
 */
public class Bin implements Serializable {
    private static final long serialVersionUID = 1L;
    private String BinCode;
    private int Qty;

    public Bin() {
    }

    public Bin(String binCode, int qty) {
        BinCode = binCode;
        Qty = qty;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("BinCode")
    public String getBinCode() {
        return BinCode;
    }

    public void setBinCode(String binCode) {
        BinCode = binCode;
    }

    @JsonProperty("Qty")
    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }
}
