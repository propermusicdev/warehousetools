package com.proper.messagequeue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by Lebel on 05/03/14.
 */
public class RabbitMqHelper {
    public Connection getConnectionChannel() throws IOException {
        ConnectionFactory factory = new ConnectionFactory();
        //factory.setHost("pmdtestserver");
        factory.setHost("192.168.10.248");
        Connection connection = factory.newConnection();
        return connection;
    }

    public boolean PublishMessage(QueueDeclaration queue, BasicPublishWrapper pub) {
        Connection con = null;
        Channel channel = null;
        try {
            con = getConnectionChannel();
            channel = con.createChannel();
            channel.queueDeclare(queue.getName(), queue.isDurable(), queue.isExclusive(), queue.isAutoDelete(), queue.getArguments());
            channel.basicPublish(pub.getExchange(), pub.getRoutingKey(), pub.getProperties(), pub.getMessageBody());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (channel != null) {
                    channel.close();
                }
                if (con != null) {
                    con.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return  true;
    }
}

