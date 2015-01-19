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

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;
import com.orm.query.Select;

import java.util.List;

public class TestActivity extends BaseActivity {
    private List<Blip> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        items = (Select.from(Blip.class).orderBy("rank ASC").list());

        EditText etTestMessage = (EditText) findViewById(R.id.etTestMessage);
        etTestMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_GO ||
                        actionId == EditorInfo.IME_NULL) {
                    testMessage(v);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_test, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void testMessage (View view) {
        EditText etTestMessage = (EditText) findViewById(R.id.etTestMessage);
        String testMessage = etTestMessage.getText().toString();
        boolean matched = false;
        for (Blip blip : items) {
            if (blip.matches(testMessage)) {
                blip.squeakWithoutDelay();
                matched = true;
                SnackbarManager.show(
                        Snackbar.with(TestActivity .this)
                                .attachToAbsListView((AbsListView) findViewById(R.id.listView))
                                .text(getResources().getString(R.string.test_triggers) + blip.name));
                break;
            }
        }
        if (!matched) {
            SnackbarManager.show(
                    Snackbar.with(TestActivity .this)
                            .attachToAbsListView((AbsListView) findViewById(R.id.listView))
                            .text(getResources().getString(R.string.test_doesnttrigger)));
        }
    }

}
