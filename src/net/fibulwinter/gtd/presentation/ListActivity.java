package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import com.google.common.base.Predicate;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;

import static com.google.common.collect.FluentIterable.from;
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

    private enum Mode {ALL, NEXT_ACTIONS, PROJECTS}

    ;

    private Mode mode = Mode.NEXT_ACTIONS;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        taskList = (ListView) findViewById(R.id.taskList);
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver()));
        Spinner spinner = (Spinner) findViewById(R.id.mode_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.modes_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mode = Mode.values()[i];
                fillData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        spinner.setSelection(Mode.NEXT_ACTIONS.ordinal());
    }


    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        Iterable<Task> tasks = taskRepository.getAll();
        switch (mode) {
            case ALL:
                break;
            case NEXT_ACTIONS:
                tasks = from(tasks).filter(new Predicate<Task>() {
                    @Override
                    public boolean apply(Task task) {
                        return !task.isProject() && !task.getStatus().isDone();
                    }
                });
                break;
            case PROJECTS:
                tasks = from(tasks).filter(new Predicate<Task>() {
                    @Override
                    public boolean apply(Task task) {
                        return task.isProject() && !task.getStatus().isDone() && (from(task.getSubTasks()).allMatch(new Predicate<Task>() {
                            @Override
                            public boolean apply(Task task) {
                                return task.getStatus().isDone();
                            }
                        }));
                    }
                });
                break;
        }
        taskList.setAdapter(new TaskItemAdapter(this, taskUpdateListener, newArrayList(tasks), true));
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
}
