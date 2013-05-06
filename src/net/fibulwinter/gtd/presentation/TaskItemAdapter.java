package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;
import net.fibulwinter.gtd.service.TaskListService;

public class TaskItemAdapter extends ArrayAdapter<Task> {
    public static final int CONTEXT_FG_COLOR = Color.parseColor("#6666cc");
    public static final int CONTEXT_BG_COLOR = Color.parseColor("#303066");

    private LayoutInflater inflater;
    private final TaskUpdateListener taskUpdateListener;
    private final TaskItemAdapterConfig config;
    private TaskItemAdapterConfig selectedConfig;
    private Optional<Task> highlightedTask = Optional.absent();
    private TimeConstraints timeConstraints;


    public TaskItemAdapter(Context context, TaskUpdateListener taskUpdateListener, TaskItemAdapterConfig config) {
        this(context, taskUpdateListener, config, config);
    }

    public TaskItemAdapter(Context context, TaskUpdateListener taskUpdateListener, TaskItemAdapterConfig config, TaskItemAdapterConfig selectedConfig) {
        super(context, R.layout.task_list_item, Lists.<Task>newArrayList());
        this.taskUpdateListener = taskUpdateListener;
        this.config = config;
        this.selectedConfig = selectedConfig;
        inflater = LayoutInflater.from(context);
        clearDatePicker = new ClearDatePicker(context);
        timeConstraints = new TimeConstraints(context);
    }

    public void setHighlightedTask(Optional<Task> highlightedTask) {
        this.highlightedTask = highlightedTask;
    }

    public void setData(Iterable<Task> tasks) {
        clear();
        for (Task task : tasks) {
            add(task);
        }
        notifyDataSetChanged();
    }

    private void replace(Task replaced, Task replacing) {
        int position = getPosition(replaced);
        remove(replaced);
        insert(replacing, position);
        notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list_item, null);
            holder = new ViewHolder(getItem(position), convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.update(getItem(position), highlightedTask.isPresent() && highlightedTask.get() == holder.task);
        return convertView;
    }

    private ClearDatePicker clearDatePicker;

    private class ViewHolder {
        private Task task;
        private boolean selected;

        private final View convertView;
        private ImageButton doneStatus;
        private TextView text;
        private TextView contextSpinner;
        private TextView timeConstraints;
        private LinearLayout extraPanel;

