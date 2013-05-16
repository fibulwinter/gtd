package net.fibulwinter.gtd.presentation;

import java.util.Date;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import com.google.common.base.Optional;
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
    private TemporalLogic temporalLogic;
    private TemporalPredicates temporalPredicates;

    public TimeConstraintsUtils(TemporalLogic temporalLogic) {
        this.temporalLogic = temporalLogic;
        this.temporalPredicates = new TemporalPredicates(temporalLogic);
    }

    public SpannedText getNonEmptyConstraintsText(Task task) {
        SpannedText spannedText = new SpannedText("");
//        spannedText = spannedText.join(addStartWarning(task, false));
        SpannedText anotherText = addDueDateWarning(task);
        if (anotherText.isEmpty()) {
            anotherText = new SpannedText("anytime");
        }
        spannedText = spannedText.join(anotherText);
        return spannedText;
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
        long days = temporalLogic.relativeDays(startingDate.get());
        if (days > 1) {
            return "starting\nin " + days + " days";
        } else if (days == 1) {
            return "starting\ntomorrow";
        } else if (days == 0) {
            return "started today";
        } else {
            return "started " + DateMarshaller.optionalDateToString(startingDate);
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
            return "anytime";
        }
        long days = temporalLogic.relativeDays(dueDate.get());
        if (days > 2) {
            return "in " + days + " days";
        } else if (days == 2) {
            return "tomorrow";
        } else if (days == 1) {
            return "today";
        } else if (days == 0) {
            return "yesterday";
        } else {
            return "overdue " + ((-days) + 1) + " days";
        }
    }
}
