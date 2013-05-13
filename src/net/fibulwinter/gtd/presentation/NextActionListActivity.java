package net.fibulwinter.gtd.presentation;

import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;

public class NextActionListActivity extends SimpleListActivity {

    @Override
    protected TaskItemAdapterConfig getConfig() {
        return TaskItemAdapterConfig.list();
    }

    @Override
    protected Iterable<Task> loadActions() {
        return taskListService.getNextActions();
    }

    @Override
    protected TaskStatus getNewStatus() {
        return TaskStatus.NextAction;
    }

}
