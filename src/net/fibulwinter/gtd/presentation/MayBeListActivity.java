package net.fibulwinter.gtd.presentation;

import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;

public class MayBeListActivity extends SimpleListActivity {
    @Override
    protected TaskItemAdapterConfig getConfig() {
        return TaskItemAdapterConfig.list();
    }

    @Override
    protected Iterable<Task> loadActions() {
        return taskListService.getMaybe();
    }

    @Override
    protected int newTaskText() {
        return R.string.new_may_be;
    }

    @Override
    protected TaskStatus getNewStatus() {
        return TaskStatus.Maybe;
    }

}