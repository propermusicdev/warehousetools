package com.proper.data.binchecker;

import com.proper.data.binmove.BinMoveMessage;
import com.proper.data.binmove.BinMoveObject;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by lebel on 13/04/2015.
 */
public class BinCheckerResponse {
        private static final long serialVersionUID = 1L;
        private List<BinMoveMessage> Messages;
        private List<BinMoveObject> MessageObjects;

    public BinCheckerResponse() {
    }

    public BinCheckerResponse(List<BinMoveMessage> messages, List<BinMoveObject> messageObjects) {
        Messages = messages;
        MessageObjects = messageObjects;
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
}
