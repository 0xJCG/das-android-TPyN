package com.das.jcastro.tpyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class NoteList implements Iterable<Note> {
    private ArrayList<Note> noteList;
    private Context context;

    private static NoteList instance = null;

    private NoteList(Context context) {
        this.noteList = Database.getDatabase(context).getNotes();
        this.context = context;
    }

    public static NoteList getInstance(Context context) {
        if(instance == null) {
            instance = new NoteList(context);
        }
        return instance;
    }

    public ArrayList<Note> getNoteList() {
        return this.noteList;
    }

    public Note getNote(int index) {
        return this.noteList.get(index);
    }

    public void addNote(String title, String content, String location, byte[] image) {
        long code = Database.getDatabase(NewNoteActivity.getAppContext()).saveNote(title, content, location, image);
        this.noteList.add(0, new Note((int) code, title, content, location, image));
        String url = "";
        new AsyncHttpTask().execute(url, title, content);
    }

    public long addNote(int code, String title, String content) {
        long ok = Database.getDatabase(NewNoteActivity.getAppContext()).saveNote(code, title, content);
        if (ok != -1)
            this.noteList.add(0, new Note(code, title, content));
        return ok;
    }

    public void updateNote(int index, String title, String content, String location, byte[] image) {
        Note note = this.getNote(index);
        note.setTitle(title);
        note.setContent(content);
        note.setLocation(location);
        note.setImage(image);
        Database.getDatabase(NewNoteActivity.getAppContext()).updateNote(note.getCode(), title, content, location, image);
    }

    public void removeNote(int index) {
        Note note = this.noteList.get(index);
        this.noteList.remove(index);
        Database.getDatabase(NewNoteActivity.getAppContext()).deleteNote(note.getCode());
    }

    public boolean isEmpty() {
        return this.noteList.isEmpty();
    }

    public void truncateNotes() {
        this.noteList.clear();
        Database.getDatabase(context).truncateNotes();
    }

    @Override
    public Iterator<Note> iterator() {
        return this.noteList.iterator();
    }

    // http://javatechig.com/android/android-networking-tutorial
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            try {
                /* forming th java.net.URL object */
                URL url = new URL(params[0]);
                String title = params[1];
                String content = params[2];

                SharedPreferences sp = context.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                String user = sp.getString("username", "");

                Log.d("USER", user);

                urlConnection = (HttpURLConnection) url.openConnection();

                /* for POST request */
                urlConnection.setRequestMethod("POST");

                /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                /* for sending data */
                urlConnection.setDoOutput(true);

                String parameters = "title=" + title + "&content=" + content + "&user=" + user;

                urlConnection.setFixedLengthStreamingMode(parameters.getBytes().length);
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parameters);
                out.close();

                int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
                if (statusCode == 200) {
                    result = 1;
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {}
    }
}
