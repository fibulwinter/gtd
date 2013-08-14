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

        addTab(tabHost, getResources().getString(R.string.tab_next_action), NextActionListActivity.class, R.drawable.a_not_done);
        addTab(tabHost, getResources().getString(R.string.tab_blocked), ProjectListActivity.class, R.drawable.a_blocked);
        addTab(tabHost, getResources().getString(R.string.tab_may_be), MayBeListActivity.class, R.drawable.a_maybe);
        addTab(tabHost, getResources().getString(R.string.tab_log), DoneListActivity.class, R.drawable.a_done);
        addTab(tabHost, getResources().getString(R.string.tab_search), SearchListActivity.class, R.drawable.ic_btn_search);
    }

    private void addTab(TabHost tabHost, String label, Class<? extends Activity> targetClass, int icon) {
        TabHost.TabSpec nextActionTabSpec = tabHost.newTabSpec(TAB_TAG);
        nextActionTabSpec.setIndicator(label, getResources().getDrawable(icon)).setContent(new Intent(this, targetClass));
        tabHost.addTab(nextActionTabSpec);
    }
}
