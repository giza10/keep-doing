package com.hkb48.keepdo.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import androidx.core.content.ContextCompat;

public class CompatUtil {
    public static int getColor(Context context, int id) {
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            return ContextCompat.getColor(context, id);
        } else {
            //noinspection deprecation
            return context.getResources().getColor(id);
        }
    }

    public static SoundPool getSoundPool() {
        if (Build.VERSION.SDK_INT < 21) {
            //noinspection deprecation
            return new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        } else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            return new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(1).build();
        }
    }

    public static boolean isNotificationChannelSupported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }
}
