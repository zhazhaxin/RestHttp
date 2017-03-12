package cn.alien95.restdemo.main.music;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

import cn.alien95.util.Utils;

/**
 * Created by linlongxin on 2016/5/17.
 */
public class MusicPlayer {

    private MediaPlayer player;
    private boolean isPrepared = false;

    public MusicPlayer(Context context) {
        player = new MediaPlayer();
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
