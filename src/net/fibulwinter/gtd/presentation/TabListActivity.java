package net.fibulwinter.gtd.presentation;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import net.fibulwinter.gtd.R;

public class TabListActivity extends TabActivity {

    private static final String TAB_TAG = "tid1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab);
        TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);

        addTab(tabHost, "Next Action", NextActionListActivity.class);
        addTab(tabHost, "Projects", ProjectListActivity.class);
        addTab(tabHost, "May Be", MayBeListActivity.class);
        addTab(tabHost, "Done", DoneListActivity.class);
    }

    private void addTab(TabHost tabHost, String label, Class<? extends Activity> targetClass) {
        TabHost.TabSpec nextActionTabSpec = tabHost.newTabSpec(TAB_TAG);
        nextActionTabSpec.setIndicator(label).setContent(new Intent(this, targetClass));
        tabHost.addTab(nextActionTabSpec);
    }
}
