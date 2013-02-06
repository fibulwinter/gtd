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

        addTab(tabHost, "Next Action", NextActionListActivity.class, R.drawable.a_not_done);
        addTab(tabHost, "Projects", ProjectListActivity.class, R.drawable.p_not_done);
        addTab(tabHost, "May Be", MayBeListActivity.class, R.drawable.a_maybe);
        addTab(tabHost, "Log", DoneListActivity.class, R.drawable.a_done);
    }

    private void addTab(TabHost tabHost, String label, Class<? extends Activity> targetClass, int icon) {
        TabHost.TabSpec nextActionTabSpec = tabHost.newTabSpec(TAB_TAG);
        nextActionTabSpec.setIndicator(label, getResources().getDrawable(icon)).setContent(new Intent(this, targetClass));
        tabHost.addTab(nextActionTabSpec);
    }
}
