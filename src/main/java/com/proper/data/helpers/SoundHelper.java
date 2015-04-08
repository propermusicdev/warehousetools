package com.proper.data.helpers;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import com.proper.warehousetools.R;

/**
 * Created by Lebel on 28/05/2014.
 */
public class SoundHelper {
    private Context context;
    private SoundPool soundPool;
    private	int scanSoundId;
    private int errorSoundId;

    public int getScanSoundId() {
        return scanSoundId;
    }

    public void setScanSoundId(int scanSoundId) {
        this.scanSoundId = scanSoundId;
    }

    public int getErrorSoundId() {
        return errorSoundId;
    }

    public void setErrorSoundId(int errorSoundId) {
        this.errorSoundId = errorSoundId;
    }

    public SoundHelper(Context context) {
        this.context = context;
        this.setErrorSoundId(soundPool.load(this.context.getString(R.string.SOUND_ERROR), 0));
        this.setScanSoundId(soundPool.load(this.context.getString(R.string.SOUND_SCAN), 0));
    }

    public void play(int soundId) {
        //Play with 10% (percent) sound volume
        if (this.context != null) {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
            soundPool.play(soundId, Float.valueOf("0.1"), Float.valueOf("0.1"), 0, 0, 1);
        }
    }

    public void release() {
        if (this.context != null) {
            this.soundPool.release();
        }
    }
}
