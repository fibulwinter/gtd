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
        private Optional<Date> optionalDate;

        public NextTemporal(DateType dateType, int relativeDays, Optional<Date> optionalDate) {
            this.dateType = dateType;
            this.relativeDays = relativeDays;
            this.optionalDate = optionalDate;
        }

        public DateType getDateType() {
            return dateType;
        }

        public int getRelativeDays() {
            return relativeDays;
        }

        public Optional<Date> getOptionalDate() {
            return optionalDate;
        }

        @Override
        public int compareTo(NextTemporal nextTemporal) {
            int comp = dateType.compareTo(nextTemporal.dateType);
            if (comp == 0) {
                comp = relativeDays - nextTemporal.relativeDays;
            }
            return comp;
        }

        @Override
        public String toString() {
            switch (dateType) {
                case OVERDUE:
                    return "Overdue";
                case DUE_TO:
                    return "in";
                case ANYTIME:
                    break;
                case STARTING:
                    break;
                case DONE:
                    return "Completed";
            }
            throw new IllegalStateException();
        }
    }

    public NextTemporal getTimeDetails(Task task) {
        if (task.getCompleteDate().isPresent()) {
            return new NextTemporal(DateType.DONE, 0, task.getCompleteDate());
        }
        Optional<Date> startingDate = task.getStartingDate();
        if (startingDate.isPresent()) {
            int relativeDays = relativeDays(startingDate.get());
            if (relativeDays > 0) {
                return new NextTemporal(DateType.STARTING, relativeDays, Optional.<Date>absent());
            }
        }
        Optional<Date> dueDate = task.getDueDate();
        if (dueDate.isPresent()) {
            int relativeDays = relativeDays(dueDate.get());
            if (relativeDays > 0) {
                return new NextTemporal(DateType.DUE_TO, relativeDays, Optional.<Date>absent());
            } else {
                return new NextTemporal(DateType.OVERDUE, relativeDays, Optional.<Date>absent());
            }
        }
        return new NextTemporal(DateType.ANYTIME, 0, Optional.<Date>absent());
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
