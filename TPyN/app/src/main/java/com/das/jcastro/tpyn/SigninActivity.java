package com.das.jcastro.tpyn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.LoginFilter;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class SigninActivity extends ActionBarActivity {
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_signin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.sign_in) {
            String url = "";
            new AsyncHttpTask().execute(url);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() { // loading the login activity if the user wants
        Intent intent = new Intent();
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        /* Close Stream */
        if (null != inputStream) {
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

    /**
     * RESULT:
     * 0 - input blanks
     * 1 - pass don't match
     * 2 - failed
     * 3 - ok
     */
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
                urlConnection = (HttpURLConnection) url.openConnection();

                /* for POST request */
                urlConnection.setRequestMethod("POST");

                /* optional request header */
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                /* for sending data */
                urlConnection.setDoOutput(true);

                final EditText userET = (EditText) findViewById(R.id.signinEditTextUser);
                final EditText passET = (EditText) findViewById(R.id.signinEditTextPass);
                final EditText pas2ET = (EditText) findViewById(R.id.signinEditTextPass2);
                String user = userET.getText().toString().trim();
                String pass = passET.getText().toString().trim();
                String pas2 = pas2ET.getText().toString().trim();

                if (!user.equals("") && !pass.equals("") && !pas2.equals("")) {
                    if (pass.equals(pas2)) {
                        username = user;
                        String parameters = "user=" + user + "&pass=" + encryptPassword(pass);

                        urlConnection.setFixedLengthStreamingMode(parameters.getBytes().length);
                        PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
                        out.print(parameters);
                        out.close();

                        int statusCode = urlConnection.getResponseCode();

                        /* 200 represents HTTP OK */
                        if (statusCode == 200) {
                            inputStream = new BufferedInputStream(urlConnection.getInputStream());
                            String response = convertInputStreamToString(inputStream);

                            if (response.equals("[]"))
                                result = 2;
                            else {
                                JSONArray json = new JSONArray(response);
                                int code = json.getJSONObject(0).getInt("NoteCode");
                                String title = json.getJSONObject(1).getString("NoteTitle");
                                String content = json.getJSONObject(2).getString("NoteContent");
                                if (NoteList.getInstance(SigninActivity.this).addNote(code, title, content) != -1)
                                    result = 3;
                            }
                        }
                    } else
                        result = 1;
                }
                urlConnection.disconnect();
            } catch (Exception e) {
                e.getLocalizedMessage();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            switch (result) {
                case 0:
                    Toast.makeText(SigninActivity.this, getString(R.string.input_empty), Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    Toast.makeText(SigninActivity.this, getString(R.string.pass_match), Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(SigninActivity.this, getString(R.string.not_signed), Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    // http://stackoverflow.com/questions/9771061/how-to-save-login-information-locally-in-android
                    SharedPreferences sp = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();
                    ed.putBoolean("Logged", true);
                    ed.putString("username", username);
                    ed.commit();

                    // telling the user that he/she is logged and signed
                    Toast.makeText(SigninActivity.this, getString(R.string.signed), Toast.LENGTH_SHORT).show();

                    // opening the main activity once logged and signed
                    Intent intent = new Intent(SigninActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    }
}
