package net.fibulwinter.gtd.domain;

import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;

public class TaskRepository {
    private final TaskDAO taskDAO;
    private final TaskExportService taskExportService;
    private TaskImportService taskImportService;

    public TaskRepository(TaskDAO taskDAO, TaskExportService taskExportService, TaskImportService taskImportService) {
        this.taskDAO = taskDAO;
        this.taskExportService = taskExportService;
        this.taskImportService = taskImportService;
    }

    public Iterable<Task> getAll() {
        return getIdMap().values();
    }

    public Optional<Task> getById(long id) {
        Map<Long, Task> idMap = getIdMap();
        return idMap.containsKey(id) ? Optional.of(idMap.get(id)) : Optional.<Task>absent();
    }

    public void save(Task task) {
        taskDAO.save(task);
    }

    public void delete(Task task) {
        taskDAO.delete(task);
    }

    public String exportTasks() {
        return taskExportService.export(getAll());
    }

    public int importTasks() {
        List<Task> tasks = taskImportService.importTasks();
        int count = tasks.size();
        if (count > 0) {
            Map<Task, Long> allTasks = taskDAO.getAll();
            for (Task task : allTasks.keySet()) {
                taskDAO.delete(task);
            }
            for (Task task : tasks) {
                taskDAO.save(task);
            }
        }
        return count;
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
}
