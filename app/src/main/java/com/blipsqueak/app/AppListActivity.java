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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppListActivity extends BaseActivity {
    private ProgressBar pBar;
    private ArrayList<PInfo> apps;
    private ListView lvApps;
    private AsyncTask<Void, String, ArrayList<PInfo>> appsTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
        setContentView(R.layout.activity_applist);

        pBar = (ProgressBar) findViewById(R.id.progressBar);
        appsTask = new LoadAppsTask().execute();

        lvApps = (ListView) findViewById(R.id.lvApps);
    }

    @Override
    public void onBackPressed() {
        confirmDiscard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveSelectedApps();
                return true;
            case android.R.id.home:
                confirmDiscard();
                return true;
            case R.id.action_selectall:
                if (lvApps.getAdapter() != null)
                    for (int i = 0; i < lvApps.getAdapter().getCount(); i++) {
                        lvApps.setItemChecked(i, true);
                    }
                return true;
            case R.id.action_selectnone:
                if (lvApps.getAdapter() != null)
                    for (int i = 0; i < lvApps.getAdapter().getCount(); i++) {
                        lvApps.setItemChecked(i, false);
                    }
                return true;
            case R.id.action_help:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.help_applist_title))
                        .setMessage(getResources().getString(R.string.help_applist))
                        .setCancelable(true).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void confirmDiscard() {
        appsTask.cancel(true);
        SparseBooleanArray checked = lvApps.getCheckedItemPositions();
        Set<String> selectedApps = new HashSet<>();
        if (lvApps.getAdapter() == null) {
            super.onBackPressed();
            return;
        }
        for (int i = 0; i < lvApps.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                selectedApps.add(apps.get(i).pname);
            }
        }
        SharedPreferences settings = getSharedPreferences("Blipsqueak", 0);
        Set<String> previouslySelectedApps = settings.getStringSet("selectedApps", new HashSet<String>());

        if (selectedApps.equals(previouslySelectedApps)) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.discard_prompt))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    AppListActivity.super.onBackPressed();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {}
                            }).show();
        }
    }

    private void saveSelectedApps() {
        SparseBooleanArray checked = lvApps.getCheckedItemPositions();
        Set<String> selectedApps = new HashSet<>();
        for (int i = 0; i < lvApps.getAdapter().getCount(); i++) {
            if (checked.get(i)) {
                selectedApps.add(apps.get(i).pname);
            }
        }
        SharedPreferences settings = getSharedPreferences("Blipsqueak", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet("selectedApps", selectedApps);
        editor.apply();
        finish();
        overridePendingTransition(R.anim.exit_in, R.anim.exit_out);
    }

    private class LoadAppsTask extends AsyncTask<Void, String, ArrayList<PInfo>> {
        @Override
        protected ArrayList<PInfo> doInBackground(Void... v) {

            ArrayList<PInfo> res = new ArrayList<>();
            List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
            for (PackageInfo p : packs) {
                if (p.versionName == null || p.packageName.equals("com.blipsqueak.app")) {
                    continue;
                }
                PInfo newInfo = new PInfo();
                newInfo.appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
                newInfo.pname = p.packageName;
                newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
                res.add(newInfo);
                if (isCancelled()) break;
            }
            Collections.sort(res);
            return res;
        }

        @Override
        protected void onPostExecute(ArrayList<PInfo> appList) {
            apps = appList;
            pBar.setVisibility(View.INVISIBLE);

            lvApps.setAdapter(new AppArrayAdapter());
            lvApps.setItemsCanFocus(false);

            SharedPreferences settings = getSharedPreferences("Blipsqueak", 0);
            Set<String> selectedApps = settings.getStringSet("selectedApps", new HashSet<String>());
            for (int i = 0; i < lvApps.getAdapter().getCount(); i++) {
                if (selectedApps.contains(apps.get(i).pname)) {
                    lvApps.setItemChecked(i, true);
                }
            }
        }
    }

    private class AppArrayAdapter extends ArrayAdapter<PInfo> {
        public AppArrayAdapter() {
            super(AppListActivity.this, R.layout.list_item_app, R.id.text, apps);
        }

        @Override
        public View getView(final int position, View view, ViewGroup parent) {
            Context context = AppListActivity.this;
            LayoutInflater inflater = LayoutInflater.from(context);
            View itemView = inflater.inflate(R.layout.list_item_app, null, true);
            TextView text = (TextView) itemView.findViewById(R.id.text);
            ImageView icon = (ImageView) itemView.findViewById(R.id.icon);
            CheckBox checkbox = (CheckBox) itemView.findViewById(R.id.checkBox);
            text.setText(apps.get(position).appname);
            icon.setImageDrawable(apps.get(position).icon);

            final ListView lv = (ListView) parent;
            CheckableLinearLayout iv = (CheckableLinearLayout) itemView;
            iv.setChecked(lv.isItemChecked(position));
            checkbox.setClickable(false);

            return itemView;
        }
    }

    class PInfo implements Comparable {
        private String appname = "";
        private String pname = "";
        private Drawable icon;

        public String toString() {
            return appname;
        }

        public int compareTo(@NonNull Object otherApp) {
            return appname.compareToIgnoreCase(otherApp.toString());
        }
    }

}
