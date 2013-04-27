package net.fibulwinter.gtd.service;

import static com.google.common.collect.Lists.newArrayList;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.domain.Context;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskStatus;
import net.fibulwinter.gtd.infrastructure.DateUtils;

public class TaskImportService {

    private ContextRepository contextRepository;

    public TaskImportService(ContextRepository contextRepository) {
        this.contextRepository = contextRepository;
    }

    public List<Task> importTasks() {
        try {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
//            File pictures = new File(externalStorageDirectory, "Pictures");
            File file = new File(externalStorageDirectory, "gtd.txt");
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            return importTasks(bufferedReader);
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Task> importTasks(BufferedReader bufferedReader) throws IOException {
        String line;
        List<Task> tasks = newArrayList();
        Task[] lastByLevel = new Task[20];
        while ((line = bufferedReader.readLine()) != null) {
            tasks.add(importTask(line, lastByLevel));
        }
        bufferedReader.close();
        return tasks;
    }

    private Task importTask(String line, Task[] lastByLevel) {
        Pattern pattern = Pattern.compile("(\\s*)\\[(.)\\] (@\\S+) (\\S+) (\\S+) (<>|\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) (.+)");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            int level = matcher.group(1).length() / 4;
            String statusString = matcher.group(2);
            TaskStatus status =
                    statusString.equals(" ") ? TaskStatus.NextAction :
                            statusString.equals("+") ? TaskStatus.Completed :
                                    statusString.equals("-") ? TaskStatus.Cancelled :
                                            statusString.equals("?") ? TaskStatus.Maybe :
                                                    statusString.equals("P") ? TaskStatus.Project : null;
            Context context = contextRepository.getByName(matcher.group(3)).get();
            Optional<Date> start = DateUtils.stringToOptionalDate(matcher.group(4));
            Optional<Date> due = DateUtils.stringToOptionalDate(matcher.group(5));
            Optional<Date> completed = DateUtils.stringToOptionalDateTime(matcher.group(6));
            String text = matcher.group(7);

            Task task = new Task(text);
            task.setStatus(status);
            task.setCompleteDate(completed);
            task.setContext(context);
            task.setDueDate(due);
            task.setStartingDate(start);
            if (level > 0) {
                task.setMaster(lastByLevel[level - 1]);
            }
            lastByLevel[level] = task;
            return task;
        } else {
            Log.e("GTD", "Can't match " + line);
            return null;
        }
    }

    /*
        private void export(List<Task> tasks, PrintStream out) {
            for(Task task:tasks){
                export(task, out);
            }
        }

        private void export(Task task, PrintStream out) {
            out.println(
                    Strings.repeat("    ", task.getMasterTasks().size())
                    + status(task.getStatus())+" "
                    + task.getContext().getName() + " "
                    + DateUtils.optionalDateToString(task.getStartingDate()) + " "
                    + DateUtils.optionalDateToString(task.getDueDate()) + " "
                    + DateUtils.optionalDateTimeToString(task.getCompleteDate()) + " "
                    + task.getText().replace('\n',' ')
            );
        }

    */
    private String status(TaskStatus status) {
        String s = " ";
        switch (status) {
            case NextAction:
                s = " ";
                break;
            case Project:
                s = "P";
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
