package net.fibulwinter.gtd.service;

import com.google.common.base.Predicate;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.domain.TaskStatus;

import static com.google.common.collect.FluentIterable.from;

public class TaskListService {
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
        return from(taskRepository.getAll()).filter(NEXT_ACTION_PREDICATE);
    }

    public Iterable<Task> getWaitingFor() {
        return from(taskRepository.getAll()).filter(WAITING_FOR_PREDICATE);
    }

    public Iterable<Task> getMaybe() {
        return from(taskRepository.getAll()).filter(MAYBE_PREDICATE);
    }

    public Iterable<Task> getDone() {
        return from(taskRepository.getAll()).filter(DONE_PREDICATE);
    }

    public Iterable<Task> getProjectsWithoutNextAction() {
        return from(taskRepository.getAll()).filter(PROJECT_WITHOUT_ACTIONS_PREDICATE);
    }
}
