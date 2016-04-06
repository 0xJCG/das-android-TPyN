package com.das.jcastro.tpyn;

import android.app.NotificationManager;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

// https://sekthdroid.wordpress.com/2013/02/10/crear-service-en-android/
public class MyService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // show a notification if there are any task for the current day
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String date = df.format(Calendar.getInstance().getTime());
        int pendingTaskCounter = TaskList.getInstance(getApplicationContext()).pendingTasks(date);

        if (pendingTaskCounter > 0) { // showing the notification only if the user has pending tasks
            NotificationCompat.Builder notificationConstructor = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.btn_star_big_off)
                    .setLargeIcon((((BitmapDrawable)getResources().getDrawable(R.drawable.ic_drawer)).getBitmap()))
                    .setContentTitle(getString(R.string.notification_title))
                    .setContentText(getString(R.string.notification_content))
                    .setTicker(getString(R.string.new_notification))
                    .setAutoCancel(true);
            NotificationManager notificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
            notificationManager.notify(1, notificationConstructor.build()); // show the notification
        }

        // updating, always, the widgets
        RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(), R.layout.pending_tasks_counter_widget);
        ComponentName thisWidget = new ComponentName(getApplicationContext(), PendingTasksCounterWidget.class );
        remoteViews.setTextViewText(R.id.appwidget_text, getString(R.string.pending_tasks) + pendingTaskCounter);
        AppWidgetManager.getInstance(getApplicationContext()).updateAppWidget( thisWidget, remoteViews );

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
