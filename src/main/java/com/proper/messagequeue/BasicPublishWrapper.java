package com.proper.messagequeue;

import com.rabbitmq.client.AMQP;

/**
 * Created by Lebel on 05/03/14.
 */
public class BasicPublishWrapper {
    private String Exchange;
    private String RoutingKey;
    private AMQP.BasicProperties Properties;
    private byte[] MessageBody;

    public BasicPublishWrapper(String exchange, String routingKey, AMQP.BasicProperties properties, byte[] messageBody) {
        Exchange = exchange;
        RoutingKey = routingKey;
        Properties = properties;
        MessageBody = messageBody;
    }

    public String getExchange() {
        return Exchange;
    }

    public void setExchange(String exchange) {
        Exchange = exchange;
    }

    public String getRoutingKey() {
        return RoutingKey;
    }

    public void setRoutingKey(String routingKey) {
        RoutingKey = routingKey;
    }

    public AMQP.BasicProperties getProperties() {
        return Properties;
    }

    public void setProperties(AMQP.BasicProperties properties) {
        Properties = properties;
    }

    public byte[] getMessageBody() {
        return MessageBody;
    }

    public void setMessageBody(byte[] messageBody) {
        MessageBody = messageBody;
    }
}

