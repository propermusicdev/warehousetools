package com.proper.data.replen;

import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lebel on 29/04/2015.
 */
public class ReplenMoveListProcessResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private int RequstedMovelistId;
    private String Result;
    private List<BinMoveMessage> Messages;
    private List<BinMoveObject> MessageObjects;

    public ReplenMoveListProcessResponse() {
    }

    public ReplenMoveListProcessResponse(int requstedMovelistId, String result, List<BinMoveMessage> messages, List<BinMoveObject> messageObjects) {
        RequstedMovelistId = requstedMovelistId;
        Result = result;
        Messages = messages;
        MessageObjects = messageObjects;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("RequstedMovelistId")
    public int getRequstedMovelistId() {
        return RequstedMovelistId;
    }

    public void setRequstedMovelistId(int requstedMovelistId) {
        RequstedMovelistId = requstedMovelistId;
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
