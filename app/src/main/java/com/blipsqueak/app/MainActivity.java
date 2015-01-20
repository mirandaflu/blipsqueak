/*
Copyright (C) 2015 Miranda Fluharty

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.blipsqueak.app;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class MainActivity extends BaseActivity {
    private LayoutInflater vi;
    private LinearLayout llMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(R.string.title_activity_main);

        vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        llMenu = (LinearLayout) findViewById(R.id.llMenu);

        populateMainMenu();
        checkService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkService();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        switch (item.getItemId()) {
            case R.id.action_settings:
                openActivity(SettingsActivity.class);
                break;
            case R.id.action_contact:
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
                emailIntent.setType("plain/text");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"blipsqueakapp@gmail.com"});
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
                break;
            case R.id.action_website:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://blipsqueak.io"));
                startActivity(browserIntent);
                break;
            case R.id.action_help:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.help_main_title))
                        .setMessage(getString(R.string.help_main))
                        .setCancelable(true).show();
                break;
            case R.id.action_backuprestore:
                openActivity(BackupRestore.class);
        }

        return super.onOptionsItemSelected(item);
    }

    void populateMainMenu() {
        String[] titles = getResources().getStringArray(R.array.mainMenuTitles);
        String[] descriptions = getResources().getStringArray(R.array.mainMenuDescriptions);
        TypedArray drawables = getResources().obtainTypedArray(R.array.mainMenuDrawables);
        for (int i = 0; i < titles.length; i++) {
            View entry = vi.inflate(R.layout.list_item_main, null);
            TextView text1 = (TextView) entry.findViewById(R.id.text1);
            text1.setText(titles[i]);
            TextView text2 = (TextView) entry.findViewById(R.id.text2);
            text2.setText(descriptions[i]);
            ImageView drawable = (ImageView) entry.findViewById(R.id.drawable);
            drawable.setImageResource(drawables.getResourceId(i, -1));
            switch (i) {
                case 0:
                    entry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openActivity(AppListActivity.class);
                        }
                    });
                    break;
                case 1:
                    entry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openActivity(BlipListActivity.class);
                        }
                    });
                    break;
                case 2:
                    entry.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openActivity(TestActivity.class);
                        }
                    });
                    break;
            }
            llMenu.addView(entry);
        }
    }

    private void checkService() {
        TextView tvServiceStatus = (TextView) findViewById(R.id.tvServiceStatus);
        TextView tvServiceStatusDetail = (TextView) findViewById(R.id.tvServiceStatusDetail);
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        boolean running = false;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (blipNotificationListener.class.getName().equals(service.service.getClassName())) {
                running = true;
            }
        }
        if (running) {
            tvServiceStatus.setText(getString(R.string.status_running));
            tvServiceStatus.setTextColor(getResources().getColor(R.color.black));
            tvServiceStatusDetail.setText(getString(R.string.statusdetail_running));
        } else {
            tvServiceStatus.setText(getString(R.string.status_notrunning));
            tvServiceStatus.setTextColor(getResources().getColor(R.color.red));
            tvServiceStatusDetail.setText(getString(R.string.statusdetail_notrunning));
        }
    }

    public void openNotificationAccessSettings(View view) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
    }

}
