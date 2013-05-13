package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskListService;

public class TaskUpdateListenerFactory {
    public static TaskUpdateListener simple(final Activity callingActivity, final TaskListService taskListService) {
        return new TaskUpdateListener() {

            @Override
            public void onTaskSelected(Task selectedTask) {
                Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, selectedTask.getId());
                Intent intent = new Intent("edit", uri, callingActivity, TaskEditActivity.class);
                callingActivity.startActivity(intent);
            }

            @Override
            public void onTaskUpdated(Task updatedTask) {
                taskListService.save(updatedTask);
            }

            @Override
            public void onTaskDeleted(Task deletedTask) {
                taskListService.delete(deletedTask);
            }
        };
    }
}
