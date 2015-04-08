package com.proper.data.binmove;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 11/04/2014.
 */
public class BarcodeInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private String FullArtist;
    private String FullTitle;

    public BarcodeInfo() {
    }

    public BarcodeInfo(String fullArtist, String fullTitle) {
        FullArtist = fullArtist;
        FullTitle = fullTitle;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("FullArtist")
    public String getFullArtist() {
        return FullArtist;
    }

    public void setFullArtist(String fullArtist) {
        FullArtist = fullArtist;
    }

    @JsonProperty("FullTitle")
    public String getFullTitle() {
        return FullTitle;
    }

    public void setFullTitle(String fullTitle) {
        FullTitle = fullTitle;
    }
}
