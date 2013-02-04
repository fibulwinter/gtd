package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.service.TaskListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.google.common.collect.Lists.newArrayList;

public class DoneListActivity extends Activity {
    private TaskListService taskListService;
    private TaskItemAdapter taskItemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.done_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        TaskRepository taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), new ContextRepository()));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskRepository);
        taskListService = new TaskListService(taskRepository);
        TaskItemAdapterConfig taskItemAdapterConfig = new TaskItemAdapterConfig();
        taskItemAdapterConfig.setAllowChangeStatus(false);
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        taskList.setAdapter(taskItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        Iterable<Task> tasks = taskListService.getDone();
        ArrayList<Task> tasksArrayList = newArrayList(tasks);
        Collections.sort(tasksArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        taskItemAdapter.setData(tasksArrayList);
    }
}
