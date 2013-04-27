package net.fibulwinter.gtd.presentation;

import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.infrastructure.DateUtils;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;

public class TaskEditActivity extends Activity {

    public static final String TYPE = "type";

    private TaskRepository taskRepository;
    private TextView title;
    private Spinner statusSpinner;
    private Spinner contextSpinner;
    private Button startingDatePicker;
    private Button dueDatePicker;
    private Task task;
    private TaskUpdateListener taskUpdateListener = new TaskUpdateListener() {
        @Override
        public void onTaskSelected(Task selectedTask) {
            task = selectedTask;
            updateToTask();
        }

        @Override
        public void onTaskUpdated(Task updatedTask) {
            taskRepository.save(updatedTask);
        }
    };
    private ClearDatePicker clearDatePicker;
    private TaskItemAdapter masterTasksAdapter;
    private ContextRepository contextRepository;
    private ListView masterTaskList;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);
        long id = ContentUris.parseId(getIntent().getData());
        title = (TextView) findViewById(R.id.task_title);
        masterTaskList = (ListView) findViewById(R.id.task_list);
        statusSpinner = (Spinner) findViewById(R.id.task_status_spinner);
        contextSpinner = (Spinner) findViewById(R.id.task_context_spinner);
        startingDatePicker = (Button) findViewById(R.id.startingDatePicker);
        dueDatePicker = (Button) findViewById(R.id.dueDatePicker);
        TaskItemAdapterConfig taskItemAdapterConfig = new TaskItemAdapterConfig();
        taskItemAdapterConfig.setShowMasterProject(false);
        taskItemAdapterConfig.setShowSubActions(false);
        taskItemAdapterConfig.setShowStartingDate(true);
        taskItemAdapterConfig.setShowDueDate(true);
        taskItemAdapterConfig.setShowLevel(true);
        masterTasksAdapter = new TaskItemAdapter(this, taskUpdateListener, taskItemAdapterConfig);
        masterTaskList.setAdapter(masterTasksAdapter);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.status_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);
        statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                task.setStatus(TaskStatus.values()[i]);
                saveAndUpdate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        contextRepository = new ContextRepository();
        SpinnerUtils.setupContextSpinner(this, contextRepository, contextSpinner, new SpinnerUtils.ContextSpinnerListener() {
            @Override
            public void onSelectedContext(Context context) {
                task.setContext(context);
                saveAndUpdate();
            }
        }, true);
        clearDatePicker = new ClearDatePicker(this);
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository));
        boolean isNew = id == -1;
        if (isNew) {
            task = new Task("");
            if (getIntent().hasExtra(TYPE)) {
                task.setStatus((TaskStatus) getIntent().getExtras().get(TYPE));
            }
        } else {
            task = taskRepository.getById(id).get();
        }
        updateToTask();
        if (isNew) {
            onTitleClick(title);
        }
    }

    private void updateToTask() {
        title.setText(task.getText());
        List<Task> masterTasks = task.getProjectRoot().getProjectView();
        masterTasksAdapter.setData(masterTasks);
        masterTasksAdapter.setHighlightedTask(Optional.of(task));
        statusSpinner.setSelection(task.getStatus().ordinal());
        contextSpinner.setSelection(contextRepository.getAll().indexOf(task.getContext()));
        startingDatePicker.setText(DateUtils.optionalDateToString(task.getStartingDate()));
        dueDatePicker.setText(DateUtils.optionalDateToString(task.getDueDate()));
    }

    public void onTitleClick(View view) {
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        final String initialText = task.getText();
        input.setText(initialText);
        new AlertDialog.Builder(this)
                .setTitle("Edit task text")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (!input.getText().toString().isEmpty()) {
                            task.setText(input.getText().toString());
                            saveAndUpdate();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (initialText.isEmpty() && input.getText().toString().isEmpty()) {
                            finish();
                        }
                        // Do nothing.
                    }
                }).show();
    }


    public void onStartDateClick(View view) {
        clearDatePicker.pickDate("Edit task start", task.getStartingDate(), new ClearDatePicker.DatePickListener() {
            @Override
            public void setOptionalDate(Optional<Date> date) {
                task.setStartingDate(date);
                saveAndUpdate();
            }
        });
    }

    public void onDueDateClick(View view) {
        clearDatePicker.pickDate("Edit task due date", task.getDueDate(), new ClearDatePicker.DatePickListener() {
            @Override
            public void setOptionalDate(Optional<Date> date) {
                task.setDueDate(date);
                saveAndUpdate();
            }
        });
    }

    private void saveAndUpdate() {
        if (!task.getText().isEmpty()) {
            taskRepository.save(task);
        }
        updateToTask();
    }

    public void onNewSubTask(View view) {
        Task master = task;
        task = new Task("");
        task.setMaster(master);
        updateToTask();
        onTitleClick(title);
    }

    public void onDeleteSubTask(View view) {
        new AlertDialog.Builder(this)
                .setTitle("Delete task?")
                .setPositiveButton("Yes, delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        delete();
                    }
                })
                .setNegativeButton("No, keep it", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                }).show();
    }

    private void delete() {
        Optional<Task> masterTask = task.getMasterTask();
        taskRepository.delete(task);
        if (masterTask.isPresent()) {
            task = taskRepository.getById(masterTask.get().getId()).get();
            updateToTask();
        } else {
            finish();
        }
    }

}
