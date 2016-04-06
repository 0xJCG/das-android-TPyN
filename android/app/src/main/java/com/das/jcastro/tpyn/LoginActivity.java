package com.das.jcastro.tpyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class LoginActivity extends ActionBarActivity {
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences sp = this.getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        boolean logged = sp.getBoolean("Logged", false);

        if (logged) {
            // opening the main activity if the user is already logged
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.log_in) {
            String url = "";
            new AsyncHttpTask().execute(url);
        }

        if (id == R.id.sign_in) {
            Intent intent = new Intent(this, SigninActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    // http://stackoverflow.com/questions/23149282/sha512-hashes-differ-on-android-php-and-javascript
    public String encryptPassword(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (md != null) {
            md.update(password.getBytes());
            byte byteData[] = md.digest();
            String base64 = Base64.encodeToString(byteData, Base64.NO_WRAP);

            return base64;
        }
        return password;
    }

    private int parseResult(String result) {
        int success = 0;

        try {
            JSONObject response = new JSONObject(result);
            success = response.getInt("success");
        }catch (JSONException e){
            e.printStackTrace();
        }

        return success;
    }

    /**
     * RESULT:
     * 0 - failed
     * 1 - ok
     * 2 - input blanks
     */
    // http://javatechig.com/android/android-networking-tutorial
    public class AsyncHttpTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection urlConnection = null;
            Integer result = 2;
            try {
                /* forming th java.net.URL object */
                URL url = new URL(params[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                /* for POST request */
                urlConnection.setRequestMethod("POST");

                /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                /* for sending data */
                urlConnection.setDoOutput(true);

                final EditText userET = (EditText) findViewById(R.id.loginEditTextUser);
                final EditText passET = (EditText) findViewById(R.id.loginEditTextPass);
                String user = userET.getText().toString().trim();
                String pass = passET.getText().toString().trim();

                if (!user.equals("") && !pass.equals("")) {
                    username = user;
                    String parameters = "user=" + user + "&pass=" + encryptPassword(pass);

                    urlConnection.setFixedLengthStreamingMode(parameters.getBytes().length);
                    PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                    out.print(parameters);
                    out.close();

                    int statusCode = urlConnection.getResponseCode();

                    /* 200 represents HTTP OK */
                    if (statusCode ==  200) {
                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        String response = convertInputStreamToString(inputStream);

                        if (response.equals("0"))
                            result = 0;
                        else {
                            JSONArray json = new JSONArray(response);
                            for (int i = 0; i < json.length(); i++) {
                                JSONObject json_data = json.getJSONObject(i);
                                NoteList.getInstance(LoginActivity.this).addNote(json_data.getInt("NoteCode"), json_data.getString("NoteTitle"), json_data.getString("NoteContent"));
                            }
                            result = 1;
                        }
                    }
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            /* Download complete. Lets update UI */
            switch (result) {
                case 0:
                    Toast.makeText(LoginActivity.this, getString(R.string.not_logged), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    // http://stackoverflow.com/questions/9771061/how-to-save-login-information-locally-in-android
                    SharedPreferences sp = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putBoolean("Logged", true);
                    ed.putString("username", username);
                    ed.commit();

                    // telling the user that he/she is logged
                    Toast.makeText(LoginActivity.this, getString(R.string.logged), Toast.LENGTH_SHORT).show();

                    // opening the main activity once logged
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
                case 2:
                    Toast.makeText(LoginActivity.this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
