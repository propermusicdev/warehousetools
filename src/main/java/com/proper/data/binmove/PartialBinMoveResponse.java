package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Lebel on 23/06/2014.
 */
public class PartialBinMoveResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private String RequestedSrcBin;
    private String RequestedDstBin;
    private List<BinMoveMessage> Messages;
    private List<BinMoveObject> MessageObjects;

    public PartialBinMoveResponse() {
    }

    public PartialBinMoveResponse(String requestedSrcBin, String requestedDstBin, List<BinMoveMessage> messages, List<BinMoveObject> messageObjects) {
        RequestedSrcBin = requestedSrcBin;
        RequestedDstBin = requestedDstBin;
        Messages = messages;
        MessageObjects = messageObjects;
    }

    public PartialBinMoveResponse(BinMoveResponse binMoveResponse) {
        RequestedSrcBin = binMoveResponse.getRequestedSrcBin();
        RequestedDstBin = binMoveResponse.getRequestedDstBin();
        Messages = binMoveResponse.getMessages();
        MessageObjects = binMoveResponse.getMessageObjects();
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
