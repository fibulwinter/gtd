package net.fibulwinter.gtd.presentation;

import java.util.ArrayList;
import java.util.List;

import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;

public abstract class StatusTransitionsFactory {
    private EditDialogFactory editDialogFactory;

    protected StatusTransitionsFactory(EditDialogFactory editDialogFactory) {
        this.editDialogFactory = editDialogFactory;
    }

    public List<StatusTransition> getTransitions(Task task) {
        List<StatusTransition> transitions = new ArrayList<StatusTransition>();
        if (task.getStatus() == TaskStatus.NextAction) {
            transitions.add(new StatusTransition("Done!") {
                @Override
                public void doTransition(Task task) {
                    task.complete();
                    justUpdate(task);
                }
            });
            transitions.add(new StatusTransition("Sub action") {
                @Override
                public void doTransition(final Task task) {
                    editDialogFactory.showTitleDialog("", "Enter sub action", new EditDialogFactory.TitleEdited() {
                        @Override
                        public void onValidText(String title) {
                            Task subTask = new Task(title);
                            subTask.setMaster(task);
                            addedSubtask(task, subTask);
                        }

                    });
                }
            });
            transitions.add(new StatusTransition("Do it later") {
                @Override
                public void doTransition(final Task task) {
                    editDialogFactory.showTimeDialog(task, new Runnable() {
                        @Override
                        public void run() {
                            justUpdate(task);
                        }
                    });
                }
            });
        } else {
            transitions.add(new StatusTransition("Let's do it!") {
                @Override
                public void doTransition(Task task) {
                    task.setStatus(TaskStatus.NextAction);
                    justUpdate(task);
                }
            });
        }
        if (task.getStatus() != TaskStatus.Maybe) {
            transitions.add(new StatusTransition("May be later...") {
                @Override
                public void doTransition(Task task) {
                    task.setStatus(TaskStatus.Maybe);
                    justUpdate(task);
                }
            });
        }
        if (task.getStatus() != TaskStatus.Cancelled) {
            transitions.add(new StatusTransition("Never. Cancel it!") {
                @Override
                public void doTransition(Task task) {
                    task.cancel();
                    justUpdate(task);
                }
            });
        } else {
            transitions.add(new StatusTransition("Delete it forever") {
                @Override
                public void doTransition(Task task) {
                    justDelete(task);
                }
            });
        }
        return transitions;
    }

    protected abstract void justDelete(Task task);

    protected abstract void addedSubtask(Task masterTask, Task subTask);
    /*
        highlightedTask = Optional.of(subTask);
        updateAfterTransition(subTask);
    */

    protected abstract void justUpdate(Task task);
//        updateAfterTransition(task);
}
