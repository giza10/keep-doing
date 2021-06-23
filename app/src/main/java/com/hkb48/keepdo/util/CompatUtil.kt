package com.hkb48.keepdo.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import androidx.core.content.ContextCompat

object CompatUtil {
    fun getColor(context: Context, id: Int): Int {
        val version = Build.VERSION.SDK_INT
        return if (version >= 23) {
            ContextCompat.getColor(context, id)
        } else {
            @Suppress("DEPRECATION")
            context.resources.getColor(id)
        }
    }

    val soundPool: SoundPool
        get() = if (Build.VERSION.SDK_INT < 21) {
            @Suppress("DEPRECATION")
            SoundPool(1, AudioManager.STREAM_SYSTEM, 0)
        } else {
            val attr = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(1).build()
        }

    val isNotificationChannelSupported: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
}