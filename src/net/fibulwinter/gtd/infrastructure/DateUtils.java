package net.fibulwinter.gtd.infrastructure;

import android.text.format.DateFormat;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {

    public static final int MS_IN_24H = 24 * 60 * 60 * 1000;

    public static String optionalDateToString(Optional<Date> dateOptional) {
        if (dateOptional.isPresent()) {
            return DateFormat.format("yyyy-MM-dd", dateOptional.get()).toString();
//            return dateOptional.get().toString();
//            GregorianCalendar calendar = new GregorianCalendar();
//            calendar.setTime(dateOptional.get());
//            return calendar.get(Calendar.YEAR)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            return "<Any>";
        }
    }

    public static String optionalDateTimeToString(Optional<Date> dateTimeOptional) {
        if (dateTimeOptional.isPresent()) {
            return dateTimeToString(dateTimeOptional.get());
//            return dateOptional.get().toString();
//            GregorianCalendar calendar = new GregorianCalendar();
//            calendar.setTime(dateOptional.get());
//            return calendar.get(Calendar.YEAR)+"."+(calendar.get(Calendar.MONTH)+1)+"."+calendar.get(Calendar.DAY_OF_MONTH);
        } else {
            return "<>";
        }
    }

    public static String dateTimeToString(Date date) {
        return DateFormat.format("yyyy-MM-dd hh:mm:ss", date).toString();
    }

    public static Date nextMidnight(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static Date nextDay(Date date) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTime();
    }

    public static long daysBefore(Date date) {
        long ms = date.getTime() - System.currentTimeMillis();
        if (ms < 0) {
            return -1;
        } else {
            return ms / MS_IN_24H;
        }
    }
}
