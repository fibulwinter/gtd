package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.common.collect.Iterables;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.google.common.collect.Lists.newArrayList;

public class ListActivity extends Activity {

    private static final int EDIT_REQUEST = 1;

    private ListView taskList;
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
    private TextView doneCounter;
    private TextView todayCounter;
    private TextView overdueCounter;
    private TextView projectsWithouActionCounter;
    private Spinner modeSpinner;

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
        taskList = (ListView) findViewById(R.id.taskList);
        doneCounter = (TextView) findViewById(R.id.doneTodayCounter);
        todayCounter = (TextView) findViewById(R.id.dueTodayCounter);
        overdueCounter = (TextView) findViewById(R.id.overdueCounter);
        projectsWithouActionCounter = (TextView) findViewById(R.id.projectWithoutActionCounter);
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver()));
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
        modeSpinner.setSelection(Mode.NEXT_ACTIONS.ordinal());
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        modeSpinner.setSelection(mode.ordinal());
        Iterable<Task> tasks = taskRepository.getAll();
        switch (mode) {
            case ALL:
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
        }
        ArrayList<Task> taskArrayList = newArrayList(tasks);
        Collections.sort(taskArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        taskList.setAdapter(null);
        taskList.setAdapter(new TaskItemAdapter(this, taskUpdateListener, taskArrayList, true));
        doneCounter.setText("" + Iterables.size(taskListService.getDone()));
        todayCounter.setText("" + Iterables.size(taskListService.getTodayActions()));
        overdueCounter.setText("" + Iterables.size(taskListService.getOverdueActions()));
        projectsWithouActionCounter.setText("" + Iterables.size(taskListService.getProjectsWithoutNextAction()));
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
