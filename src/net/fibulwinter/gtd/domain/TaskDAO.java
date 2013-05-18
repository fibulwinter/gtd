package net.fibulwinter.gtd.domain;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Date;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;

public class TaskDAO {
    public static final String[] PROJECTION = new String[]{
            TaskTableColumns.TASK_ID,
            TaskTableColumns.TITLE,
            TaskTableColumns.STATUS,
            TaskTableColumns.MASTER,
            TaskTableColumns.START_DATE,
            TaskTableColumns.DUE_DATE,
            TaskTableColumns.CONTEXT,
            TaskTableColumns.COMPLETED_DATE,
            TaskTableColumns.CREATED_DATE
    };
    private ContentResolver contentResolver;
    private final ContextRepository contextRepository;

    public TaskDAO(ContentResolver contentResolver, ContextRepository contextRepository) {
        this.contentResolver = contentResolver;
        this.contextRepository = contextRepository;
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
        values.put(TaskTableColumns.CONTEXT, Context.DEFAULT.equals(task.getContext()) ? "" : task.getContext().getName());
        values.put(TaskTableColumns.COMPLETED_DATE, task.getCompleteDate().isPresent() ? task.getCompleteDate().get().getTime() : 0);
        values.put(TaskTableColumns.CREATED_DATE, task.getCreatedDate().getTime());
        int updatedRows = contentResolver.update(
                TaskTableColumns.CONTENT_URI,
                values,
                TaskTableColumns.TASK_ID + "=? ",
                new String[]{Long.toString(task.getId())});
        if (updatedRows == 0) {
            contentResolver.insert(TaskTableColumns.CONTENT_URI, values);
        }
    }

    public void delete(Task task) {
        for (Task subTask : task.getSubTasks()) {
            delete(subTask);
        }
        contentResolver.delete(
                TaskTableColumns.CONTENT_URI,
                TaskTableColumns.TASK_ID + "=? ",
                new String[]{Long.toString(task.getId())});
    }

    private Task cursor2LevelRecord(Cursor cursor) {
        long id = cursor.getLong(0);
        String text = cursor.getString(1);
        String statusString = cursor.getString(2);
        if ("Project".equals(statusString)) {
            statusString = TaskStatus.NextAction.name();
        }
        TaskStatus status = TaskStatus.valueOf(statusString);
        long startDate = cursor.getLong(4);
        long dueDate = cursor.getLong(5);
        String context = cursor.getString(6);
        long completeDate = cursor.getLong(7);
        long createdDate = cursor.getLong(8);
        Task task = new Task(id, text, status, new Date(createdDate));
        if (startDate != 0) {
            task.setStartingDate(Optional.of(new Date(startDate)));
        }
        if (dueDate != 0) {
            task.setDueDate(Optional.of(new Date(dueDate)));
        }
        if (!Strings.isNullOrEmpty(context)) {
            Optional<Context> contextOptional = contextRepository.getByName(context);
            if (!contextOptional.isPresent()) {
                Log.e("gtd", context + " is not found");
                contextOptional = Optional.of(Context.DEFAULT);
            }
            task.setContext(contextOptional.get());
        }
        if (completeDate != 0) {
            task.setCompleteDate(Optional.of(new Date(completeDate)));
        }
        return task;
    }

    private long cursor2MasterId(Cursor cursor) {
        return cursor.getLong(3);
    }
}