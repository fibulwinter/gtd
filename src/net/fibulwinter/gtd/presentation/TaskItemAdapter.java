package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.getOnlyElement;

import java.util.Date;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.*;
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
import net.fibulwinter.gtd.infrastructure.DateMarshaller;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;
import net.fibulwinter.gtd.service.TaskListService;

public class TaskItemAdapter extends ArrayAdapter<Task> {
    public static final int CONTEXT_FG_COLOR2 = Color.parseColor("#3333ff");
    public static final int CONTEXT_FG_COLOR = Color.parseColor("#6666cc");
    public static final int CONTEXT_BG_COLOR = Color.parseColor("#303066");
    public static final int SELECT_FG_COLOR = Color.parseColor("#00ff00");
    public static final int SELECT_BG_COLOR = Color.parseColor("#004400");

    private LayoutInflater inflater;
    private final TaskUpdateListener taskUpdateListener;
    private final TaskItemAdapterConfig config;
    private TaskItemAdapterConfig selectedConfig;
    private Optional<Task> highlightedTask = Optional.absent();
    private TimeConstraintsUtils timeConstraintsUtils;
    private EditDialogFactory editDialogFactory;


    public TaskItemAdapter(Context context, TaskUpdateListener taskUpdateListener, TaskItemAdapterConfig config) {
        this(context, taskUpdateListener, config, config);
    }


