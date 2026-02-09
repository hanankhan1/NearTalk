package com.example.neartalk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

public class MyService extends Service {

    static Ringtone ringtone;
    String CHANNEL_ID = "Alarm_notify";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String eventTitle = intent.getStringExtra("eventTitle");
        if (eventTitle == null) eventTitle = "Event Reminder";

        int requestCode = intent.getIntExtra("requestCode", 0);

        Intent stopIntent = new Intent(this, MyReceiver.class);
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        createChannel();

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Event Reminder")
                        .setContentText(eventTitle)
                        .setSmallIcon(R.drawable.outline_add_alert_24)
                        .setOngoing(true)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(NotificationCompat.DEFAULT_ALL)
                        .addAction(R.drawable.outline_add_alert_24, "Stop", stopPendingIntent);

        startForeground(1, builder.build());

        playAlarm();

        new Handler().postDelayed(this::stopSelf, 120000);

        return START_NOT_STICKY;
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Alarm Clock",
                    NotificationManager.IMPORTANCE_HIGH
            );
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    private void playAlarm() {
        Uri path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (path == null)
            path = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        ringtone = RingtoneManager.getRingtone(this, path);

        if (ringtone != null && !ringtone.isPlaying())
            ringtone.play();
    }

    public static void stopAlarm() {
        if (ringtone != null && ringtone.isPlaying())
            ringtone.stop();
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        stopAlarm();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
