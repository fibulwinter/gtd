package net.fibulwinter.gtd.domain;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

public class Task {
    private long id;
    private String text;
    private TaskStatus status;
    private List<Task> subTasks = newArrayList();
    private Optional<Task> masterAction = Optional.absent();

    public Task(String text) {
        this(text, Optional.<Task>absent());
    }

    public Task(String text, Optional<Task> masterAction) {
        this.id = UUID.randomUUID().getLeastSignificantBits();
        this.text = text;
        this.masterAction = masterAction;
        this.status = TaskStatus.NextAction;
        if (masterAction.isPresent()) {
            masterAction.get().addSubAction(this);
        }
    }

    public Task(long id, String text, TaskStatus status) {
        this.id = id;
        this.text = text;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public TaskStatus getStatus() {
        return (status.isDone() || !masterAction.isPresent() || !masterAction.get().getStatus().isDone())
                ? status
                : TaskStatus.Cancelled;
    }

    public void complete() {
        this.status = TaskStatus.Completed;
    }

    public void cancel() {
        this.status = TaskStatus.Cancelled;
    }

    private void addSubAction(Task subTask) {
        subTasks.add(subTask);
    }

    public List<Task> getSubTasks() {
        return Collections.unmodifiableList(subTasks);
    }

    public boolean isProject() {
        return !subTasks.isEmpty();
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Optional<Task> getMasterAction() {
        return masterAction;
    }

    @Override
    public String toString() {
        return getText() + " " + getStatus().name();
    }
}
