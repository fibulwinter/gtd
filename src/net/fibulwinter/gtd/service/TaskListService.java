package net.fibulwinter.gtd.service;

import com.google.common.base.Predicate;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;

import static com.google.common.collect.FluentIterable.from;

public class TaskListService {
    private static final Predicate<Task> NEXT_ACTION_PREDICATE = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return !task.isProject() && task.getStatus().isActive();
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

    public Iterable<Task> getProjectsWithoutNextAction() {
        return from(taskRepository.getAll()).filter(PROJECT_WITHOUT_ACTIONS_PREDICATE);
    }
}
