package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 30/05/2014.
 */
public class BinMoveMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String MessageName;
    private String MessageText;
    private Timestamp MessageTimeStamp;

    public BinMoveMessage() {
    }

    public BinMoveMessage(String messageName, String messageText, Timestamp messageTimeStamp) {
        MessageName = messageName;
        MessageText = messageText;
        MessageTimeStamp = messageTimeStamp;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("MessageName")
    public String getMessageName() {
        return MessageName;
    }

    public void setMessageName(String messageName) {
        MessageName = messageName;
    }

    @JsonProperty("MessageText")
    public String getMessageText() {
        return MessageText;
    }

    public void setMessageText(String messageText) {
        MessageText = messageText;
    }

    @JsonProperty("MessageTimeStamp")
    public Timestamp getMessageTimeStamp() {
        return MessageTimeStamp;
    }

    public void setMessageTimeStamp(Timestamp messageTimeStamp) {
        MessageTimeStamp = messageTimeStamp;
    }
}
