package com.hkb48.keepdo

import android.content.Context
import android.media.SoundPool
import com.hkb48.keepdo.util.CompatUtil

class CheckSoundPlayer(private val context: Context) {
    private var soundPool: SoundPool? = null
    private var soundId = 0
    fun load() {
        soundPool = CompatUtil.soundPool
        soundId = soundPool!!.load(context, R.raw.done_pressed, 1)
    }

    fun unload() {
        soundPool!!.release()
    }

    fun play() {
        soundPool!!.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }
}