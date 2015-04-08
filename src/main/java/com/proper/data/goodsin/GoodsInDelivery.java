package com.proper.data.goodsin;


import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 14/10/2014.
 */
public class GoodsInDelivery implements Serializable {
    private static final long serialVersionUID = 1L;
    private int GoodsInId;
    private Timestamp DateTimeReceived;
    private String Supplier;
    private String Label;
    private int NumberOfPallets;
    private int NumberOfBoxes;
    private String Location;
    private String Courier;
    private String Notes;

    public GoodsInDelivery() {
    }

    public GoodsInDelivery(int goodsInId, Timestamp dateTimeReceived, String supplier, String label, int numberOfPallets, int numberOfBoxes, String location, String courier, String notes) {
        GoodsInId = goodsInId;
        DateTimeReceived = dateTimeReceived;
        Supplier = supplier;
        Label = label;
        NumberOfPallets = numberOfPallets;
        NumberOfBoxes = numberOfBoxes;
        Location = location;
        Courier = courier;
        Notes = notes;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("GoodsInId")
    public int getGoodsInId() {
        return GoodsInId;
    }

    public void setGoodsInId(int goodsInId) {
        GoodsInId = goodsInId;
    }

    @JsonProperty("DateTimeReceived")
    public Timestamp getDateTimeReceived() {
        return DateTimeReceived;
    }

    public void setDateTimeReceived(Timestamp dateTimeReceived) {
        DateTimeReceived = dateTimeReceived;
    }

    @JsonProperty("Supplier")
    public String getSupplier() {
        return Supplier;
    }

    public void setSupplier(String supplier) {
        Supplier = supplier;
    }

    @JsonProperty("Label")
    public String getLabel() {
        return Label;
    }

    public void setLabel(String label) {
        Label = label;
    }

    @JsonProperty("NumberOfPallets")
    public int getNumberOfPallets() {
        return NumberOfPallets;
    }

    public void setNumberOfPallets(int numberOfPallets) {
        NumberOfPallets = numberOfPallets;
    }

    @JsonProperty("NumberOfBoxes")
    public int getNumberOfBoxes() {
        return NumberOfBoxes;
    }

    public void setNumberOfBoxes(int numberOfBoxes) {
        NumberOfBoxes = numberOfBoxes;
    }

    @JsonProperty("Location")
    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    @JsonProperty("Courier")
    public String getCourier() {
        return Courier;
    }

    public void setCourier(String courier) {
        Courier = courier;
    }

    @JsonProperty("Notes")
    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }
}
