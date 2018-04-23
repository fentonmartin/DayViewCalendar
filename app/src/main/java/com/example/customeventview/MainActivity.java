package com.example.customeventview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        CalendarDayViewFragment calendarDayViewFragment = CalendarDayViewFragment.newInstance();
        calendarDayViewFragment.show(getSupportFragmentManager(), CalendarDayViewFragment.TAG);
    }
}
