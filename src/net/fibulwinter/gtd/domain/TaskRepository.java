package net.fibulwinter.gtd.domain;

import com.google.common.collect.Iterables;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class TaskRepository {
    private Map<Long, Task> tasks = newHashMap();

    public TaskRepository() {
        save(new Task("Create a doom day device"));
        save(new Task("Start WW3"));
    }

    public void save(Task task) {
        tasks.put(task.getId(), task);
    }

    public Iterable<Task> getAll() {
        return Iterables.unmodifiableIterable(tasks.values());
    }
}
