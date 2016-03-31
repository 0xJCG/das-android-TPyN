package com.das.jcastro.tpyn;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.widget.DrawerLayout;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

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


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        NoteFragment.OnNoteFragmentInteractionListener, TaskFragment.OnTaskFragmentInteractionListener, NoteDetailsFragment.OnNoteDetailsFragmentInteractionListener,
        TaskDetailsFragment.OnTaskDetailsFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        MainActivity.context = getApplicationContext();

        // calling the service to show if there are tasks for the current day with a notification
        startService(new Intent(getBaseContext(), MyService.class));

        // calling the gcm service to store de gcm id of the user in background
        startService(new Intent(getBaseContext(), GCMService.class));

        // depending where we are getting click, we have to open one, or more, specifically fragments
        Bundle extras = getIntent().getExtras();
        if (extras != null) { // if we are getting nothing, we do nothing
            String fragment = extras.getString("fragment"); // getting the fragment to open
            int index = extras.getInt("index"); // getting the index
            if (fragment != null) { // always checking the things
                if (fragment.equals("Note")) { // note fragment
                    if (index != -1) // if there is an index, we open the note fragment details
                        this.callNoteDetails(extras.getInt("index"));
                } else { // it is not necessary to open the note fragment, it always opens that first
                    if (index != -1)
                        this.callTaskDetails(extras.getInt("index"));
                    else
                        this.callTasks(); // opening the task fragment
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Fragment fragment = new Fragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        switch(position) {
            case 0:
                fragment = new NoteFragment();
                break;
            case 1:
                fragment = new TaskFragment();
                break;
            case 2:
                logout();
                break;
        }
        onSectionAttached(position);
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment) // important! container to open the lists
                .commit();

        // if the screen is landscape, try to open the first item of the list selected
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            boolean emptyLists = true;
            switch(position) {
                case 0:
                    emptyLists = NoteList.getInstance(this).isEmpty();
                    break;
                case 1:
                    emptyLists = TaskList.getInstance(this).isEmpty();
                    break;
                case 2:
                    logout();
                    break;
            }
            if (!emptyLists) { // if the list is empty, we don't call the fragment
                Bundle bundle = new Bundle();
                bundle.putString("id", String.valueOf(0));
                Fragment fragInfo = new Fragment();
                switch(position) {
                    case 0:
                        fragInfo = new NoteDetailsFragment();
                        break;
                    case 1:
                        fragInfo = new TaskDetailsFragment();
                        break;
                    case 2:
                        logout();
                        break;
                }
                fragInfo.setArguments(bundle);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.detail_container, fragInfo); // this container only appears if the screen is landscape
                transaction.commit();
            }
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 0:
                mTitle = getString(R.string.notes_section);
                break;
            case 1:
                mTitle = getString(R.string.tasks_section);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // listener for note fragment
    public void onNoteFragmentInteraction(String id) {
        this.callNoteDetails(Integer.parseInt(id));
    }

    // listener for task fragment
    public void onTaskFragmentInteraction(String id) {
        this.callTaskDetails(Integer.parseInt(id));
    }

    // listener for note details fragment, nothing to do
    public void onNoteDetailsFragmentInteraction(Uri uri) {}

    // listener for task details fragment, nothing to do
    public void onTaskDetailsFragmentInteraction(Uri uri) {}

    /*
    * FUNCTIONS TO OPEN THE DESIRED FRAGMENTS.
    * */

     private void callNoteDetails(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", index);
        NoteDetailsFragment fragInfo = new NoteDetailsFragment();
        fragInfo.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) // screen in landscape
            transaction.replace(R.id.detail_container, fragInfo);
        else // portrait
            transaction.replace(R.id.container, fragInfo);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void callTasks() {
        TaskFragment fragInfo = new TaskFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragInfo); // always container the lists!
        transaction.commit();
    }

    private void callTaskDetails(int index) {
        Bundle bundle = new Bundle();
        bundle.putInt("id", index);
        TaskDetailsFragment fragInfo = new TaskDetailsFragment();
        fragInfo.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) // screen in landscape
            transaction.replace(R.id.detail_container, fragInfo);
        else // portrait
            transaction.replace(R.id.container, fragInfo);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onBackPressed() { // opening the previous opened fragment if back space is pressed. Could not be any fragment to open, so the app closes
        if(getFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
        else
            getFragmentManager().popBackStack();
    }

    private void logout() {
        // putting logged as false
        SharedPreferences sp = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("Logged", false);
        ed.putString("username", "");
        ed.commit();

        // truncating tables
        NoteList.getInstance(this).truncateNotes();
        TaskList.getInstance(this).truncateTasks();

        // opening the login activity again
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
