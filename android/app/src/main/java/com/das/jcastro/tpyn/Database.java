package com.das.jcastro.tpyn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Database extends SQLiteOpenHelper {

    private static Database instance;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

    public static synchronized Database getDatabase(Context context) {
        if (instance == null)
            instance = new Database(context);
        return instance;
    }

    private Database(Context context) {
        super(context, "TPyN", null, 1);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS Notes ('NoteCode' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'NoteTitle' TEXT, 'NoteContent' TEXT, 'NoteLocation' TEXT DEFAULT NULL, 'NoteImage' BLOB DEFAULT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS Tasks ('TaskCode' INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 'TaskTitle' TEXT, 'TaskContent' TEXT, 'TaskWhen' DATE DEFAULT NULL, 'TaskWhere' TEXT DEFAULT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long saveNote(String title, String content, String where, byte[] image) {
        ContentValues values = new ContentValues();
        values.put("NoteTitle", title);
        values.put("NoteContent", content);
        values.put("NoteLocation", where);
        values.put("NoteImage", image);

        return this.getWritableDatabase().insert("Notes", null, values);
    }

    public long saveNote(int code, String title, String content) {
        ContentValues values = new ContentValues();
        values.put("NoteCode", code);
        values.put("NoteTitle", title);
        values.put("NoteContent", content);

        return this.getWritableDatabase().insert("Notes", null, values);
    }

    public long saveTask(String title, String content, Date when, String where) {
        ContentValues values = new ContentValues();
        values.put("TaskTitle", title);
        values.put("TaskContent", content);
        values.put("TaskWhen", formatter.format(when));
        values.put("TaskWhere", where);

        return this.getWritableDatabase().insert("Tasks", null, values);
    }

    public long updateNote(int id, String title, String content, String where, byte[] image) {
        ContentValues values = new ContentValues();
        values.put("NoteTitle", title);
        values.put("NoteContent", content);
        values.put("NoteLocation", where);
        values.put("NoteImage", image);

        return this.getWritableDatabase().update("Notes", values, "NoteCode" + " =?", new String[]{String.valueOf(id)});

    }

    public long updateTask(int id, String title, String content, Date when, String where) {
        ContentValues values = new ContentValues();
        values.put("TaskTitle", title);
        values.put("TaskContent", content);
        values.put("TaskWhen", formatter.format(when));
        values.put("TaskWhere", where);

        return this.getWritableDatabase().update("Tasks", values, "TaskCode" + " =?", new String[]{String.valueOf(id)});

    }

    public int deleteNote(int id) {
        return this.getWritableDatabase().delete("Notes", "NoteCode" + " =?", new String[]{String.valueOf(id)});
    }

    public int deleteTask(int id) {
        return this.getWritableDatabase().delete("Tasks", "TaskCode" + " =?", new String[]{String.valueOf(id)});
    }

    public ArrayList<Note> getNotes() {
        Cursor notes = this.getWritableDatabase().query("Notes", new String[]{"NoteCode", "NoteTitle", "NoteContent", "NoteLocation", "NoteImage"}, null, null, null, null, "NoteCode DESC");
        ArrayList<Note> noteList = new ArrayList<Note>();

        if (notes.moveToFirst()) {
            do {
                int noteCode = notes.getInt(0);
                String noteTitle = notes.getString(1);
                String noteContent = notes.getString(2);
                String noteLocation = notes.getString(3);
                byte[] noteImage = notes.getBlob(4);
                noteList.add(new Note(noteCode, noteTitle, noteContent, noteLocation, noteImage));
            } while (notes.moveToNext());
        }

        notes.close(); // closing the cursor

        return noteList;
    }

    public ArrayList<Task> getTasks() {
        Cursor tasks = this.getWritableDatabase().query("Tasks", new String[]{"TaskCode", "TaskTitle", "TaskContent", "TaskWhen", "TaskWhere"}, null, null, null, null, "TaskCode DESC");
        ArrayList<Task> taskList = new ArrayList<Task>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        if (tasks.moveToFirst()) {
            do {
                int taskCode = tasks.getInt(0);
                String taskTitle = tasks.getString(1);
                String taskContent = tasks.getString(2);
                Date taskWhen = null;
                try {
                    taskWhen = dateFormat.parse(tasks.getString(3));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                String taskWhere = tasks.getString(4);
                taskList.add(new Task(taskCode, taskTitle, taskContent, taskWhen, taskWhere));
            } while (tasks.moveToNext());
        }

        tasks.close(); // closing the cursor

        return taskList;
    }

    public int pendingTasks(String date) {
        int pendingTasks = 0;
        Cursor tasks = this.getWritableDatabase().query("Tasks", new String[]{"COUNT(TaskCode)"}, "TaskWhen" + "=?", new String[]{date}, null, null, null);

        if (tasks.moveToFirst()) { // there are pending tasks for the current day
            pendingTasks = tasks.getInt(0); // getting the exact number
        }

        return pendingTasks;
    }

    public void truncateNotes() {
        this.getWritableDatabase().delete("Notes", null, null);
    }

    public void truncateTasks() {
        this.getWritableDatabase().delete("Tasks", null, null);
    }
}