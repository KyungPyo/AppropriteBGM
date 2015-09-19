package com.kp.appropritebgm;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.File;

/**
 * Created by KP on 2015-08-06.
 */
public class MusicPlayer {
    private MediaPlayer mediaPlayer = null;
    private Uri uri = null;
    private boolean isReady = false, isPaused = false;

    public MusicPlayer() {

    }
    public MusicPlayer(Context target, String path) {
        changeMusic(target, path);
    }

    public void changeMusic(Context target, String path) {
        Log.i("changeMusic", "path : "+path);
        // 타겟 액티비티와 재생할 파일을 받아서 설정
        uri = Uri.fromFile(new File(path));
        mediaPlayer = MediaPlayer.create(target, uri);   // MediaPlayer.create

        isReady = true;
        Log.i("changeMusic", "setting ok");
    }

    public void playFromFirst() {
        if (isReady) {
            mediaPlayer.seekTo(0);
            mediaPlayer.start();
            isPaused = false;
        } else {
            Log.e("playFromFirst", "File did not ready");
        }
    }

    public void stopMusic() {
        if(mediaPlayer.isPlaying()){
            try {
                mediaPlayer.stop();
                mediaPlayer.prepare();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer.seekTo(0);
            isPaused = false;
        }
    }

    public void playAndPause() {
        if (isReady) {
            Log.e("playAndPause","isPlaying??? "+mediaPlayer.isPlaying() + " or?? " + isPlaying());
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
                isPaused = true;
            } else {
                mediaPlayer.start();
                isPaused = false;
            }
        } else {
            Log.e("playAndPause", "File did not ready");
        }
    }

    public boolean isPlaying() {
        // 재생중이거나 일시정지중이면 재생중으로 간주
        return mediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    public void setLooping(boolean set) {
        mediaPlayer.setLooping(set);
    }
}
