package com.proper.data.updater;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Lebel on 10/11/2014.
 */
public class UpdaterResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private boolean IsAvailable;
    private String currentVersion;
    private String knownFile;
    private Date Signature;

    public UpdaterResponse(String currentVersion) {
        this.currentVersion = currentVersion;

    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public boolean isAvailable() {
        return IsAvailable;
    }

    public void setAvailable(boolean isAvailable) {
        IsAvailable = isAvailable;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(String currentVersion) {
        this.currentVersion = currentVersion;
    }

    public String getKnownFile() {
        return knownFile;
    }

    public void setKnownFile(String knownFile) {
        this.knownFile = knownFile;
    }

    public Date getSignature() {
        return Signature;
    }

    public void setSignature(Date signature) {
        Signature = signature;
    }
}
