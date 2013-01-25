package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;

import java.util.Date;
import java.util.List;

public class TaskEditActivity extends Activity {

    private TaskRepository taskRepository;
    private TextView masterActionsTitle;
    private TextView title;
    private Spinner statusSpinner;
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
    private TaskItemAdapter subTasksAdapter;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);
        long id = ContentUris.parseId(getIntent().getData());
        title = (TextView) findViewById(R.id.task_title);
        masterActionsTitle = (TextView) findViewById(R.id.master_task_title);
        ListView masterTaskList = (ListView) findViewById(R.id.master_task_ist);
        ListView subTaskList = (ListView) findViewById(R.id.subTaskList);
        statusSpinner = (Spinner) findViewById(R.id.task_status_spinner);
        startingDatePicker = (Button) findViewById(R.id.startingDatePicker);
        dueDatePicker = (Button) findViewById(R.id.dueDatePicker);
        masterTasksAdapter = new TaskItemAdapter(this, taskUpdateListener, false);
        masterTaskList.setAdapter(masterTasksAdapter);
        subTasksAdapter = new TaskItemAdapter(this, taskUpdateListener, false);
        subTaskList.setAdapter(subTasksAdapter);

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
        clearDatePicker = new ClearDatePicker(this);
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver()));
        boolean isNew = id == -1;
        task = isNew ? new Task("") : taskRepository.getById(id).get();
        updateToTask();
        if (isNew) {
            onTitleClick(title);
        }
    }

    private void updateToTask() {
        title.setText(task.getText());
        List<Task> masterTasks = task.getMasterTasks();
        masterActionsTitle.setVisibility(masterTasks.isEmpty() ? View.INVISIBLE : View.VISIBLE);
        masterTasksAdapter.setData(task.getMasterTasks());
        subTasksAdapter.setData(task.getSubTasks());
        statusSpinner.setSelection(task.getStatus().ordinal());
        startingDatePicker.setText(DateUtils.optionalDateToString(task.getStartingDate()));
        dueDatePicker.setText(DateUtils.optionalDateToString(task.getDueDate()));
    }

    public void onTitleClick(View view) {
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setText(task.getText());
        new AlertDialog.Builder(this)
                .setTitle("Edit task text")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        task.setText(input.getText().toString());
                        saveAndUpdate();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
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
        taskRepository.save(task);
        updateToTask();
    }

    public void onNewSubTask(View view) {
        Task master = task;
        task = new Task("");
        task.setMaster(master);
        updateToTask();
        onTitleClick(title);
    }

}
