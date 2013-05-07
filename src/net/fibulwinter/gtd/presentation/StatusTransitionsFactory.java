package net.fibulwinter.gtd.presentation;

public abstract class StatusTransitionsFactory {
/*
    public List<StatusTransition> getTransitions(Task task){
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
                    Context context = ViewHolder.this.convertView.getContext();
                    final EditText input = new EditText(context);
                    input.setText("");
                    new AlertDialog.Builder(context)
                            .setTitle("Enter sub action")
                            .setView(input)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String inputText = input.getText().toString().trim();
                                    if (!inputText.isEmpty()) {
                                        Task subTask = new Task(inputText);
                                        subTask.setMaster(task);
                                        highlightedTask = Optional.of(subTask);
                                        updateAfterTransition(subTask);
                                    }
                                }

                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            }).show();
                }
            });
            transitions.add(new StatusTransition("Do it later") {
                @Override
                public void doTransition(final Task task) {
                    timeConstraintsUtils.showDialog(task, new Runnable() {
                        @Override
                        public void run() {
                            updateTask();
                        }
                    });
                }
            });
        } else {
            transitions.add(new StatusTransition("Let's do it!") {
                @Override
                public void doTransition(Task task) {
                    task.setStatus(TaskStatus.NextAction);
                    updateAfterTransition(task);
                }
            });
        }
        if (task.getStatus() != TaskStatus.Maybe) {
            transitions.add(new StatusTransition("May be later...") {
                @Override
                public void doTransition(Task task) {
                    task.setStatus(TaskStatus.Maybe);
                    updateAfterTransition(task);
                }
            });
        }
        if (task.getStatus() != TaskStatus.Cancelled) {
            transitions.add(new StatusTransition("Never. Cancel it!") {
                @Override
                public void doTransition(Task task) {
                    task.cancel();
                    updateAfterTransition(task);
                }
            });
        }
        return transitions;
    }

    protected abstract void justUpdate(Task task);
*/
}
