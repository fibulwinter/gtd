package net.fibulwinter.gtd.presentation;

import java.util.Date;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.infrastructure.DateMarshaller;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;
import net.fibulwinter.gtd.service.TemporalPredicates;

public class TimeConstraintsUtils {
    public static final int TODAY_FG_COLOR = Color.parseColor("#ff8000");
    public static final int TODAY_BG_COLOR = Color.parseColor("#663000");
    public static final int OVERDUE_FG_COLOR = Color.parseColor("#ff0000");
    public static final int OVERDUE_BG_COLOR = Color.parseColor("#660000");
    public static final int NOT_STARTED_FG_COLOR = Color.parseColor("#999999");
    public static final int NOT_STARTED_BG_COLOR = Color.parseColor("#444444");
    public static final int STARTED_TODAY_FG_COLOR = Color.parseColor("#00cc00");
    public static final int STARTED_TODAY_BG_COLOR = Color.parseColor("#006600");
    private Resources resources;
    private TemporalLogic temporalLogic;
    private TemporalPredicates temporalPredicates;

    public TimeConstraintsUtils(Resources resources, TemporalLogic temporalLogic) {
        this.resources = resources;
        this.temporalLogic = temporalLogic;
        this.temporalPredicates = new TemporalPredicates(temporalLogic);
    }

    public SpannedText addFutureStartWarning(Task task) {
        Optional<Date> startingDate = task.getStartingDate();
        boolean inFuture = inFuture(startingDate);
        boolean startedToday = startedToday(startingDate);
        if (!(startedToday || inFuture)) {
            return new SpannedText();
        }
        String text = startingDate(task);
        if (inFuture) {
            return new SpannedText().space().join(text,
                    new StyleSpan(Typeface.ITALIC));
        } else if (startedToday) {
            return new SpannedText().space().join(text,
                    new ForegroundColorSpan(TimeConstraintsUtils.STARTED_TODAY_FG_COLOR),
                    new BackgroundColorSpan(TimeConstraintsUtils.STARTED_TODAY_BG_COLOR));
        } else {
            return new SpannedText().space().join(text);
        }
    }

    private boolean inFuture(Optional<Date> startingDate) {
        return startingDate.isPresent() && temporalLogic.relativeDays(startingDate.get()) > 0;
    }

    private boolean startedToday(Optional<Date> startingDate) {
        return startingDate.isPresent() && temporalLogic.relativeDays(startingDate.get()) == 0;
    }

    private String startingDate(Task task) {
        Optional<Date> startingDate = task.getStartingDate();
        if (!startingDate.isPresent()) {
            return "";
        }
        int days = temporalLogic.relativeDays(startingDate.get());
        if (days > 1) {
            return resources.getString(R.string.starting) + "\n" + resources.getQuantityString(R.plurals.in_n_days, days, days);
        } else if (days == 1) {
            return resources.getString(R.string.starting) + "\n" + resources.getString(R.string.tomorrow);
        } else if (days == 0) {
            return resources.getString(R.string.started_today);
        } else {
            return resources.getString(R.string.started_s, DateMarshaller.optionalDateToString(startingDate));
        }
    }

    public SpannedText addDueDateWarning(Task task) {
        if (task.getDueDate().isPresent() && task.getStatus().isActive()) {
            String dueDate = dueDate(task);
            if (temporalPredicates.dueTomorrow().apply(task)) {
                return new SpannedText().space().join(dueDate,
                        new ForegroundColorSpan(TimeConstraintsUtils.TODAY_FG_COLOR),
                        new BackgroundColorSpan(TimeConstraintsUtils.TODAY_BG_COLOR)
                );
            } else if (temporalPredicates.overdue().apply(task)) {
                return new SpannedText().space().join(dueDate,
                        new ForegroundColorSpan(TimeConstraintsUtils.OVERDUE_FG_COLOR),
                        new BackgroundColorSpan(TimeConstraintsUtils.OVERDUE_BG_COLOR)
                );
            } else {
                return new SpannedText().space().join(dueDate);
            }
        } else {
            return new SpannedText();
        }
    }


    public String dueDate(Task task) {
        Optional<Date> dueDate = task.getDueDate();
        if (!dueDate.isPresent()) {
            return resources.getString(R.string.anytime);
        }
        int days = temporalLogic.relativeDays(dueDate.get());
        if (days > 2) {
            return resources.getQuantityString(R.plurals.in_n_days, days, days);
        } else if (days == 2) {
            return resources.getString(R.string.tomorrow);
        } else if (days == 1) {
            return resources.getString(R.string.today);
        } else if (days == 0) {
            return resources.getString(R.string.yesterday);
        } else {
            int quantity = (int) ((-days) + 1);
            return resources.getQuantityString(R.plurals.overdue_n_days, quantity, quantity);
        }
    }
}
