package net.fibulwinter.gtd.presentation;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import net.fibulwinter.gtd.domain.Context;
import net.fibulwinter.gtd.domain.ContextRepository;

public class SpinnerUtils {

    public static void setSelection(Spinner contextSpinner, Context context) {
        int count = contextSpinner.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            if (context.equals(contextSpinner.getAdapter().getItem(i))) {
                contextSpinner.setSelection(i);
                return;
            }
        }
    }

    public static interface ContextSpinnerListener {
        void onSelectedContext(Context context);
    }

    public static void setupContextSpinner(android.content.Context androidContext, final ContextRepository contextRepository,
                                           Spinner contextSpinner, final ContextSpinnerListener contextSpinnerListener) {
        setupContextSpinner(androidContext, contextRepository, contextSpinner, contextSpinnerListener, false);

    }

    public static void setupContextSpinner(android.content.Context androidContext, final ContextRepository contextRepository,
                                           Spinner contextSpinner, final ContextSpinnerListener contextSpinnerListener, boolean onlySingle) {
        final ArrayAdapter<Context> contextArrayAdapter = new ArrayAdapter<Context>(androidContext, android.R.layout.simple_spinner_item);
        if (!onlySingle) {
            contextArrayAdapter.add(Context.ANY);
        }
        for (Context context : contextRepository.getAll()) {
            contextArrayAdapter.add(context);
        }
        if (!onlySingle) {
            contextArrayAdapter.add(Context.TODAY);
            contextArrayAdapter.add(Context.OVERDUE);
        }
        contextArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contextSpinner.setAdapter(contextArrayAdapter);
        contextSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                contextSpinnerListener.onSelectedContext(contextArrayAdapter.getItem(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }
}
