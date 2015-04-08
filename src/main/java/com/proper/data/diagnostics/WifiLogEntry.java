package com.proper.data.diagnostics;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by Lebel on 13/02/2015.
 */
public class WifiLogEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    private String APName;
    private String BSSID;
    private String OldAPName;
    private int SignalLevel;
    private int Channel;
    private String BinScanned;
    private long PingTime;
    private long DbResponseTime;
    private long EntryTimeStamp;

    public WifiLogEntry() {
    }

    public WifiLogEntry(String APName, String BSSID, String oldAPName, int signalLevel, int channel, String binScanned, long pingTime, long dbResponseTime, long entryTimeStamp) {
        this.APName = APName;
        this.BSSID = BSSID;
        OldAPName = oldAPName;
        SignalLevel = signalLevel;
        Channel = channel;
        BinScanned = binScanned;
        PingTime = pingTime;
        DbResponseTime = dbResponseTime;
        EntryTimeStamp = entryTimeStamp;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("APName")
    public String getAPName() {
        return APName;
    }

    public void setAPName(String APName) {
        this.APName = APName;
    }

    @JsonProperty("BSSID")
    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    @JsonProperty("OldAPName")
    public String getOldAPName() {
        return OldAPName;
    }

    public void setOldAPName(String oldAPName) {
        OldAPName = oldAPName;
    }

    @JsonProperty("SignalLevel")
    public int getSignalLevel() {
        return SignalLevel;
    }

    public void setSignalLevel(int signalLevel) {
        SignalLevel = signalLevel;
    }

    @JsonProperty("Channel")
    public int getChannel() {
        return Channel;
    }

    public void setChannel(int channel) {
        Channel = channel;
    }

    @JsonProperty("BinScanned")
    public String getBinScanned() {
        return BinScanned;
    }

    public void setBinScanned(String binScanned) {
        this.BinScanned = binScanned;
    }

    @JsonProperty("PingTime")
    public long getPingTime() {
        return PingTime;
    }

    public void setPingTime(long pingTime) {
        PingTime = pingTime;
    }

    @JsonProperty("DbResponseTime")
    public long getDbResponseTime() {
        return DbResponseTime;
    }

    public void setDbResponseTime(long dbResponseTime) {
        DbResponseTime = dbResponseTime;
    }

    @JsonProperty("EntryTimeStamp")
    public long getDateTimeStamp() {
        return EntryTimeStamp;
    }

    public void setDateTimeStamp(long dateTimeStamp) {
        this.EntryTimeStamp = dateTimeStamp;
    }
}
