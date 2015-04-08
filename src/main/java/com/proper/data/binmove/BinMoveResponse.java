package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 29/05/2014.
 */
public class BinMoveResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String RequestedSrcBin;
    private String RequestedDstBin;
    private String Result;
    private List<BinMoveMessage> Messages;
    private List<BinMoveObject> MessageObjects;

    public BinMoveResponse() {
    }

    public BinMoveResponse(String requestedSrcBin, String requestedDstBin, String result, List<BinMoveMessage> messages, List<BinMoveObject> messageObjects) {
        RequestedSrcBin = requestedSrcBin;
        RequestedDstBin = requestedDstBin;
        Result = result;
        Messages = messages;
        MessageObjects = messageObjects;
    }

    public BinMoveResponse(PartialBinMoveResponse partialBinMoveResponse) {
        RequestedSrcBin = partialBinMoveResponse.getRequestedSrcBin();
        RequestedDstBin = partialBinMoveResponse.getRequestedDstBin();
        Result = "PartialMove";
        Messages = partialBinMoveResponse.getMessages();
        MessageObjects = partialBinMoveResponse.getMessageObjects();
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("RequestedSrcBin")
    public String getRequestedSrcBin() {
        return RequestedSrcBin;
    }

    public void setRequestedSrcBin(String requestedSrcBin) {
        RequestedSrcBin = requestedSrcBin;
    }

    @JsonProperty("RequestedDstBin")
    public String getRequestedDstBin() {
        return RequestedDstBin;
    }

    public void setRequestedDstBin(String requestedDstBin) {
        RequestedDstBin = requestedDstBin;
    }

    @JsonProperty("Result")
    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
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
}
