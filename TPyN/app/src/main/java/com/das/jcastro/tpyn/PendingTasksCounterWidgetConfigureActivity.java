package com.das.jcastro.tpyn;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;


/**
 * The configuration screen for the {@link PendingTasksCounterWidget PendingTasksCounterWidget} AppWidget.
 */
public class PendingTasksCounterWidgetConfigureActivity extends Activity {

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText mAppWidgetText;
    EditText mAppWidgetColorText;
    EditText mAppWidgetColorBack;
    EditText mAppWidgetColorTitle;
    private static final String PREFS_NAME = "com.das.jcastro.tpyn.PendingTasksCounterWidget";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    public PendingTasksCounterWidgetConfigureActivity() {
        super();
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.pending_tasks_counter_widget_configure);
        mAppWidgetText = (EditText) findViewById(R.id.widget_text_size);
        mAppWidgetColorText = (EditText) findViewById(R.id.widget_text_color);
        mAppWidgetColorBack = (EditText) findViewById(R.id.widget_background_color);
        mAppWidgetColorTitle = (EditText) findViewById(R.id.wtitle_color);
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        mAppWidgetText.setText(loadTitlePref(PendingTasksCounterWidgetConfigureActivity.this, mAppWidgetId));

        String hexColor = String.format("#%06X", (0xFFFFFF & loadColorText(PendingTasksCounterWidgetConfigureActivity.this, mAppWidgetId)));
        mAppWidgetColorText.setText(hexColor);

        mAppWidgetColorBack.setText(String.valueOf(loadColorBack(PendingTasksCounterWidgetConfigureActivity.this, mAppWidgetId)));

        hexColor = String.format("#%06X", (0xFFFFFF & loadColorTitle(PendingTasksCounterWidgetConfigureActivity.this, mAppWidgetId)));
        mAppWidgetColorTitle.setText(hexColor);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = PendingTasksCounterWidgetConfigureActivity.this;

            // When the button is clicked, store the string locally
            String widgetText = mAppWidgetText.getText().toString();
            saveTitlePref(context, mAppWidgetId, widgetText);

            String widgetColorText = mAppWidgetColorText.getText().toString();
            saveColorTextPref(context, mAppWidgetId, widgetColorText);

            String widgetColorBack = mAppWidgetColorBack.getText().toString();
            saveColorBackPref(context, mAppWidgetId, widgetColorBack);

            String widgetColorTitle = mAppWidgetColorTitle.getText().toString();
            saveColorTitlePref(context, mAppWidgetId, widgetColorTitle);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            PendingTasksCounterWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    // Write the prefix to the SharedPreferences object for this widget
    static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.commit();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.commit();
    }

    static void saveColorTextPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "CText" + appWidgetId, text);
        prefs.commit();
    }

    static int loadColorText(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String colorValue = prefs.getString(PREF_PREFIX_KEY + "CText" + appWidgetId, null);
        if (colorValue != null) {
            return Color.parseColor(colorValue);
        } else {
            return R.color.white;
        }
    }

    static void deleteColorTextPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + "CText" + appWidgetId);
        prefs.commit();
    }

    static void saveColorBackPref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "CBack" + appWidgetId, text);
        prefs.commit();
    }

    static int loadColorBack(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String size = prefs.getString(PREF_PREFIX_KEY + "CBack" + appWidgetId, null);
        if (size != null) {
            return Integer.parseInt(size);
        } else {
            return 10;
        }
    }

    static void deleteColorBackPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + "CBack" + appWidgetId);
        prefs.commit();
    }

    static void saveColorTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + "CTitle" + appWidgetId, text);
        prefs.commit();
    }

    static int loadColorTitle(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String colorValue = prefs.getString(PREF_PREFIX_KEY + "CTitle" + appWidgetId, null);
        if (colorValue != null) {
            return Color.parseColor(colorValue);
        } else {
            return R.color.black;
        }
    }

    static void deleteColorTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + "CTitle" + appWidgetId);
        prefs.commit();
    }
}



