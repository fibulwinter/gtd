package net.fibulwinter.gtd.presentation;

import java.util.ArrayList;
import java.util.List;

import android.content.res.Resources;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Context;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;

public abstract class StatusTransitionsFactory {
    private EditDialogFactory editDialogFactory;
    private Resources resources;

    protected StatusTransitionsFactory(EditDialogFactory editDialogFactory, Resources resources) {
        this.editDialogFactory = editDialogFactory;
        this.resources = resources;
    }

    public List<StatusTransition> getTransitions(Task task) {
        List<StatusTransition> transitions = new ArrayList<StatusTransition>();
        if (task.getStatus() == TaskStatus.NextAction) {
            transitions.add(new StatusTransition(resources.getString(R.string.transition_to_done)) {
                @Override
                public void doTransition(Task task) {
                    task.complete();
                    justUpdate(task);
                }
            });
            if (task.getMasterTask().isPresent()) {
                transitions.add(new StatusTransition(resources.getString(R.string.transition_to_done_with_follow_up)) {
                    @Override
                    public void doTransition(final Task task) {
                        task.complete();
                        justUpdate(task);
                        editDialogFactory.showTitleDialog("", Context.DEFAULT, resources.getString(R.string.enter_follow_up_action),
                                new EditDialogFactory.TitleEdited() {
                                    @Override
                                    public void onValidText(String title, Context context) {
                                        Task subTask = new Task(title);
                                        subTask.setMaster(task.getMasterTask().get());
                                        subTask.setContext(context);
                                        addedSubtask(task.getMasterTask().get(), subTask);
                                    }

                                });
                    }
                });
            }
            transitions.add(new StatusTransition(resources.getString(R.string.add_child_action)) {
                @Override
                public void doTransition(final Task task) {
                    editDialogFactory.showTitleDialog("", Context.DEFAULT, resources.getString(R.string.enter_sub_action), new EditDialogFactory.TitleEdited() {
                        @Override
                        public void onValidText(String title, Context context) {
                            Task subTask = new Task(title);
                            subTask.setMaster(task);
                            subTask.setContext(context);
                            addedSubtask(task, subTask);
                        }

                    });
                }
            });
            transitions.add(new StatusTransition(resources.getString(R.string.transition_to_do_it_later)) {
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
        } else if (!task.isInherentlyCancelled()) {
            transitions.add(new StatusTransition(resources.getString(R.string.transition_to_do_it)) {
                @Override
                public void doTransition(Task task) {
                    task.setStatus(TaskStatus.NextAction);
                    justUpdate(task);
                }
            });
        }
        if (!task.isInherentlyCancelled() && task.getStatus() != TaskStatus.Maybe) {
            transitions.add(new StatusTransition(resources.getString(R.string.transition_to_may_be)) {
                @Override
                public void doTransition(Task task) {
                    task.setStatus(TaskStatus.Maybe);
                    justUpdate(task);
                }
            });
        }
        if (task.getStatus() != TaskStatus.Cancelled) {
            transitions.add(new StatusTransition(resources.getString(R.string.transition_to_cancel)) {
                @Override
                public void doTransition(Task task) {
                    task.cancel();
                    justUpdate(task);
                }
            });
        } else {
            transitions.add(new StatusTransition(resources.getString(R.string.transition_to_delete)) {
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
