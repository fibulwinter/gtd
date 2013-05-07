package net.fibulwinter.gtd.presentation;

import net.fibulwinter.gtd.domain.Task;

public interface TaskUpdateListener {
    void onTaskSelected(Task selectedTask);

    void onTaskUpdated(Task updatedTask);

    void onTaskDeleted(Task deletedTask);
}
