package com.dasom.activitytracker;
//
public class Location_in {
    private String locationName;
    private String top1APId;
    private int top1rssi;
    private String top2APId;
    private int top2rssi;
    private String top3APId;
    private int top3rssi;
    private boolean isProximate;

    public Location_in(String locationName, String top1APId, int top1rssi, String top2APId, int top2rssi, String top3APId, int top3rssi) {
        this.locationName = locationName;
        this.top1APId = top1APId;
        this.top1rssi = top1rssi;
        this.top2APId = top2APId;
        this.top2rssi = top2rssi;
        this.top3APId = top3APId;
        this.top3rssi = top3rssi;
    }

    public String getlocationName() {
        return locationName;
    }

    public void setlocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getTop1APId() {
        return top1APId;
    }

    public void setTop1APId(String top1APId) {
        this.top1APId = top1APId;
    }

    public int getTop1rssi() {
        return top1rssi;
    }

    public void setTop1rssi(int top1rssi) {
        this.top1rssi = top1rssi;
    }

    public String getTop2APId() {
        return top2APId;
    }

    public void setTop2APId(String top2APId) {
        this.top2APId = top2APId;
    }

    public int getTop2rssi() {
        return top2rssi;
    }

    public void setTop2rssi(int top2rssi) {
        this.top2rssi = top2rssi;
    }

    public String getTop3APId() {
        return top3APId;
    }

    public void setTop3APId(String top3APId) {
        this.top3APId = top3APId;
    }

    public int getTop3rssi() {
        return top3rssi;
    }

    public void setTop3rssi(int top3rssi) {
        this.top3rssi = top3rssi;
    }

    public boolean isProximate() {
        return isProximate;
    }

    public void setProximate(boolean proximate) {
        isProximate = proximate;
    }
}
