package net.fibulwinter.gtd.domain;

import com.google.common.base.Optional;

public class TaskRepository {
    private final TaskDAO taskDAO;

    public TaskRepository(TaskDAO taskDAO) {
        this.taskDAO = taskDAO;
        save(new Task("Create a doom day device: attempt " + (int) (Math.random() * 1000)));
    }

    public void save(Task task) {
        taskDAO.save(task);
    }

    public Iterable<Task> getAll() {
        return taskDAO.getAll();
    }

    public Optional<Task> getById(long id) {
        return taskDAO.getById(id);
    }
}
