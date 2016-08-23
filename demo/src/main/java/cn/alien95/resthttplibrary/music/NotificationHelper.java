package cn.alien95.resthttplibrary.music;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import cn.alien95.resthttplibrary.R;

/**
 * Created by linlongxin on 2016/5/17.
 */
public class NotificationHelper {

    private Notification notification;
    private Notification.Builder builder;
    private Intent intent;
    private PendingIntent pendingIntent;
    private RemoteViews remoteViews;
    private Context mContext;

    public void create(Context context) {
        mContext = context;
        builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.logo);
        builder.setWhen(System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = builder.build();
        }
        intent = new Intent(mContext, MusicListActivity.class);
        pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_music);
        remoteViews.setOnClickPendingIntent(R.id.play, pendingIntent);

        notification.when = System.currentTimeMillis();
        notification.contentView = remoteViews;
        notification.contentIntent = pendingIntent;
        android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(2, notification);
    }

    public void setTicker(CharSequence text) {
        builder.setTicker(text);
    }

    public void setMusicName(String name) {
        remoteViews.setTextViewText(R.id.music_name, name);
    }

    public void setProgress(int progress) {
        remoteViews.setProgressBar(R.id.progress, 100, progress, true);
    }

    public void playMusic() {

    }

}
