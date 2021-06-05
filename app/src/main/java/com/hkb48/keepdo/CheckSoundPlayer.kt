package com.hkb48.keepdo

import android.content.Context
import android.media.SoundPool
import com.hkb48.keepdo.util.CompatUtil.soundPool

class CheckSoundPlayer(private val mContext: Context) {
    private var mSoundPool: SoundPool? = null
    private var mSoundId = 0
    fun load() {
        mSoundPool = soundPool
        mSoundId = mSoundPool!!.load(mContext, R.raw.done_pressed, 1)
    }

    fun unload() {
        mSoundPool!!.release()
    }

    fun play() {
        mSoundPool!!.play(mSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
    }
}