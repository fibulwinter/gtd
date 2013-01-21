package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
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
        taskList.setAdapter(new ArrayAdapter<Task>(this, R.layout.task_list_item, R.id.task_list_item_text, newArrayList(tasks)) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View layout = super.getView(position, convertView, parent);
                Task task = getItem(position);
                CheckBox doneCheckBox = (CheckBox) layout.findViewById(R.id.task_list_item_status);
                doneCheckBox.setChecked(task.getStatus().isDone());
                return layout;
            }
        });

    }
}
