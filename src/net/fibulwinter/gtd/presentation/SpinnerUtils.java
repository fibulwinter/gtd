package net.fibulwinter.gtd.presentation;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import net.fibulwinter.gtd.domain.Context;
import net.fibulwinter.gtd.domain.ContextRepository;

public class SpinnerUtils {

    public static interface ContextSpinnerListener {
        void onSelectedContext(Context context);
    }

    public static void setupContextSpinner(android.content.Context androidContext, final ContextRepository contextRepository,
                                           Spinner contextSpinner, final ContextSpinnerListener contextSpinnerListener) {
        ArrayAdapter<Context> contextArrayAdapter = new ArrayAdapter<Context>(androidContext, android.R.layout.simple_spinner_item);
        for (Context context : contextRepository.getAll()) {
            contextArrayAdapter.add(context);
        }
        contextArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        contextSpinner.setAdapter(contextArrayAdapter);
        contextSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                contextSpinnerListener.onSelectedContext(contextRepository.getAll().get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }
}
