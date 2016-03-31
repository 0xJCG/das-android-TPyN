package com.das.jcastro.tpyn;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.util.TypedValue;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link PendingTasksCounterWidgetConfigureActivity PendingTasksCounterWidgetConfigureActivity}
 */
public class PendingTasksCounterWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {}

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            PendingTasksCounterWidgetConfigureActivity.deleteTitlePref(context, appWidgetIds[i]);
            PendingTasksCounterWidgetConfigureActivity.deleteColorTextPref(context, appWidgetIds[i]);
            PendingTasksCounterWidgetConfigureActivity.deleteColorBackPref(context, appWidgetIds[i]);
            PendingTasksCounterWidgetConfigureActivity.deleteColorTitlePref(context, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        /*int widgetTextColor = PendingTasksCounterWidgetConfigureActivity.loadColorText(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pending_tasks_counter_widget);
        views.setTextColor(R.id.appwidget_text, widgetTextColor);*/

        String widgetText = PendingTasksCounterWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        int widgetColorText = PendingTasksCounterWidgetConfigureActivity.loadColorText(context, appWidgetId);
        int widgetColorBack = PendingTasksCounterWidgetConfigureActivity.loadColorBack(context, appWidgetId);
        int widgetColorTitle = PendingTasksCounterWidgetConfigureActivity.loadColorTitle(context, appWidgetId);

        // Create an Intent to launch MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("fragment", "Task");
        intent.putExtra("index", -1);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.pending_tasks_counter_widget);
        views.setTextViewText(R.id.appwidget_text, widgetText);
        views.setTextColor(R.id.appwidget_text, widgetColorText);
        views.setTextViewTextSize(R.id.textView6, TypedValue.COMPLEX_UNIT_SP, widgetColorBack);
        views.setTextColor(R.id.textView6, widgetColorTitle);
        views.setOnClickPendingIntent(R.id.pending_task_widget, pendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}


