package com.proper.security;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 03/04/2014.
 */
public class TransactionHistory implements Serializable {
    private static final long serialVersionUID = 1L;
    private long historyId;
    private int messageId;
    private String deviceIMEI;
    private Timestamp moveDate;
    private boolean Resolved;

    public TransactionHistory() {
    }

    public TransactionHistory(long historyId, int messageId, String deviceIMEI, Timestamp moveDate, boolean resolved) {
        this.historyId = historyId;
        this.messageId = messageId;
        this.deviceIMEI = deviceIMEI;
        this.moveDate = moveDate;
        Resolved = resolved;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getHistoryId() {
        return historyId;
    }

    public void setHistoryId(long historyId) {
        this.historyId = historyId;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getDeviceIMEI() {
        return deviceIMEI;
    }

    public void setDeviceIMEI(String deviceIMEI) {
        this.deviceIMEI = deviceIMEI;
    }

    public Timestamp getMoveDate() {
        return moveDate;
    }

    public void setMoveDate(Timestamp moveDate) {
        this.moveDate = moveDate;
    }

    public boolean isResolved() {
        return Resolved;
    }

    public void setResolved(boolean resolved) {
        Resolved = resolved;
    }
}
