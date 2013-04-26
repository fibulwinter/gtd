package net.fibulwinter.gtd.domain;

import com.google.common.base.Optional;
import net.fibulwinter.gtd.service.TaskExportService;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class TaskRepository {
    private final TaskDAO taskDAO;
    private final TaskExportService taskExportService;

    public TaskRepository(TaskDAO taskDAO, TaskExportService taskExportService) {
        this.taskDAO = taskDAO;
        this.taskExportService = taskExportService;
    }

    public void save(Task task) {
        taskDAO.save(task);
        taskExportService.export(getAll());
    }

    public Iterable<Task> getAll() {
        return getIdMap().values();
    }

    private Map<Long, Task> getIdMap() {
        Map<Task, Long> allTasks = taskDAO.getAll();
        Map<Long, Task> tasksById = newHashMap();
        for (Task task : allTasks.keySet()) {
            tasksById.put(task.getId(), task);
        }
        for (Map.Entry<Task, Long> entry : allTasks.entrySet()) {
            if (entry.getValue() != 0) {
                entry.getKey().setMaster(tasksById.get(entry.getValue()));
            }
        }
        return tasksById;
    }

    public Optional<Task> getById(long id) {
        Map<Long, Task> idMap = getIdMap();
        return idMap.containsKey(id) ? Optional.of(idMap.get(id)) : Optional.<Task>absent();
    }

    public void delete(Task task) {
        taskDAO.delete(task);

    }
}
