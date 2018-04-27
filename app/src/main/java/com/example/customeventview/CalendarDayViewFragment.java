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
import android.text.TextUtils;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarDayViewFragment extends DialogFragment implements View.OnLongClickListener, View.OnDragListener {

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
    private static final String TEXT_VIEW_DEFAULT_TAG = "text_tag";


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

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(this);

                //setting the events
                for (int j = 0; j < dayHourTimes.length; j++) {
                    int parentWidth = eventsView.getWidth();
                    List<EventObject> filteredEvents = getCalendarEvents(j);
                    int size = filteredEvents.size();
                    int eventWidth;
                    if (size > 0) {
                        //send end time to compare with start time
                        EventObject filteredObject = filteredEvents.get(size - 1);
                        if (!filteredObject.isMarked()) {
                            int endTimeEventsSize = checkEndTimeEvents(filteredEvents, parentWidth, j);
                            eventWidth = parentWidth / endTimeEventsSize; //Calculating the width based on events
                            showLog("Total event count:::::" + (endTimeEventsSize));

                            int calculatedWidth = startTimeCalculation(filteredEvents);
                            if (calculatedWidth > 0) {
                                parentWidth = parentWidth - calculatedWidth;
                                eventWidth = parentWidth/size;
                                //adjusting the left margin again. Since there is a difference in width.
                                int count = 0;
                                int avgWidth = eventWidth;
                                for (EventObject tempObj: filteredEvents) {
                                    tempObj.setLeftMargin(count*avgWidth);
                                    count++;
                                }
                            }

                        } else {
                            eventWidth = parentWidth / (filteredObject.getEventsCount());
                        }
                    } else {
                        eventWidth = parentWidth;
                    }
                    for (int k = 0; k < size; k++) {
                        EventObject eventObject = filteredEvents.get(k);
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
//                        if (k != 0) {
//                            left = (int) (left + EVENT_GAP);
//                        } else if (eventObject.getLeftMargin() > 0) {
//                            left = (int) (left + EVENT_GAP);
//                        }
                        textViewParams.setMargins(left, top, 0, 0);
                        eventsView.addView(getTextView(textViewParams, eventObject.getName(), k, ContextCompat.getColor(getContext(), R.color.colorPrimary)));
                    }
                }
            }
        }, 100);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private TextView getTextView(RelativeLayout.LayoutParams textViewParams, String eventName, int id, int eventColor) {
        TextView textView = new TextView(getContext());
        textView.setId(id);
        textView.setLayoutParams(textViewParams);
        textView.setText(eventName);
        textView.setTextColor(Color.WHITE);
//        textView.setBackgroundColor(Color.BLACK);
        textView.setBackgroundResource(R.drawable.event_bg);
        GradientDrawable drawable = (GradientDrawable) textView.getBackground();
        drawable.setColor(eventColor);
        textView.setTextSize(14);
        int leftPadding = (int) convertDpToPixel(5, getContext());
        int rightPadding = (int) convertDpToPixel(5, getContext());
        textView.setPadding(leftPadding, 0, rightPadding, 0);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setTag(eventName + TEXT_VIEW_DEFAULT_TAG);
//        textView.setAlpha((float) 0.5);
        textView.setOnLongClickListener(this);
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

    private synchronized int checkEndTimeEvents(List<EventObject> filteredEvents, int totalWidth, int hourLineValue) {
        HashSet<EventObject> mainList = new HashSet<>();
        mainList.addAll(filteredEvents);//adding all the events. For multiple events from same hour line.
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

                if (startHour == startTimeCalendar.get(Calendar.HOUR_OF_DAY)) {
                    //do nothing

                } else if (checkTimeRange(startTimeCalendar, endTimeCalendar, eventObject.getEndTime())) {
                    eventIds.add(object.getId());
                    showLog("Event ID" + object.getId() + "::Start Hour: " + startTimeCalendar.get(Calendar.HOUR_OF_DAY) + "::End Hour: " + endTimeCalendar.get(Calendar.HOUR_OF_DAY));
                    List<EventObject> eventObjectList = checkAdditionalEndTimeDependents(object, eventIds);
                    showLog("Dependents size:::::" + eventObjectList.size());
                    mainList.addAll(eventObjectList);
                    mainList.add(object);
                } else if (!object.isMarked() && endTimeGreaterThanEventStartTime(eventObject.getEndTime(), startTimeCalendar)) {
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
        return mainList.size();
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
                if (!filteredEvents.contains(object) && object.isMarked()) {
                    Calendar startTimeCalendar = object.getStartTime();
                    Calendar endTimeCalendar = object.getEndTime();
                    if (checkTimeRange(startTimeCalendar, endTimeCalendar, eventObject.getStartTime())) {
                        mainList.add(object);
                    }
                }
            }
        }

        for (EventObject mainListObject: mainList) {
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

    private void setCalendarEvents() {
        //event 1
        EventObject eventObject = new EventObject();
        eventObject.setId("1");
        eventObject.setName("2:30AM to 4AM event");

        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 2);
        startCalendar.set(Calendar.MINUTE, 30);
        eventObject.setStartTime(startCalendar);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 4);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 2
        eventObject = new EventObject();
        eventObject.setId("2");
        eventObject.setName("2:30AM to 4AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 2);
        startCalendar.set(Calendar.MINUTE, 30);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 4);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 3
        eventObject = new EventObject();
        eventObject.setId("3");
        eventObject.setName("3AM to 6AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 3);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 6);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 4
        eventObject = new EventObject();
        eventObject.setId("4");
        eventObject.setName("2:30AM to 6AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 2);
        startCalendar.set(Calendar.MINUTE, 30);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 6);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 5
        eventObject = new EventObject();
        eventObject.setId("5");
        eventObject.setName("1AM to 4AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 1);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 4);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 6
        eventObject = new EventObject();
        eventObject.setId("6");
        eventObject.setName("5AM to 6AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 5);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 6);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 7
        eventObject = new EventObject();
        eventObject.setId("7");
        eventObject.setName("6AM to 7AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 6);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 7);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 8
        eventObject = new EventObject();
        eventObject.setId("8");
        eventObject.setName("7AM to 8AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 7);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 8);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 9
        eventObject = new EventObject();
        eventObject.setId("9");
        eventObject.setName("7.30AM to 8.30AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 7);
        startCalendar.set(Calendar.MINUTE, 30);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 8);
        endCalendar.set(Calendar.MINUTE, 30);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 10
        eventObject = new EventObject();
        eventObject.setId("10");
        eventObject.setName("8AM to 9AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 8);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 9);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 11
        eventObject = new EventObject();
        eventObject.setId("11");
        eventObject.setName("7AM to 11AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 7);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 11);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

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

        //event 13
        eventObject = new EventObject();
        eventObject.setId("13");
        eventObject.setName("6AM to 7AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 6);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 7);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 14
        eventObject = new EventObject();
        eventObject.setId("14");
        eventObject.setName("6AM to 7AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 6);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 7);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 15
        eventObject = new EventObject();
        eventObject.setId("15");
        eventObject.setName("6AM to 7AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 6);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 7);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

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

        //event 17
        eventObject = new EventObject();
        eventObject.setId("17");
        eventObject.setName("5.30AM to 6.30PM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 5);
        startCalendar.set(Calendar.MINUTE, 30);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 6);
        endCalendar.set(Calendar.MINUTE, 30);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 18
        eventObject = new EventObject();
        eventObject.setId("18");
        eventObject.setName("9AM to 11AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 9);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 11);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 19
        eventObject = new EventObject();
        eventObject.setId("19");
        eventObject.setName("10AM to 11AM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 10);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 11);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

//        //event 20
//        eventObject = new EventObject();
//        eventObject.setId("20");
//        eventObject.setName("6.30AM to 8AM event");
//
//        startCalendar = Calendar.getInstance();
//        startCalendar.set(Calendar.HOUR_OF_DAY, 6);
//        startCalendar.set(Calendar.MINUTE, 30);
//        eventObject.setStartTime(startCalendar);
//
//        endCalendar = Calendar.getInstance();
//        endCalendar.set(Calendar.HOUR_OF_DAY, 8);
//        endCalendar.set(Calendar.MINUTE, 0);
//        eventObject.setEndTime(endCalendar);
//
//        eventsList.add(eventObject);

        //event 21
        eventObject = new EventObject();
        eventObject.setId("21");
        eventObject.setName("1PM to 2PM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 13);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 14);
        endCalendar.set(Calendar.MINUTE, 0);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        //event 22
        eventObject = new EventObject();
        eventObject.setId("22");
        eventObject.setName("2PM to 2:30PM event");

        startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, 14);
        startCalendar.set(Calendar.MINUTE, 0);
        eventObject.setStartTime(startCalendar);

        endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 14);
        endCalendar.set(Calendar.MINUTE, 30);
        eventObject.setEndTime(endCalendar);

        eventsList.add(eventObject);

        Collections.sort(eventsList, new CustomDurationComparator());
    }
}
