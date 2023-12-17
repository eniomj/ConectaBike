package com.conectabike;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout; // Add this import statement

public class ToolbarUtil {

    public static void setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        activity.setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                activity, activity.findViewById(R.id.drawer_layout), toolbar,
                R.string.open_nav, R.string.close_nav);
        ((DrawerLayout) activity.findViewById(R.id.drawer_layout)).addDrawerListener(toggle);
        toggle.syncState();
    }
}
