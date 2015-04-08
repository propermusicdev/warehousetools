package com.proper.data.binmove;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 25/02/14.
 */
public class BinMove implements Serializable {
    private static final long serialVersionUID = 1L;
    private long TransactionId;
    private String SourceBin;
    private String DestinationBin;
    private int Quantity;
    private byte[] MovedItem;
    private Timestamp MovedDate;

    public BinMove() {
    }

    public BinMove(long transactionId, String sourceBin, String destinationBin, int quantity, byte[] movedItem, Timestamp movedDate) {
        TransactionId = transactionId;
        SourceBin = sourceBin;
        DestinationBin = destinationBin;
        Quantity = quantity;
        MovedItem = movedItem;
        MovedDate = movedDate;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(long transactionId) {
        TransactionId = transactionId;
    }

    public String getSourceBin() {
        return SourceBin;
    }

    public void setSourceBin(String sourceBin) {
        SourceBin = sourceBin;
    }

    public String getDestinationBin() {
        return DestinationBin;
    }

    public void setDestinationBin(String destinationBin) {
        DestinationBin = destinationBin;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public byte[] getMovedItem() {
        return MovedItem;
    }

    public void setMovedItem(byte[] movedItem) {
        MovedItem = movedItem;
    }

    public Timestamp getMovedDate() {
        return MovedDate;
    }

    public void setMovedDate(Timestamp movedDate) {
        MovedDate = movedDate;
    }
}