        ViewHolder(Task aTask, View convertView) {
            this.task = aTask;
            this.convertView = convertView;
            doneStatus = (ImageButton) convertView.findViewById(R.id.task_list_item_status);
            extraPanel = (LinearLayout) convertView.findViewById(R.id.extra_row);
            text = (TextView) convertView.findViewById(R.id.task_list_item_text);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onTitleClick(TaskItemAdapter.this.getContext(), view);
                }
            });
            contextSpinner = (TextView) convertView.findViewById(R.id.task_context_spinner);
            contextSpinner.setOnClickListener(new View.OnClickListener() {
                private ContextRepository contextRepository = new ContextRepository();

                @Override
                public void onClick(View view) {
                    SpinnerDialog.show(TaskItemAdapter.this.getContext(), contextRepository.getAll(), task.getContext(), new SpinnerDialog.OnSelected<net.fibulwinter.gtd.domain.Context>() {
                        @Override
                        public void selected(net.fibulwinter.gtd.domain.Context selectedItem) {
                            task.setContext(selectedItem);
                            taskUpdateListener.onTaskUpdated(task);
                            update(task, true);
                        }
                    });

                }
            });
            timeConstraints = (TextView) convertView.findViewById(R.id.time_constraints);
            timeConstraints.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clearDatePicker.pickDate("Time constraints", task.getStartingDate(), task.getDueDate(), new ClearDatePicker.DatePickListener() {
                        @Override
                        public void setOptionalDate(Optional<Date> dateStart, Optional<Date> dateDue) {
                            task.setStartingDate(dateStart);
                            task.setDueDate(dateDue);
                            taskUpdateListener.onTaskUpdated(task);
                            update(task, true);
                        }
                    });
                }
            });
            doneStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (selectedConfig.isEditMode()) {
                        List<TaskStatus> statuses = new ArrayList<TaskStatus>();
                        statuses.add(TaskStatus.NextAction);
                        statuses.add(TaskStatus.Completed);
                        statuses.add(TaskStatus.Maybe);
                        statuses.add(TaskStatus.Cancelled);
                        SpinnerDialog.show(TaskItemAdapter.this.getContext(), statuses, task.getStatus(), new SpinnerDialog.OnSelected<TaskStatus>() {
                            @Override
                            public void selected(TaskStatus selectedItem) {
                                task.setStatus(selectedItem);
                                taskUpdateListener.onTaskUpdated(task);
                                update(task, true);
                            }
                        });
                    } else {
                        List<StatusTransition> transitions = new ArrayList<StatusTransition>();
                        if (task.getStatus() == TaskStatus.NextAction) {
                            transitions.add(new StatusTransition("Done!") {
                                @Override
                                public void doTransition(Task task) {
                                    task.complete();
                                    updateAfterTransition(task);
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
                                    clearDatePicker.pickDate("Time constraints", task.getStartingDate(), task.getDueDate(), new ClearDatePicker.DatePickListener() {
                                        @Override
                                        public void setOptionalDate(Optional<Date> dateStart, Optional<Date> dateDue) {
                                            task.setStartingDate(dateStart);
                                            task.setDueDate(dateDue);
                                            taskUpdateListener.onTaskUpdated(task);
                                            update(task, true);
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

                        SpinnerDialog.show(TaskItemAdapter.this.getContext(), transitions, null, new SpinnerDialog.OnSelected<StatusTransition>() {
                            @Override
                            public void selected(StatusTransition selectedItem) {
                                selectedItem.doTransition(task);
                            }
                        });
//                        onDoneStatusUpdated();
                    }

                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSelected();
                }
            });
        }

        private void updateAfterTransition(Task newTask) {
            Task oldTask = task;
            taskUpdateListener.onTaskUpdated(newTask);
            update(newTask, true);
            if (newTask != oldTask) {
                replace(oldTask, task);
            }
        }

        public void onTitleClick(Context context, View view) {
            // Set an EditText view to get user input
            final EditText input = new EditText(context);
            final String initialText = task.getText();
            input.setText(initialText);
            new AlertDialog.Builder(context)
                    .setTitle("Edit task text")
                    .setView(input)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (!getInputText().isEmpty()) {
                                task.setText(getInputText());
                                taskUpdateListener.onTaskUpdated(task);
                                update(task, true);
                            } else if (initialText.isEmpty()) {
                            }
                        }

                        private String getInputText() {
                            return input.getText().toString().trim();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (initialText.isEmpty() && input.getText().toString().trim().isEmpty()) {
                            }
                            // Do nothing.
                        }
                    }).show();
        }


        private void onSelected() {
            taskUpdateListener.onTaskSelected(task);
            update(task, true);
        }

        void update(Task item, boolean selectedItem) {
            task = item;
            this.selected = selectedItem;

            configureText(item);

            configureStatus();
            if (canShowLevel()) {
                this.text.setPadding(task.getMasterTasks().size() * 24, 5, 5, 5);
                this.extraPanel.setPadding(task.getMasterTasks().size() * 24, 5, 5, 5);
                if (highlightedTask.isPresent() && highlightedTask.get().equals(task)) {//todo is selected?
                    this.convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.secondary_text_dark));
                } else {
                    this.convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
                }
            }
            configureEditors();
        }

        private void configureText(Task item) {
            SpannedText text = getTitle(item);

            SpannedText extra = getDetails();
            if (!extra.isEmpty() && !isEditMode()) {
                text = text.join("\n").join(extra);
            }
            text.apply(this.text);
        }

        private void configureStatus() {
            doneStatus.setImageDrawable(getContext().getResources().getDrawable(selectStatusImage()));
            if (config.isEditMode()) {
                doneStatus.setClickable(selected);
            } else {
                doneStatus.setClickable(config.isAllowChangeStatus());
            }
        }

        private void configureEditors() {
            boolean showEditors = isEditMode();
            int visibility = showEditors ? Button.VISIBLE : Button.GONE;
            timeConstraints.setText("[" + DateUtils.optionalDateToString(task.getStartingDate()) + " - " + DateUtils.optionalDateToString(task.getDueDate()) + "]");
            contextSpinner.setText(task.getContext().getName());
            extraPanel.setVisibility(visibility);
            contextSpinner.setVisibility(visibility);
            timeConstraints.setVisibility(visibility);
            timeConstraints.setClickable(showEditors);
            this.text.setClickable(showEditors);
        }

        private SpannedText getTitle(Task item) {
            SpannedText text = selected ? new SpannedText(item.getText(), Typeface.BOLD, new ForegroundColorSpan(TimeConstraints.TODAY_FG_COLOR)) : new SpannedText(item.getText(), Typeface.BOLD);
            if (task.getStatus() == TaskStatus.Cancelled) {
                text = text.style(new StrikethroughSpan());
            }
            return text;
        }

        private SpannedText getDetails() {
            SpannedText extra = new SpannedText("");

            if (canShowCompletedDate()) {
                extra = extra.space().join("at " + DateUtils.dateTimeToString(task.getCompleteDate().get()));
            }
            if (canShowMasterProject()) {
                extra = extra.space().join("to ").join(task.getMasterTask().get().getText(),
                        new StyleSpan(Typeface.ITALIC));
            }
            if (canShowSubActions()) {
                List<Task> subTasks = from(task.getSubTasks()).filter(TaskListService.ACTIVE_PREDICATE).toImmutableList();
                if (subTasks.size() == 1) {
                    extra = extra.space().join("blocked by " + getOnlyElement(subTasks).getText(),
                            new StyleSpan(Typeface.ITALIC));
                } else if (subTasks.size() > 1) {
                    extra = extra.space().join("blocked by " + subTasks.size() + " subtasks",
                            new StyleSpan(Typeface.ITALIC));
                }
            }
            if (canShowContext()) {
                extra = extra.space().join(task.getContext().getName(),
                        new ForegroundColorSpan(CONTEXT_FG_COLOR),
                        new BackgroundColorSpan(CONTEXT_BG_COLOR)
                );
            }
            if (canShowFutureStartingDate()) {
                extra = extra.space().join(startingDate(),
                        new ForegroundColorSpan(TimeConstraints.NOT_STARTED_FG_COLOR),
                        new BackgroundColorSpan(TimeConstraints.NOT_STARTED_BG_COLOR)
                );
            }
            if (canShowDueDate()) {
                String dueDate = dueDate();
                if (TaskListService.TODAY_PREDICATE().apply(task)) {
                    extra = extra.space().join(dueDate,
                            new ForegroundColorSpan(TimeConstraints.TODAY_FG_COLOR),
                            new BackgroundColorSpan(TimeConstraints.TODAY_BG_COLOR)
                    );
                } else if (TaskListService.OVERDUE_PREDICATE().apply(task)) {
                    extra = extra.space().join(dueDate,
                            new ForegroundColorSpan(TimeConstraints.OVERDUE_FG_COLOR),
                            new BackgroundColorSpan(TimeConstraints.OVERDUE_BG_COLOR)
                    );
                } else {
                    extra = extra.space().join(dueDate);
                }
            }
            if (canShowTimeConstraints()) {
                extra = extra.space().join("[" + DateUtils.optionalDateToString(task.getStartingDate()) + " - " + DateUtils.optionalDateToString(task.getDueDate()) + "]");
            }
            return extra;
        }

        private int selectStatusImage() {
            int image = 0;
            switch (task.getStatus()) {
                case NextAction:
                    image = TaskListService.PROJECT_WITHOUT_ACTIONS_PREDICATE.apply(task) ? R.drawable.p_no_action : R.drawable.a_not_done;
                    break;
                case Maybe:
                    image = R.drawable.a_maybe;
                    break;
                case Completed:
                    image = R.drawable.a_done;
                    break;
                case Cancelled:
                    image = R.drawable.a_cancelled;
                    break;
            }
            return image;
        }

        private TaskItemAdapterConfig getActualConfig() {
            return selected ? selectedConfig : config;
        }

        private boolean canShowContext() {
            return getActualConfig().isShowContext() && !task.getContext().isDefault();
        }

        private boolean canShowMasterProject() {
            return getActualConfig().isShowMasterProject() && task.getMasterTask().isPresent();
        }

        private boolean canShowSubActions() {
            return getActualConfig().isShowSubActions() && task.isProject();
        }

        private boolean canShowFutureStartingDate() {
            return getActualConfig().isShowFutureStartingDate() && task.getStartingDate().isPresent() && task.getStartingDate().get().after(new Date());
        }

        private boolean canShowTimeConstraints() {
            return getActualConfig().isShowTimeConstraints();
        }

        private boolean canShowCompletedDate() {
            return getActualConfig().isShowCompletedDate() && task.getCompleteDate().isPresent();
        }

        private boolean canShowDueDate() {
            return getActualConfig().isShowDueDate() && task.getDueDate().isPresent() && task.getStatus().isActive();
        }

        private boolean canShowLevel() {
            return getActualConfig().isShowLevel();
        }

        private boolean isEditMode() {
            return getActualConfig().isEditMode() && selected;
        }

        private String startingDate() {
            long days = DateUtils.daysBefore(task.getStartingDate().get());
            if (days > 0) {
                return "starting in " + days + " days";
            } else if (days == 0) {
                return "starting tomorrow";
            } else if (days == -1) {
                return "started today";
            } else {
                return "started " + DateUtils.optionalDateToString(task.getStartingDate());
            }
        }

        private String dueDate() {
            long days = DateUtils.daysBefore(task.getDueDate().get());
            if (days > 1) {
                return "in " + days + " days";
            } else if (days == 1) {
                return "tomorrow";
            } else if (days == 0) {
                return "today";
            } else {
                return "due to " + DateUtils.optionalDateToString(task.getDueDate());
            }
        }
    }

}
