package com.example.customeventview;

import java.util.Calendar;

public class EventObject {

    private String id;
    private String name;
    private Calendar startTime;
    private Calendar endTime;
    private int leftMargin;
    private int topMargin;
    private int eventHeight;
    private int X1;
    private int Y1;
    private int X2;
    private int Y2;

    public EventObject() {

    }

    public EventObject(String id, String name, int startHour, int startMinute, int endHour, int endMinute) {
        this.id = id;
        this.name = name;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startCalendar.set(Calendar.MINUTE, startMinute);
        this.startTime = startCalendar;
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        endCalendar.set(Calendar.MINUTE, endMinute);
        this.endTime = endCalendar;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLeftMargin() {
        return leftMargin;
    }

    public void setLeftMargin(int leftMargin) {
        this.leftMargin = leftMargin;
    }

    public int getX1() {
        return X1;
    }

    public void setX1(int x1) {
        X1 = x1;
    }

    public int getY1() {
        return Y1;
    }

    public void setY1(int y1) {
        Y1 = y1;
    }

    public int getX2() {
        return X2;
    }

    public void setX2(int x2) {
        X2 = x2;
    }

    public int getY2() {
        return Y2;
    }

    public void setY2(int y2) {
        Y2 = y2;
    }

    public int getEventHeight() {
        return eventHeight;
    }

    public void setEventHeight(int eventHeight) {
        this.eventHeight = eventHeight;
    }

    public int getTopMargin() {
        return topMargin;
    }

    public void setTopMargin(int topMargin) {
        this.topMargin = topMargin;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EventObject) {
            return ((EventObject) obj).id.equalsIgnoreCase(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
