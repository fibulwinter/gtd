package net.fibulwinter.gtd.presentation;

import static net.fibulwinter.gtd.presentation.TaskItemAdapterConfig.editProjectView;
import static net.fibulwinter.gtd.presentation.TaskItemAdapterConfig.projectView;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ListView;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public class TaskEditActivity extends Activity {

    public static final String TYPE = "type";

    private TaskListService taskListService;
    private Task task;
    private TaskUpdateListener taskUpdateListener = new TaskUpdateListener() {
        @Override
        public void onTaskSelected(Task selectedTask) {
            task = selectedTask;
            updateToTask();
        }

        @Override
        public void onTaskUpdated(Task updatedTask) {
            taskListService.save(updatedTask);
        }

        @Override
        public void onTaskDeleted(Task deletedTask) {
            delete(deletedTask);
        }
    };
    private TaskItemAdapter masterTasksAdapter;
    private ListView masterTaskList;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);
        long id = ContentUris.parseId(getIntent().getData());
        masterTaskList = (ListView) findViewById(R.id.task_list);
        masterTasksAdapter = new TaskItemAdapter(this, taskUpdateListener, projectView(), editProjectView());
        masterTaskList.setAdapter(masterTasksAdapter);

        ContextRepository contextRepository = new ContextRepository();
        taskListService = new TaskListService(new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository)));
        boolean isNew = id == -1;
        if (isNew) {
            task = new Task("");
            if (getIntent().hasExtra(TYPE)) {
                task.setStatus((TaskStatus) getIntent().getExtras().get(TYPE));
            }
        } else {
            task = taskListService.getById(id).get();
        }
        updateToTask();
        if (isNew) {
            onTitleClick();
        }
    }

    private void updateToTask() {
        List<Task> masterTasks = task.getProjectRoot().getProjectView();
        masterTasksAdapter.setData(masterTasks);
        masterTasksAdapter.setHighlightedTask(Optional.of(task));
    }

    public void onTitleClick() {
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        final String initialText = task.getText();
        input.setText(initialText);
        new AlertDialog.Builder(this)
                .setTitle("Edit task text")
                .setView(input)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (!getInputText().isEmpty()) {
                            task.setText(getInputText());
                            saveAndUpdate();
                        } else if (initialText.isEmpty()) {
                            finish();
                        }
                    }

                    private String getInputText() {
                        return input.getText().toString().trim();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (initialText.isEmpty() && input.getText().toString().trim().isEmpty()) {
                            finish();
                        }
                        // Do nothing.
                    }
                }).show();
    }


    private void saveAndUpdate() {
        if (!task.getText().isEmpty()) {
            taskListService.save(task);
        }
        updateToTask();
    }

    private void delete(Task taskToDelete) {
        Optional<Task> masterTask = taskToDelete.getMasterTask();
        taskListService.delete(taskToDelete);
        if (masterTask.isPresent()) {
            task = taskListService.getById(masterTask.get().getId()).get();
            updateToTask();
        } else {
            finish();
        }
    }

}
