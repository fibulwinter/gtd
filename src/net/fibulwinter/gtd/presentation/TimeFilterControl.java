package net.fibulwinter.gtd.presentation;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeFilterControl extends LinearLayout {

    public static final LayoutParams LAYOUT_PARAMS = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
    private final TextView textViewNotStarted;
    private final TextView textViewStartedToday;
    private final TextView textViewDueTomorrow;
    private final TextView textViewOverDue;
    private SpinnerUtils.ContextSpinnerListener listener;
    private final TextView textViewAll;

    public TimeFilterControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        LAYOUT_PARAMS.weight = 1;
        textViewAll = createButton(context, "All", Color.WHITE, Color.BLACK, net.fibulwinter.gtd.domain.Context.ANY);
        textViewAll.setVisibility(VISIBLE);
        textViewNotStarted = createButton(context, "0", TimeConstraintsUtils.NOT_STARTED_FG_COLOR, TimeConstraintsUtils.NOT_STARTED_BG_COLOR, net.fibulwinter.gtd.domain.Context.NOT_STARTED);
        textViewStartedToday = createButton(context, "0", TimeConstraintsUtils.STARTED_TODAY_FG_COLOR, TimeConstraintsUtils.STARTED_TODAY_BG_COLOR, net.fibulwinter.gtd.domain.Context.STARTED_TODAY);
        textViewDueTomorrow = createButton(context, "0", TimeConstraintsUtils.TODAY_FG_COLOR, TimeConstraintsUtils.TODAY_BG_COLOR, net.fibulwinter.gtd.domain.Context.TODAY);
        textViewOverDue = createButton(context, "0", TimeConstraintsUtils.OVERDUE_FG_COLOR, TimeConstraintsUtils.OVERDUE_BG_COLOR, net.fibulwinter.gtd.domain.Context.OVERDUE);
    }

    private TextView createButton(Context context, String text, int textColor, int backgroundColor, final net.fibulwinter.gtd.domain.Context taskContext) {
        TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setBackgroundColor(backgroundColor);
        textView.setLayoutParams(LAYOUT_PARAMS);
        textView.setClickable(true);
        textView.setGravity(Gravity.CENTER);
        textView.setVisibility(INVISIBLE);
        addView(textView, LAYOUT_PARAMS);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onSelectedContext(taskContext);
                }
            }
        });
        return textView;
    }

    public void setTodayCounter(int todayCounter) {
        setCounter(todayCounter, textViewDueTomorrow);
    }

    public void setOverdueCounter(int overdueCounter) {
        setCounter(overdueCounter, textViewOverDue);
    }

    public void setStartedTodayCounter(int startedTodayCounter) {
        setCounter(startedTodayCounter, textViewStartedToday);
    }

    public void setNotStartedCounter(int notStartedCounter) {
        setCounter(notStartedCounter, textViewNotStarted);
    }

    public void setListener(SpinnerUtils.ContextSpinnerListener listener) {
        this.listener = listener;
    }

    private void setCounter(int counter, TextView textView) {
        textView.setText(String.valueOf(counter));
        textView.setVisibility(counter > 0 ? VISIBLE : INVISIBLE);
    }
}
