package net.fibulwinter.gtd.presentation;

import net.fibulwinter.gtd.domain.Task;

public interface TaskUpdateListener {
    void onTaskUpdated(Task updatedTask);
}
