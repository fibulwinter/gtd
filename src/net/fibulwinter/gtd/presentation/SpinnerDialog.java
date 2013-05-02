package net.fibulwinter.gtd.presentation;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class SpinnerDialog {

    public interface OnSelected<T> {
        void selected(T selectedItem);
    }

    public static <T> void show(Context context, final List<T> contexts, final T selected, final OnSelected<T> listener) {
        final AlertDialog.Builder b = new AlertDialog.Builder(context);

        String[] types = new String[contexts.size()];
        for (int i = 0; i < contexts.size(); i++) {
            types[i] = contexts.get(i).toString();
        }
        b.setSingleChoiceItems(types, contexts.indexOf(selected), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                T nowSelectedItem = contexts.get(which);
                if (!(nowSelectedItem.equals(selected))) {
                    listener.selected(nowSelectedItem);
                }
            }

        });
        b.show();

    }
}
