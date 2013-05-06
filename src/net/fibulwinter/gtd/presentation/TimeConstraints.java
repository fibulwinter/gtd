package net.fibulwinter.gtd.presentation;

import android.content.Context;
import android.graphics.Color;

public class TimeConstraints {
    public static final int TODAY_FG_COLOR = Color.parseColor("#ff8000");
    public static final int TODAY_BG_COLOR = Color.parseColor("#663000");
    public static final int OVERDUE_FG_COLOR = Color.parseColor("#ff0000");
    public static final int OVERDUE_BG_COLOR = Color.parseColor("#660000");
    public static final int NOT_STARTED_FG_COLOR = Color.parseColor("#ffff00");
    public static final int NOT_STARTED_BG_COLOR = Color.parseColor("#666600");

    private ClearDatePicker clearDatePicker;


    public TimeConstraints(Context context) {
        clearDatePicker = new ClearDatePicker(context);
    }
}
