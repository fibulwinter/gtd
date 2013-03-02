package net.fibulwinter.gtd.service;

import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;

public class TaskExportService {
    private TaskRepository taskRepository;

    public TaskExportService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public String export(){
        Iterable<Task> tasks = taskRepository.getAll();

    }
}
