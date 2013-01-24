package net.fibulwinter.gtd.presentation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.DatePicker;
import com.google.common.base.Optional;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ClearDatePicker {
    private Context context;

    public ClearDatePicker(Context context) {
        this.context = context;
    }

    public void pickDate(String title, Optional<Date> optionalDate, final DatePickListener listener) {
        Date date = optionalDate.or(new Date());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // Set an EditText view to get user input
        final DatePicker input = new DatePicker(context);
        input.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), null);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 11) {
            try {
                Method m = input.getClass().getMethod("setCalendarViewShown", boolean.class);
                m.invoke(input, true);
            } catch (Exception e) {
            } // eat exception in our case
        }
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GregorianCalendar calendar = new GregorianCalendar();
                        calendar.set(input.getYear(), input.getMonth(), input.getDayOfMonth(), 0, 0, 0);
                        listener.setOptionalDate(Optional.of(calendar.getTime()));
                    }
                })
                .setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        listener.setOptionalDate(Optional.<Date>absent());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }


    public static interface DatePickListener {
        void setOptionalDate(Optional<Date> date);
    }
}
