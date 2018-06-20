package com.dasom.activitytracker;

public class StatItem {
    private String startTime;
    private String endTime;
    private String stats;
    private String location;
    private boolean stay;

    public StatItem(String startTime, String endTime, String stats, String location, boolean stay) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.stats = stats;
        this.location = location;
        this.stay = stay;
    }

    public boolean isStay() {
        return stay;
    }

    public void setStay(boolean stay) {
        this.stay = stay;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStats() {
        return stats;
    }

    public void setStats(String stats) {
        this.stats = stats;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
