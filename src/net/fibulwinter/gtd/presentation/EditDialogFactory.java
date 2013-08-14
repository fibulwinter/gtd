package net.fibulwinter.gtd.presentation;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;

public class EditDialogFactory {
    private ClearDatePicker clearDatePicker;
    private Context context;


    public EditDialogFactory(Context context) {
        this.context = context;
        clearDatePicker = new ClearDatePicker(context);
    }

    public void showTimeDialog(final Task task, final Runnable runnable) {
        clearDatePicker.pickDate(context.getResources().getString(R.string.time_constraints), task.getStartingDate(), task.getDueDate(), new ClearDatePicker.DatePickListener() {
            @Override
            public void setOptionalDate(Optional<Date> dateStart, Optional<Date> dateDue) {
                task.setStartingDate(dateStart);
                task.setDueDate(dateDue);
                runnable.run();
            }
        });
    }

    public static interface TitleEdited {
        void onValidText(String title, net.fibulwinter.gtd.domain.Context context);
    }

    public void showTitleDialog(final String initialText, final net.fibulwinter.gtd.domain.Context initialContext, String title, final TitleEdited titleEdited) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText input = new EditText(context);
        input.setText(initialText);
        final Spinner spinner = new Spinner(context);
        linearLayout.addView(input);
        linearLayout.addView(spinner);
        SpinnerUtils.setupContextSpinner(context, new ContextRepository(), spinner, new SpinnerUtils.ContextSpinnerListener() {
            @Override
            public void onSelectedContext(net.fibulwinter.gtd.domain.Context context) {
            }
        }, true);
        SpinnerUtils.setSelection(spinner, initialContext);
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(linearLayout)
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String inputText = input.getText().toString().trim();
                        if (inputText.length() > 0) {
                            titleEdited.onValidText(inputText, (net.fibulwinter.gtd.domain.Context) spinner.getSelectedItem());
                        }
                    }

                })
                .setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

}
