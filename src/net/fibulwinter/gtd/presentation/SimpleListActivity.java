package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public abstract class SimpleListActivity extends Activity {

    protected TaskListService taskListService;
    private TimeFilterControl timeFilterControl;
    private TaskItemAdapter taskItemAdapter;
    private TaskItemAdapterConfig taskItemAdapterConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_action_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        timeFilterControl = (TimeFilterControl) findViewById(R.id.timeFilter);

        ContextRepository contextRepository = new ContextRepository();
        taskListService = new TaskListService(new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository)));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskListService);
        taskItemAdapterConfig = getConfig();
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        taskList.setAdapter(taskItemAdapter);

        timeFilterControl.setListener(new SpinnerUtils.ContextSpinnerListener() {
            @Override
            public void onSelectedContext(Context context) {
                fillData();
            }
        });
        Button button = (Button) findViewById(R.id.new_task);
        button.setText(getResources().getString(newTaskText()));

    }

    protected abstract int newTaskText();

    protected abstract TaskItemAdapterConfig getConfig();

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        ArrayList<Task> taskList = newArrayList(loadActions());
        Iterable<Task> tasks = timeFilterControl.updateOn(taskList);
        ArrayList<Task> taskArrayList = newArrayList(tasks);
        Collections.sort(taskArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        //todo
//        taskItemAdapterConfig.setShowContext(context.isSpecial());
        taskItemAdapter.setData(taskArrayList);
//        timeFilterControl.updateOn(taskListService);
    }

    protected abstract Iterable<Task> loadActions();

    public void onNewTask(View view) {
        Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, -1);
        Intent intent = new Intent("edit", uri, this, TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.TYPE, getNewStatus());
        startActivity(intent);
    }

    protected abstract TaskStatus getNewStatus();


}
