package net.fibulwinter.gtd.service;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;

import android.os.Environment;
import com.google.common.base.Strings;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateMarshaller;

public class TaskExportService {

    public TaskExportService() {
    }

    public String export(Iterable<Task> tasks) {
        List<Task> sorted = newArrayList();
        for (Task task : from(tasks).filter(Task.IS_PROJECT_ROOT)) {
            sorted.addAll(task.getProjectView());
        }
        try {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
//            File pictures = new File(externalStorageDirectory, "Pictures");
            File file = new File(externalStorageDirectory, "gtd.txt");
            PrintStream out = new PrintStream(file);
            export(sorted, out);
            out.close();
            return file.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void export(List<Task> tasks, PrintStream out) {
        for (Task task : tasks) {
            export(task, out);
        }
    }

    private void export(Task task, PrintStream out) {
        out.println(
                Strings.repeat("    ", task.getMasterTasks().size())
                        + status(task.getStatus()) + " "
                        + task.getContext().getName() + " "
                        + DateMarshaller.dateTimeToString(task.getCreatedDate()) + " "
                        + DateMarshaller.optionalDateToString(task.getStartingDate()) + " "
                        + DateMarshaller.optionalDateToString(task.getDueDate()) + " "
                        + DateMarshaller.optionalDateTimeToString(task.getCompleteDate()) + " "
                        + task.getText().replace('\n', ' ')
        );
    }

    private String status(TaskStatus status) {
        String s = " ";
        switch (status) {
            case NextAction:
                s = " ";
                break;
            case Maybe:
                s = "?";
                break;
            case Completed:
                s = "+";
                break;
            case Cancelled:
                s = "-";
                break;
        }
        return "[" + s + "]";
    }
}
