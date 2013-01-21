package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;

public class TaskEditActivity extends Activity {

    private TaskRepository taskRepository;
    private EditText title;
    private Task task;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_edit);
        long id = ContentUris.parseId(getIntent().getData());
        title = (EditText) findViewById(R.id.task_title);

        taskRepository = new TaskRepository(new TaskDAO(getContentResolver()));
        task = taskRepository.getById(id).get();
        title.setText(task.getText());
    }

    public void onSave(View view) {
        task.setText(title.getText().toString());
        taskRepository.save(task);
        finish();
    }

}
