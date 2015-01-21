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
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class DetailActivity extends NewblipActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_in, R.anim.enter_out);
        setContentView(R.layout.activity_detail);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        vi = getLayoutInflater();

        Intent intent = getIntent();
        long blipID = (long) intent.getExtras().getSerializable("blipID");
        blip = Blip.findById(Blip.class, blipID);
        blip.restoreToView(this, findViewById(R.id.blipView), vi);
        oldBlip = blip.stringFromView(findViewById(R.id.blipView));

        ruleList = (LinearLayout) findViewById(R.id.llRules);
        soundList = (LinearLayout) findViewById(R.id.llSounds);
    }

}
