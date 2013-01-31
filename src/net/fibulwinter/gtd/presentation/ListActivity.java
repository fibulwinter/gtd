package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Lists.newArrayList;

public class ListActivity extends Activity {

    private static final int EDIT_REQUEST = 1;

    private TaskRepository taskRepository;
    private TaskUpdateListener taskUpdateListener = new TaskUpdateListener() {

        @Override
        public void onTaskSelected(Task selectedTask) {
            editTask(selectedTask);
        }

        @Override
        public void onTaskUpdated(Task updatedTask) {
            taskRepository.save(updatedTask);
        }
    };
    private TaskListService taskListService;
    private TextView todayCounter;
    private TextView overdueCounter;
    private TextView projectsWithouActionCounter;
    private Spinner modeSpinner;
    private TaskItemAdapter taskItemAdapter;

    private enum Mode {
        ALL,
        NEXT_ACTIONS,
        NEXT_ACTIONS_TODAY,
        NEXT_ACTIONS_OVERDUE,
        DONE,
        MAY_BE,
        PROJECTS_TOP,
        PROJECTS_WITHOUT_ACTIONS
    }

    private Mode mode = Mode.NEXT_ACTIONS;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        todayCounter = (TextView) findViewById(R.id.dueTodayCounter);
        overdueCounter = (TextView) findViewById(R.id.overdueCounter);
        projectsWithouActionCounter = (TextView) findViewById(R.id.projectWithoutActionCounter);
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), new ContextRepository()));
        taskListService = new TaskListService(taskRepository);
        modeSpinner = (Spinner) findViewById(R.id.mode_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);
        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mode = Mode.values()[i];
                fillData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, true);
        taskList.setAdapter(taskItemAdapter);
        modeSpinner.setSelection(Mode.NEXT_ACTIONS.ordinal());
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        modeSpinner.setSelection(mode.ordinal());
        Iterable<Task> tasks;
        switch (mode) {
            case ALL:
                tasks = taskRepository.getAll();
                break;
            case NEXT_ACTIONS:
                tasks = taskListService.getNextActions();
                break;
            case DONE:
                tasks = taskListService.getDone();
                break;
            case MAY_BE:
                tasks = taskListService.getMaybe();
                break;
            case PROJECTS_WITHOUT_ACTIONS:
                tasks = taskListService.getProjectsWithoutNextAction();
                break;
            case NEXT_ACTIONS_OVERDUE:
                tasks = taskListService.getOverdueActions();
                break;
            case NEXT_ACTIONS_TODAY:
                tasks = taskListService.getTodayActions();
                break;
            case PROJECTS_TOP:
                tasks = taskListService.getTopProjects();
                break;
            default:
                throw new Error("Missed mode: " + mode);
        }
        ArrayList<Task> taskArrayList = newArrayList(tasks);
        Collections.sort(taskArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        taskItemAdapter.setData(taskArrayList);
        todayCounter.setTextColor(isEmpty(taskListService.getTodayActions()) ? Color.DKGRAY : Color.YELLOW);
        overdueCounter.setTextColor(isEmpty(taskListService.getOverdueActions()) ? Color.DKGRAY : Color.YELLOW);
        projectsWithouActionCounter.setTextColor(isEmpty(taskListService.getProjectsWithoutNextAction())
                ? Color.DKGRAY : Color.YELLOW);
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

    public void onDoneTodayCounter(View view) {
        mode = Mode.DONE;
        fillData();
    }

    public void onDueTodayCounter(View view) {
        mode = Mode.NEXT_ACTIONS_TODAY;
        fillData();
    }

    public void onOverdueCounter(View view) {
        mode = Mode.NEXT_ACTIONS_OVERDUE;
        fillData();
    }

    public void onProjectWithoutActionCounter(View view) {
        mode = Mode.PROJECTS_WITHOUT_ACTIONS;
        fillData();
    }
}
