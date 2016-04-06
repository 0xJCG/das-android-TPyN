package com.das.jcastro.tpyn;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;


public class NewNoteActivity extends ActionBarActivity {
    private static Context context;
    private int index;
    private static int TAKE_PICTURE = 1;
    private String noteLocation = "";
    private static final int VR_REQUEST_T = 999;
    private static final int VR_REQUEST_C = 998;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);
        NewNoteActivity.context = getApplicationContext();
        Bundle extras = getIntent().getExtras();

        if (extras != null) { // EDITION of the note
            this.index = extras.getInt("index");
            EditText title = (EditText) findViewById(R.id.editNoteTitle);
            EditText content = (EditText) findViewById(R.id.editNoteContent);
            Note note = NoteList.getInstance(this).getNote(this.index);

            title.setText(note.getTitle());
            content.setText(note.getContent());

            // if there is an image, we show it
            byte [] image = note.getImage();
            if (image != null) {
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(note.getImage(), 0, note.getImage().length));
                imageView.setVisibility(View.VISIBLE);
            }

            // we store the gps location if the user does not want to update it
            if (!note.getLocation().equals(""))
                this.noteLocation = note.getLocation();
        } else
            index = -1;

        Button location = (Button) findViewById(R.id.buttonLocation);
        location.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // getting the manager
                LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { // if gps is not enabled, we open the settings of the phone
                    Intent i= new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(i);
                } else {
                    Location position = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (position != null) { // we have signal
                        Toast.makeText(context, getString(R.string.position_stored), Toast.LENGTH_SHORT).show();
                        Double lat = position.getLatitude(), lon = position.getLongitude();
                        noteLocation = lat + "," + lon;
                    } else // no signal
                        Toast.makeText(context, getString(R.string.position_not_stored), Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button photo = (Button) findViewById(R.id.buttonPhoto);
        photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // create intent with ACTION_IMAGE_CAPTURE action
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                // start camera activity
                startActivityForResult(intent, TAKE_PICTURE);
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

            TextView title = (TextView) findViewById(R.id.noteTitleDetails);
            title.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Empezar a escuchar
                    startActivityForResult(listenIntent, VR_REQUEST_T);
                }
            });

            TextView content = (TextView) findViewById(R.id.NoteContent);
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
        getMenuInflater().inflate(R.menu.menu_new_note, menu);
        return true;
    }

    public static Context getAppContext() {
        return NewNoteActivity.context;
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

        if (item.getItemId() == R.id.saveNote) {
            final EditText title = (EditText) findViewById(R.id.editNoteTitle);
            final EditText content = (EditText) findViewById(R.id.editNoteContent);
            final ImageView image = (ImageView) findViewById(R.id.imageView);
            String noteTitle = title.getText().toString().trim();
            String noteContent = content.getText().toString().trim();

            Drawable drawable = image.getDrawable();
            byte[] data = null;
            if (drawable != null) {
                Bitmap noteImage = ((BitmapDrawable) drawable).getBitmap();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                noteImage.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                data = outputStream.toByteArray();
            }

            if (!noteTitle.equals("") && !noteContent.equals("")) {
                if (index == -1) {
                    NoteList.getInstance(NewNoteActivity.this).addNote(noteTitle, noteContent, noteLocation, data);
                    Toast.makeText(NewNoteActivity.this, getString(R.string.note_added), Toast.LENGTH_SHORT).show();
                } else {
                    NoteList.getInstance(NewNoteActivity.this).updateNote(index, noteTitle, noteContent, noteLocation, data);
                    Toast.makeText(NewNoteActivity.this, getString(R.string.note_updated), Toast.LENGTH_SHORT).show();
                }

                Intent intent = new Intent();
                intent.setClass(NewNoteActivity.this, MainActivity.class);
                intent.putExtra("fragment", "Note");
                intent.putExtra("index", index);
                startActivity(intent);
                finish(); // finishing this activity
            } else
                Toast.makeText(NewNoteActivity.this, getString(R.string.fill_fields), Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // http://stackoverflow.com/questions/2115758/how-to-display-alert-dialog-in-android
    @Override
    public void onBackPressed() { // loading the main activity if the user wants
        new AlertDialog.Builder(this) // dialog
                .setTitle(getString(R.string.discard_note))
                .setMessage(getString(R.string.discard_note_text))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setClass(NewNoteActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "Note"); // fragment to open
                        intent.putExtra("index", index); // we show the details of the note the user was editing, if there is one
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) { // keeping this activity
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // http://hmkcode.com/android-camera-taking-photos-camera/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) { // getting the image of the camera
        if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK && intent != null){
            // get bundle and bitmap
            Bundle extras = intent.getExtras();
            Bitmap image = (Bitmap) extras.get("data");

            // resize bitmap
            int scaleToUse = 50; // this will be our percentage
            int sizeY = image.getHeight() * scaleToUse / 100;
            int sizeX = image.getWidth() * scaleToUse / 100;
            Bitmap imageScaled = Bitmap.createScaledBitmap(image, sizeX, sizeY, false);

            // set imageview
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(imageScaled);
            imageView.setVisibility(View.VISIBLE);
        }

        // Chequear el resultado de reconocimiento:
        if (requestCode == VR_REQUEST_T && resultCode == RESULT_OK && intent != null) {
            // Guardar las palabras devueltas en un ArrayList
            ArrayList<String> suggestedWords = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            EditText title = (EditText) findViewById(R.id.editNoteTitle);
            title.setText(suggestedWords.get(0));
        }

        if (requestCode == VR_REQUEST_C && resultCode == RESULT_OK && intent != null) {
            // Guardar las palabras devueltas en un ArrayList
            ArrayList<String> suggestedWords = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            EditText content = (EditText) findViewById(R.id.editNoteContent);
            content.setText(suggestedWords.get(0));
        }
    }
}
