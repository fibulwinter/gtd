package net.fibulwinter.gtd.presentation;

import android.content.res.Resources;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;

public class NextTemporalMarshaller {
    private Resources resources;

    public NextTemporalMarshaller(Resources resources) {
        this.resources = resources;
    }

    public String marshall(TemporalLogic.NextTemporal nextTemporal) {
        TemporalLogic.DateType dateType = nextTemporal.getDateType();
        int relativeDays = nextTemporal.getRelativeDays();
        switch (dateType) {
            case OVERDUE:
                if (relativeDays == 0) {
                    return resources.getString(R.string.yesterday);
                } else {
                    return resources.getQuantityString(R.plurals.overdue_n_days, (1 - relativeDays), (1 - relativeDays));
                }
            case DUE_TO:
                if (relativeDays == 1) {
                    return resources.getString(R.string.today);
                } else if (relativeDays == 2) {
                    return resources.getString(R.string.tomorrow);
                } else if (relativeDays == 3) {
                    return resources.getQuantityString(R.plurals.in_n_days, relativeDays, relativeDays);
                } else if (relativeDays <= 7) {
                    return resources.getString(R.string.in_a_week);
                } else if (relativeDays <= 14) {
                    return resources.getString(R.string.in_2_weeks);
                } else if (relativeDays <= 21) {
                    return resources.getString(R.string.in_3_weeks);
                } else if (relativeDays <= 31) {
                    return resources.getString(R.string.in_a_month);
                } else if (relativeDays <= 365) {
                    return resources.getQuantityString(R.plurals.in_n_mounths, (relativeDays / 30), (relativeDays / 30));
                } else {
                    return resources.getString(R.string.in_future);
                }
            case ANYTIME:
                return resources.getString(R.string.anytime);
            case STARTING:
                if (relativeDays == 1) {
                    return resources.getString(R.string.starting_tomorrow);
                } else if (relativeDays == 2) {
                    return resources.getQuantityString(R.plurals.starting_in_n_days, relativeDays, relativeDays);
                } else if (relativeDays <= 7) {
                    return resources.getString(R.string.starting_in_a_week);
                } else if (relativeDays <= 14) {
                    return resources.getQuantityString(R.plurals.starting_in_n_weeks, 2, 2);
                } else if (relativeDays <= 21) {
                    return resources.getQuantityString(R.plurals.starting_in_n_weeks, 3, 3);
                } else if (relativeDays <= 31) {
                    return resources.getString(R.string.starting_in_a_month);
                } else if (relativeDays <= 365) {
                    return resources.getQuantityString(R.plurals.starting_in_n_months, (relativeDays / 30), (relativeDays / 30));
                } else {
                    return resources.getString(R.string.starting_in_future);
                }
            case DONE:
                return resources.getString(R.string.done);
        }
        throw new IllegalStateException();
    }
}
