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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class TaskEditActivity extends Activity {

    private TaskRepository taskRepository;
    private TextView masterActionsTitle;
    private ListView masterTaskList;
    private ListView subTaskList;
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
        masterTaskList = (ListView) findViewById(R.id.master_task_ist);
        subTaskList = (ListView) findViewById(R.id.subTaskList);
        statusSpinner = (Spinner) findViewById(R.id.task_status_spinner);
        startingDatePicker = (Button) findViewById(R.id.startingDatePicker);
        dueDatePicker = (Button) findViewById(R.id.dueDatePicker);
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
        masterTaskList.setAdapter(new TaskItemAdapter(this, taskUpdateListener, masterTasks, false));
        subTaskList.setAdapter(new TaskItemAdapter(this, taskUpdateListener, task.getSubTasks(), false));
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

    private static interface DatePickListener {
        void setOptionalDate(Optional<Date> date);
    }

    public void pickDate(String title, Optional<Date> optionalDate, final DatePickListener listener) {
        Date date = optionalDate.or(new Date());
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        // Set an EditText view to get user input
        final DatePicker input = new DatePicker(this);
        input.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        GregorianCalendar calendar = new GregorianCalendar();
                        calendar.set(input.getYear(), input.getMonth(), input.getDayOfMonth(), 0, 0, 0);
                        listener.setOptionalDate(Optional.of(calendar.getTime()));
                    }
                })
                .setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        listener.setOptionalDate(Optional.<Date>absent());
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
    }


    public void onStartDateClick(View view) {
        pickDate("Edit task start", task.getStartingDate(), new DatePickListener() {
            @Override
            public void setOptionalDate(Optional<Date> date) {
                task.setStartingDate(date);
                saveAndUpdate();
            }
        });
    }

    public void onDueDateClick(View view) {
        pickDate("Edit task due date", task.getDueDate(), new DatePickListener() {
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
