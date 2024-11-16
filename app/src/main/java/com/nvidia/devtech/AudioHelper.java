package com.nvidia.devtech;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.util.Log;
import java.io.IOException;

public class AudioHelper {
    private static final int MAX_SOUND_STREAMS = 10;
    private static String ResourceLocation = "com.nvidia.devtech.audio:raw/";
    private static final String TAG = "AudioHelper";
    private static AudioHelper instance;
    private MediaPlayer MusicPlayer = null;
    private SoundPool Sounds = null;
    private Context context = null;

    private AudioHelper() {
    }

    public static AudioHelper getInstance() {
        if (instance == null) {
            AudioHelper audioHelper = new AudioHelper();
            instance = audioHelper;
            audioHelper.Initialise();
        }
        return instance;
    }

    void Initialise() {
        SoundPool soundPool = new SoundPool(MAX_SOUND_STREAMS, 3, 0);
        this.Sounds = soundPool;
        String str = TAG;
        if (soundPool == null) {
            Log.e(str, "failed to create soundpool instance");
        }
        Log.i(str, "created sound pool");
    }

    public int LoadSound(String str, int i) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Load sound ");
        stringBuilder.append(str);
        String stringBuilder2 = stringBuilder.toString();
        String str2 = TAG;
        Log.i(str2, stringBuilder2);
        stringBuilder = new StringBuilder();
        stringBuilder.append(ResourceLocation);
        stringBuilder.append(str);
        int identifier = this.context.getResources().getIdentifier(stringBuilder.toString(), null, null);
        if (identifier != 0) {
            return this.Sounds.load(this.context, identifier, i);
        }
        StringBuilder stringBuilder3 = new StringBuilder();
        stringBuilder3.append("unidentified resource id for ");
        stringBuilder3.append(str);
        Log.i(str2, stringBuilder3.toString());
        return 0;
    }

    public int LoadSoundAsset(String str, int i) {
        AssetFileDescriptor openFd;
        try {
            openFd = this.context.getAssets().openFd(str);
        } catch (IOException e) {
            e.printStackTrace();
            openFd = null;
        }
        return this.Sounds.load(openFd, i);
    }

    public void MusicSetDataSource(String str) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(ResourceLocation);
            stringBuilder.append(str);
            int identifier = this.context.getResources().getIdentifier(stringBuilder.toString(), null, null);
            String str2 = TAG;
            if (identifier == 0) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("unidentified resource id for ");
                stringBuilder.append(str);
                Log.i(str2, stringBuilder.toString());
                return;
            }
            MediaPlayer create = MediaPlayer.create(this.context, identifier);
            this.MusicPlayer = create;
            if (create == null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("failed to create music player");
                stringBuilder.append(str);
                Log.i(str2, stringBuilder.toString());
                return;
            }
            create.start();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e2) {
            e2.printStackTrace();
        }
    }

    public void MusicStart() {
        this.MusicPlayer.start();
    }

    public void MusicStop() {
        MediaPlayer mediaPlayer = this.MusicPlayer;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            this.MusicPlayer.stop();
            this.MusicPlayer.reset();
        }
    }

    public void MusicVolume(float f, float f2) {
        this.MusicPlayer.setVolume(f, f2);
    }

    public void PauseSound(int i) {
        this.Sounds.pause(i);
    }

    public int PlaySound(int i, float f, float f2, int i2, int i3, float f3) {
        return this.Sounds.play(i, f, f2, i2, i3, f3);
    }

    public void ResumeSound(int i) {
        this.Sounds.resume(i);
    }

    void SetMaxVolume() {
        AudioManager audioManager = (AudioManager) this.context.getSystemService("audio");
        audioManager.setStreamVolume(3, audioManager.getStreamMaxVolume(3), 0);
    }

    public void SetResouceLocation(String str) {
        ResourceLocation = str;
    }

    public void SetVolume(int i, float f, float f2) {
        this.Sounds.setVolume(i, f, f2);
    }

    public void StopSound(int i) {
        this.Sounds.stop(i);
    }

    public boolean UnloadSample(int i) {
        return this.Sounds.unload(i);
    }

    public void finalize() {
        SoundPool soundPool = this.Sounds;
        if (soundPool != null) {
            soundPool.release();
            this.Sounds = null;
        }
        MediaPlayer mediaPlayer = this.MusicPlayer;
        if (mediaPlayer != null) {
            mediaPlayer.release();
            this.MusicPlayer = null;
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }
}