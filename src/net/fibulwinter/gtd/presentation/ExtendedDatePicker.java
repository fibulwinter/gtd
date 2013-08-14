package net.fibulwinter.gtd.presentation;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.res.Resources;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;

public class ExtendedDatePicker {

    private final LinearLayout linearLayout;
    private final DatePicker datePicker;
    private final CheckBox checkBox;
    private final TextView textView;
    private final TemporalLogic temporalLogic;
    private final Resources resources;

    public ExtendedDatePicker(Context context, Optional<Date> optionalDate, String text) {
        temporalLogic = new TemporalLogic();
        Calendar calendar = temporalLogic.getCalendar(optionalDate.or(new Date()));
        // Set an EditText view to get user input
        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        datePicker = new DatePicker(context);
        checkBox = new CheckBox(context);
        checkBox.setText(text);
        checkBox.setChecked(optionalDate.isPresent());
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                datePicker.setEnabled(b);
            }
        });
        datePicker.setEnabled(optionalDate.isPresent());
        textView = new TextView(context);
        textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        resources = context.getResources();
        updateStatus(calendar);
        datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker datePicker, int day, int month, int year) {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
                updateStatus(calendar);
            }
        });
        linearLayout.addView(checkBox);
        linearLayout.addView(datePicker);
        linearLayout.addView(textView);
    }

    private void updateStatus(Calendar calendar) {
        textView.setText(DateFormat.format("EEEE", calendar) + ", " + diffMessage(calendar));
    }

    private String diffMessage(Calendar calendar) {
        int d = temporalLogic.relativeDays(calendar.getTime());
        if (d == 0) {
            return resources.getString(R.string.today);
        } else if (d == 1) {
            return resources.getString(R.string.tomorrow);
        } else if (d == -1) {
            return resources.getString(R.string.yesterday);
        } else if (d > 1) {
            return resources.getQuantityString(R.plurals.n_days_after_today, d, d);
        } else {
            return resources.getQuantityString(R.plurals.n_days_before_today, -d, -d);
        }

    }

    public View getView() {
        return linearLayout;
    }

    public Optional<Date> getDate() {
        if (checkBox.isChecked()) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(), 0, 0, 0);
            return Optional.of(calendar.getTime());
        } else {
            return Optional.absent();
        }
    }
}
