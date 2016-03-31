package com.das.jcastro.tpyn;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


public class NewTaskActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener {
    private static Context context;
    private int index;
    private ArrayList<String> locationsFile = new ArrayList<String>();
    private static String path;
    private Date date = null;
    private static final int VR_REQUEST_T = 999;
    private static final int VR_REQUEST_C = 998;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        NewTaskActivity.context = getApplicationContext();
        Bundle extras = getIntent().getExtras();

        this.loadLocationFile();

        AutoCompleteTextView where = (AutoCompleteTextView) findViewById(R.id.editTaskWhere);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, locationsFile);
        where.setAdapter(adapter);

        if (extras != null) {
            this.index = extras.getInt("index");
            EditText title = (EditText) findViewById(R.id.editTaskTitle);
            EditText content = (EditText) findViewById(R.id.editTaskContent);

            Task task = TaskList.getInstance(this).getTask(this.index);

            title.setText(task.getTitle());
            content.setText(task.getContent());
            where.setText(task.getWhere());

            // we store the date if the user does not want to update it
            if (task.getWhen() != null)
                this.date = task.getWhen();
        } else
            index = -1;

        Button pickDate = (Button) findViewById(R.id.buttonDate);
        pickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DialogFragment newFragment = new DatePicker();
                newFragment.show(getSupportFragmentManager(), "datePicker");

            }
        });

        // speech recognition
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0) { // we can
            // Iniciar el intent de reconocimiento
            final Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            // Indicar datos de reconocimiento mediante el intent
            listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
            listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.say_something));
            listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);

            TextView title = (TextView) findViewById(R.id.TaskTitle);
            title.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Empezar a escuchar
                    startActivityForResult(listenIntent, VR_REQUEST_T);
                }
            });

            TextView content = (TextView) findViewById(R.id.TaskContent);
            content.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Empezar a escuchar
                    startActivityForResult(listenIntent, VR_REQUEST_C);
                }
            });
        } else { // we can't
            Toast.makeText(this, getString(R.string.no_vr), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_task, menu);
        return true;
    }

    public static Context getAppContext() {
        return NewTaskActivity.context;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        if (item.getItemId() == R.id.saveTask) {
            final EditText title = (EditText) findViewById(R.id.editTaskTitle);
            final EditText content = (EditText) findViewById(R.id.editTaskContent);
            final EditText where = (EditText) findViewById(R.id.editTaskWhere);
            String taskTitle = title.getText().toString().trim();
            String taskContent = content.getText().toString().trim();
            String taskWhere = where.getText().toString().trim();

            if (!taskTitle.equals("") && !taskContent.equals("") && !taskWhere.equals("")) {
                if (date == null) {
                    Toast.makeText(NewTaskActivity.this, getString(R.string.need_pick_date), Toast.LENGTH_SHORT).show();
                } else {
                    if (index == -1) {
                        if (!locationsFile.contains(taskWhere)) { // Saving the location if does not exit in the array.
                            try {
                                FileWriter fileWriter = new FileWriter(path, true); // writing in the file
                                BufferedWriter out = new BufferedWriter(fileWriter);
                                out.write(taskWhere);
                                out.newLine();
                                out.flush();
                                out.close(); // closing the buffer
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            locationsFile.add(taskWhere);
                        }
                        TaskList.getInstance(NewTaskActivity.this).addTask(taskTitle, taskContent, date, taskWhere);
                        Toast.makeText(NewTaskActivity.this, getString(R.string.task_added), Toast.LENGTH_SHORT).show();

                        /*Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData("content://com.android.calendar/events")
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date.getTime())
                                .putExtra(CalendarContract.Events.ALL_DAY, true)
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date.getTime())
                                .putExtra(CalendarContract.Events.TITLE, taskTitle)
                                .putExtra(CalendarContract.Events.DESCRIPTION, taskContent)
                                .putExtra(CalendarContract.Events.EVENT_LOCATION, taskWhere);
                        startActivity(intent);*/

                        ContentResolver cr = getContentResolver();
                        ContentValues values = new ContentValues();
                        values.put(CalendarContract.Events.DTSTART, date.getTime());
                        values.put(CalendarContract.Events.ALL_DAY, true);
                        values.put(CalendarContract.Events.DTEND, date.getTime());
                        values.put(CalendarContract.Events.TITLE, taskTitle);
                        values.put(CalendarContract.Events.DESCRIPTION, taskContent);
                        values.put(CalendarContract.Events.CALENDAR_ID, 1);
                        values.put(CalendarContract.Events.EVENT_LOCATION, taskWhere);
                        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getID());
                        cr.insert(CalendarContract.Events.CONTENT_URI, values);
                    } else {
                        TaskList.getInstance(NewTaskActivity.this).updateTask(index, taskTitle, taskContent, date, taskWhere);
                        Toast.makeText(NewTaskActivity.this, getString(R.string.task_updated), Toast.LENGTH_SHORT).show();
                    }
                    Intent intent = new Intent();
                    intent.setClass(NewTaskActivity.this, MainActivity.class);
                    intent.putExtra("fragment", "Task"); // loading the task fragment
                    intent.putExtra("index", index); // or the task details if the user is editing one
                    startActivity(intent);
                    finish(); // finishing this activity
                }
            } else
                Toast.makeText(NewTaskActivity.this, getString(R.string.fill_fields), Toast.LENGTH_SHORT).show();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // http://stackoverflow.com/questions/151777/saving-activity-state-in-android
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        EditText title = (EditText) findViewById(R.id.editTaskTitle);
        EditText content = (EditText) findViewById(R.id.editTaskContent);
        EditText where = (EditText) findViewById(R.id.editTaskWhere);
        savedInstanceState.putInt("index", index);
        savedInstanceState.putString("title", title.getText().toString().trim());
        savedInstanceState.putString("content", content.getText().toString().trim());
        if (this.date == null)
            savedInstanceState.putString("date", "");
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            savedInstanceState.putString("date", dateFormat.format(date));
        }
        savedInstanceState.putString("where", where.getText().toString().trim());
    }

    // http://stackoverflow.com/questions/151777/saving-activity-state-in-android
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        EditText title = (EditText) findViewById(R.id.editTaskTitle);
        EditText content = (EditText) findViewById(R.id.editTaskContent);
        EditText where = (EditText) findViewById(R.id.editTaskWhere);
        this.index = savedInstanceState.getInt("index");
        title.setText(savedInstanceState.getString("title"));
        content.setText(savedInstanceState.getString("content"));
        where.setText(savedInstanceState.getString("where"));

        String date1 = savedInstanceState.getString("date");
        if (!date1.equals("")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                this.date = dateFormat.parse(date1);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    // http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.discard_task))
                .setMessage(getString(R.string.discard_task_text))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(NewTaskActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "Task");
                        intent.putExtra("index", index);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * File creation, if it does not exist, and read the content, if there is one.
     */
    public void loadLocationFile() {
        File root = new File(Environment.getExternalStorageDirectory(), "TPyN");
        if (!root.exists())
            root.mkdirs();
        File file = new File(root, "locations.txt");
        path = file.getAbsolutePath();
        try {
            if (!file.createNewFile()) { // File exists.
                BufferedReader buff = new BufferedReader(new FileReader(file));
                String line;

                while ((line = buff.readLine()) != null)
                    this.locationsFile.add(line);

                buff.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // storing the selected date by the user
    public void onDateSet(android.widget.DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(year, monthOfYear, dayOfMonth, 0, 0);
        this.date = c.getTime();
        Toast.makeText(NewTaskActivity.this, getString(R.string.date_stored), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // Chequear el resultado de reconocimiento:
        if (requestCode == VR_REQUEST_T && resultCode == RESULT_OK && intent != null) {
            // Guardar las palabras devueltas en un ArrayList
            ArrayList<String> suggestedWords = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            EditText title = (EditText) findViewById(R.id.editTaskTitle);
            title.setText(suggestedWords.get(0));
        }

        if (requestCode == VR_REQUEST_C && resultCode == RESULT_OK && intent != null) {
            // Guardar las palabras devueltas en un ArrayList
            ArrayList<String> suggestedWords = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            EditText content = (EditText) findViewById(R.id.editTaskContent);
            content.setText(suggestedWords.get(0));
        }
    }
}
