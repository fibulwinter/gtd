package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public abstract class SimpleListActivity extends Activity {

    protected TaskListService taskListService;
    private TimeFilterControl timeFilterControl;
    protected TaskItemAdapter taskItemAdapter;
    private TaskItemAdapterConfig taskItemAdapterConfig;
    private ListView taskList;
    private ImageButton sortButton;
    private List<SortAndGroup> sortAndGroups = newArrayList();
    private SortAndGroup sortAndGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_action_list);
        taskList = (ListView) findViewById(R.id.taskList);
        timeFilterControl = (TimeFilterControl) findViewById(R.id.timeFilter);
        sortButton = (ImageButton) findViewById(R.id.sortBtn);

        ContextRepository contextRepository = new ContextRepository();
        taskListService = new TaskListService(new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository)));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskListService);
        taskItemAdapterConfig = getConfig();
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        taskList.setAdapter(taskItemAdapter);

        sortAndGroups.add(new SortAndGroup() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().trim().toLowerCase().compareTo(task1.getText().trim().toLowerCase());
            }

            @Override
            public Optional<?> apply(Task task) {
                return Optional.absent();
            }

            @Override
            public int getResource() {
                return R.drawable.ic_menu_sort_alphabetically;
            }

            @Override
            public String toString() {
                return "A-Z";
            }
        });
        sortAndGroups.add(new SortAndGroup() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getContext().getName().compareTo(task1.getContext().getName());
            }

            @Override
            public Optional<?> apply(Task task) {
                return Optional.of(task.getContext().getName());
            }

            @Override
            public int getResource() {
                return R.drawable.ic_menu_sort_context;
            }

            @Override
            public String toString() {
                return "@";
            }
        });
        sortAndGroups.add(new SortAndGroup() {
            @Override
            public int compare(Task task, Task task1) {
                Optional<Date> dueDate = task.getDueDate();
                Optional<Date> dueDate1 = task1.getDueDate();
                if (!dueDate.isPresent() && !dueDate1.isPresent()) return 0;
                if (dueDate.isPresent() && !dueDate1.isPresent()) return -1;
                if (!dueDate.isPresent() && dueDate1.isPresent()) return 1;
                return dueDate.get().compareTo(dueDate1.get());
            }

            @Override
            public Optional<?> apply(Task task) {
                if (task.getDueDate().isPresent()) {
                    return Optional.of(new TimeConstraintsUtils(new TemporalLogic()).dueDate(task));
                } else {
                    return Optional.of("Anytime");
                }
            }

            @Override
            public int getResource() {
                return R.drawable.ic_menu_recent_history;
            }

            @Override
            public String toString() {
                return "t";
            }
        });
        sortAndGroup = sortAndGroups.get(sortAndGroups.size() - 1);
        onSort(null);

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
        ArrayList<Task> arrayList = newArrayList(loadActions());
        List<Task> taskArrayList = newArrayList(timeFilterControl.updateOn(arrayList));
        Collections.sort(taskArrayList, sortAndGroup);
        taskItemAdapter.setData(taskArrayList);
//        timeFilterControl.updateOn(taskListService);
    }

    protected abstract Iterable<Task> loadActions();

    public void onSort(View view) {
        sortAndGroup = sortAndGroups.get((sortAndGroups.indexOf(sortAndGroup) + 1) % sortAndGroups.size());
        sortButton.setImageDrawable(getResources().getDrawable(sortAndGroup.getResource()));
        taskItemAdapter.setGroupFunction(sortAndGroup);
        fillData();
    }

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
