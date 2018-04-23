package com.example.customeventview;

import java.util.Comparator;

public class CustomDurationComparator implements Comparator<EventObject> {

    @Override
    public int compare(EventObject o1, EventObject o2) {

        return Long.compare(o2.getDurationInMillSeconds(), o1.getDurationInMillSeconds());
    }
}
