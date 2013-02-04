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
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;
import net.fibulwinter.gtd.service.TaskListService;

public class TaskItemAdapter extends ArrayAdapter<Task> {
    public static final int TODAY_FG_COLOR = Color.parseColor("#ff8000");
    public static final int TODAY_BG_COLOR = Color.parseColor("#663000");
    public static final int OVERDUE_FG_COLOR = Color.parseColor("#ff0000");
    public static final int OVERDUE_BG_COLOR = Color.parseColor("#660000");
    public static final int CONTEXT_FG_COLOR = Color.parseColor("#6666cc");
    public static final int CONTEXT_BG_COLOR = Color.parseColor("#303066");
    private final boolean showProject;
    private LayoutInflater inflater;
    private final TaskUpdateListener taskUpdateListener;


    public TaskItemAdapter(Context context, TaskUpdateListener taskUpdateListener, boolean showProject) {
        super(context, R.layout.task_list_item, Lists.<Task>newArrayList());
        this.taskUpdateListener = taskUpdateListener;
        this.showProject = showProject;
        inflater = LayoutInflater.from(context);
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
        holder.update2(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        private Task task;

        private CheckBox doneStatus;
        private TextView text;
        private TextView details;
        private TextView warning;

        ViewHolder(Task aTask, View convertView) {
            this.task = aTask;
            doneStatus = (CheckBox) convertView.findViewById(R.id.task_list_item_status);
            text = (TextView) convertView.findViewById(R.id.task_list_item_text);
//            details = (TextView) convertView.findViewById(R.id.task_list_item_details);
//            warning = (TextView) convertView.findViewById(R.id.task_list_item_warnings);
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
            if (doneStatus.isChecked()) {
                task.complete();
            } else {
                task.setStatus(TaskStatus.NextAction);
            }
            taskUpdateListener.onTaskUpdated(task);
            update2(task);
        }

        void update2(Task item) {
            task = item;
            SpannedText text = new SpannedText(item.getText(), new StyleSpan(Typeface.BOLD));
            if (task.getStatus() == TaskStatus.Cancelled) {
                text = text.style(new StrikethroughSpan());
            }

            SpannedText extra = new SpannedText("");

            if (showProject) {
                Optional<Task> masterTask = task.getMasterTask();
                if (masterTask.isPresent()) {
                    extra = extra.join("to ").join(new SpannedText(masterTask.get().getText(), new StyleSpan(Typeface.ITALIC)));
                }
            } else {
                int subTasksCount = task.getSubTasks().size();
                if (subTasksCount > 0) {
                    extra = extra.join(subTasksCount + " subtasks");
                }
                if (task.getStartingDate().isPresent()) {
                    extra = extra.join(" from " + DateUtils.optionalDateToString(task.getStartingDate()));
                }
            }

            String context = addContext();
            if (!context.isEmpty()) {
                extra = extra.join(" ").join(new SpannedText(context,
                        new ForegroundColorSpan(CONTEXT_FG_COLOR),
                        new BackgroundColorSpan(CONTEXT_BG_COLOR)
                ));
            }

            String dueDate = dueDate();
            if (!dueDate.isEmpty()) {
                if (task.getStatus().isActive() && task.getDueDate().isPresent()) {
                    if (TaskListService.TODAY_PREDICATE().apply(task)) {
                        extra = extra.join(" ").join(new SpannedText(dueDate,
                                new ForegroundColorSpan(TODAY_FG_COLOR),
                                new BackgroundColorSpan(TODAY_BG_COLOR)
                        ));
                    } else if (TaskListService.OVERDUE_PREDICATE().apply(task)) {
                        extra = extra.join(" ").join(new SpannedText(dueDate,
                                new ForegroundColorSpan(OVERDUE_FG_COLOR),
                                new BackgroundColorSpan(OVERDUE_BG_COLOR)
                        ));
                    }
                }
            }
            if (!extra.isEmpty()) {
                text = text.join("\n").join(extra);
            }
            text.apply(this.text);
            doneStatus.setChecked(task.getStatus() == TaskStatus.Completed);
            doneStatus.setEnabled(task.getStatus().isActive() || task.getStatus() == TaskStatus.Completed);
        }

        private String addContext() {
            if (!task.getContext().equals(net.fibulwinter.gtd.domain.Context.DEFAULT)) {
                return task.getContext().getName();
            }
            return "";
        }

        private String dueDate() {
            if (task.getDueDate().isPresent()) {
                long days = DateUtils.daysBefore(task.getDueDate().get());
                if (days > 0) {
                    return " in " + days + " days";
                } else if (days == 0) {
                    return " today";
                } else {
                    return " due to " + DateUtils.optionalDateToString(task.getDueDate());
                }
            } else {
                return "";
            }
        }
    }

}