    public TaskItemAdapter(Context context, TaskUpdateListener taskUpdateListener, TaskItemAdapterConfig config,
                           TaskItemAdapterConfig selectedConfig) {
        super(context, R.layout.task_list_item, Lists.<Task>newArrayList());
        this.taskUpdateListener = taskUpdateListener;
        this.config = config;
        this.selectedConfig = selectedConfig;
        inflater = LayoutInflater.from(context);
        timeConstraintsUtils = new TimeConstraintsUtils(new TemporalLogic());
        editDialogFactory = new EditDialogFactory(context);
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

    private void addAfter(Task origin, Task next) {
        int position = getPosition(origin);
        insert(next, position + 1);
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
        holder.update(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        private Task task;

        private final View convertView;
        private ImageButton doneStatus;
        private TextView text;
        private TextView contextSpinner;
        private TextView timeConstraints;
        private LinearLayout extraPanel;
        private TextView header;

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
            header = (TextView) convertView.findViewById(R.id.task_list_item_header);
            contextSpinner = (TextView) convertView.findViewById(R.id.task_context_spinner);
            contextSpinner.setOnClickListener(new View.OnClickListener() {
                private ContextRepository contextRepository = new ContextRepository();

                @Override
                public void onClick(View view) {
                    SpinnerDialog.show(TaskItemAdapter.this.getContext(), contextRepository.getAll(), task.getContext(), new SpinnerDialog.OnSelected<net.fibulwinter.gtd.domain.Context>() {
                        @Override
                        public void selected(net.fibulwinter.gtd.domain.Context selectedItem) {
                            task.setContext(selectedItem);
                            updateTask();
                        }
                    });

                }
            });
            timeConstraints = (TextView) convertView.findViewById(R.id.time_constraints);
            timeConstraints.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editDialogFactory.showTimeDialog(task, new Runnable() {
                        @Override
                        public void run() {
                            updateTask();
                        }
                    });
                }
            });
            doneStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StatusTransitionsFactory statusTransitionsFactory = new StatusTransitionsFactory(editDialogFactory) {
                        @Override
                        protected void addedSubtask(Task masterTask, Task subTask) {
                            taskUpdateListener.onTaskUpdated(subTask);
                            if (canShowLevel()) {
                                highlightedTask = Optional.of(subTask);
                                addAfter(masterTask, subTask);
                            } else {
                                highlightedTask = Optional.absent();
                                update(subTask);
                                replace(masterTask, subTask);
                            }
                            notifyDataSetChanged();
                        }

                        @Override
                        protected void justUpdate(Task task) {
                            taskUpdateListener.onTaskUpdated(task);
                            highlightedTask = Optional.of(task);
                            update(task);
                            notifyDataSetChanged();
                        }

                        @Override
                        protected void justDelete(Task task) {
                            remove(task);
                            taskUpdateListener.onTaskDeleted(task);
                            notifyDataSetChanged();
                        }
                    };
                    SpinnerDialog.show(TaskItemAdapter.this.getContext(), statusTransitionsFactory.getTransitions(task), null, new SpinnerDialog.OnSelected<StatusTransition>() {
                        @Override
                        public void selected(StatusTransition selectedItem) {
                            selectedItem.doTransition(task);
                        }
                    });
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSelected();
                }
            });
        }

        private void updateTask() {
            taskUpdateListener.onTaskUpdated(task);
            update(task);
        }

        public void onTitleClick(Context context, View view) {
            editDialogFactory.showTitleDialog(task.getText(), "Edit task text", new EditDialogFactory.TitleEdited() {
                @Override
                public void onValidText(String title) {
                    task.setText(title);
                    updateTask();
                }

            });
        }


        private void onSelected() {

            if (canShowLevel() || !task.isComplex()) {
                highlightedTask = Optional.of(task);
            } else {
                highlightedTask = Optional.absent();
            }
            taskUpdateListener.onTaskSelected(task);
            update(task);
            notifyDataSetChanged();
        }

        void update(Task item) {
            task = item;
            int position = getPosition(item);

            TemporalLogic temporalLogic = new TemporalLogic();
            Optional<Date> completeDateN = item.getCompleteDate();
            if (config.isShowCompletedDate()) {
                boolean haveHeader = completeDateN.isPresent();
                if (position > 0) {
                    Optional<Date> completeDateP = getItem(position - 1).getCompleteDate();
                    haveHeader = completeDateN.isPresent() && completeDateP.isPresent();
                    if (haveHeader) {
                        haveHeader = temporalLogic.relativeDays(completeDateN.get()) != temporalLogic.relativeDays(completeDateP.get());
                    }
                }
                if (!haveHeader) {
                    header.setVisibility(View.GONE);
                } else {
                    header.setVisibility(View.VISIBLE);
                    header.setText(DateMarshaller.optionalDateToString(Optional.of(temporalLogic.getCalendar(item.getCompleteDate().get()).getTime())));
                }
            }

            configureText(item);

            configureStatus();
            if (isSelected()) {
                this.convertView.setBackgroundColor(Color.rgb(50, 50, 50));
            } else {
                this.convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
            }
            if (canShowLevel()) {
                doneStatus.setPadding(task.getMasterTasks().size() * 32 + 5, 0, 5, 0);
            }
            configureEditors();
        }

        private boolean isSelected() {
            return highlightedTask.isPresent() && highlightedTask.get().equals(task);
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
                doneStatus.setClickable(isSelected());
            } else {
                doneStatus.setClickable(config.isAllowChangeStatus());
            }
        }

        private void configureEditors() {
            boolean showEditors = isEditMode();
            int visibility = showEditors ? Button.VISIBLE : Button.GONE;
            SpannedText nonEmptyConstraintsText = timeConstraintsUtils.getNonEmptyConstraintsText(task);
            nonEmptyConstraintsText = nonEmptyConstraintsText.style(new UnderlineSpan());
            nonEmptyConstraintsText.apply(timeConstraints);
            new SpannedText(task.getContext().getName(),
                    new ForegroundColorSpan(Color.WHITE),
                    new BackgroundColorSpan(CONTEXT_FG_COLOR),
                    new UnderlineSpan()
            ).apply(contextSpinner);
            extraPanel.setVisibility(visibility);
            contextSpinner.setVisibility(visibility);
            timeConstraints.setVisibility(visibility);
            timeConstraints.setClickable(showEditors);
            this.text.setClickable(showEditors);
        }

        private SpannedText getTitle(Task item) {
            SpannedText text = isSelected() ? new SpannedText(item.getText(), new StyleSpan(Typeface.BOLD),
                    new RelativeSizeSpan(1.8f)) :
                    new SpannedText(item.getText(), new StyleSpan(Typeface.BOLD));
            if (task.getStatus() == TaskStatus.Cancelled) {
                text = text.style(new StrikethroughSpan());
            }
            if (isSelected()) {
                text = text.style(new UnderlineSpan());
            }
            return text;
        }

        private SpannedText getDetails() {
            SpannedText extra = new SpannedText("");

            if (canShowCompletedDate()) {
                extra = extra.space().join("at " + DateMarshaller.dateTimeToString(task.getCompleteDate().get()));
            }
            if (canShowMasterProject()) {
                extra = extra.space().join("to ").join(task.getMasterTask().get().getText(),
                        new StyleSpan(Typeface.ITALIC));
            }
            if (canShowContext()) {
                extra = extra.space().join(task.getContext().getName(),
                        new ForegroundColorSpan(CONTEXT_FG_COLOR),
                        new BackgroundColorSpan(CONTEXT_BG_COLOR)
                );
            }
            if (canShowFutureStartingDate()) {
                extra = extra.join(timeConstraintsUtils.addFutureStartWarning(task));
            }
            if (canShowDueDate()) {
                extra = extra.join(timeConstraintsUtils.addDueDateWarning(task));
            }
            if (canShowTimeConstraints()) {
                extra = extra.space().join(timeConstraintsUtils.addFutureStartWarning(task))
                        .join(" - " + DateMarshaller.optionalDateToString(task.getDueDate()) + "]");
            }
            if (canShowSubActions()) {
                List<Task> subTasks = from(task.getSubTasks()).filter(TaskListService.ACTIVE_PREDICATE).toImmutableList();
                if (subTasks.size() == 1) {
                    if (!extra.isEmpty()) {
                        extra = extra.join("\n");
                    }
                    extra = extra.space().join("blocked by " + getOnlyElement(subTasks).getText(),
                            new StyleSpan(Typeface.ITALIC));
                } else if (subTasks.size() > 1) {
                    if (!extra.isEmpty()) {
                        extra = extra.join("\n");
                    }
                    extra = extra.space().join("blocked by " + subTasks.size() + " subtasks",
                            new StyleSpan(Typeface.ITALIC));
                }
            }
            return extra;
        }

        private int selectStatusImage() {
            int image = 0;
            switch (task.getStatus()) {
                case NextAction:
                    image = task.isProject() ? R.drawable.a_blocked : R.drawable.a_not_done;
                    break;
                case Maybe:
                    image = R.drawable.a_maybe;
                    break;
                case Completed:
                    image = R.drawable.a_done;
                    break;
                case Cancelled:
                    image = task.isInherentlyCancelled() ? R.drawable.a_cancelled_i : R.drawable.a_cancelled;
                    break;
            }
            return image;
        }

        private TaskItemAdapterConfig getActualConfig() {
            return isSelected() ? selectedConfig : config;
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
            return getActualConfig().isShowFutureStartingDate();
        }

        private boolean canShowTimeConstraints() {
            return getActualConfig().isShowTimeConstraints();
        }

        private boolean canShowCompletedDate() {
            return getActualConfig().isShowCompletedDate() && task.getCompleteDate().isPresent();
        }

        private boolean canShowDueDate() {
            return getActualConfig().isShowDueDate();
        }

        private boolean canShowLevel() {
            return getActualConfig().isShowLevel();
        }

        private boolean isEditMode() {
            return /*getActualConfig().isEditMode() && */isSelected();
        }
    }
}
