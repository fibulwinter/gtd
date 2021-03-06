package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.FluentIterable.from;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.service.TemporalPredicates;

public class TimeFilterControl extends LinearLayout {

    public static final LayoutParams LAYOUT_PARAMS_MAIN = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
    public static final LayoutParams LAYOUT_PARAMS = new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
    private final TextView textViewNotStarted;
    private final TextView textViewStartedToday;
    private final TextView textViewDueTomorrow;
    private final TextView textViewOverDue;
    private Runnable listener;
    private final TextView textViewAll;
    private final ContextRepository contextRepository;
    private net.fibulwinter.gtd.domain.Context currentContext = net.fibulwinter.gtd.domain.Context.ANY;
    private Predicate<Task> currentTimeContext = null;
    private TemporalPredicates temporalPredicates = new TemporalPredicates();
    private final Predicate<Task> notStartedContext;
    private final Predicate<Task> startedTodayContext;
    private final Predicate<Task> dueTomorrowContext;
    private final Predicate<Task> overdueContext;
    private Predicate<Task> filterPredicate = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            if (currentTimeContext != null) {
                return currentTimeContext.apply(task);
            } else {
                return !temporalPredicates.notStarted().apply(task) && currentContext.match(task);
            }
        }
    };

    public TimeFilterControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.HORIZONTAL);
        contextRepository = new ContextRepository();
        LAYOUT_PARAMS_MAIN.weight = 0.8f;
        LAYOUT_PARAMS.weight = 1;
        textViewAll = createContextButton(context);
        notStartedContext = temporalPredicates.notStarted();
        startedTodayContext = temporalPredicates.startedToday();
        dueTomorrowContext = temporalPredicates.dueTomorrow();
        overdueContext = temporalPredicates.overdue();
        textViewNotStarted = createButton(context, "0", TimeConstraintsUtils.NOT_STARTED_FG_COLOR, TimeConstraintsUtils.NOT_STARTED_BG_COLOR, notStartedContext, LAYOUT_PARAMS);
        textViewStartedToday = createButton(context, "0", TimeConstraintsUtils.STARTED_TODAY_FG_COLOR, TimeConstraintsUtils.STARTED_TODAY_BG_COLOR, startedTodayContext, LAYOUT_PARAMS);
        textViewDueTomorrow = createButton(context, "0", TimeConstraintsUtils.TODAY_FG_COLOR, TimeConstraintsUtils.TODAY_BG_COLOR, dueTomorrowContext, LAYOUT_PARAMS);
        textViewOverDue = createButton(context, "0", TimeConstraintsUtils.OVERDUE_FG_COLOR, TimeConstraintsUtils.OVERDUE_BG_COLOR, overdueContext, LAYOUT_PARAMS);
    }

    private TextView createContextButton(final Context context) {
        final TextView textView = new TextView(context);
        textView.setText(currentContext.getName());
        textView.setTextColor(TaskItemAdapter.CONTEXT_FG_COLOR);
        textView.setBackgroundColor(TaskItemAdapter.CONTEXT_BG_COLOR);
        textView.setClickable(true);
        textView.setGravity(Gravity.CENTER);
        textView.setVisibility(VISIBLE);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    if (currentContext == net.fibulwinter.gtd.domain.Context.ANY) {
                        if (currentTimeContext == null) {
                            SpinnerDialog.show(context, contextRepository.getAll(), currentContext, new SpinnerDialog.OnSelected<net.fibulwinter.gtd.domain.Context>() {
                                @Override
                                public void selected(net.fibulwinter.gtd.domain.Context selectedItem) {
                                    currentContext = selectedItem;
                                    currentTimeContext = null;
                                    textView.setText(currentContext.getName());
                                    updateColors();
                                    listener.run();
                                }
                            });
                        } else {
                            currentTimeContext = null;
                            textView.setText(currentContext.getName());
                            updateColors();
                            listener.run();
                        }

                    } else {
                        if (currentTimeContext == null) {
                            currentContext = net.fibulwinter.gtd.domain.Context.ANY;
                            currentTimeContext = null;
                            textView.setText(currentContext.getName());
                            updateColors();
                            listener.run();
                        } else {
                            currentTimeContext = null;
                            textView.setText(currentContext.getName());
                            updateColors();
                            listener.run();
                        }
                    }
                }
            }
        });
        addView(textView, LAYOUT_PARAMS_MAIN);
        return textView;
    }

    private TextView createButton(final Context context, String text, final int textColor, final int backgroundColor, final Predicate<Task> taskContext, LayoutParams layoutParams) {
        final TextView textView = new TextView(context);
        textView.setText(text);
        textView.setTextColor(textColor);
        textView.setBackgroundColor(backgroundColor);
        textView.setClickable(true);
        textView.setGravity(Gravity.CENTER);
        textView.setVisibility(INVISIBLE);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    if (currentTimeContext == taskContext) {
                        currentTimeContext = null;
                        updateColors();
                        listener.run();
                    } else {
                        currentTimeContext = taskContext;
                        updateColors();
                        listener.run();
                    }
                }
            }
        });
        addView(textView, LAYOUT_PARAMS);
        return textView;
    }

    private void updateColors() {
        if (currentTimeContext == null && currentContext != net.fibulwinter.gtd.domain.Context.ANY) {
            textViewAll.setTextColor(Color.WHITE);
            textViewAll.setBackgroundColor(TaskItemAdapter.CONTEXT_FG_COLOR2);
        } else {
            textViewAll.setTextColor(TaskItemAdapter.CONTEXT_FG_COLOR);
            textViewAll.setBackgroundColor(TaskItemAdapter.CONTEXT_BG_COLOR);
        }
        updateColors(textViewNotStarted, notStartedContext, TimeConstraintsUtils.NOT_STARTED_FG_COLOR, TimeConstraintsUtils.NOT_STARTED_BG_COLOR);
        updateColors(textViewStartedToday, startedTodayContext, TimeConstraintsUtils.STARTED_TODAY_FG_COLOR, TimeConstraintsUtils.STARTED_TODAY_BG_COLOR);
        updateColors(textViewDueTomorrow, dueTomorrowContext, TimeConstraintsUtils.TODAY_FG_COLOR, TimeConstraintsUtils.TODAY_BG_COLOR);
        updateColors(textViewOverDue, overdueContext, TimeConstraintsUtils.OVERDUE_FG_COLOR, TimeConstraintsUtils.OVERDUE_BG_COLOR);

    }

    private void updateColors(TextView textView, Predicate<Task> taskPredicate, int fg, int bg) {
        if (currentTimeContext == taskPredicate) {
            textView.setTextColor(bg);
            textView.setBackgroundColor(fg);
        } else {
            textView.setTextColor(fg);
            textView.setBackgroundColor(bg);
        }
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

    public void setListener(Runnable listener) {
        this.listener = listener;
    }

    private void setCounter(int counter, TextView textView) {
        textView.setText(String.valueOf(counter));
        textView.setVisibility(counter > 0 ? VISIBLE : GONE);
    }

    public Iterable<Task> updateOn(List<Task> taskList) {
        setTodayCounter(count(taskList, temporalPredicates.dueTomorrow()));
        setOverdueCounter(count(taskList, temporalPredicates.overdue()));
        setStartedTodayCounter(count(taskList, temporalPredicates.startedToday()));
        setNotStartedCounter(count(taskList, temporalPredicates.notStarted()));
        return from(taskList).filter(filterPredicate);
    }

    private int count(List<Task> taskList, Predicate<Task> predicate) {
        return Iterables.size(from(taskList).filter(predicate));
    }
}
