package net.fibulwinter.gtd.presentation;

import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;

public class ProjectListActivity extends SimpleListActivity {

    @Override
    protected TaskItemAdapterConfig getConfig() {
        return TaskItemAdapterConfig.blockedList();
    }

    @Override
    protected Iterable<Task> loadActions() {
        return taskListService.getProjects();
    }

    @Override
    protected int newTaskText() {
        return R.string.new_project;
    }

    @Override
    protected TaskStatus getNewStatus() {
        return TaskStatus.NextAction;
    }

}
