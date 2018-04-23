package com.example.customeventview;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;

import com.example.customview.BitmapStickerIcon;
import com.example.customview.DeleteIconEvent;
import com.example.customview.FlipHorizontallyEvent;
import com.example.customview.HelloIconEvent;
import com.example.customview.Sticker;
import com.example.customview.StickerView;
import com.example.customview.TextSticker;
import com.example.customview.ZoomIconEvent;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements StickerView.OnStickerOperationListener, View.OnClickListener {

    private StickerView stickerView;
    private TextSticker textSticker;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        button.setOnClickListener(this);

//        stickerView = findViewById(R.id.sticker_view);
//
//        //currently you can config your own icons and icon event
//        //the event you can custom
//        BitmapStickerIcon deleteIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
//                R.drawable.sticker_ic_close_white_18dp),
//                BitmapStickerIcon.LEFT_TOP);
//        deleteIcon.setIconEvent(new DeleteIconEvent());
//
//        BitmapStickerIcon zoomIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
//                R.drawable.sticker_ic_scale_white_18dp),
//                BitmapStickerIcon.RIGHT_BOTOM);
//        zoomIcon.setIconEvent(new ZoomIconEvent());
//
//        BitmapStickerIcon flipIcon = new BitmapStickerIcon(ContextCompat.getDrawable(this,
//                R.drawable.sticker_ic_flip_white_18dp),
//                BitmapStickerIcon.RIGHT_TOP);
//        flipIcon.setIconEvent(new FlipHorizontallyEvent());
//
//        BitmapStickerIcon heartIcon =
//                new BitmapStickerIcon(ContextCompat.getDrawable(this, R.drawable.ic_favorite_white_24dp),
//                        BitmapStickerIcon.LEFT_BOTTOM);
//        heartIcon.setIconEvent(new HelloIconEvent());
//
//        stickerView.setIcons(Arrays.asList(deleteIcon, zoomIcon, flipIcon, heartIcon));
//        stickerView.setBackgroundColor(Color.WHITE);
//        stickerView.setLocked(true);
//        stickerView.setShowIcons(false);
//        stickerView.setConstrained(true);
//
//
//        textSticker = new TextSticker(this);
//        textSticker.setDrawable(ContextCompat.getDrawable(getApplicationContext(),
//                R.drawable.sticker_transparent_background));
//        textSticker.setText("Hello, world!");
//        textSticker.setMinTextSize(12);
//        textSticker.setMaxTextSize(12);
//        textSticker.setTextColor(Color.BLACK);
//        textSticker.setTextAlign(Layout.Alignment.ALIGN_NORMAL);
//        textSticker.resizeText();
//
//        stickerView.addSticker(textSticker, Sticker.Position.BOTTOM);
//        stickerView.setOnStickerOperationListener(this);
    }

    @Override
    public void onStickerAdded(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerClicked(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerDeleted(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerDragFinished(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerTouchedDown(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerZoomFinished(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerFlipped(@NonNull Sticker sticker) {

    }

    @Override
    public void onStickerDoubleTapped(@NonNull Sticker sticker) {
        if (stickerView.isLocked()) {
            stickerView.setShowIcons(true);
            stickerView.setLocked(false);
        } else {
            stickerView.setShowIcons(false);
            stickerView.setLocked(true);
        }
        stickerView.invalidate();
    }

    @Override
    public void onClick(View v) {
        CalendarDayViewFragment calendarDayViewFragment = CalendarDayViewFragment.newInstance();
        calendarDayViewFragment.show(getSupportFragmentManager(), CalendarDayViewFragment.TAG);
    }
}
