package net.fibulwinter.gtd.domain;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Ordering;

public class Task {
    private long id;
    private String text;
    private TaskStatus status;
    private List<Task> subTasks = newArrayList();
    private Optional<Task> masterTask = Optional.absent();
    private Optional<Date> startingDate = Optional.absent();
    private Optional<Date> dueDate = Optional.absent();
    private Context context = Context.DEFAULT;
    private Optional<Date> completeDate = Optional.absent();
    private Date createdDate = new Date();
    public static final Predicate<? super Task> IS_PROJECT_ROOT = new Predicate<Task>() {
        @Override
        public boolean apply(Task task) {
            return task.getProjectRoot() == task;
        }
    };

    public Task(String text) {
        this.id = UUID.randomUUID().getLeastSignificantBits();
        this.text = text;
        this.status = TaskStatus.NextAction;
        if (masterTask.isPresent()) {
            masterTask.get().addSubAction(this);
        }
        this.createdDate = new Date();
    }

    public Task(long id, String text, TaskStatus status, Date createdDate) {
        this.id = id;
        this.text = text;
        this.status = status;
        this.createdDate = createdDate;
    }

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public TaskStatus getStatus() {
        return ((status == TaskStatus.Completed || status == TaskStatus.Cancelled) || !masterTask.isPresent() || masterTask.get().getStatus().isActive())
                ? status
                : TaskStatus.Cancelled;
    }

    public boolean isInherentlyCancelled() {
        return getStatus() != status;
    }

    public void complete() {
        setStatus(TaskStatus.Completed);
    }

    public void cancel() {
        setStatus(TaskStatus.Cancelled);
    }

    private void addSubAction(Task subTask) {
        subTasks.add(subTask);
        Collections.sort(subTasks, new Ordering<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                return task.getCreatedDate().compareTo(task1.getCreatedDate());
            }
        });
    }

    public List<Task> getSubTasks() {
        return Collections.unmodifiableList(subTasks);
    }

    public boolean isProject() {
        for (Task task : subTasks) {
            if (task.getStatus().isActive()) return true;
        }
        return false;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
        if (status == TaskStatus.Cancelled || status == TaskStatus.Completed) {
            this.completeDate = Optional.of(new Date());
        } else {
            this.completeDate = Optional.absent();
        }
    }

    public Optional<Task> getMasterTask() {
        return masterTask;
    }

    @Override
    public String toString() {
        return getText() + " " + getStatus().name();
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setMaster(Task masterTask) {
        if (this.masterTask.isPresent()) {
            this.masterTask.get().subTasks.remove(this);
        }
        this.masterTask = Optional.of(masterTask);
        masterTask.addSubAction(this);
    }

    public List<Task> getMasterTasks() {
        if (masterTask.isPresent()) {
            List<Task> masterTasks = masterTask.get().getMasterTasks();
            masterTasks.add(masterTask.get());
            return masterTasks;
        } else {
            return newArrayList();
        }
    }

    public Task getProjectRoot() {
        return masterTask.isPresent() ? masterTask.get().getProjectRoot() : this;
    }

    public List<Task> getProjectView() {
        List<Task> result = newArrayList();
        result.add(this);
        for (Task subTask : getSubTasks()) {
            result.addAll(subTask.getProjectView());
        }
        return result;
    }

    public Optional<Date> getStartingDate() {
        return startingDate;
    }

    public void setStartingDate(Optional<Date> startingDate) {
        this.startingDate = startingDate;
    }

    public Optional<Date> getDueDate() {
        return dueDate;
    }

    public void setDueDate(Optional<Date> dueDate) {
        this.dueDate = dueDate;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Optional<Date> getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Optional<Date> completeDate) {
        this.completeDate = completeDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        if (id != task.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    public boolean isComplex() {
        return getMasterTask().isPresent() || !getSubTasks().isEmpty();
    }
}
