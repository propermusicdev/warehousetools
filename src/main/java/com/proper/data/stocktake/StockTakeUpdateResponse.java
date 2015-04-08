package com.proper.data.stocktake;

import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Knight on 27/01/2015.
 */
public class StockTakeUpdateResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<BinMoveMessage> Messages;
    private List<BinMoveObject> MessageObjects;
    private String Result;

    public StockTakeUpdateResponse() {
    }

    public StockTakeUpdateResponse(List<BinMoveMessage> messages, List<BinMoveObject> messageObjects, String result) {
        Messages = messages;
        MessageObjects = messageObjects;
        Result = result;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("Messages")
    public List<BinMoveMessage> getMessages() {
        return Messages;
    }

    public void setMessages(List<BinMoveMessage> messages) {
        Messages = messages;
    }

    @JsonProperty("MessageObjects")
    public List<BinMoveObject> getMessageObjects() {
        return MessageObjects;
    }

    public void setMessageObjects(List<BinMoveObject> messageObjects) {
        MessageObjects = messageObjects;
    }

    @JsonProperty("Result")
    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }
}
