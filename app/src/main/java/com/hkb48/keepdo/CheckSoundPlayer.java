package com.hkb48.keepdo;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

class CheckSoundPlayer {
    private final Context mContext;
    private SoundPool mSoundPool;
    private int mSoundId;

    CheckSoundPlayer(Context context) {
        mContext = context;
    }

    void load() {
        if(Build.VERSION.SDK_INT < 21) {
            //noinspection deprecation
            mSoundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        } else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setAudioAttributes(attr).setMaxStreams(1).build();
        }
        mSoundId = mSoundPool.load(mContext, R.raw.done_pressed, 1);
    }

    void unload() {
        mSoundPool.release();
    }

    void play() {
        mSoundPool.play(mSoundId, 1.0f, 1.0f, 0, 0, 1.0f);
    }
}
