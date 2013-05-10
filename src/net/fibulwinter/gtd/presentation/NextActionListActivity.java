package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.FluentIterable.from;
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
import android.widget.ListView;
import android.widget.Spinner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public class NextActionListActivity extends Activity {

    private TaskListService taskListService;
    private TimeFilterControl timeFilterControl;
    private Spinner contextSpinner;
    private TaskItemAdapter taskItemAdapter;
    private Context context = Context.ANY;
    private TaskItemAdapterConfig taskItemAdapterConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_action_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        contextSpinner = (Spinner) findViewById(R.id.context_spinner);
        timeFilterControl = (TimeFilterControl) findViewById(R.id.timeFilter);

        ContextRepository contextRepository = new ContextRepository();
        TaskRepository taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskRepository);
        taskListService = new TaskListService(taskRepository);
        taskItemAdapterConfig = TaskItemAdapterConfig.list();
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        taskList.setAdapter(taskItemAdapter);

        SpinnerUtils.ContextSpinnerListener contextSpinnerListener = new SpinnerUtils.ContextSpinnerListener() {
            @Override
            public void onSelectedContext(Context context) {
                NextActionListActivity.this.context = context;
                fillData();
            }
        };
        SpinnerUtils.setupContextSpinner(this, contextRepository, contextSpinner, contextSpinnerListener);
        timeFilterControl.setListener(contextSpinnerListener);

    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        SpinnerUtils.setSelection(contextSpinner, context);
        Iterable<Task> tasks = from(taskListService.getNextActions()).filter(new Predicate<Task>() {
            @Override
            public boolean apply(Task task) {
                return context.match(task);
            }
        });
        ArrayList<Task> taskArrayList = newArrayList(tasks);
        Collections.sort(taskArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        taskItemAdapterConfig.setShowContext(context.isSpecial());
        taskItemAdapter.setData(taskArrayList);
        int todaySize = Iterables.size(taskListService.getTodayActions());
        int overdueSize = Iterables.size(taskListService.getOverdueActions());
        int startedTodayCounter = Iterables.size(taskListService.getStartedTodayActions());
        int notStartedCounter = Iterables.size(taskListService.getNotStartedActions());

        timeFilterControl.setTodayCounter(todaySize);
        timeFilterControl.setOverdueCounter(overdueSize);
        timeFilterControl.setStartedTodayCounter(startedTodayCounter);
        timeFilterControl.setNotStartedCounter(notStartedCounter);
    }

    public void onNewTask(View view) {
        Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, -1);
        Intent intent = new Intent("edit", uri, this, TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.TYPE, TaskStatus.NextAction);
        startActivity(intent);
    }


}
