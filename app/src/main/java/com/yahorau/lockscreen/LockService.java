package com.yahorau.lockscreen;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
/**
 * Служба отслеживает два состояния
 * - переход экрана из выкл. в вкл. режим
 * - была выполнена перезагрузка системы
 * Создается ресивер, если было зафиксировано
 * необходимое состояние.
 */
public class LockService extends Service {

    BroadcastReceiver receiver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock lock = keyguardManager.newKeyguardLock(MainActivity.TAG);
        lock.disableKeyguard();

        IntentFilter filter = setIntentFilter();

        startForeground();

        receiver = new LockReceiver();
        registerReceiver(receiver, filter);
    }

    @NonNull
    private IntentFilter setIntentFilter() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        return filter;
    }

    /**
     * Служба не будет уничножена, если она запущена в foreground
     */
    private void startForeground() {
        Notification notification = new NotificationCompat.Builder(this)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setTicker(getResources().getString(R.string.app_name))
                .setContentText(getString(R.string.notification_content_text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(null)
                .setOngoing(true)
                .build();
        startForeground(9999, notification);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }
}
