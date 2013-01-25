package net.fibulwinter.gtd.infrastructure;

import android.text.format.DateFormat;
import com.google.common.base.Optional;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
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
}
