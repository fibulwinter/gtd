package net.fibulwinter.gtd.presentation;

import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.domain.Task;

public class EditDialogFactory {
    private ClearDatePicker clearDatePicker;
    private Context context;


    public EditDialogFactory(Context context) {
        this.context = context;
        clearDatePicker = new ClearDatePicker(context);
    }

    public void showTimeDialog(final Task task, final Runnable runnable) {
        clearDatePicker.pickDate("Time constraints", task.getStartingDate(), task.getDueDate(), new ClearDatePicker.DatePickListener() {
            @Override
            public void setOptionalDate(Optional<Date> dateStart, Optional<Date> dateDue) {
                task.setStartingDate(dateStart);
                task.setDueDate(dateDue);
                runnable.run();
            }
        });
    }

    public static interface TitleEdited {
        void onValidText(String title);
    }

    public void showTitleDialog(final String initialText, String title, final TitleEdited titleEdited) {
        final EditText input = new EditText(context);
        input.setText(initialText);
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String inputText = input.getText().toString().trim();
                        if (!inputText.isEmpty()) {
                            titleEdited.onValidText(inputText);
                        }
                    }

                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
    }

}
