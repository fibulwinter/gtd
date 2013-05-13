package net.fibulwinter.gtd.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;

import net.fibulwinter.gtd.infrastructure.TemporalLogic;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test
public class TemporalLogicTest {
    private static final long HOUR_IN_MS = 60 * 60 * 1000L;
    private static final long DAY_IN_MS = 24 * HOUR_IN_MS;

    private Date now;
    private TemporalLogic temporalLogic;

    @BeforeMethod
    public void setUp() {
        now = new Date();
        temporalLogic = new TemporalLogic(now);
    }

    public void testRelativeDaysForNow() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime())), is(0));
    }

    public void testRelativeDaysForTodayFuture() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime() + HOUR_IN_MS)), is(0));
    }

    public void testRelativeDaysForTodayPast() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime() - HOUR_IN_MS)), is(0));
    }

    public void testRelativeDaysForTomorrow() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime() + DAY_IN_MS)), is(1));
    }

    public void testRelativeDaysForYesterday() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime() - DAY_IN_MS)), is(-1));
    }

    public void testRelativeDaysForDayAfterTomorrow() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime() + 2 * DAY_IN_MS)), is(2));
    }

    public void testRelativeDaysForDayBeforeYesterday() {
        assertThat(temporalLogic.relativeDays(new Date(now.getTime() - 2 * DAY_IN_MS)), is(-2));
    }
}
