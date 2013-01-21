package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskRepository;

import static com.google.common.collect.Lists.newArrayList;

public class ListActivity extends Activity {

    private ListView taskList;
    private TaskRepository taskRepository;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        taskList = (ListView) findViewById(R.id.taskList);
        taskRepository = new TaskRepository();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        Iterable<Task> tasks = taskRepository.getAll();
        taskList.setAdapter(new TaskItemAdapter(this, newArrayList(tasks)));
    }
}
