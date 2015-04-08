package com.proper.mail;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 05/11/2014.
 */
public class MailItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private String[] Recepients;
    private String Sender;
    private String Subject;
    private String Message;
    private boolean isHtml;

    public MailItem() {
    }

    public MailItem(String[] recepients, String sender, String subject, String message, boolean isHtml) {
        Recepients = recepients;
        Sender = sender;
        Subject = subject;
        Message = message;
        this.isHtml = isHtml;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("Recepients")
    public String[] getRecepients() {
        return Recepients;
    }

    public void setRecepients(String[] recepients) {
        Recepients = recepients;
    }

    @JsonProperty("Sender")
    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        Sender = sender;
    }

    @JsonProperty("Subject")
    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        Subject = subject;
    }

    @JsonProperty("Message")
    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    @JsonProperty("isHtml")
    public boolean isHtml() {
        return isHtml;
    }

    public void setHtml(boolean isHtml) {
        this.isHtml = isHtml;
    }
}
