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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.orm.query.Select;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class blipNotificationListener extends NotificationListenerService {
    private NLServiceReceiver nlservicereceiver;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();
        nlservicereceiver = new NLServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.blipsqueak.app.NOTIFICATION_LISTENER_SERVICE");
        registerReceiver(nlservicereceiver,filter);

        handler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(nlservicereceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Object tickerText = sbn.getNotification().tickerText;
        if (sbn.isOngoing() || !sbn.isClearable() || tickerText == null) return;

        List<Blip> items = (Select.from(Blip.class).orderBy("rank ASC").list());
        SharedPreferences settings = getSharedPreferences("Blipsqueak", 0);
        Set<String> selectedApps = settings.getStringSet("selectedApps", new HashSet<String>());
        String pack = sbn.getPackageName();

        if (selectedApps.contains(pack)) {
            Bundle extras = sbn.getNotification().extras;
            String text;
            try {
                text = extras.getCharSequence("android.bigText").toString();
            } catch (Exception e) {
                text = extras.getCharSequence("android.text").toString();
            }

            for (Blip blip : items) {
                if (blip.matches(text)) {
                    blip.squeak(this, handler);
                    break;
                }
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    class NLServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) { }
    }

}
