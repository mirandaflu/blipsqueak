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

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.Menu;
import android.view.View;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.orm.query.Select;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class BackupRestore extends BaseActivity {

    private final int RESTORE_RESULT_CODE = 97;
    private final int IMPORT_RESULT_CODE = 98;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
        setContentView(R.layout.activity_backup_restore);
        setTitle("Backup and Restore");

        if (Select.from(Blip.class).count() == 0) {
            (findViewById(R.id.btnBackup)).setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RESTORE_RESULT_CODE:
                    new RestoreTask().execute(data.getData().getPath());
                    break;
                case IMPORT_RESULT_CODE:
                    new ImportTask().execute(data.getData().getPath());
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_backup_restore, menu);
        return true;
    }

    public void backup (View view) {
        SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String date = s.format(new Date());
        String toFileName = Environment.getExternalStorageDirectory() + "/Blipsqueak/backup_" + date + ".zip";
        new BackupTask().execute(toFileName);
    }

    public void startRestore (View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, RESTORE_RESULT_CODE);
    }

    public void startImport (View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("file/*");
        startActivityForResult(intent, IMPORT_RESULT_CODE);
    }

    private class RestoreTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SnackbarManager.show(Snackbar.with(BackupRestore.this)
                    .type(SnackbarType.SINGLE_LINE)
                    .swipeToDismiss(false)
                    .text("Restoring...")
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
        }

        protected String doInBackground(String... paths) {
            try {
                for (String path : paths) {
                    Decompress decompress = new Decompress(path, getApplicationContext().getFilesDir() + "/sounds/");
                    decompress.unzip();
                    String fromFileName = getApplicationContext().getFilesDir() + "/sounds/Sugar.db";
                    String toFileName = getApplicationContext().getFilesDir().getParentFile().getPath() + "/databases/Sugar.db";
                    Util.copyFile(fromFileName, toFileName);
                    (new File(fromFileName)).delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Restore failed";
            }
            return "Restored from " + paths[0];
        }

        protected void onPostExecute(String result) {
            SnackbarManager.show(Snackbar.with(BackupRestore.this)
                    .type(SnackbarType.MULTI_LINE)
                    .swipeToDismiss(true)
                    .text(result)
                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG));
        }
    }

    private class BackupTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SnackbarManager.show(Snackbar.with(BackupRestore.this)
                    .type(SnackbarType.SINGLE_LINE)
                    .swipeToDismiss(false)
                    .text("Backing up...")
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
        }

        protected String doInBackground(String... paths) {
            Looper.prepare();
            for (String toFileName : paths) {
                File folder = new File(Environment.getExternalStorageDirectory() + "/Blipsqueak");
                boolean success = true;
                if (!folder.exists()) {
                    success = folder.mkdirs();
                }
                if (success) {
                    try {
                        List<Squeak> squeaks = Select.from(Squeak.class).list();
                        String[] filesToGoInZip = new String[squeaks.size() + 1];
                        filesToGoInZip[0] = getApplicationContext().getFilesDir().getParentFile().getPath() + "/databases/Sugar.db";
                        int i = 1;
                        for (Squeak squeak : squeaks) {
                            filesToGoInZip[i] = squeak.soundPath;
                            i += 1;
                        }
                        Compress compress = new Compress(filesToGoInZip, toFileName);
                        compress.zip();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "Backup failed";
                    }
                } else {
                    return "Backup failed";
                }
            }
            return "Backed up to " + paths[0];
        }

        protected void onPostExecute(String result) {
            SnackbarManager.show(Snackbar.with(BackupRestore.this)
                    .type(SnackbarType.MULTI_LINE)
                    .swipeToDismiss(true)
                    .text(result)
                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG));
        }
    }

    private class ImportTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SnackbarManager.show(Snackbar.with(BackupRestore.this)
                    .type(SnackbarType.SINGLE_LINE)
                    .swipeToDismiss(false)
                    .text("Importing...")
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
        }

        protected String doInBackground(String... paths) {
            try {
                for (String path : paths) {
                    Decompress decompress = new Decompress(path, getApplicationContext().getFilesDir() + "/sounds/");
                    decompress.unzip();

                    String dbFile = getApplicationContext().getFilesDir() + "/sounds/Sugar.db";
                    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);

                    Cursor c = db.rawQuery("SELECT * FROM blip", null);
                    while (c.moveToNext()) {
                        Blip blip = new Blip();
                        blip.name = c.getString(1);
                        List<Blip> blips = Select.from(Blip.class).orderBy("rank DESC").limit("1").list();
                        if (blips.size() == 0) {
                            blip.rank = 0;
                        } else if (blips.get(0).rank != null) {
                            blip.rank = blips.get(0).rank + 1;
                        } else {
                            blip.rank = 0;
                        }
                        blip.regex = c.getString(3);
                        blip.caseSensitive = c.getString(4).equals("1");
                        blip.matchAll = c.getString(5).equals("1");
                        blip.save();

                        Cursor cRules = db.rawQuery("SELECT * FROM rule WHERE blip_id = " + c.getString(0), null);
                        while (cRules.moveToNext())
                            (new Rule(blip, cRules.getString(4), cRules.getString(2), cRules.getString(3))).save();

                        Cursor cSqueaks = db.rawQuery("SELECT * FROM squeak WHERE blip_id = " + c.getString(0), null);
                        while (cSqueaks.moveToNext())
                            (new Squeak(getApplicationContext(), blip, cSqueaks.getString(1), true, cSqueaks.getString(2))).save();
                    }
                    (new File(dbFile)).delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Import failed";
            }
            return "Imported from " + paths[0];
        }

        protected void onPostExecute(String result) {
            SnackbarManager.show(Snackbar.with(BackupRestore.this)
                    .type(SnackbarType.MULTI_LINE)
                    .swipeToDismiss(true)
                    .text(result)
                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG));
        }
    }


}
