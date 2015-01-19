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
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import com.orm.SugarRecord;

import java.io.File;

public class Squeak extends SugarRecord<Squeak> {

    public Squeak() {}
    long blipId;
    String soundName;
    String soundPath;

    public Squeak(Context context, Blip blip, String name, boolean isPath, String inputString) {
        this.blipId = blip.getId();
        this.soundName = name;

        if (isPath) {
            this.soundPath = inputString;
        } else {
            Uri uri = Uri.parse(inputString);
            String currentPath = Util.getPathFromUri(context, uri);
            String targetPath = context.getFilesDir() + "/sounds" + currentPath.substring(currentPath.lastIndexOf('/'));
            File folder = new File(context.getFilesDir() + "/sounds");
            boolean success = true;
            boolean copied = false;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (success) {
                copied = Util.copyFile(currentPath, targetPath);
            }
            this.soundPath = (copied) ? targetPath : currentPath;
        }
    }

    public void squeak() {
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
            mediaPlayer.setDataSource(this.soundPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
