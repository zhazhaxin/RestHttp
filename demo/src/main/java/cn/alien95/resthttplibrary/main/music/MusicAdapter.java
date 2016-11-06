package cn.alien95.resthttplibrary.main.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.alien95.resthttp.view.HttpImageView;
import cn.alien95.resthttplibrary.R;
import cn.alien95.resthttplibrary.data.bean.Music;
import cn.lemon.view.adapter.BaseViewHolder;
import cn.lemon.view.adapter.RecyclerAdapter;

/**
 * Created by linlongxin on 2016/5/15.
 */
public class MusicAdapter extends RecyclerAdapter<Music> {

    private MusicPlayer musicPlayer;
    private int currentPosition;

    public MusicAdapter(Context context) {
        super(context);

        musicPlayer = new MusicPlayer(context);
    }

    @Override
    public BaseViewHolder<Music> onCreateBaseViewHolder(ViewGroup parent, int viewType) {
        return new MusicViewHolder(parent);
    }

    class MusicViewHolder extends BaseViewHolder<Music> {

        private HttpImageView image;
        private TextView songName;
        private TextView singerName;
        private ImageView musicSwitch;

        public MusicViewHolder(ViewGroup parent) {
            super(parent, R.layout.item_music);
        }

        @Override
        public void onInitializeView() {
            super.onInitializeView();
            image = findViewById(R.id.image);
            songName = findViewById(R.id.song_name);
            singerName = findViewById(R.id.singer_name);
            musicSwitch = findViewById(R.id.music_switch);
        }

        @Override
        public void setData(final Music object) {
            super.setData(object);

            image.setImageUrl(object.getAlbumpic_small());
            songName.setText(object.getSongname());
            singerName.setText(object.getSingername());
            musicSwitch.setImageResource(R.drawable.ic_close);

            if (currentPosition != 0 && currentPosition == getAdapterPosition()) {
                musicSwitch.setImageResource(R.drawable.ic_open);
            }

            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    musicPlayer.stop();
                    musicSwitch.setImageResource(R.drawable.ic_close);
                }
            });
            musicSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (musicPlayer.isPlaying() && currentPosition == getAdapterPosition()) {
                        return;
                    } else if (musicPlayer.isPlaying()) {
                        musicPlayer.stop();
                        notifyItemChanged(currentPosition);
                        musicSwitch.setImageResource(R.drawable.ic_open);
                        musicPlayer.start(object.getUrl());
                    } else {
                        musicPlayer.start(object.getUrl());
                        musicSwitch.setImageResource(R.drawable.ic_open);
                    }
                    currentPosition = getAdapterPosition();
                }
            });
        }

        @Override
        public void onItemViewClick(Music object) {
            super.onItemViewClick(object);
        }
    }
}
