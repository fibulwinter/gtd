package net.fibulwinter.gtd.presentation;

import static net.fibulwinter.gtd.presentation.TaskItemAdapterConfig.editProjectView;
import static net.fibulwinter.gtd.presentation.TaskItemAdapterConfig.projectView;

import java.util.List;

import android.app.Activity;
import android.content.ContentUris;
import android.os.Bundle;
import android.widget.ListView;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public class TaskEditActivity extends Activity {

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
        task = taskListService.getById(id).get();
        updateToTask();
    }

    private void updateToTask() {
        List<Task> masterTasks = task.getProjectRoot().getProjectView();
        masterTasksAdapter.setData(masterTasks);
        masterTasksAdapter.setHighlightedTask(Optional.of(task));
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
