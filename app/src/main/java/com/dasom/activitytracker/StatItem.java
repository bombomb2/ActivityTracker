package com.dasom.activitytracker;

public class StatItem { // RecyclerView에 출력하기 위한 Item 객체
    private String startTime;
    private String endTime;
    private long stats;
    private String location;
    private boolean stay;

    public StatItem(String startTime, String endTime, long stats, String location, boolean stay) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.stats = stats;
        this.location = location;
        this.stay = stay;
    }

    public StatItem() {
        this.startTime = "";
        this.endTime = "";
        this.stats = 0;
        this.location = "";
        this.stay = false;
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

    public long getStats() {
        return stats;
    }

    public void setStats(long stats) {
        this.stats = stats;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
