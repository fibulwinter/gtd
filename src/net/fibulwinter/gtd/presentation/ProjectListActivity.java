package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.collect.Iterables;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.*;
import net.fibulwinter.gtd.infrastructure.TaskTableColumns;
import net.fibulwinter.gtd.service.TaskListService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static com.google.common.collect.Lists.newArrayList;

public class ProjectListActivity extends Activity {

    private TaskListService taskListService;
    private TaskItemAdapter taskItemAdapter;
    private TextView projectWithoutActionCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projects_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        projectWithoutActionCounter = (TextView) findViewById(R.id.projectWithoutActionCounter);
        TaskRepository taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), new ContextRepository()));
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskRepository);
        taskListService = new TaskListService(taskRepository);
        TaskItemAdapterConfig config = new TaskItemAdapterConfig();
        config.setShowSubActions(true);
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, config);
        taskList.setAdapter(taskItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        Iterable<Task> projects = taskListService.getProjects();
        ArrayList<Task> projectsArrayList = newArrayList(projects);
        Collections.sort(projectsArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getText().compareTo(task1.getText());
            }
        });
        taskItemAdapter.setData(projectsArrayList);
        int projectWithoutActionsSize = Iterables.size(taskListService.getProjectsWithoutNextAction());
        projectWithoutActionCounter.setVisibility(projectWithoutActionsSize > 0 ? View.VISIBLE : View.GONE);
        projectWithoutActionCounter.setText("" + projectWithoutActionsSize + " project(s) don't have next action");
    }

    public void onProjectWithoutActionCounter(View view) {
        fillData();
    }

    public void onNewTask(View view) {
        Uri uri = ContentUris.withAppendedId(TaskTableColumns.CONTENT_URI, -1);
        Intent intent = new Intent("edit", uri, this, TaskEditActivity.class);
        intent.putExtra(TaskEditActivity.TYPE, TaskStatus.Project);
        startActivity(intent);
    }

}
