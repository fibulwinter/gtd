package net.fibulwinter.gtd.infrastructure;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.google.common.base.Optional;
import net.fibulwinter.gtd.domain.Task;

public class TemporalLogic {
    private Calendar today;

    public TemporalLogic() {
        this(new Date());
    }

    public TemporalLogic(Date now) {
        today = getCalendar(now);
    }

    public int relativeDays(Date date) {
        return relativeDays(getCalendar(date));
    }

    public enum DateType {
        OVERDUE,
        DUE_TO,
        ANYTIME,
        STARTING,
        DONE
    }

    public class NextTemporal implements Comparable<NextTemporal> {
        private DateType dateType;
        private int relativeDays;
        private Task task;

        public NextTemporal(DateType dateType, int relativeDays, Task task) {
            this.dateType = dateType;
            this.relativeDays = relativeDays;
            this.task = task;
        }

        public DateType getDateType() {
            return dateType;
        }

        public int getRelativeDays() {
            return relativeDays;
        }

        public Task getTask() {
            return task;
        }

        @Override
        public int compareTo(NextTemporal nextTemporal) {
            int comp = dateType.compareTo(nextTemporal.dateType);
            if (comp == 0) {
                comp = relativeDays - nextTemporal.relativeDays;
                if (comp == 0) {
                    if (task.getCompleteDate().isPresent() && nextTemporal.task.getCompleteDate().isPresent()) {
                        comp = task.getCompleteDate().get().compareTo(nextTemporal.task.getCompleteDate().get());
                    }
                    if (comp == 0) {
                        comp = task.getText().trim().toLowerCase().compareTo(nextTemporal.task.getText().trim().toLowerCase());
                    }
                }
            }
            return comp;
        }

        @Override
        public String toString() {
            switch (dateType) {
                case OVERDUE:
                    if (relativeDays == 0) {
                        return "Yesterday";
                    } else {
                        return "Overdue " + (1 - relativeDays) + " days";
                    }
                case DUE_TO:
                    if (relativeDays == 1) {
                        return "Today";
                    } else if (relativeDays == 2) {
                        return "Tomorrow";
                    } else if (relativeDays == 3) {
                        return "In " + relativeDays + " days";
                    } else if (relativeDays <= 7) {
                        return "In a week";
                    } else if (relativeDays <= 14) {
                        return "In 2 weeks";
                    } else if (relativeDays <= 21) {
                        return "In 3 weeks";
                    } else if (relativeDays <= 31) {
                        return "In a month";
                    } else if (relativeDays <= 365) {
                        return "In " + (relativeDays / 30) + " months";
                    } else {
                        return "In future";
                    }
                case ANYTIME:
                    return "Anytime";
                case STARTING:
                    if (relativeDays == 1) {
                        return "Starting tomorrow";
                    } else if (relativeDays == 2) {
                        return "Starting in " + relativeDays + " days";
                    } else if (relativeDays <= 7) {
                        return "Starting in a week";
                    } else if (relativeDays <= 14) {
                        return "Starting in 2 weeks";
                    } else if (relativeDays <= 21) {
                        return "Starting in 3 weeks";
                    } else if (relativeDays <= 31) {
                        return "Starting in a month";
                    } else if (relativeDays <= 365) {
                        return "Starting in " + (relativeDays / 30) + " months";
                    } else {
                        return "Starting in future";
                    }
                case DONE:
                    return "Done";
            }
            throw new IllegalStateException();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NextTemporal)) return false;

            NextTemporal that = (NextTemporal) o;

            if (relativeDays != that.relativeDays) return false;
            if (dateType != that.dateType) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = dateType.hashCode();
            result = 31 * result + relativeDays;
            return result;
        }
    }

    public NextTemporal getTimeDetails(Task task) {
        String title = task.getText();
        if (task.getCompleteDate().isPresent()) {
            return new NextTemporal(DateType.DONE, 0, task);
        }
        Optional<Date> startingDate = task.getStartingDate();
        if (startingDate.isPresent()) {
            int relativeDays = relativeDays(startingDate.get());
            if (relativeDays > 0) {
                return new NextTemporal(DateType.STARTING, relativeDays, task);
            }
        }
        Optional<Date> dueDate = task.getDueDate();
        if (dueDate.isPresent()) {
            int relativeDays = relativeDays(dueDate.get());
            if (relativeDays > 0) {
                return new NextTemporal(DateType.DUE_TO, relativeDays, task);
            } else {
                return new NextTemporal(DateType.OVERDUE, relativeDays, task);
            }
        }
        return new NextTemporal(DateType.ANYTIME, 0, task);
    }


    private int relativeDays(Calendar calendar) {
        int days = 0;
        while (after(today, calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            days++;
        }
        while (after(calendar, today)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            days--;
        }
        return days;
    }

    private boolean after(Calendar calendar1, Calendar calendar2) {
        if (calendar2.get(Calendar.YEAR) > calendar1.get(Calendar.YEAR)) {
            return true;
        }
        if (calendar2.get(Calendar.YEAR) < calendar1.get(Calendar.YEAR)) {
            return false;
        }
        if (calendar2.get(Calendar.MONTH) > calendar1.get(Calendar.MONTH)) {
            return true;
        }
        if (calendar2.get(Calendar.MONTH) < calendar1.get(Calendar.MONTH)) {
            return false;
        }
        if (calendar2.get(Calendar.DAY_OF_MONTH) > calendar1.get(Calendar.DAY_OF_MONTH)) {
            return true;
        }
        if (calendar2.get(Calendar.DAY_OF_MONTH) < calendar1.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }
        return false;
    }


    public Calendar getCalendar(Date date) {
        GregorianCalendar today = new GregorianCalendar();
        today.setTime(date);
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today;
    }
}
