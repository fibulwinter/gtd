package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;

import java.util.List;

public class TaskEditActivity extends Activity {

    private TaskRepository taskRepository;
    private TextView masterActionsTitle;
    private ListView masterTaskList;
    private ListView subTaskList;
    private TextView title;
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
                        taskRepository.save(task);
                        updateToTask();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();
    }


    public void onNewSubTask(View view) {
        Task master = task;
        task = new Task("");
        task.setMaster(master);
        updateToTask();
        onTitleClick(title);
    }

}
