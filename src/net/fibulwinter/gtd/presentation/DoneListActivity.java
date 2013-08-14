package net.fibulwinter.gtd.presentation;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import net.fibulwinter.gtd.R;
import net.fibulwinter.gtd.domain.ContextRepository;
import net.fibulwinter.gtd.domain.Task;
import net.fibulwinter.gtd.domain.TaskDAO;
import net.fibulwinter.gtd.domain.TaskRepository;
import net.fibulwinter.gtd.infrastructure.DateMarshaller;
import net.fibulwinter.gtd.infrastructure.TemporalLogic;
import net.fibulwinter.gtd.service.TaskExportService;
import net.fibulwinter.gtd.service.TaskImportService;
import net.fibulwinter.gtd.service.TaskListService;

public class DoneListActivity extends Activity {
    private TaskListService taskListService;
    private TaskItemAdapter taskItemAdapter;
    private TaskRepository taskRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.done_list);
        ListView taskList = (ListView) findViewById(R.id.taskList);
        ContextRepository contextRepository = new ContextRepository();
        taskRepository = new TaskRepository(new TaskDAO(getContentResolver(), contextRepository), new TaskExportService(), new TaskImportService(contextRepository));
        taskListService = new TaskListService(taskRepository);
        TaskUpdateListener taskUpdateListener = TaskUpdateListenerFactory.simple(this, taskListService);
        taskItemAdapter = new TaskItemAdapter(this, taskUpdateListener, TaskItemAdapterConfig.logList());
        taskItemAdapter.setGroupFunction(new SortAndGroup() {
            final TemporalLogic temporalLogic = new TemporalLogic();

            @Override
            public int compare(Task task, Task task1) {
                return 0;  //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public int getResource() {
                return 0;
            }

            @Override
            public Optional<?> apply(Task input) {
                if (input.getCompleteDate().isPresent()) {
                    return Optional.of(DateMarshaller.dateToString(temporalLogic.getCalendar(input.getCompleteDate().get()).getTime()));
                } else {
                    return Optional.absent();
                }
            }
        });
        taskList.setAdapter(taskItemAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillData();
    }

    private void fillData() {
        Iterable<Task> tasks = taskListService.getCompleted();
        ArrayList<Task> tasksArrayList = newArrayList(tasks);
        Collections.sort(tasksArrayList, new Comparator<Task>() {
            @Override
            public int compare(Task task, Task task1) {
                int c = -(task.getCompleteDate().get().compareTo(task1.getCompleteDate().get()));
                return c != 0 ? c : task.getText().compareTo(task1.getText());
            }
        });
        taskItemAdapter.setData(tasksArrayList);
    }

    public void onExport(View view) {
        String fileName = taskRepository.exportTasks();
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle(R.string.export_complete)
                .setMessage(fileName)
                .show();
    }

    public void onImport(View view) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.are_you_sure_to_import)
                .setMessage(R.string.are_you_sure_to_import)
                .setPositiveButton(R.string.ok_to_import, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doImport();
                    }

                })
                .setNegativeButton(R.string.cancel_to_import, null)
                .show();
    }

    public void onArchive(View view) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.archive_log));
        i.putExtra(Intent.EXTRA_TEXT, Joiner.on("\n").join(taskListService.getCompleted()));
        try {
            startActivity(Intent.createChooser(i, getResources().getString(R.string.send_mail)));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, getResources().getString(R.string.there_are_no_email_clients_installed), Toast.LENGTH_SHORT).show();
        }
    }

    private void doImport() {
        new AsyncTask<Object, Object, Integer>() {
            ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                progressDialog = ProgressDialog.show(DoneListActivity.this,
                        getResources().getString(R.string.please_wait), getResources().getString(R.string.importing),
                        true);
            }

            @Override
            protected Integer doInBackground(Object... objects) {
                return taskRepository.importTasks();
            }

            @Override
            protected void onPostExecute(Integer count) {
                progressDialog.dismiss();
                if (count > 0) {
                    new AlertDialog.Builder(DoneListActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setTitle(R.string.import_complete)
                            .setMessage(getResources().getQuantityString(R.plurals.in_n_days, count, count))
//                    .setPositiveButton(R.string.import_complete, null)
                            .show();
                    fillData();
                } else {
                    new AlertDialog.Builder(DoneListActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle(R.string.import_failed)
                            .setMessage(R.string.import_failed_desc)
//                    .setPositiveButton(R.string.import_failed, null)
                            .show();
                }
            }
        }.execute();

    }

}
