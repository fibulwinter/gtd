package net.fibulwinter.gtd.service;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.FluentIterable.from;

import java.util.Date;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;

public class TaskListService {
    public static Predicate<Task> CAN_START_PREDICATE() {
        return new Predicate<Task>() {

            private Date now = new Date();

            @Override
            public boolean apply(Task task) {
                return !task.getStartingDate().isPresent() || task.getStartingDate().get().before(now);
            }
        };
    }

    public static Predicate<Task> OVERDUE_PREDICATE() {
        return new Predicate<Task>() {
            private Date now = new Date();

            @Override
            public boolean apply(Task task) {
                return task.getStatus().isActive() && task.getDueDate().isPresent() && task.getDueDate().get().before(now);
            }
        };
    }

    public static Predicate<Task> STARTED_TODAY_PREDICATE() {
        return new Predicate<Task>() {
            @Override
            public boolean apply(Task task) {
                Optional<Date> startingDate = task.getStartingDate();
                return startingDate.isPresent() && DateUtils.dayDiff(DateUtils.asCalendar(startingDate.get())) == 0;
            }
        };
    }

    public static Predicate<Task> NOT_STARTED_PREDICATE() {
        return new Predicate<Task>() {
            @Override
            public boolean apply(Task task) {
                Optional<Date> startingDate = task.getStartingDate();
                return startingDate.isPresent() && DateUtils.dayDiff(DateUtils.asCalendar(startingDate.get())) > 0;
            }
        };
    }

    public static Predicate<Task> TODAY_PREDICATE() {
        return new Predicate<Task>() {
            private Date now = new Date();
            private Date plusDay = DateUtils.nextDay(now);

            @Override
            public boolean apply(Task task) {
                return task.getStatus().isActive() && task.getDueDate().isPresent()
                        && task.getDueDate().get().after(now)
                        && task.getDueDate().get().before(plusDay);
            }
        };
    }

    ;

    public static final Predicate<Task> ACTIVE_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getStatus().isActive();
        }
    };

    private static final Predicate<Task> NEXT_ACTION_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return !task.isProject() && task.getStatus() == TaskStatus.NextAction;
        }
    };

    private static final Predicate<Task> MAYBE_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getStatus() == TaskStatus.Maybe;
        }
    };

    private static final Predicate<Task> COMPLETED_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getCompleteDate().isPresent();
        }
    };

    public static final Predicate<Task> PROJECT_WITHOUT_ACTIONS_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.isProject() && task.getStatus().isActive() && (from(task.getSubTasks()).allMatch(new Predicate<Task>() {
                @Override
                public boolean apply(Task task) {
                    return !task.getStatus().isActive();
                }
            }));
        }
    };

    private static final Predicate<Task> PROJECT_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.isProject();
        }
    };

    private static final Predicate<Task> TOP_PROJECT_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.isProject() && !task.getMasterTask().isPresent();
        }
    };

    private TaskRepository taskRepository;

    public TaskListService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Iterable<Task> getNextActions() {
        return from(taskRepository.getAll()).filter(and(ACTIVE_PREDICATE, not(PROJECT_PREDICATE)));
    }

    public Iterable<Task> getMaybe() {
        return from(taskRepository.getAll()).filter(and(MAYBE_PREDICATE, CAN_START_PREDICATE()));
    }

    public Iterable<Task> getCompleted() {
        return from(taskRepository.getAll()).filter(COMPLETED_PREDICATE);
    }

    public Iterable<Task> getProjectsWithoutNextAction() {
        return from(taskRepository.getAll()).filter(PROJECT_WITHOUT_ACTIONS_PREDICATE);
    }

    public Iterable<Task> getOverdueActions() {
        return from(taskRepository.getAll()).filter(OVERDUE_PREDICATE());
    }

    public Iterable<Task> getNotStartedActions() {
        return from(taskRepository.getAll()).filter(NOT_STARTED_PREDICATE());
    }

    public Iterable<Task> getStartedTodayActions() {
        return from(taskRepository.getAll()).filter(STARTED_TODAY_PREDICATE());
    }

    public Iterable<Task> getTodayActions() {
        return from(taskRepository.getAll()).filter(TODAY_PREDICATE());
    }

    public Iterable<Task> getTopProjects() {
        return from(taskRepository.getAll()).filter(and(ACTIVE_PREDICATE, TOP_PROJECT_PREDICATE));
    }

    public Iterable<Task> getProjects() {
        return from(taskRepository.getAll()).filter(and(ACTIVE_PREDICATE, PROJECT_PREDICATE, CAN_START_PREDICATE()));
    }
}
