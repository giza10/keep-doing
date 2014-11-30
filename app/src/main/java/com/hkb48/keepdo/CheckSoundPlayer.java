package com.hkb48.keepdo;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

class CheckSoundPlayer {
    private final Context mContext;
    private SoundPool mSoundPool;
    private int mSoundId;

    CheckSoundPlayer(Context context) {
        mContext = context;
    }

    void load() {
        mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        mSoundId = mSoundPool.load(mContext, R.raw.done_pressed, 1);
    }

    void unload() {
        mSoundPool.release();
    }

    void play() {
        mSoundPool.play(mSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }
}
