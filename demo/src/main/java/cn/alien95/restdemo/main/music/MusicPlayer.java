package cn.alien95.restdemo.main.music;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

import cn.alien95.restdemo.main.music.NotificationHelper;
import cn.alien95.util.Utils;

/**
 * Created by linlongxin on 2016/5/17.
 */
public class MusicPlayer {

    private MediaPlayer player;
    private NotificationHelper notificationHelper;
    private boolean isPrepared = false;
    private int progress;
    private boolean isEnd = false;


    public MusicPlayer(Context context) {
        player = new MediaPlayer();
        notificationHelper = new NotificationHelper();
        notificationHelper.create(context);
    }

    public void setDataSource(String url) {

    }

    public void start(String url) {
        try {
            player = new MediaPlayer();
            player.setDataSource(url);
            player.prepareAsync();
            if (!isPrepared) {
                Utils.Toast("准备中...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;
                player.start();
            }
        });

        player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                notificationHelper.setProgress(percent);
                Utils.Log("progress : " + percent);
            }
        });

    }

    public int getProgress() {
        return player.getCurrentPosition();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        player.setOnCompletionListener(listener);
    }

    public void stop() {
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();

    }


}
