package com.example.customeventview;


import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarDayViewFragment extends DialogFragment implements View.OnLongClickListener, View.OnDragListener, View.OnClickListener {

    public static final String TAG = "CalendarDayViewFragment";
    private String[] dayHourTimes = {"12 AM", "1 AM", "2 AM", "3 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM", "10 AM", "11 AM", "12 PM", "1 PM", "2 PM", "3 PM",
            "4 PM", "5 PM", "6 PM", "7 PM", "8 PM", "9 PM", "10 PM", "11 PM"};
    private List<EventObject> eventsList = new ArrayList<>();

    private LinearLayout hoursLinearLayout;
    private RelativeLayout eventsView;
    private ScrollView scrollView;

    private static final float HOUR_MARGIN_LEFT = 12;
    private static final float HOUR_MARGIN_RIGHT = 5;
    private static final float HOUR_VIEW_HEIGHT = 63;
    private static final float DIVIDER_LINE_MARGIN_LEFT = 45;
    private static final float DIVIDER_LINE_MARGIN_TOP = 8;
    private static final float EVENT_GAP = 1;
    private static final int MAX_EVENTS_SIZE = 3;
    private Random rand = new Random();
    private static int MAX_RANDOM_VALUE_LIMIT = 1000;
    private static final int STANDARD_EVENT_TEXT_SIZE = 12;
    private static final int ADDITIONAL_EVENT_TEXT_SIZE = 18;
    private static final String ADDITIONAL_EVENT_ID_SEPARATOR = ":";
    public boolean showNowLine = true;


    public static CalendarDayViewFragment newInstance() {

        Bundle args = new Bundle();

        CalendarDayViewFragment fragment = new CalendarDayViewFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar_day_view, container, false);

        scrollView = view.findViewById(R.id.scroll_view);
        hoursLinearLayout = view.findViewById(R.id.hours_linear_layout);
        eventsView = view.findViewById(R.id.events_view);

        RelativeLayout.LayoutParams eventsViewParams = (RelativeLayout.LayoutParams) eventsView.getLayoutParams();
        int eventsViewLeftMargin = (int) convertDpToPixel(DIVIDER_LINE_MARGIN_LEFT + HOUR_MARGIN_LEFT, getContext());
        int eventsViewRightMargin = (int) convertDpToPixel(HOUR_MARGIN_RIGHT, getContext());
        eventsViewParams.setMargins(eventsViewLeftMargin, 0, eventsViewRightMargin, 0);
        eventsView.setLayoutParams(eventsViewParams);
        eventsView.setOnDragListener(this);

        int hourHeight = (int) convertDpToPixel(HOUR_VIEW_HEIGHT, getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, hourHeight);
        int leftMargin = (int) convertDpToPixel(HOUR_MARGIN_LEFT, getContext());
        int rightMargin = (int) convertDpToPixel(HOUR_MARGIN_RIGHT, getContext());
        layoutParams.setMargins(leftMargin, 0, rightMargin, 0);

        for (int i = 0; i < dayHourTimes.length; i++) {
            View hourLayout = inflater.inflate(R.layout.hour_layout, null, false);
            TextView textView = hourLayout.findViewById(R.id.hour_text);
            textView.setText(dayHourTimes[i]);
            hourLayout.setLayoutParams(layoutParams);
            hoursLinearLayout.addView(hourLayout);
        }

        setCalendarEvents();

//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                handler.removeCallbacks(this);
//
//                //setting the events
//                for (int j = 0; j < dayHourTimes.length; j++) {
//                    int parentWidth = eventsView.getWidth();
//                    List<EventObject> filteredEvents = getCalendarEvents(j);
//                    int size = filteredEvents.size();
//                    int eventWidth;
//                    if (size > 0) {
//                        //send end time to compare with start time
//                        EventObject filteredObject = filteredEvents.get(size - 1);
//                        if (!filteredObject.isMarked()) {
//                            List<EventObject> allOverlappingEvents = checkEndTimeEvents(filteredEvents, parentWidth, j);
//                            int endTimeEventsSize = allOverlappingEvents.size();
//                            eventWidth = parentWidth / endTimeEventsSize; //Calculating the width based on events
//                            showLog("Total event count:::::" + (endTimeEventsSize));
//
//                            int calculatedWidth = startTimeCalculation(filteredEvents);
//                            if (calculatedWidth > 0) {
//                                parentWidth = parentWidth - calculatedWidth;
//                                eventWidth = parentWidth / endTimeEventsSize;
//                                //adjusting the left margin again. Since there is a difference in width.
//                                int count = 0;
//                                int avgWidth = eventWidth;
//                                for (EventObject tempObj : allOverlappingEvents) {
//                                    tempObj.setLeftMargin(count * avgWidth);
//                                    count++;
//                                }
//                            }
//                            if (allOverlappingEvents.size() > MAX_EVENTS_SIZE) {
//                                Collections.sort(allOverlappingEvents, new CustomDurationComparator());
//                                float maxPercent = 85.0f;
//                                float minPercent = 15.0f;
//                                float totalPercent = 100.0f;
//                                int threeEventsWidth = (int) (parentWidth * (maxPercent / totalPercent));
//                                showLog("Three events width:::::" + threeEventsWidth);
//                                int additionalEventsWidth = (int) (parentWidth * (minPercent / totalPercent));
//                                showLog("Additional Events width:::::" + additionalEventsWidth);
//                                List<EventObject> subList = allOverlappingEvents.subList(0, MAX_EVENTS_SIZE);
//                                //adjusting the left margin again. Since there is a difference in width.
//                                //for the first three events
//                                int count = 0;
//                                eventWidth = threeEventsWidth / MAX_EVENTS_SIZE;
//                                int avgWidth = eventWidth;
//                                for (EventObject tempObj : subList) {
//                                    tempObj.setLeftMargin(count * avgWidth);
//                                    count++;
//                                }
//                                //draw first three events
//                                drawOverLappingEvents(subList, eventWidth, Gravity.NO_GRAVITY, STANDARD_EVENT_TEXT_SIZE);
//                                //additional event overlay
//                                List<EventObject> additionalList = allOverlappingEvents.subList(MAX_EVENTS_SIZE, allOverlappingEvents.size());
//                                //creating additional event object overlay
//                                List<EventObject> tempList = new ArrayList<>();
//                                tempList.add(getAdditionalEventsObject(j, threeEventsWidth, additionalList));
//                                //draw additional event
//                                drawOverLappingEvents(tempList, additionalEventsWidth, Gravity.CENTER_HORIZONTAL, ADDITIONAL_EVENT_TEXT_SIZE);
//                            } else {
//                                //if size <=3 then draw the events
//                                drawOverLappingEvents(allOverlappingEvents, eventWidth, Gravity.NO_GRAVITY, STANDARD_EVENT_TEXT_SIZE);
//                            }
//                        }
//                    }
//                }
//                if (showNowLine) {
//                    View nowLineView = new View(getContext());
//                    nowLineView.setBackgroundColor(Color.MAGENTA);
//                    RelativeLayout.LayoutParams viewParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
//                            (int) convertDpToPixel(1, getContext()));
//                    Calendar nowCalendar = Calendar.getInstance();
//                    int currentHour = nowCalendar.get(Calendar.HOUR_OF_DAY);
//                    int currentMinutes = nowCalendar.get(Calendar.MINUTE);
//                    int top = (int) convertDpToPixel((HOUR_VIEW_HEIGHT * currentHour) + currentMinutes + DIVIDER_LINE_MARGIN_TOP, getContext());
//                    viewParams.setMargins(0, top, 0, 0);
//                    nowLineView.setLayoutParams(viewParams);
//                    eventsView.addView(nowLineView);
//                }
//            }
//        }, 100);
        return view;
    }

    /**
     * Overlays all the events and its dependent child events
     *
     * @param allOverlappingEvents
     * @param eventWidth
     * @param textGravity
     * @param textSize
     */
    private void drawOverLappingEvents(List<EventObject> allOverlappingEvents, int eventWidth, int textGravity, int textSize) {
        int size = allOverlappingEvents.size();
        for (int k = 0; k < size; k++) {
            EventObject eventObject = allOverlappingEvents.get(k);
            int[] startTime = getStartTime(eventObject.getStartTime());
            int[] endTime = getEndTime(eventObject.getEndTime());
            //0 -> hour & 1 -> minute
            int eventHeight = (int) ((HOUR_VIEW_HEIGHT * endTime[0]) + endTime[1] + DIVIDER_LINE_MARGIN_TOP)
                    - (int) ((HOUR_VIEW_HEIGHT * startTime[0]) + startTime[1] + DIVIDER_LINE_MARGIN_TOP);
            //No size conversion, pass direct pixels for width
            RelativeLayout.LayoutParams textViewParams = new RelativeLayout.LayoutParams((int) (eventWidth - EVENT_GAP),
                    (int) convertDpToPixel(eventHeight, getContext()));
            int top = (int) convertDpToPixel((HOUR_VIEW_HEIGHT * startTime[0]) + startTime[1] + DIVIDER_LINE_MARGIN_TOP, getContext());
            int left = eventObject.getLeftMargin();
            textViewParams.setMargins(left, top, 0, 0);
            eventsView.addView(getTextView(textViewParams, eventObject, ContextCompat.getColor(getContext(), R.color.colorPrimary), textGravity, textSize));
        }
    }

    private TextView getTextView(RelativeLayout.LayoutParams textViewParams, EventObject eventObject, int eventColor, int textGravity, int textSize) {
        TextView textView = new TextView(getContext());
        textView.setId(rand.nextInt(MAX_RANDOM_VALUE_LIMIT));
        textView.setLayoutParams(textViewParams);
        textView.setText(eventObject.getName());
        textView.setTextColor(Color.WHITE);
//        textView.setBackgroundColor(Color.BLACK);
        textView.setBackgroundResource(R.drawable.event_bg);
        GradientDrawable drawable = (GradientDrawable) textView.getBackground();
        drawable.setColor(eventColor);
        textView.setTextSize(textSize);
        int leftPadding = (int) convertDpToPixel(5, getContext());
        int rightPadding = (int) convertDpToPixel(5, getContext());
        textView.setPadding(leftPadding, 0, rightPadding, 0);
        textView.setGravity(textGravity);
        textView.setTag(eventObject.getId());
//        textView.setAlpha((float) 0.5);
        textView.setOnLongClickListener(this);
        textView.setOnClickListener(this);
        return textView;
    }

    private List<EventObject> getCalendarEvents(int startHour) {
        List<EventObject> filteredEvents = new ArrayList<>();
        for (EventObject object : eventsList) {
            Calendar startCalendar = object.getStartTime();
            int startHourOfDay = startCalendar.get(Calendar.HOUR_OF_DAY);
            if (startHourOfDay == startHour) {
                filteredEvents.add(object);
            }
        }
        return filteredEvents;
    }

    private synchronized List<EventObject> checkEndTimeEvents(List<EventObject> filteredEvents, int totalWidth, int hourLineValue) {
        List<EventObject> finalList = new ArrayList<>();
        HashSet<EventObject> mainList = new HashSet<>();
//        mainList.addAll(filteredEvents);//adding all the events. For multiple events from same hour line.
        Set<String> eventIds = new TreeSet<>();

        for (EventObject eventObject : filteredEvents) {//considering multiple events on same hour line
            eventIds.add(eventObject.getId());

            int startHour = eventObject.getStartTime().get(Calendar.HOUR_OF_DAY);
            int startMinute = eventObject.getStartTime().get(Calendar.MINUTE);
            int endHour = eventObject.getEndTime().get(Calendar.HOUR_OF_DAY);
            int endMinute = eventObject.getEndTime().get(Calendar.MINUTE);
            showLog("Main event time::Start Hour: " + startHour + "::End Hour: " + endHour);
            for (EventObject object : eventsList) {
                Calendar startTimeCalendar = object.getStartTime();
                Calendar endTimeCalendar = object.getEndTime();

                if (!object.isMarked() && checkTimeRange(startTimeCalendar, endTimeCalendar, eventObject.getEndTime()) && !object.getId().equalsIgnoreCase(eventObject.getId())) {
                    eventIds.add(object.getId());
                    showLog("Event ID" + object.getId() + "::Start Hour: " + startTimeCalendar.get(Calendar.HOUR_OF_DAY) + "::End Hour: " + endTimeCalendar.get(Calendar.HOUR_OF_DAY));
                    List<EventObject> eventObjectList = checkAdditionalEndTimeDependents(object, eventIds);
                    showLog("Dependents size:::::" + eventObjectList.size());
                    mainList.addAll(eventObjectList);
                    mainList.add(object);
                } else if (!object.isMarked() && endTimeGreaterThanEventStartTime(eventObject.getEndTime(), startTimeCalendar) && !object.getId().equalsIgnoreCase(eventObject.getId())) {
                    eventIds.add(object.getId());
                    List<EventObject> eventObjectList = checkAdditionalEndTimeDependents(object, eventIds);
                    mainList.addAll(eventObjectList);
                    mainList.add(object);
                }
            }
        }

        showLog("Total size:::::" + mainList.size());
        showLog("========================================");
        //Events whose width is already calculated, there width size has to be marked!
        int count = 0;
        int leftMargin = totalWidth / mainList.size();
        for (EventObject obj : eventsList) {
            for (EventObject mainListObject : mainList) {
                if (obj.getId().equalsIgnoreCase(mainListObject.getId())) {
                    obj.setMarked(true);
                    obj.setEventsCount(mainList.size());
                    obj.setLeftMargin(count * leftMargin);
                    obj.setEventWidth(leftMargin);
                    count++;
                }
            }
        }
        finalList.addAll(mainList);
        return finalList;
    }

    private synchronized List<EventObject> checkAdditionalEndTimeDependents(EventObject eventObject, Set<String> eventIds) {
        List<EventObject> eventObjectList = new ArrayList<>();
        int startHour = eventObject.getStartTime().get(Calendar.HOUR_OF_DAY);
        int startMinute = eventObject.getStartTime().get(Calendar.MINUTE);
        int endHour = eventObject.getEndTime().get(Calendar.HOUR_OF_DAY);
        int endMinute = eventObject.getEndTime().get(Calendar.MINUTE);
        for (EventObject object : eventsList) {
            Calendar startTimeCalendar = object.getStartTime();
            Calendar endTimeCalendar = object.getEndTime();

            if (startHour == startTimeCalendar.get(Calendar.HOUR_OF_DAY)) {
                //do nothing
            } else if (!eventIds.contains(object.getId()) &&
                    checkTimeRange(startTimeCalendar, endTimeCalendar, eventObject.getEndTime()) ||
                    endHour == (endTimeCalendar.get(Calendar.HOUR_OF_DAY))) {
                eventObjectList.add(object);
            }
        }
        return eventObjectList;
    }

    private boolean checkTimeRange(Calendar startTime, Calendar endTime, Calendar currentTime) {
        boolean status = false;
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
        int currentMinute = currentTime.get(Calendar.MINUTE);

        int startHour = startTime.get(Calendar.HOUR_OF_DAY);
        int startMinute = startTime.get(Calendar.MINUTE);

        int endHour = endTime.get(Calendar.HOUR_OF_DAY);
        int endMinute = endTime.get(Calendar.MINUTE);

        Date currentDateTime = parseDate(currentHour + ":" + currentMinute);
        Date startDateTime = parseDate(startHour + ":" + startMinute);
        Date endDateTime = parseDate(endHour + ":" + endMinute);

        if (currentDateTime.before(endDateTime) && currentDateTime.after(startDateTime)) {
            //your logic
            status = true;
        }
        return status;
    }

    private boolean endTimeGreaterThanEventStartTime(Calendar filteredEventEndTime, Calendar allEventCurrentTime) {
        boolean status = false;
        int startHour = allEventCurrentTime.get(Calendar.HOUR_OF_DAY); // Get hour in 24 hour format
        int startMinute = allEventCurrentTime.get(Calendar.MINUTE);

        int endHour = filteredEventEndTime.get(Calendar.HOUR_OF_DAY);
        int endMinute = filteredEventEndTime.get(Calendar.MINUTE);

        Date startEventDateTime = parseDate(startHour + ":" + startMinute);
        Date filteredEventDateTime = parseDate(endHour + ":" + endMinute);

        if (filteredEventDateTime.after(startEventDateTime)) {
            //your logic
            status = true;
        }
        return status;
    }

    private int startTimeCalculation(List<EventObject> filteredEvents) {
        HashSet<EventObject> mainList = new HashSet<>();
        int calculatedWidth = 0;
        for (EventObject eventObject : filteredEvents) {//considering multiple events on same hour line
            for (EventObject object : eventsList) {
                if (!filteredEvents.contains(object) && !object.isMarked()) {
                    Calendar startTimeCalendar = object.getStartTime();
                    Calendar endTimeCalendar = object.getEndTime();
                    if (checkTimeRange(startTimeCalendar, endTimeCalendar, eventObject.getStartTime())) {
                        mainList.add(object);
                    }
                }
            }
        }

        for (EventObject mainListObject : mainList) {
            calculatedWidth = calculatedWidth + mainListObject.getEventWidth();
        }
        return calculatedWidth;
    }

    private Date parseDate(String date) {

        final String inputFormat = "HH:mm";
        SimpleDateFormat inputParser = new SimpleDateFormat(inputFormat, Locale.US);
        try {
            return inputParser.parse(date);
        } catch (java.text.ParseException e) {
            return new Date(0);
        }
    }

    private int[] getStartTime(Calendar startCalendar) {
        int startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
        int startMinute = startCalendar.get(Calendar.MINUTE);
        int[] startTime = new int[]{startHour, startMinute};
        return startTime;
    }

    private int[] getEndTime(Calendar endCalendar) {
        int endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
        int endMinute = endCalendar.get(Calendar.MINUTE);
        int[] endTime = new int[]{endHour, endMinute};
        return endTime;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    @Override
    public boolean onLongClick(View view) {
        // Create a new ClipData.
        // This is done in two steps to provide clarity. The convenience method
        // ClipData.newPlainText() can create a plain text ClipData in one step.

        // Create a new ClipData.Item from the ImageView object's tag
        ClipData.Item item = new ClipData.Item((CharSequence) view.getTag());

        // Create a new ClipData using the tag as a label, the plain text MIME type, and
        // the already-created item. This will create a new ClipDescription object within the
        // ClipData, and set its MIME type entry to "text/plain"
        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

        ClipData data = new ClipData(view.getTag().toString(), mimeTypes, item);

        // Instantiates the drag shadow builder.
        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        // Starts the drag
        view.startDrag(data//data to be dragged
                , shadowBuilder //drag shadow
                , view//local data about the drag and drop operation
                , 0//no needed flags
        );

        //Set view visibility to INVISIBLE as we are going to drag the view
        view.setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public boolean onDrag(View view, DragEvent event) {
        // Defines a variable to store the action type for the incoming event
        int action = event.getAction();
        // Handles each of the expected events
        switch (action) {
            case DragEvent.ACTION_DRAG_STARTED:
                // Determines if this View can accept the dragged data
                if (event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
                    // if you want to apply color when drag started to your view you can uncomment below lines
                    // to give any color tint to the View to indicate that it can accept
                    // data.

                    // returns true to indicate that the View can accept the dragged data.
                    return true;

                }

                // Returns false. During the current drag and drop operation, this View will
                // not receive events again until ACTION_DRAG_ENDED is sent.
                return false;

            case DragEvent.ACTION_DRAG_ENTERED:
                // Return true; the return value is ignored.
                return true;
            case DragEvent.ACTION_DRAG_LOCATION:
                //no action necessary
                int y = Math.round(view.getY()) + Math.round(event.getY());
                int translatedY = y - scrollView.getScrollY();
                Log.i("translated", "" + translatedY + " " + scrollView.getScrollY() + " " + y);
                int threshold = 50;
                // make a scrolling up due the y has passed the threshold
                if (translatedY < 200) {
                    // make a scroll up by 30 px
                    scrollView.smoothScrollBy(0, -15);
                }
                // make a autoscrolling down due y has passed the 500 px border
                if (translatedY + threshold > scrollView.getHeight() - 200) {
                    // make a scroll down by 30 px
                    scrollView.smoothScrollBy(0, 15);
                }

                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                // Ignore the event
                return true;
            case DragEvent.ACTION_DROP:
                // Gets the item containing the dragged data
                ClipData.Item item = event.getClipData().getItemAt(0);

                // Gets the text data from the item.
                String dragData = item.getText().toString();

                // Displays a message containing the dragged data.
                Toast.makeText(getContext(), "Dragged data is " + dragData, Toast.LENGTH_SHORT).show();

                float X = event.getX();
                float Y = event.getY();

                View textView = (View) event.getLocalState();
                RelativeLayout.LayoutParams textViewParams = (RelativeLayout.LayoutParams) textView.getLayoutParams();
                int top = (int) Y - (textView.getHeight() / 2);
                int left = (int) X - (textView.getWidth() / 2);
                textViewParams.setMargins(left, top, 0, 0);
                textView.setLayoutParams(textViewParams);
                textView.setVisibility(View.VISIBLE);//finally set Visibility to VISIBLE

                // Returns true. DragEvent.getResult() will return true.
                return true;
            case DragEvent.ACTION_DRAG_ENDED:
                // Does a getResult(), and displays what happened.
                if (event.getResult()) {
                    Toast.makeText(getContext(), "The drop was handled.", Toast.LENGTH_SHORT).show();
                } else {
                    View restoreView = (View) event.getLocalState();
                    restoreView.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "The drop didn't work.", Toast.LENGTH_SHORT).show();
                }
                // returns true; the value is ignored.
                return true;

            // An unknown action type was received.
            default:
                Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                break;
        }
        return false;
    }

    private void showLog(String message) {
        Log.i("Events Info", message);
    }

    /**
     * Creates the additional event object (...)
     *
     * @param hourLine
     * @param threeEventsWidth
     * @param additionalList
     * @return
     */
    private EventObject getAdditionalEventsObject(int hourLine, int threeEventsWidth, List<EventObject> additionalList) {
        //creating additional event object overlay
        EventObject additionalEvenObject = new EventObject();
        additionalEvenObject.setLeftMargin((int) (threeEventsWidth - (1 * EVENT_GAP)));
        additionalEvenObject.setName("...");
        StringBuilder sb = new StringBuilder();
        for (EventObject obj : additionalList) {
            sb.append(obj.getId());
            sb.append(ADDITIONAL_EVENT_ID_SEPARATOR);
        }
        additionalEvenObject.setId(sb.toString().substring(0, sb.toString().length() - 1));

        Calendar startCalendar = additionalList.get(3).getStartTime();
        additionalEvenObject.setStartTime(startCalendar);

        Calendar endCalendar = Calendar.getInstance();//default height to 30 mins
        endCalendar.set(Calendar.HOUR_OF_DAY, startCalendar.get(Calendar.HOUR_OF_DAY));
        endCalendar.set(Calendar.MINUTE, startCalendar.get(Calendar.MINUTE));
        endCalendar.add(Calendar.MINUTE, 30);
        additionalEvenObject.setEndTime(endCalendar);

        return additionalEvenObject;
    }

    private void setCalendarEvents() {
//        //event 1
//        EventObject eventObject = new EventObject("1", "2:30AM to 4AM event", 2, 30, 4, 0);
//        eventsList.add(eventObject);
//        //event 2
//        eventObject = new EventObject("2", "2:30AM to 4AM event", 2, 30, 4, 0);
//        eventsList.add(eventObject);
//        //event 3
//        eventObject = new EventObject("3", "3AM to 6AM event", 3, 0, 6, 0);
//        eventsList.add(eventObject);
//        //event 4
//        eventObject = new EventObject("4", "2:30AM to 6AM event",2, 30, 6, 0);
//        eventsList.add(eventObject);
//        //event 5
//        eventObject = new EventObject("5", "1AM to 4AM event", 1, 0, 4, 0);
//        eventsList.add(eventObject);
//        //event 6
//        eventObject = new EventObject("6", "5AM to 6AM event", 5, 0, 6, 0);
//        eventsList.add(eventObject);
//        //event 7
//        eventObject = new EventObject("7", "6AM to 7AM event", 6, 0, 7, 0);
//        eventsList.add(eventObject);
//        //event 8
//        eventObject = new EventObject("8", "7AM to 8AM event", 7, 0, 8, 0);
//        eventsList.add(eventObject);
//        //event 9
//        eventObject = new EventObject("9", "7.30AM to 8.30AM event", 7, 30, 8, 30);
//        eventsList.add(eventObject);
//        //event 10
//        eventObject = new EventObject("10", "8AM to 9AM event",8,0,9,0);
//        eventsList.add(eventObject);
//        //event 11
//        eventObject = new EventObject("11", "7AM to 11AM event", 7,0,11,0);
//        eventsList.add(eventObject);

//        //event 12
//        eventObject = new EventObject();
//        eventObject.setId("12");
//        eventObject.setName("1AM to 11PM event");
//
//        startCalendar = Calendar.getInstance();
//        startCalendar.set(Calendar.HOUR_OF_DAY, 1);
//        startCalendar.set(Calendar.MINUTE, 0);
//        eventObject.setStartTime(startCalendar);
//
//        endCalendar = Calendar.getInstance();
//        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
//        endCalendar.set(Calendar.MINUTE, 0);
//        eventObject.setEndTime(endCalendar);
//
//        eventsList.add(eventObject);

//        //event 13
//        eventObject = new EventObject("13", "6AM to 7AM event",6,0,7,0);
//        eventsList.add(eventObject);
//        //event 14
//        eventObject = new EventObject("14", "6AM to 7AM event",6,0,7,0);
//        eventsList.add(eventObject);
//        //event 15
//        eventObject = new EventObject("15", "6AM to 7AM event", 6,0,7,0);
//        eventsList.add(eventObject);

//        //event 16
//        eventObject = new EventObject();
//        eventObject.setId("16");
//        eventObject.setName("12AM to 10PM event");
//
//        startCalendar = Calendar.getInstance();
//        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
//        startCalendar.set(Calendar.MINUTE, 0);
//        eventObject.setStartTime(startCalendar);
//
//        endCalendar = Calendar.getInstance();
//        endCalendar.set(Calendar.HOUR_OF_DAY, 22);
//        endCalendar.set(Calendar.MINUTE, 0);
//        eventObject.setEndTime(endCalendar);
//
//        eventsList.add(eventObject);

//        //event 17
//        eventObject = new EventObject("17", "5.30AM to 6.30PM event",5,30, 6,30);
//        eventsList.add(eventObject);
//        //event 18
//        eventObject = new EventObject("18", "9AM to 11AM event",9,0,11,0);
//        eventsList.add(eventObject);
//        //event 19
//        eventObject = new EventObject("19", "10AM to 11AM event",10, 0,11,0);
//        eventsList.add(eventObject);
//        //event 20
//        eventObject = new EventObject("20", "6.30AM to 8AM event", 6,30,8,0);
//        eventsList.add(eventObject);
//        //event 21
//        eventObject = new EventObject("21", "1PM to 2PM event",13,0,14,0);
//        eventsList.add(eventObject);
//        //event 22
//        eventObject = new EventObject("22", "2PM to 2:30PM event", 14,0,14,30);
//        eventsList.add(eventObject);

        ///////////////////////////////////////////////////////////////////////////
        //event 1
        EventObject eventObject = new EventObject("1", "8:15AM to 8:30AM event", 8, 15, 8, 30);
        eventsList.add(eventObject);
        //event 10
        eventObject = new EventObject("10", "11:00AM to 12:00PM event", 11, 0, 13, 0);
        eventsList.add(eventObject);
        //event 4
        eventObject = new EventObject("4", "9:42AM to 10:42AM event", 9, 42, 10, 42);
        eventsList.add(eventObject);
        //event 6
        eventObject = new EventObject("6", "10:15AM to 10:45AM event", 10, 15, 10, 45);
        eventsList.add(eventObject);
//        //event 8
//        eventObject = new EventObject("8", "10:30AM to 11:30AM event", 10, 30, 11, 30);
//        eventsList.add(eventObject);
//        //event 9
//        eventObject = new EventObject("9", "10:30AM to 11:30AM event", 10, 30, 11, 30);
//        eventsList.add(eventObject);
        //event 2
        eventObject = new EventObject("2", "8:30AM to 9:00AM event", 8, 30, 9, 0);
        eventsList.add(eventObject);
        //event 5
        eventObject = new EventObject("5", "10:00AM to 11:00AM event", 10, 0, 11, 0);
        eventsList.add(eventObject);
        //event 7
        eventObject = new EventObject("7", "10:30AM to 11:30AM event", 10, 30, 11, 30);
        eventsList.add(eventObject);
        //event 3
        eventObject = new EventObject("3", "8:53AM to 9:53AM event", 8, 53, 9, 53);
        eventsList.add(eventObject);


        Collections.sort(eventsList, new Comparator<EventObject>() {
            public int compare(EventObject o1, EventObject o2) {
                return o1.getStartTime().compareTo(o2.getStartTime());
            }
        });
        for (EventObject object : eventsList) {
            showLog("Event id: " + object.getId());
            showLog("Event start time: " + object.getName());
            showLog("===============================================");
        }
        List<EventObject> tempEventsList = new ArrayList<>();
        tempEventsList.addAll(eventsList);
        manipulateOverlappingEvents(tempEventsList);

//        Collections.sort(eventsList, new CustomDurationComparator());
    }

    private void manipulateOverlappingEvents(List<EventObject> tempEventsList) {
//        HashMap<String, List<EventObject>> mappedObjects = new HashMap<>();
        List<EventObject> overlappingEventObjects = new ArrayList<>();
        showLog("Temp list before size: " + tempEventsList.size());
        for (Iterator<EventObject> iterator = tempEventsList.iterator(); iterator.hasNext(); ) {
            EventObject iteratorEventObject = iterator.next();
            showLog("Iterator event object id: "+iteratorEventObject.getId());
            showLog("Iterator event object name: "+iteratorEventObject.getName());
            overlappingEventObjects = getOverlappingEventObjects(iteratorEventObject);
            showLog("Iterator object mapped events: "+overlappingEventObjects.size());
            mappedEventObjects = new ArrayList<>();
//            mappedObjects.put(iteratorEventObject.getId(), overlappingEventObjects);
            iterator.remove();
            showLog("================================================================");
            if (overlappingEventObjects.size() > 0) {
                break;
            }
        }

        for (EventObject obj: overlappingEventObjects) {
            for (Iterator<EventObject> innerIterator = tempEventsList.iterator(); innerIterator.hasNext(); ) {
                EventObject innerIteratorObject = innerIterator.next();
                if (obj.getId().equalsIgnoreCase(innerIteratorObject.getId())) {
                    innerIterator.remove();
                }
            }
        }

        if (tempEventsList.size() > 0) {
            manipulateOverlappingEvents(tempEventsList);
        }
        showLog("Temp list after size: " + tempEventsList.size());
    }

    List<EventObject> mappedEventObjects = new ArrayList<>();
    private List<EventObject> getOverlappingEventObjects(EventObject iteratorEventObject) {

        int size = eventsList.size();
        for (int i = 0; i < size; i++) {
            if (!iteratorEventObject.getId().equalsIgnoreCase(eventsList.get(i).getId()) && !eventsList.get(i).isMarked()) {
                boolean fallsInTime = checkTimeRange(eventsList.get(i).getStartTime(), eventsList.get(i).getEndTime(), iteratorEventObject.getEndTime());
                if (fallsInTime) {
                    eventsList.get(i).setMarked(true);
                    mappedEventObjects.add(eventsList.get(i));
                    getOverlappingEventObjects(eventsList.get(i));
                }
            }
        }
        //Also marking the sent object
        if (!iteratorEventObject.isMarked()) {
            for (int j = 0; j < size; j++) {
                if (iteratorEventObject.getId().equalsIgnoreCase(eventsList.get(j).getId())) {
                    eventsList.get(j).setMarked(true);
                    iteratorEventObject.setMarked(true);//to skip multiple iterations, this is necessary
                }
            }
        }
        return mappedEventObjects;
    }

    @Override
    public void onClick(View v) {

        String eventId = (String) v.getTag();
        if (!eventId.contains(ADDITIONAL_EVENT_ID_SEPARATOR)) {
            for (EventObject obj : eventsList) {
                if (obj.getId().equalsIgnoreCase(eventId)) {
                    Toast.makeText(getContext(), obj.getName(), Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        } else {
            Toast.makeText(getContext(), v.getTag().toString(), Toast.LENGTH_SHORT).show();
        }
    }
}
