package net.fibulwinter.gtd.presentation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;
import net.fibulwinter.gtd.service.TaskListService;

import java.util.List;

public class TaskItemAdapter extends ArrayAdapter<Task> {
    public static final int TODAY_FG_COLOR = Color.parseColor("#ff8000");
    public static final int TODAY_BG_COLOR = Color.parseColor("#663000");
    public static final int OVERDUE_FG_COLOR = Color.parseColor("#ff0000");
    public static final int OVERDUE_BG_COLOR = Color.parseColor("#660000");
    public static final int CONTEXT_FG_COLOR = Color.parseColor("#6666cc");
    public static final int CONTEXT_BG_COLOR = Color.parseColor("#303066");

    private LayoutInflater inflater;
    private final TaskUpdateListener taskUpdateListener;
    private final TaskItemAdapterConfig config;
    private Optional<Task> highlightedTask = Optional.absent();


    public TaskItemAdapter(Context context, TaskUpdateListener taskUpdateListener, TaskItemAdapterConfig config) {
        super(context, R.layout.task_list_item, Lists.<Task>newArrayList());
        this.taskUpdateListener = taskUpdateListener;
        this.config = config;
        inflater = LayoutInflater.from(context);
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

        ViewHolder(Task aTask, View convertView) {
            this.task = aTask;
            this.convertView = convertView;
            doneStatus = (ImageButton) convertView.findViewById(R.id.task_list_item_status);
            text = (TextView) convertView.findViewById(R.id.task_list_item_text);
            doneStatus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDoneStatusUpdated();
                }
            });
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSelected();
                }
            });
        }

        private void onSelected() {
            taskUpdateListener.onTaskSelected(task);
        }

        private void onDoneStatusUpdated() {
            if (task.getStatus().isActive()) {
                task.complete();
            } else {
                task.setStatus(TaskStatus.NextAction);
            }
            taskUpdateListener.onTaskUpdated(task);
            update(task);
        }

        void update(Task item) {
            task = item;
            SpannedText text = new SpannedText(item.getText(), new StyleSpan(Typeface.BOLD));
            if (task.getStatus() == TaskStatus.Cancelled) {
                text = text.style(new StrikethroughSpan());
            }

            SpannedText extra = new SpannedText("");

            if (canShowCompletedDate()) {
                extra = extra.space().join("at " + DateUtils.dateTimeToString(task.getCompleteDate().get()));
            }
            if (canShowMasterProject()) {
                extra = extra.space().join("to ").join(task.getMasterTask().get().getText(),
                        new StyleSpan(Typeface.ITALIC));
            }
            if (canShowSubActions()) {
                List<Task> subTasks = task.getSubTasks();
                if (TaskListService.PROJECT_WITHOUT_ACTIONS_PREDICATE.apply(task)) {
                    extra = extra.space().join("No Next Action",
                            new ForegroundColorSpan(TODAY_FG_COLOR),
                            new BackgroundColorSpan(TODAY_BG_COLOR)
                    );
                }
                extra = extra.space().join(subTasks.size() + " subtasks");
            }
            if (canShowContext()) {
                extra = extra.space().join(task.getContext().getName(),
                        new ForegroundColorSpan(CONTEXT_FG_COLOR),
                        new BackgroundColorSpan(CONTEXT_BG_COLOR)
                );
            }
            if (canShowStartingDate()) {
                extra = extra.space().join("from " + DateUtils.optionalDateToString(task.getStartingDate()));
            }
            if (canShowDueDate()) {
                String dueDate = dueDate();
                if (TaskListService.TODAY_PREDICATE().apply(task)) {
                    extra = extra.space().join(dueDate,
                            new ForegroundColorSpan(TODAY_FG_COLOR),
                            new BackgroundColorSpan(TODAY_BG_COLOR)
                    );
                } else if (TaskListService.OVERDUE_PREDICATE().apply(task)) {
                    extra = extra.space().join(dueDate,
                            new ForegroundColorSpan(OVERDUE_FG_COLOR),
                            new BackgroundColorSpan(OVERDUE_BG_COLOR)
                    );
                } else {
                    extra = extra.space().join(dueDate);
                }
            }
            if (!extra.isEmpty()) {
                text = text.join("\n").join(extra);
            }
            text.apply(this.text);
            int image = 0;
            switch (task.getStatus()) {
                case Project:
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

            doneStatus.setImageDrawable(getContext().getResources().getDrawable(image));
            doneStatus.setClickable(config.isAllowChangeStatus() && (task.getStatus().isActive() || task.getStatus() == TaskStatus.Completed));
            if (canShowLevel()) {
                this.text.setPadding(task.getMasterTasks().size() * 24, 5, 5, 5);
                if (highlightedTask.isPresent() && highlightedTask.get().equals(task)) {
                    this.convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.secondary_text_dark));
                } else {
                    this.convertView.setBackgroundColor(getContext().getResources().getColor(android.R.color.background_dark));
                }
            }
        }

        private boolean canShowContext() {
            return config.isShowContext() && !task.getContext().isDefault();
        }

        private boolean canShowMasterProject() {
            return config.isShowMasterProject() && task.getMasterTask().isPresent();
        }

        private boolean canShowSubActions() {
            return config.isShowSubActions() && task.isProject();
        }

        private boolean canShowStartingDate() {
            return config.isShowStartingDate() && task.getStartingDate().isPresent();
        }

        private boolean canShowCompletedDate() {
            return config.isShowCompletedDate() && task.getCompleteDate().isPresent();
        }

        private boolean canShowDueDate() {
            return config.isShowDueDate() && task.getDueDate().isPresent() && task.getStatus().isActive();
        }

        private boolean canShowLevel() {
            return config.isShowLevel();
        }

        private String dueDate() {
            long days = DateUtils.daysBefore(task.getDueDate().get());
            if (days > 0) {
                return "in " + days + " days";
            } else if (days == 0) {
                return "today";
            } else {
                return "due to " + DateUtils.optionalDateToString(task.getDueDate());
            }
        }
    }

}
