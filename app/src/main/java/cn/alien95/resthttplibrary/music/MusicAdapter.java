package cn.alien95.resthttplibrary.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.alien95.resthttp.view.HttpImageView;
import cn.alien95.resthttplibrary.R;
import cn.alien95.resthttplibrary.data.bean.Music;
import cn.alien95.view.adapter.BaseViewHolder;
import cn.alien95.view.adapter.RecyclerAdapter;

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
        return new MusicViewHolder(parent, R.layout.item_music);
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    class MusicViewHolder extends BaseViewHolder<Music> {

        @BindView(R.id.image)
        HttpImageView image;
        @BindView(R.id.song_name)
        TextView songName;
        @BindView(R.id.singer_name)
        TextView singerName;
        @BindView(R.id.music_switch)
        ImageView musicSwitch;

        public MusicViewHolder(ViewGroup parent, int layoutId) {
            super(parent, layoutId);
            ButterKnife.bind(this, itemView);
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
    }
}
