package com.das.jcastro.tpyn;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class TaskList implements Iterable<Task> {
    private ArrayList<Task> taskList;
    private Context context;

    private static TaskList instance = null;

    private TaskList(Context context) {
        this.taskList = Database.getDatabase(context).getTasks();
        this.context = context;
    }

    public static TaskList getInstance(Context context) {
        if(instance == null) {
            instance = new TaskList(context);
        }
        return instance;
    }

    public ArrayList<Task> getTaskList() {
        return this.taskList;
    }

    public Task getTask(int index) {
        return this.taskList.get(index);
    }

    public void addTask(String title, String content, Date when, String where) {
        long code = Database.getDatabase(NewTaskActivity.getAppContext()).saveTask(title, content, when, where);
        this.taskList.add(0, new Task((int) code, title, content, when, where));
        String url = "";
        new AsyncHttpTask().execute(url, title, content);
    }

    public void updateTask(int index, String title, String content, Date when, String where) {
        Task task = this.getTask(index);
        task.setTitle(title);
        task.setContent(content);
        task.setWhen(when);
        task.setWhere(where);
        Database.getDatabase(NewTaskActivity.getAppContext()).updateTask(task.getCode(), title, content, when, where);
    }

    public void removeTask(int index) {
        Task task = this.getTask(index);
        this.taskList.remove(index);
        Database.getDatabase(NewTaskActivity.getAppContext()).deleteTask(task.getCode());
    }

    public int pendingTasks(String date) {
        return Database.getDatabase(NewTaskActivity.getAppContext()).pendingTasks(date);
    }

    public boolean isEmpty() {
        return this.taskList.isEmpty();
    }

    public void truncateTasks() {
        this.taskList.clear();
        Database.getDatabase(context).truncateTasks();
    }

    @Override
    public Iterator<Task> iterator() {
        return this.taskList.iterator();
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
