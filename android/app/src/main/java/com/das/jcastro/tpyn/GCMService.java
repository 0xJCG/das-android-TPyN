package com.das.jcastro.tpyn;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Locale;

public class GCMService extends Service {
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private GoogleCloudMessaging gcm;
    private String regid = "";

    private String SENDER_ID = "23997718676";

    public GCMService() {}

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Check device for Play Services APK.
        if (checkPlayServices()) {
            String url = "http://galan.ehu.eus/jcastro004/DAS/TPyN/updategcm.php";
            new AsyncHttpTask().execute(url);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // http://javatechig.com/android/android-networking-tutorial
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 0;
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(GCMService.this);
                }
                regid = gcm.register(SENDER_ID);

                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                /* for POST request */
                urlConnection.setRequestMethod("POST");

                /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                /* for sending data */
                urlConnection.setDoOutput(true);

                SharedPreferences sp = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                String username = sp.getString("username", "");
                String parameters = "user=" + username + "&gcm=" + regid;

                urlConnection.setFixedLengthStreamingMode(parameters.getBytes().length);
                PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                out.print(parameters);
                out.close();

                int statusCode = urlConnection.getResponseCode();

                /* 200 represents HTTP OK */
                if (statusCode ==  200) {
                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                    String response = convertInputStreamToString(inputStream);
                    result = Integer.valueOf(response);
                } else {
                    result = 0; //"Failed to fetch data!";
                }
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            if (result == 1) {
                // http://stackoverflow.com/questions/9771061/how-to-save-login-information-locally-in-android
                SharedPreferences sp = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.putString("gcm", regid);
                ed.commit();
            }
            stopSelf(); // stopping the service once done
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
            return false;
        return true;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        /* Close Stream */
        if(null != inputStream){
            inputStream.close();
        }

        return result;
    }
}
