package net.fibulwinter.gtd.presentation;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.LinearLayout;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;

public class ClearDatePicker {
    private Context context;

    public ClearDatePicker(Context context) {
        this.context = context;
    }

    public void pickDate(String title, Optional<Date> optionalDateStart, Optional<Date> optionalDateDue, final DatePickListener listener) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final ExtendedDatePicker extendedDatePickerStart = new ExtendedDatePicker(context, optionalDateStart,
                context.getResources().getString(R.string.hide_before));
        final ExtendedDatePicker extendedDatePickerDue = new ExtendedDatePicker(context, optionalDateDue,
                context.getResources().getString(R.string.must_be_done_before));
        linearLayout.addView(extendedDatePickerStart.getView());
        linearLayout.addView(extendedDatePickerDue.getView());
        new AlertDialog.Builder(context)
//                .setTitle(title)
                .setView(linearLayout)
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        listener.setOptionalDate(extendedDatePickerStart.getDate(), extendedDatePickerDue.getDate());
                    }
                })
                .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }


    public static interface DatePickListener {
        void setOptionalDate(Optional<Date> dateStart, Optional<Date> dateDue);
    }
}
