package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;

import static com.google.common.collect.Lists.newArrayList;

public class ListActivity extends Activity {

    private ListView taskList;
    private TaskRepository taskRepository;
    private static final int EDIT_REQUEST = 1;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        taskList = (ListView) findViewById(R.id.taskList);
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver()));
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        Iterable<Task> tasks = taskRepository.getAll();
        taskList.setAdapter(new TaskItemAdapter(this, new TaskUpdateListener() {

            @Override
            public void onTaskSelected(Task selectedTask) {
                editTask(selectedTask);
            }

            @Override
            public void onTaskUpdated(Task updatedTask) {
                taskRepository.save(updatedTask);
            }
        }, newArrayList(tasks)));
    }

    private void editTask(Task task) {
        Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, task.getId());
        Intent intent = new Intent("edit", uri, this, TaskEditActivity.class);
        startActivityForResult(intent, EDIT_REQUEST);
    }

    public void onNewTask(View view) {
        Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, -1);
        Intent intent = new Intent("edit", uri, this, TaskEditActivity.class);
        startActivityForResult(intent, EDIT_REQUEST);
    }
}
