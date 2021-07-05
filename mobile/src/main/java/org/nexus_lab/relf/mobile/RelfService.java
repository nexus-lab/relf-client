package org.nexus_lab.relf.mobile;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.nexus_lab.relf.mobile.R;

import org.nexus_lab.relf.client.RelfClient;
import org.nexus_lab.relf.client.exceptions.MemoryExceededException;
import org.nexus_lab.relf.lib.config.AssetsConfig;
import org.nexus_lab.relf.lib.config.ConfigLib;
import org.nexus_lab.relf.lib.config.SharedPreferenceConfig;

/**
 * @author Ruipeng Zhang
 */
public class RelfService extends Service {
    private static final String TAG = RelfService.class.getSimpleName();
    private static final String POLLING_BROADCAST = RelfService.class.getPackage().getName() + ".POLLING";
    private static final String NOTIFICATION_CHANNEL_ID = RelfService.class.getName();
    private static final String NOTIFICATION_CHANNEL_NAME = RelfService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    private RelfClient client;
    private boolean restartService = false;
    private BroadcastReceiver scheduler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
            // 10 min timeout
            lock.acquire(10 * 60 * 1000L);
            new Thread() {
                @Override
                public void run() {
                    try {
                        synchronized (this) {
                            client.run();
                        }
                        scheduleNextPolling(client.getTimer().getNextDelay());
                    } catch (Exception e) {
                        Log.w(TAG, e);
                        cancelNextPolling();
                        if (e instanceof MemoryExceededException) {
                            restartService = true;
                            stopService(new Intent(RelfService.this, RelfService.class));
                        }
                    }
                    lock.release();
                }
            }.start();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(POLLING_BROADCAST);
        registerReceiver(scheduler, filter);

        NotificationChannel channel;
        NotificationCompat.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_NONE);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(this);
        }

        Notification notification = builder.setSmallIcon(R.drawable.ic_desktop_windows_black_24dp)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Listening for forensic actions in the background")
                .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(scheduler);
        if (restartService) {
            restartService = false;
            startService(new Intent(this, RelfService.class));
        }
    }

    /**
     * Schedule next polling for the {@link RelfClient}.
     *
     * @param delay time till the next polling in seconds.
     */
    private void scheduleNextPolling(float delay) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(POLLING_BROADCAST);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        long triggerAt = System.currentTimeMillis() + (int) (delay * 1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
    }

    /**
     * Cancel the next polling (which essentially cancel all following polling).
     */
    private void cancelNextPolling() {
        Intent intent = new Intent();
        intent.setAction(POLLING_BROADCAST);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        SharedPreferenceConfig config = new SharedPreferenceConfig(this);
        if (config.isEmpty()) {
            config.merge(new AssetsConfig(this, "config.yaml"));
            config.write(getString(R.string.config_file_main));
        }
        ConfigLib.use(config);

        if (client == null) {
            client = new RelfClient(getApplicationContext());
        }
        scheduleNextPolling(1);

        return START_STICKY;
    }

}
