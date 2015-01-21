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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.nispok.snackbar.enums.SnackbarType;
import com.orm.query.Select;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewblipActivity extends BaseActivity {
    Blip blip;
    String oldBlip;
    LayoutInflater vi;
    LinearLayout ruleList;
    LinearLayout soundList;
    private int SOUND_REQUEST_CODE = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //hide soft keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        blip = new Blip();
        vi = getLayoutInflater();
        ruleList = (LinearLayout) findViewById(R.id.llRules);
        soundList = (LinearLayout) findViewById(R.id.llSounds);
        addRule(findViewById(R.id.llRules));
        oldBlip = blip.stringFromView(findViewById(R.id.blipView));
    }

    @Override
    public void onBackPressed() {
        confirmDiscard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBlip();
                return true;
            case R.id.action_export:
                startExport();
                return true;
            case R.id.action_help:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.help_detail_title))
                        .setMessage(getResources().getString(R.string.help_detail))
                        .setCancelable(true).show();
                return true;
            case android.R.id.home:
                confirmDiscard();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (requestCode == SOUND_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = intent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (uri != null) {
                addSound(uri);
            }
        }
    }

    private void startExport() {
        boolean changed = !(oldBlip.equals(blip.stringFromView(findViewById(R.id.blipView))));
        if (changed) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.save_export_prompt))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    String validMessage = blip.validateFromView(findViewById(R.id.blipView));
                                    if (validMessage.equals("valid")) {
                                        blip.saveFromView(getApplicationContext(), findViewById(R.id.blipView));
                                        new ExportTask().execute();
                                    } else {
                                        new AlertDialog.Builder(getApplicationContext())
                                                .setMessage(validMessage)
                                                .setCancelable(true)
                                                .setPositiveButton(android.R.string.ok,
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                            }
                                                        }).show();
                                    }
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
        } else {
            new ExportTask().execute();
        }
    }

    private void confirmDiscard() {
        boolean changed = !(oldBlip.equals(blip.stringFromView(findViewById(R.id.blipView))));
        if (changed) {
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.discard_prompt))
                    .setCancelable(false)
                    .setPositiveButton(android.R.string.yes,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    NewblipActivity.super.onBackPressed();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
        } else {
            NewblipActivity.super.onBackPressed();
        }
    }

    private void saveBlip() {
        String validMessage = blip.validateFromView(findViewById(R.id.blipView));
        if (validMessage.equals("valid")) {
            blip.saveFromView(this, findViewById(R.id.blipView));
            Intent resultIntent = new Intent();
            resultIntent.putExtra("blipID", blip.getId());
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            overridePendingTransition(R.anim.exit_in, R.anim.exit_out);
        } else {
            new AlertDialog.Builder(this)
                    .setMessage(validMessage)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
        }
    }

    public void addRule(View view) {
        final View rule = vi.inflate(R.layout.list_item_rule, null);
        rule.findViewById(R.id.btnRemoveRule).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup parentView = (ViewGroup) view.getParent();
                ViewGroup grandparentView = (ViewGroup) parentView.getParent();
                grandparentView.removeView(parentView);
            }
        });
        Spinner ruleType = (Spinner) rule.findViewById(R.id.spinnerFields);
        ruleType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView)parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.black));
                EditText etMatchString = (EditText) rule.findViewById(R.id.etMatchString);
                switch (position) {
                    case 0:
                    case 1:
                        etMatchString.setHint("word/phrase");
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        etMatchString.setHint("character sequence");
                        break;
                    case 6:
                    case 7:
                        etMatchString.setHint("number (3) or range (0,5)");
                        break;
                    case 8:
                        etMatchString.setHint("exact message text");
                        break;
                    case 9:
                        etMatchString.setHint("-");
                        etMatchString.setEnabled(false);
                        break;
                    case 10:
                        etMatchString.setHint("Java regular expression");
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        EditText etMatchString = (EditText) rule.findViewById(R.id.etMatchString);
        etMatchString.setTextColor(getResources().getColor(R.color.black));
        ruleList.addView(rule);
        if (ruleList.getChildCount() > 1) rule.findViewById(R.id.etMatchString).requestFocus();
    }

    void addSound(Uri soundUri) {
        View sound = vi.inflate(R.layout.list_item_sound, null);

        String soundName = RingtoneManager.getRingtone(this, soundUri).getTitle(this);
        TextView tvSoundName = (TextView) sound.findViewById(R.id.tvSoundName);
        tvSoundName.setText(soundName);
        TextView tvSoundUri = (TextView) sound.findViewById(R.id.tvSoundUri);
        tvSoundUri.setText(soundUri.toString());

        final String soundPath = Util.getPathFromUri(this, soundUri);

        sound.findViewById(R.id.btnRemoveSound).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewGroup parentView = (ViewGroup) view.getParent();
                ViewGroup grandparentView = (ViewGroup) parentView.getParent();
                grandparentView.removeView(parentView);
            }
        });

        sound.findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                try {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            mp.reset();
                            mp.release();
                        }
                    });
                    mediaPlayer.setDataSource(soundPath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        soundList.addView(sound);
    }

    public void requestNotificationSound(View view) {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getResources().getString(R.string.select_sound));
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
        this.startActivityForResult(intent, SOUND_REQUEST_CODE);
    }

    private class ExportTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SnackbarManager.show(Snackbar.with(NewblipActivity.this)
                    .type(SnackbarType.SINGLE_LINE)
                    .swipeToDismiss(false)
                    .text("Exporting...")
                    .duration(Snackbar.SnackbarDuration.LENGTH_INDEFINITE));
        }

        protected String doInBackground(String... paths) {
            Looper.prepare();

            SimpleDateFormat s = new SimpleDateFormat("yyyyMMdd-hhmmss");
            String date = s.format(new Date());
            String toFileName = Environment.getExternalStorageDirectory() + "/Blipsqueak/"+ blip.name + "_" + date + ".zip";

            File folder = new File(Environment.getExternalStorageDirectory() + "/Blipsqueak");
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                try {
                    long blipId = blip.getId();
                    String dbSugar = getApplicationContext().getFilesDir().getParentFile().getPath() + "/databases/Sugar.db";
                    String dbTemp = getApplicationContext().getFilesDir().getParentFile().getPath() + "/databases/temp.db";
                    Util.copyFile(dbSugar, dbTemp);

                    SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbTemp, null);
                    Cursor c = db.rawQuery("DELETE FROM blip WHERE id != " + blipId, null);
                    while (c.moveToNext()) {
                    }
                    c = db.rawQuery("DELETE FROM rule WHERE blip_id != " + blipId, null);
                    while (c.moveToNext()) {
                    }
                    c = db.rawQuery("DELETE FROM squeak WHERE blip_id != " + blipId, null);
                    while (c.moveToNext()) {
                    }

                    List<Squeak> squeaks = Select.from(Squeak.class).where("blip_id = " + blipId).list();
                    String[] filesToGoInZip = new String[squeaks.size() + 1];
                    filesToGoInZip[0] = getApplicationContext().getFilesDir().getParentFile().getPath() + "/databases/temp.db";
                    int i = 1;
                    for (Squeak squeak : squeaks) {
                        filesToGoInZip[i] = squeak.soundPath;
                        i += 1;
                    }
                    Compress compress = new Compress(filesToGoInZip, toFileName);
                    compress.zip();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "Export failed";
                }
            } else {
                return "Export failed";
            }
            return "Exported to " + toFileName;
        }

        protected void onPostExecute(String result) {
            SnackbarManager.show(Snackbar.with(NewblipActivity.this)
                    .type(SnackbarType.MULTI_LINE)
                    .swipeToDismiss(true)
                    .text(result)
                    .duration(Snackbar.SnackbarDuration.LENGTH_LONG));
        }
    }

}
