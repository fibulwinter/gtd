package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public abstract class SimpleListActivity extends Activity {

    protected TaskListService taskListService;
    private TimeFilterControl timeFilterControl;
    private TaskItemAdapter taskItemAdapter;
    private TaskItemAdapterConfig taskItemAdapterConfig;
    private ListView taskList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_action_list);
        taskList = (ListView) findViewById(R.id.taskList);
        timeFilterControl = (TimeFilterControl) findViewById(R.id.timeFilter);

        ContextRepository contextRepository = new ContextRepository();
        taskListService = new TaskListService(new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository)));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskListService);
        taskItemAdapterConfig = getConfig();
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        taskList.setAdapter(taskItemAdapter);

        timeFilterControl.setListener(new Runnable() {
            @Override
            public void run() {
                fillData();
            }
        });
    }

    protected abstract TaskItemAdapterConfig getConfig();

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        ArrayList<Task> taskList = newArrayList(loadActions());
        List<Task> taskArrayList = newArrayList(timeFilterControl.updateOn(taskList));
        Collections.sort(taskArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().trim().toLowerCase().compareTo(task1.getText().trim().toLowerCase());
            }
        });
        taskItemAdapter.setData(taskArrayList);
//        timeFilterControl.updateOn(taskListService);
    }

    protected abstract Iterable<Task> loadActions();

    public void onNewTask(View view) {
        new EditDialogFactory(this).showTitleDialog("", Context.DEFAULT, "Enter new task title and context", new EditDialogFactory.TitleEdited() {
            @Override
            public void onValidText(String title, Context context) {
                Task task = new Task(title);
                task.setStatus(getNewStatus());
                task.setContext(context);
                taskListService.save(task);
                fillData();
                taskItemAdapter.setHighlightedTask(Optional.of(task));
                taskList.setSelection(taskItemAdapter.getPosition(task));
            }
        });
    }

    protected abstract TaskStatus getNewStatus();


}
