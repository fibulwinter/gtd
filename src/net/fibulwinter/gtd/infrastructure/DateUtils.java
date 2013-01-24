package net.fibulwinter.gtd.infrastructure;

import android.text.format.DateFormat;
import com.google.common.base.Optional;

import java.util.Date;

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
}
