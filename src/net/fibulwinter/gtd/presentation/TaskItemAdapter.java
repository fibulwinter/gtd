package net.fibulwinter.gtd.presentation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
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

import java.util.Date;

public class TaskItemAdapter extends ArrayAdapter<Task> {
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
        holder.update(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        private Task task;

        private CheckBox doneStatus;
        private TextView text;
        private TextView details;

        ViewHolder(Task aTask, View convertView) {
            this.task = aTask;
            doneStatus = (CheckBox) convertView.findViewById(R.id.task_list_item_status);
            text = (TextView) convertView.findViewById(R.id.task_list_item_text);
            details = (TextView) convertView.findViewById(R.id.task_list_item_details);
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
            update(task);
        }

        void update(Task item) {
            task = item;
            text.setText(task.getText() + (task.getStatus() == TaskStatus.Maybe ? " ???" : ""));
            if (task.getStatus() == TaskStatus.Cancelled) {
                text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                text.setPaintFlags(text.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            }
            if (task.getStatus().isActive() && task.getDueDate().isPresent() && task.getDueDate().get().before(new Date())) {
                details.setTextColor(Color.RED);
            } else {
                details.setTextColor(Color.LTGRAY);
            }
            if (showProject) {
                Optional<Task> masterTask = task.getMasterTask();
                String detailsText = "";
                if (masterTask.isPresent()) {
                    detailsText += "to " + masterTask.get().getText();
                }
                if (task.getDueDate().isPresent()) {
                    detailsText += " due to " + DateUtils.optionalDateToString(task.getDueDate());
                }
                details.setText(detailsText);
            } else {
                int subTasksCount = task.getSubTasks().size();
                String detailsText = subTasksCount == 0 ? "" : subTasksCount + " subtasks";
                if (task.getStartingDate().isPresent()) {
                    detailsText += " from " + DateUtils.optionalDateToString(task.getStartingDate());
                }
                if (task.getDueDate().isPresent()) {
                    detailsText += " due to " + DateUtils.optionalDateToString(task.getDueDate());
                }
                details.setText(detailsText);
            }
            doneStatus.setChecked(task.getStatus() == TaskStatus.Completed);
            doneStatus.setEnabled(task.getStatus().isActive() || task.getStatus() == TaskStatus.Completed);
            if (task.getStatus() == TaskStatus.Maybe) {

            }
        }
    }

}
