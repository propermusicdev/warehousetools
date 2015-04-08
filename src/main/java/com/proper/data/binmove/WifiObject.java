package com.proper.data.binmove;

import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import org.codehaus.jackson.annotate.JsonProperty;

import java.io.Serializable;

/**
 * Created by Lebel on 14/08/2014.
 */
public class WifiObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private String BSSID;
    private static NetworkInfo.DetailedState DetailState;
    private boolean HiddenSSID;
    private int IpAddress;
    private int LinkSpeed;
    private String MacAddress;
    private int NetworkId;
    private int RSSI;
    private String SSID;
    private SupplicantState SupplicantState;
    private String Capabilities;
    private int Frequency;
    private int Level;
    private long Timestamp;

    public WifiObject() {
    }

    public WifiObject(String BSSID, boolean hiddenSSID, int ipAddress, int linkSpeed, String macAddress, int networkId, int RSSI, String SSID, android.net.wifi.SupplicantState supplicantState, String capabilities, int frequency, int level, long timestamp) {
        this.BSSID = BSSID;
        HiddenSSID = hiddenSSID;
        IpAddress = ipAddress;
        LinkSpeed = linkSpeed;
        MacAddress = macAddress;
        NetworkId = networkId;
        this.RSSI = RSSI;
        this.SSID = SSID;
        SupplicantState = supplicantState;
        Capabilities = capabilities;
        Frequency = frequency;
        Level = level;
        Timestamp = timestamp;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    @JsonProperty("BSSID")
    public String getBSSID() {
        return BSSID;
    }

    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }

    @JsonProperty("DetailState")
    public static NetworkInfo.DetailedState getDetailState() {
        return DetailState;
    }

    public static void setDetailState(NetworkInfo.DetailedState detailState) {
        DetailState = detailState;
    }

    @JsonProperty("HiddenSSID")
    public boolean isHiddenSSID() {
        return HiddenSSID;
    }

    public void setHiddenSSID(boolean hiddenSSID) {
        HiddenSSID = hiddenSSID;
    }

    @JsonProperty("IpAddress")
    public int getIpAddress() {
        return IpAddress;
    }

    public void setIpAddress(int ipAddress) {
        IpAddress = ipAddress;
    }

    @JsonProperty("LinkSpeed")
    public int getLinkSpeed() {
        return LinkSpeed;
    }

    public void setLinkSpeed(int linkSpeed) {
        LinkSpeed = linkSpeed;
    }

    @JsonProperty("MacAddress")
    public String getMacAddress() {
        return MacAddress;
    }

    public void setMacAddress(String macAddress) {
        MacAddress = macAddress;
    }

    @JsonProperty("NetworkId")
    public int getNetworkId() {
        return NetworkId;
    }

    public void setNetworkId(int networkId) {
        NetworkId = networkId;
    }

    @JsonProperty("RSSI")
    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    @JsonProperty("SSID")
    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    @JsonProperty("SupplicantState")
    public SupplicantState getSupplicantState() {
        return SupplicantState;
    }

    public void setSupplicantState(SupplicantState supplicantState) {
        SupplicantState = supplicantState;
    }

    @JsonProperty("Capabilities")
    public String getCapabilities() {
        return Capabilities;
    }

    public void setCapabilities(String capabilities) {
        Capabilities = capabilities;
    }

    @JsonProperty("Frequency")
    public int getFrequency() {
        return Frequency;
    }

    public void setFrequency(int frequency) {
        Frequency = frequency;
    }

    @JsonProperty("Level")
    public int getLevel() {
        return Level;
    }

    public void setLevel(int level) {
        Level = level;
    }

    @JsonProperty("Timestamp")
    public long getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(long timestamp) {
        Timestamp = timestamp;
    }
}
