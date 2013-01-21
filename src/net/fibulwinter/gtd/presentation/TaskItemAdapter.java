package net.fibulwinter.gtd.presentation;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;

import java.util.List;

public class TaskItemAdapter extends ArrayAdapter<Task> {
    private List<Task> items;
    private LayoutInflater inflater;


    public TaskItemAdapter(Context context, List<Task> objects) {
        super(context, R.layout.task_list_item, objects);
        this.items = objects;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list_item, null);
            holder = new ViewHolder(items.get(position), convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.update();
        return convertView;
    }

    private static class ViewHolder {
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
        }

        private void onDoneStatusUpdated() {
            if (doneStatus.isChecked()) {
                task.complete();
            } else {
                task.setStatus(TaskStatus.NextAction);
            }
            update();
        }

        void update() {
            text.setText(task.getText());
            details.setText(task.getStatus().name());
            doneStatus.setChecked(task.getStatus().isDone());
        }
    }

}
