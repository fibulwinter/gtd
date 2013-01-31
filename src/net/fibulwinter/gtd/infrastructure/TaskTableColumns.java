package net.fibulwinter.gtd.infrastructure;

import android.net.Uri;
import android.provider.BaseColumns;

public interface TaskTableColumns extends BaseColumns {
    public static final Uri CONTENT_URI = Uri.parse("content://"
            + TaskContentProvider.AUTHORITY + "/tasks");

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.fibulwinter.tasks";

    public static final String TASK_ID = "_id";
    public static final String TITLE = "title";
    public static final String STATUS = "status";
    public static final String MASTER = "master";
    public static final String START_DATE = "starting";
    public static final String DUE_DATE = "duedate";
    public static final String CONTEXT = "context";
}

