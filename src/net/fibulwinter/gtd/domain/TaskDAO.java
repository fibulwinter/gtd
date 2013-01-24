package net.fibulwinter.gtd.domain;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;

import java.util.Date;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class TaskDAO {
    public static final String[] PROJECTION = new String[]{
            TaskTableColumns.TASK_ID,
            TaskTableColumns.TITLE,
            TaskTableColumns.STATUS,
            TaskTableColumns.MASTER,
            TaskTableColumns.START_DATE,
            TaskTableColumns.DUE_DATE
    };
    private ContentResolver contentResolver;

    public TaskDAO(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public Map<Task, Long> getAll() {
        Map<Task, Long> tasks = newHashMap();
        Cursor cursor = contentResolver.query(
                TaskTableColumns.CONTENT_URI,
                PROJECTION,
                null,
                new String[]{},
                TaskTableColumns.TASK_ID);
        while (cursor.moveToNext()) {
            Task task = cursor2LevelRecord(cursor);
            tasks.put(task, cursor2MasterId(cursor));
        }
        cursor.close();
        return tasks;
    }

    public Optional<Task> getById(long levelId) {
        Cursor cursor = contentResolver.query(
                TaskTableColumns.CONTENT_URI,
                PROJECTION,
                TaskTableColumns.TASK_ID + "=?",
                new String[]{String.valueOf(levelId)},
                null);
        if (cursor.getCount() == 0) {
            return Optional.absent();
        }
        cursor.moveToNext();
        Task levelRecord = cursor2LevelRecord(cursor);
        cursor.close();
        return Optional.of(levelRecord);
    }

    public void save(Task task) {
        ContentValues values = new ContentValues();
        values.put(TaskTableColumns.TASK_ID, task.getId());
        values.put(TaskTableColumns.TITLE, task.getText());
        values.put(TaskTableColumns.STATUS, task.getStatus().name());
        values.put(TaskTableColumns.MASTER, task.getMasterTask().isPresent() ? task.getMasterTask().get().getId() : 0);
        values.put(TaskTableColumns.START_DATE, task.getStartingDate().isPresent() ? task.getStartingDate().get().getTime() : 0);
        values.put(TaskTableColumns.DUE_DATE, task.getDueDate().isPresent() ? task.getDueDate().get().getTime() : 0);
        int updatedRows = contentResolver.update(
                TaskTableColumns.CONTENT_URI,
                values,
                TaskTableColumns.TASK_ID + "=? ",
                new String[]{Long.toString(task.getId())});
        if (updatedRows == 0) {
            contentResolver.insert(TaskTableColumns.CONTENT_URI, values);
        }
    }

    private Task cursor2LevelRecord(Cursor cursor) {
        long id = cursor.getLong(0);
        String text = cursor.getString(1);
        TaskStatus status = TaskStatus.valueOf(cursor.getString(2));
        long startDate = cursor.getLong(4);
        long dueDate = cursor.getLong(5);
        Task task = new Task(id, text, status);
        if (startDate != 0) {
            task.setStartingDate(Optional.of(new Date(startDate)));
        }
        if (dueDate != 0) {
            task.setDueDate(Optional.of(new Date(dueDate)));
        }
        return task;
    }

    private long cursor2MasterId(Cursor cursor) {
        return cursor.getLong(3);
    }

}