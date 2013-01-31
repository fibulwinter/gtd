package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import com.google.common.base.Predicate;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
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
    private Spinner contextSpinner;
    private TaskItemAdapter taskItemAdapter;
    private ContextRepository contextRepository;

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
    private Context context = Context.DEFAULT;

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
        contextRepository = new ContextRepository();
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), contextRepository));
        taskListService = new TaskListService(taskRepository);
        modeSpinner = (Spinner) findViewById(R.id.mode_spinner);
        contextSpinner = (Spinner) findViewById(R.id.context_spinner);
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
        SpinnerUtils.setupContextSpinner(this, contextRepository, contextSpinner, new SpinnerUtils.ContextSpinnerListener() {
            @Override
            public void onSelectedContext(Context context) {
                ListActivity.this.context = context;
                fillData();
            }
        });

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                List<Context> contextList = contextRepository.getAll();
                if (velocityX > 5) {
                    context = contextList.get((contextList.indexOf(context) + 1) % contextList.size());
                    fillData();
                    return true;
                }
                if (velocityX < 5) {
                    context = contextList.get((contextList.indexOf(context) + contextList.size() - 1) % contextList.size());
                    fillData();
                    return true;
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
        contextSpinner.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return gestureDetector.onTouchEvent(motionEvent);
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
        contextSpinner.setSelection(contextRepository.getAll().indexOf(context));
        contextSpinner.setVisibility(mode == Mode.NEXT_ACTIONS ? View.VISIBLE : View.GONE);
        Iterable<Task> tasks;
        switch (mode) {
            case ALL:
                tasks = taskRepository.getAll();
                break;
            case NEXT_ACTIONS:
                tasks = from(taskListService.getNextActions()).filter(new Predicate<Task>() {
                    @Override
                    public boolean apply(Task task) {
                        return task.getContext().equals(context);
                    }
                });
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
