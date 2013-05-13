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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
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
    private ImageButton searchButton;
    private EditText searchText;
    private boolean searchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_action_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        timeFilterControl = (TimeFilterControl) findViewById(R.id.timeFilter);
        searchButton = (ImageButton) findViewById(R.id.searchButton);
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
        List<Task> taskArrayList;
        if (isSearchMode()) {
            taskArrayList = newArrayList(from(taskList).filter(new Predicate<Task>() {
                @Override
                public boolean apply(Task task) {
                    String s = searchText.getText().toString();
                    return s.isEmpty() || task.getText().toLowerCase().contains(s.toLowerCase());
                }
            }));
        } else {
            taskArrayList = newArrayList(timeFilterControl.updateOn(taskList));
        }
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
        new EditDialogFactory(this).showTitleDialog("", "Enter new task title", new EditDialogFactory.TitleEdited() {
            @Override
            public void onValidText(String title) {
                Task task = new Task(title);
                task.setStatus(getNewStatus());
                taskListService.save(task);
                fillData();
                taskItemAdapter.setHighlightedTask(Optional.of(task));
            }
        });
    }

    public void onSearch(View view) {
        searchMode = !searchMode;
        if (isSearchMode()) {
            searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_search_a));
            timeFilterControl.setVisibility(View.INVISIBLE);
            searchText.setVisibility(View.VISIBLE);
            fillData();
        } else {
            searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_btn_search));
            timeFilterControl.setVisibility(View.VISIBLE);
            searchText.setVisibility(View.INVISIBLE);
            fillData();
        }
    }

    private boolean isSearchMode() {
        return searchMode;
    }

    protected abstract TaskStatus getNewStatus();


}
