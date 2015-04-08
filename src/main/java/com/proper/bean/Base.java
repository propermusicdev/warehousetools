package com.proper.bean;

import java.io.Serializable;

/**
 * Created by Lebel on 21/08/2014.
 */
public abstract class Base implements Serializable {
    private String cacheKey;

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

}
