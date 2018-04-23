package com.example.customeventview;

import java.util.Calendar;
import java.util.Comparator;

public class EventObject {

    private String id;
    private String name;
    private Calendar startTime;
    private Calendar endTime;
    private String duration;
    private int eventsCount;
    private int leftMargin;
    private int eventWidth;

    public long getDurationInMillSeconds() {
        return getEndTime().getTimeInMillis() - getStartTime().getTimeInMillis();
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        isMarked = marked;
    }

    private boolean isMarked;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public void setEndTime(Calendar endTime) {
        this.endTime = endTime;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getEventsCount() {
        return eventsCount;
    }

    public void setEventsCount(int eventsCount) {
        this.eventsCount = eventsCount;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getEventWidth() {
        return eventWidth;
    }

    public void setEventWidth(int eventWidth) {
        this.eventWidth = eventWidth;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventObject) {
            return ((EventObject) obj).id.equalsIgnoreCase(id);
        }
        return false;
    }
}
