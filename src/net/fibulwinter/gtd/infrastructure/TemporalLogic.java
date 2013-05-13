package net.fibulwinter.gtd.infrastructure;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
