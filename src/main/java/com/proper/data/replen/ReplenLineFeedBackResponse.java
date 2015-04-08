package com.proper.data.replen;

import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Knight on 13/01/2015.
 */
public class ReplenLineFeedBackResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private ReplenLineFeedBack NewLine;
    private ReplenLineFeedBack OldLine;
    private List<BinMoveMessage> Messages;
    private List<BinMoveObject> MessageObjects;
    private String Result;

    public ReplenLineFeedBackResponse() {
    }

    public ReplenLineFeedBackResponse(ReplenLineFeedBack newLine, ReplenLineFeedBack oldLine, List<BinMoveMessage> messages, List<BinMoveObject> messageObjects, String result) {
        NewLine = newLine;
        OldLine = oldLine;
        Messages = messages;
        MessageObjects = messageObjects;
        Result = result;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("NewLine")
    public ReplenLineFeedBack getNewLine() {
        return NewLine;
    }

    public void setNewLine(ReplenLineFeedBack newLine) {
        NewLine = newLine;
    }

    @JsonProperty("OldLine")
    public ReplenLineFeedBack getOldLine() {
        return OldLine;
    }

    public void setOldLine(ReplenLineFeedBack oldLine) {
        OldLine = oldLine;
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

    @JsonProperty("NResult")
    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }
}
