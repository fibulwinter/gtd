package net.fibulwinter.gtd.service;

import com.google.common.base.Predicate;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;

import java.util.Date;

import static com.google.common.collect.FluentIterable.from;

public class TaskListService {
    private static Predicate<Task> CAN_START_PREDICATE() {
        return new Predicate<Task>() {

            private Date now;

            @Override
            public boolean apply(Task task) {
                now = new Date();
                return !task.getStartingDate().isPresent() || task.getStartingDate().get().before(now);
            }
        };
    }

    private static Predicate<Task> OVERDUE_PREDICATE() {
        return new Predicate<Task>() {
            private Date now;

            @Override
            public boolean apply(Task task) {
                now = new Date();
                return task.getStatus().isActive() && task.getDueDate().isPresent() && task.getDueDate().get().before(now);
            }
        };
    }

    private static Predicate<Task> TODAY_PREDICATE() {
        return new Predicate<Task>() {
            private Date nextMidnight = DateUtils.nextMidnight(new Date());

            @Override
            public boolean apply(Task task) {
                return task.getStatus().isActive() && task.getDueDate().isPresent() && task.getDueDate().equals(nextMidnight);
            }
        };
    }

    ;

    private static final Predicate<Task> ACTIVE_PREDICATE = new Predicate<Task>() {
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

    private static final Predicate<Task> WAITING_FOR_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return !task.isProject() && task.getStatus() == TaskStatus.WaitingFor;
        }
    };

    private static final Predicate<Task> MAYBE_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getStatus() == TaskStatus.Maybe;
        }
    };

    private static final Predicate<Task> DONE_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getStatus() == TaskStatus.Completed;
        }
    };

    private static final Predicate<Task> PROJECT_WITHOUT_ACTIONS_PREDICATE = new Predicate<Task>() {
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

    private TaskRepository taskRepository;

    public TaskListService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Iterable<Task> getNextActions() {
        return from(taskRepository.getAll()).filter(NEXT_ACTION_PREDICATE).filter(CAN_START_PREDICATE());
    }

    public Iterable<Task> getWaitingFor() {
        return from(taskRepository.getAll()).filter(WAITING_FOR_PREDICATE).filter(CAN_START_PREDICATE());
    }

    public Iterable<Task> getMaybe() {
        return from(taskRepository.getAll()).filter(MAYBE_PREDICATE).filter(CAN_START_PREDICATE());
    }

    public Iterable<Task> getDone() {
        return from(taskRepository.getAll()).filter(DONE_PREDICATE);
    }

    public Iterable<Task> getProjectsWithoutNextAction() {
        return from(taskRepository.getAll()).filter(PROJECT_WITHOUT_ACTIONS_PREDICATE);
    }

    public Iterable<Task> getOverdueActions() {
        return from(taskRepository.getAll()).filter(OVERDUE_PREDICATE());
    }

    public Iterable<Task> getTodayActions() {
        return from(taskRepository.getAll()).filter(TODAY_PREDICATE());
    }
}
