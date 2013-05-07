package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;

public class TaskUpdateListenerFactory {
    public static TaskUpdateListener simple(final Activity callingActivity, final TaskRepository taskRepository) {
        return new TaskUpdateListener() {

            @Override
            public void onTaskSelected(Task selectedTask) {
                Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, selectedTask.getId());
                Intent intent = new Intent("edit", uri, callingActivity, TaskEditActivity.class);
                callingActivity.startActivity(intent);
            }

            @Override
            public void onTaskUpdated(Task updatedTask) {
                taskRepository.save(updatedTask);
            }

            @Override
            public void onTaskDeleted(Task deletedTask) {
                taskRepository.delete(deletedTask);
            }
        };
    }
}
