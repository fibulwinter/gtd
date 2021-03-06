package net.fibulwinter.gtd.infrastructure;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.text.format.DateFormat;
import com.google.common.base.Optional;

public class DateMarshaller {

    public static String optionalDateToString(Optional<Date> dateOptional) {
        if (dateOptional.isPresent()) {
            return DateFormat.format("yyyy-MM-dd", dateOptional.get()).toString();
        } else {
            return "<Any>";
        }
    }

    public static Optional<Date> stringToOptionalDate(String string) {
        if ("<Any>".equals(string)) {
            return Optional.absent();
        } else {
            try {
                return Optional.of(new SimpleDateFormat("yyyy-MM-dd").parse(string));
            } catch (ParseException e) {
                return Optional.absent();
            }
        }
    }

    public static Optional<Date> stringToOptionalDateTime(String string) {
        if ("<>".equals(string)) {
            return Optional.absent();
        } else {
            try {
                return Optional.of(new SimpleDateFormat("yyyy-MM-dd kk:mm:ss").parse(string));
            } catch (ParseException e) {
                return Optional.absent();
            }
        }
    }

    public static String optionalDateTimeToString(Optional<Date> dateTimeOptional) {
        if (dateTimeOptional.isPresent()) {
            return dateTimeToString(dateTimeOptional.get());
        } else {
            return "<>";
        }
    }

    public static String dateTimeToString(Date date) {
        return DateFormat.format("yyyy-MM-dd kk:mm:ss", date).toString();
    }

    public static String dateTimeTo2Strings(Date date) {
        return DateFormat.format("yyyy-MM-dd\nkk:mm:ss", date).toString();
    }

    public static String timeToString(Date date) {
        return DateFormat.format("kk:mm:ss", date).toString();
    }

    public static String dateToString(Date date) {
        return DateFormat.format("EE, yyyy-MM-dd", date).toString();
    }
}
