package net.fibulwinter.gtd.service;

import static com.google.common.base.Predicates.*;
import static com.google.common.collect.FluentIterable.from;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.domain.TaskStatus;

public class TaskListService {
    public static final Predicate<Task> ACTIVE_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getStatus().isActive();
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

    private static final Predicate<Task> PROJECT_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.isProject();
        }
    };

    private TaskRepository taskRepository;
    private TemporalPredicates temporalPredicates = new TemporalPredicates();

    public TaskListService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Iterable<Task> getNextActions() {
        return from(taskRepository.getAll()).filter(and(ACTIVE_PREDICATE, not(temporalPredicates.notStarted()), not(PROJECT_PREDICATE)));
    }

    public Iterable<Task> getMaybe() {
        return from(taskRepository.getAll()).filter(MAYBE_PREDICATE);
    }

    public Iterable<Task> getCompleted() {
        return from(taskRepository.getAll()).filter(COMPLETED_PREDICATE);
    }

    public Iterable<Task> getProjects() {
        return from(taskRepository.getAll()).filter(and(ACTIVE_PREDICATE, or(PROJECT_PREDICATE, temporalPredicates.notStarted())));
    }

    public Optional<Task> getById(long id) {
        return taskRepository.getById(id);
    }

    public void save(Task task) {
        taskRepository.save(task);
    }

    public void delete(Task task) {
        taskRepository.delete(task);
    }

    public Iterable<Task> getAll() {
        return taskRepository.getAll();
    }
}
