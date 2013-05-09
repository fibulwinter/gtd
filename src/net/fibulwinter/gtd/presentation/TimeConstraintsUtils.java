package net.fibulwinter.gtd.presentation;

import java.util.Date;

import android.graphics.Color;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.infrastructure.DateUtils;
import net.fibulwinter.gtd.service.TaskListService;

public class TimeConstraintsUtils {
    public static final int TODAY_FG_COLOR = Color.parseColor("#ff8000");
    public static final int TODAY_BG_COLOR = Color.parseColor("#663000");
    public static final int OVERDUE_FG_COLOR = Color.parseColor("#ff0000");
    public static final int OVERDUE_BG_COLOR = Color.parseColor("#660000");
    public static final int NOT_STARTED_FG_COLOR = Color.parseColor("#999999");
    public static final int NOT_STARTED_BG_COLOR = Color.parseColor("#444444");
    public static final int STARTED_TODAY_FG_COLOR = Color.parseColor("#00cc00");
    public static final int STARTED_TODAY_BG_COLOR = Color.parseColor("#006600");

    public SpannedText getNonEmptyConstraintsText(Task task) {
        SpannedText spannedText = new SpannedText("");
        spannedText = spannedText.join(addStartWarning(task, false));
        spannedText = spannedText.join(addDueDateWarning(task));
        return spannedText;
    }

    public SpannedText addFutureStartWarning(Task task) {
        return addStartWarning(task, true);
    }

    private SpannedText addStartWarning(Task task, boolean onlyIfInFuture) {
        Optional<Date> startingDate = task.getStartingDate();
        boolean inFuture = inFuture(startingDate);
        boolean startedToday = startedToday(startingDate);
        if (!(startedToday || inFuture) && onlyIfInFuture) {
            return new SpannedText();
        }
        String text = startingDate(task);
        if (inFuture) {
            return new SpannedText().space().join(text,
                    new ForegroundColorSpan(TimeConstraintsUtils.NOT_STARTED_FG_COLOR),
                    new BackgroundColorSpan(TimeConstraintsUtils.NOT_STARTED_BG_COLOR));
        } else if (startedToday) {
            return new SpannedText().space().join(text,
                    new ForegroundColorSpan(TimeConstraintsUtils.STARTED_TODAY_FG_COLOR),
                    new BackgroundColorSpan(TimeConstraintsUtils.STARTED_TODAY_BG_COLOR));
        } else {
            return new SpannedText().space().join(text);
        }
    }

    private boolean inFuture(Optional<Date> startingDate) {
        return startingDate.isPresent() && startingDate.get().after(new Date());
    }

    private boolean startedToday(Optional<Date> startingDate) {
        return startingDate.isPresent() && DateUtils.dayDiff(DateUtils.asCalendar(startingDate.get())) == 0;
    }

    private String startingDate(Task task) {
        Optional<Date> startingDate = task.getStartingDate();
        if (!startingDate.isPresent()) {
            return "anytime";
        }
        long days = DateUtils.daysBefore(startingDate.get());
        if (days > 0) {
            return "starting in " + days + " days";
        } else if (days == 0) {
            return "starting tomorrow";
        } else if (days == -1) {
            return "started today";
        } else {
            return "started " + DateUtils.optionalDateToString(startingDate);
        }
    }

    public SpannedText addDueDateWarning(Task task) {
        if (task.getDueDate().isPresent() && task.getStatus().isActive()) {
            String dueDate = dueDate(task);
            if (TaskListService.TODAY_PREDICATE().apply(task)) {
                return new SpannedText().space().join(dueDate,
                        new ForegroundColorSpan(TimeConstraintsUtils.TODAY_FG_COLOR),
                        new BackgroundColorSpan(TimeConstraintsUtils.TODAY_BG_COLOR)
                );
            } else if (TaskListService.OVERDUE_PREDICATE().apply(task)) {
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


    private String dueDate(Task task) {
        Optional<Date> dueDate = task.getDueDate();
        if (!dueDate.isPresent()) {
            return "anytime";
        }
        long days = DateUtils.daysBefore(dueDate.get());
        if (days > 1) {
            return "in " + days + " days";
        } else if (days == 1) {
            return "tomorrow";
        } else if (days == 0) {
            return "today";
        } else {
            return "due to " + DateUtils.optionalDateToString(dueDate);
        }
    }
}
