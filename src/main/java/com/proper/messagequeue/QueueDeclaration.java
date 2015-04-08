package com.proper.messagequeue;

import java.util.Map;

/**
 * Created by Lebel on 05/03/14.
 */
public class QueueDeclaration {
    private String Name;
    private boolean Durable;
    private boolean Exclusive;
    private boolean AutoDelete;
    private Map<String,Object> Arguments;

    public QueueDeclaration(String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String,Object> arguments) {
        Name = name;
        Durable = durable;
        Exclusive = exclusive;
        AutoDelete = autoDelete;
        Arguments = arguments;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public boolean isDurable() {
        return Durable;
    }

    public void setDurable(boolean durable) {
        Durable = durable;
    }

    public boolean isExclusive() {
        return Exclusive;
    }

    public void setExclusive(boolean exclusive) {
        Exclusive = exclusive;
    }

    public boolean isAutoDelete() {
        return AutoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        AutoDelete = autoDelete;
    }

    public Map<String, Object> getArguments() {
        return Arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        Arguments = arguments;
    }
}

