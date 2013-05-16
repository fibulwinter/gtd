package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ListView;
import com.google.common.base.Predicate;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public class SearchListActivity extends Activity {

    protected TaskListService taskListService;
    private TaskItemAdapter taskItemAdapter;
    private TaskItemAdapterConfig taskItemAdapterConfig;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_action_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        searchText = (EditText) findViewById(R.id.searchText);
        searchText.setLines(1);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                fillData();
            }
        });

        ContextRepository contextRepository = new ContextRepository();
        taskListService = new TaskListService(new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository)));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskListService);
        taskItemAdapterConfig = getConfig();
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        taskList.setAdapter(taskItemAdapter);
    }

    protected TaskItemAdapterConfig getConfig() {
        return TaskItemAdapterConfig.searchList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        ArrayList<Task> taskList = newArrayList(loadActions());
        List<Task> taskArrayList;
        taskArrayList = newArrayList(from(taskList).filter(new Predicate<Task>() {
            @Override
            public boolean apply(Task task) {
                String s = searchText.getText().toString();
                return s.length() == 0 || task.getText().toLowerCase().contains(s.toLowerCase());
            }
        }));
        Collections.sort(taskArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        taskItemAdapter.setData(taskArrayList);
//        timeFilterControl.updateOn(taskListService);
    }

    protected Iterable<Task> loadActions() {
        return taskListService.getAll();
    }

}
