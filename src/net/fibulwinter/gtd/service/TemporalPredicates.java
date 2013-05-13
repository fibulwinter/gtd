package net.fibulwinter.gtd.service;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;

public class TemporalPredicates {
    private TemporalLogic temporalLogic;
    private Predicate<Task> startedTodayPredicate = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            Optional<Date> startingDate = task.getStartingDate();
            return startingDate.isPresent() && temporalLogic.relativeDays(startingDate.get()) == 0;
        }
    };
    private Predicate<Task> notStartedPredicate = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            Optional<Date> startingDate = task.getStartingDate();
            return startingDate.isPresent() && temporalLogic.relativeDays(startingDate.get()) > 0;
        }
    };
    private Predicate<Task> dueTomorrowPredicate = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            Optional<Date> dueDate = task.getDueDate();
            return dueDate.isPresent() && temporalLogic.relativeDays(dueDate.get()) == 1;
        }
    };
    private Predicate<Task> overduePredicate = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            Optional<Date> dueDate = task.getDueDate();
            return dueDate.isPresent() && temporalLogic.relativeDays(dueDate.get()) < 1;
        }
    };

    public TemporalPredicates() {
        this(new TemporalLogic());
    }

    public TemporalPredicates(TemporalLogic temporalLogic) {
        this.temporalLogic = temporalLogic;
    }

    public Predicate<Task> startedToday() {
        return startedTodayPredicate;
    }

    public Predicate<Task> notStarted() {
        return notStartedPredicate;
    }

    public Predicate<Task> dueTomorrow() {
        return dueTomorrowPredicate;
    }

    public Predicate<Task> overdue() {
        return overduePredicate;
    }
}
