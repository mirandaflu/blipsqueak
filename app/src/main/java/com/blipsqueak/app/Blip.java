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

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.orm.SugarRecord;
import com.orm.query.Select;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Blip extends SugarRecord implements Serializable{

    String name;
    String regex;
    Integer rank;
    boolean caseSensitive;
    boolean matchAll;

    public Blip () {}

    public String toString() { return name; }

    public boolean matches(String message) {
        boolean res = message.matches(this.regex);
        Log.v("blipno", "\"" + message + "\".match(" + this.regex + ") ? "+res);
        return res;
    }

    public void squeak(Context context, Handler handler) {
        final Blip that = this;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int delay = preferences.getInt("sound_delay", 0);
        final List<Squeak> squeaks = Select.from(Squeak.class).where("blip_id = " + that.getId()).list();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (squeaks.size() == 1) {
                    squeaks.get(0).squeak();
                } else if (squeaks.size() > 1) {
                    Random generator = new Random();
                    int i = generator.nextInt(squeaks.size());
                    squeaks.get(i).squeak();
                }
            }
        }, delay*1000);
    }

    public void squeakWithoutDelay() {
        List<Squeak> squeaks = Select.from(Squeak.class).where("blip_id = " + this.getId()).list();
        if (squeaks.size() == 1) {
            squeaks.get(0).squeak();
        } else if (squeaks.size() > 1) {
            Random generator = new Random();
            int i = generator.nextInt(squeaks.size());
            squeaks.get(i).squeak();
        }
    }

    void updateRegex() {
        List<Rule> rules = Select.from(Rule.class).where("blip_id = " + this.getId()).list();
        String r = "";
        if (!this.caseSensitive) r += "(?i)";
        if (rules.size() == 1) {
            r += rules.get(0).reg;
        } else if (this.matchAll) {
            r += "\\A";
            for (int i = 0; i < rules.size() - 1; i++) {
                Rule rule = rules.get(i);
                r += "(?=" + rule.reg + ")";
            }
            r += "(" + rules.get(rules.size() - 1).reg + ")";
            r += ".*\\z";
        } else {
            for (Rule rule : rules) {
                r += rule.reg + "|";
            }
            r = r.substring(0,r.length() - 1);
        }
        this.regex = r;
        this.save();
    }

    String regexFrom(String type, String input) {
        if (!type.equals("matches regular expression") &&
                !type.equals("number of words") && !type.equals("number of characters")) {
            input = Pattern.quote(input);
        }
        switch(type) {
            case "contains word/phrase":
                return ".*\\b" + input + "\\b.*";
            case "does not contain word/phrase":
                return "\\A((?!\\b" + input + "\\b).)*\\z";
            case "contains character sequence":
                return ".*" + input + ".*";
            case "does not contain character sequence":
                return "\\A((?!" + input + ").)*\\z";
            case "starts with":
                return "\\A" + input + ".*";
            case "ends with":
                return ".*" + input + "\\z";
            case "number of words":
                String strippedInput = input.replaceAll("[.&&[^\\d,]]+", "");
                return "(?:\\b\\w+\\b[\\s\\r\\n]*){" + strippedInput + "}";
            case "number of characters":
                strippedInput = input.replaceAll("[.&&[^\\d,]]+", "");
                return "(.){" + strippedInput + "}";
            case "matches any message":
                return ".*";
            case "matches exactly":
            case "matches regular expression":
                return input;
            default:
                return ".*" + input + ".*";
        }
    }

    boolean valid(String type, String input) {
        switch(type) {
            case "contains word/phrase":
            case "does not contain word/phrase":
            case "contains character sequence":
            case "does not contain character sequence":
            case "starts with":
            case "ends with":
            case "matches exactly":
                return input.matches(".+");
            case "matches any message":
                return true;
            case "number of words":
            case "number of characters":
                String strippedInput = input.replaceAll("[^\\d,]+", "");
                Log.v("blipno", strippedInput);
                return strippedInput.matches("^\\d+$") ||
                        strippedInput.matches("^\\d+,\\d+$") ||
                        strippedInput.matches("^,\\d+$") ||
                        strippedInput.matches("^\\d+,$");
            case "matches regular expression":
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Pattern.compile(input);
                } catch (PatternSyntaxException exception) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    public String validateFromView(View view) {
        EditText etName = (EditText) view.findViewById(R.id.etName);
        if (etName.getText().toString().equals("")) return "Please provide a profile name";

        LinearLayout llRules = (LinearLayout) view.findViewById(R.id.llRules);
        if (llRules.getChildCount() == 0) return "Please define one or more patterns to match";
        for (int i = 0; i < llRules.getChildCount(); i++) {
            View ruleView = llRules.getChildAt(i);
            Spinner typeSpinner = (Spinner) ruleView.findViewById(R.id.spinnerFields);
            String type = typeSpinner.getSelectedItem().toString();
            EditText etMatchString = (EditText) ruleView.findViewById(R.id.etMatchString);
            String input = etMatchString.getText().toString();
            if (!valid(type, input)) return "One of your patterns seems to be invalid:\n\n   " + type + ": " + input;
        }
        LinearLayout llSounds = (LinearLayout) view.findViewById(R.id.llSounds);
        if (llSounds.getChildCount() == 0) return "Please select one or more sounds to play when this profile is matched";
        return "valid";
    }
    public void saveFromView(Context context, View view) {
        if (this.rank == null) {
            List<Blip> blips = Select.from(Blip.class).orderBy("rank DESC").limit("1").list();
            if (blips.size() == 0) {
                this.rank = 0;
            } else if (blips.get(0).rank != null) {
                this.rank = blips.get(0).rank + 1;
            } else {
                this.rank = 0;
            }
        }
        EditText etName = (EditText) view.findViewById(R.id.etName);
        this.name = etName.getText().toString();

        ToggleButton matchAll = (ToggleButton) view.findViewById(R.id.toggleMatch);
        this.matchAll = matchAll.isChecked();

        ToggleButton caseSensitive = (ToggleButton) view.findViewById(R.id.toggleCase);
        this.caseSensitive = caseSensitive.isChecked();

        this.save();

        List<Rule> rules = Select.from(Rule.class).where("blip_id = "+this.getId()).list();
        for (int i = 0; i < rules.size(); i += 1) rules.get(i).delete();
        LinearLayout llRules = (LinearLayout) view.findViewById(R.id.llRules);
        for (int i = 0; i < llRules.getChildCount(); i++) {
            View ruleView = llRules.getChildAt(i);
            Spinner typeSpinner = (Spinner) ruleView.findViewById(R.id.spinnerFields);
            String type = typeSpinner.getSelectedItem().toString();
            EditText etMatchString = (EditText) ruleView.findViewById(R.id.etMatchString);
            String input = etMatchString.getText().toString();
            Rule rule = new Rule(this, type, input, regexFrom(type, input));
            rule.save();
        }

        this.updateRegex();

        List<Squeak> sounds = Select.from(Squeak.class).where("blip_id = "+this.getId()).list();
        for (int i = 0; i < sounds.size(); i += 1) sounds.get(i).delete();
        LinearLayout llSounds = (LinearLayout) view.findViewById(R.id.llSounds);
        for (int i = 0; i < llSounds.getChildCount(); i++) {
            View soundView = llSounds.getChildAt(i);
            String soundUri = ((TextView) soundView.findViewById(R.id.tvSoundUri)).getText().toString();
            String soundPath = ((TextView) soundView.findViewById(R.id.tvSoundPath)).getText().toString();
            String soundName = ((TextView) soundView.findViewById(R.id.tvSoundName)).getText().toString();
            boolean isPath = soundUri.equals("");
            String inputString = (isPath)? soundPath: soundUri;
            Squeak squeak = new Squeak(context, this, soundName, isPath, inputString);
            squeak.save();
        }

    }
    public String stringFromView(View view) {
        String res = "";
        EditText etName = (EditText) view.findViewById(R.id.etName);
        res += etName.getText().toString();
        ToggleButton swMatchAll = (ToggleButton) view.findViewById(R.id.toggleMatch);
        res += String.valueOf(swMatchAll.isChecked());
        ToggleButton cs = (ToggleButton) view.findViewById(R.id.toggleCase);
        res += String.valueOf(cs.isChecked());
        LinearLayout llRules = (LinearLayout) view.findViewById(R.id.llRules);
        for(int i = 0; i < llRules.getChildCount(); i++) {
            View ruleView = llRules.getChildAt(i);
            Spinner typeSpinner = (Spinner) ruleView.findViewById(R.id.spinnerFields);
            res += typeSpinner.getSelectedItem().toString();
            EditText etMatchString = (EditText) ruleView.findViewById(R.id.etMatchString);
            res += etMatchString.getText().toString();
        }
        LinearLayout llSounds = (LinearLayout) view.findViewById(R.id.llSounds);
        for(int i = 0; i < llSounds.getChildCount(); i++) {
            View soundView = llSounds.getChildAt(i);
            TextView tvSoundUri = (TextView) soundView.findViewById(R.id.tvSoundUri);
            res += tvSoundUri.getText().toString();
        }
        return res;
    }
    public void restoreToView(final Context context, View view, LayoutInflater vi) {
        EditText etName = (EditText) view.findViewById(R.id.etName);
        etName.setText(this.name);

        if (this.matchAll) {
            ToggleButton swMatchAll = (ToggleButton) view.findViewById(R.id.toggleMatch);
            swMatchAll.setChecked(true);
        }

        if (this.caseSensitive) {
            ToggleButton cs = (ToggleButton) view.findViewById(R.id.toggleCase);
            cs.setChecked(true);
        }

        LinearLayout llRules = (LinearLayout) view.findViewById(R.id.llRules);
        List<Rule> rules = Select.from(Rule.class).where("blip_id = "+this.getId()).list();
        for (Rule rule : rules) {
            final View v = vi.inflate(R.layout.list_item_rule, null);
            EditText etMatchString = (EditText) v.findViewById(R.id.etMatchString);
            etMatchString.setText(rule.input);
            Spinner typeSpinner = (Spinner) v.findViewById(R.id.spinnerFields);
            String[] types = context.getResources().getStringArray(R.array.matchOptions);
            typeSpinner.setSelection(Arrays.asList(types).indexOf(rule.type));
            v.findViewById(R.id.btnRemoveRule).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewGroup parentView = (ViewGroup) view.getParent();
                    ViewGroup grandparentView = (ViewGroup) parentView.getParent();
                    grandparentView.removeView(parentView);
                }
            });
            typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    EditText etMatchString = (EditText) v.findViewById(R.id.etMatchString);
                    etMatchString.setEnabled(true);
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
                public void onNothingSelected(AdapterView<?> parent) {}
            });
            llRules.addView(v);
        }

        LinearLayout llSounds = (LinearLayout) view.findViewById(R.id.llSounds);
        List<Squeak> sounds = Select.from(Squeak.class).where("blip_id = "+this.getId()).list();
        for(final Squeak squeak : sounds) {
            View v = vi.inflate(R.layout.list_item_sound, null);

            ((TextView) v.findViewById(R.id.tvSoundName)).setText(squeak.soundName);
            ((TextView) v.findViewById(R.id.tvSoundPath)).setText(squeak.soundPath);

            v.findViewById(R.id.btnRemoveSound).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ViewGroup parentView = (ViewGroup) view.getParent();
                    ViewGroup grandparentView = (ViewGroup) parentView.getParent();
                    grandparentView.removeView(parentView);
                }
            });
            v.findViewById(R.id.btnPlay).setOnClickListener(new View.OnClickListener() {
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
                        Log.v("blipno", squeak.soundPath);
                        mediaPlayer.setDataSource(squeak.soundPath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            llSounds.addView(v);
        }
    }


}
