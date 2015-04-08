package com.proper.data.replen;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 08/09/2014.
 */
public class ReplenMiniMove implements Serializable {
    private static final long serialVersionUID = 1L;
    private String Destination;
    private int Quantity;

    public ReplenMiniMove() {
    }

    public ReplenMiniMove(String detination, int quantity) {
        Destination = detination;
        Quantity = quantity;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("Destination")
    public String getDestination() {
        return Destination;
    }

    public void setDestination(String destination) {
        Destination = destination;
    }

    @JsonProperty("Quantity")
    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }
}
