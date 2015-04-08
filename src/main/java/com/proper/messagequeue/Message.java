package com.proper.messagequeue;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 01/04/2014.
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private int MessageId;
    private String Source;
    private String MessageType;
    private int IncomingStatus;
    private String IncomingMessage;
    private int OutgoingStatus;
    private String OutgoingMessage;
    private Timestamp InsertedTimeStamp;
    private int TTL;

    public Message() {
    }

    public Message(int messageId, String source, String messageType, int incomingStatus, String incomingMessage, int outgoingStatus, String outgoingMessage, Timestamp insertedTimeStamp, int TTL) {
        MessageId = messageId;
        Source = source;
        MessageType = messageType;
        IncomingStatus = incomingStatus;
        IncomingMessage = incomingMessage;
        OutgoingStatus = outgoingStatus;
        OutgoingMessage = outgoingMessage;
        InsertedTimeStamp = insertedTimeStamp;
        this.TTL = TTL;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public int getMessageId() {
        return MessageId;
    }

    public void setMessageId(int messageId) {
        MessageId = messageId;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public int getIncomingStatus() {
        return IncomingStatus;
    }

    public void setIncomingStatus(int incomingStatus) {
        IncomingStatus = incomingStatus;
    }

    public String getIncomingMessage() {
        return IncomingMessage;
    }

    public void setIncomingMessage(String incomingMessage) {
        IncomingMessage = incomingMessage;
    }

    public int getOutgoingStatus() {
        return OutgoingStatus;
    }

    public void setOutgoingStatus(int outgoingStatus) {
        OutgoingStatus = outgoingStatus;
    }

    public String getOutgoingMessage() {
        return OutgoingMessage;
    }

    public void setOutgoingMessage(String outgoingMessage) {
        OutgoingMessage = outgoingMessage;
    }

    public Timestamp getInsertedTimeStamp() {
        return InsertedTimeStamp;
    }

    public void setInsertedTimeStamp(Timestamp insertedTimeStamp) {
        InsertedTimeStamp = insertedTimeStamp;
    }

    public int getTTL() {
        return TTL;
    }

    public void setTTL(int TTL) {
        this.TTL = TTL;
    }
}

