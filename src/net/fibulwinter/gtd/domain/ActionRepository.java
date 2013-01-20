package net.fibulwinter.gtd.domain;

import com.google.common.collect.Iterables;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class ActionRepository {
    private Map<Long, Task> actions = newHashMap();

    public void save(Task task) {
        actions.put(task.getId(), task);
    }

    public Iterable<Task> getAll() {
        return Iterables.unmodifiableIterable(actions.values());
    }
}
