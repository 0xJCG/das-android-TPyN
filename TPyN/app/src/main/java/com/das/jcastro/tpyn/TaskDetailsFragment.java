package com.das.jcastro.tpyn;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TaskDetailsFragment extends Fragment implements TextToSpeech.OnInitListener {
    private int index;
    private int MY_DATA_CHECK_CODE = 0;
    private TextToSpeech myTTS;

    private OnTaskDetailsFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment TaskDetailsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TaskDetailsFragment newInstance() {
        TaskDetailsFragment fragment = new TaskDetailsFragment();
        return fragment;
    }

    public TaskDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.index = this.getArguments().getInt("id");

        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_task_details, menu);
        showGlobalContextActionBar();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.new_task) {
            Intent intent = new Intent(getActivity(), NewTaskActivity.class);
            startActivity(intent);
            getActivity().finish();
            return true;
        }

        if (item.getItemId() == R.id.edit_task) {
            Intent intent = new Intent(getActivity(), NewTaskActivity.class);
            intent.putExtra("index", index);
            startActivity(intent);
            getActivity().finish();
            return true;
        }

        if (item.getItemId() == R.id.delete_task) {
            TaskList.getInstance(getActivity()).removeTask(index);
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("fragment", "Task"); // loading the task fragment
            intent.putExtra("index", -1); // or the task details if the user is editing one
            startActivity(intent);
            Toast.makeText(getActivity(), getString(R.string.task_deleted), Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.task_section);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ScrollView layout = (ScrollView) inflater.inflate(R.layout.fragment_task_details, container, false);
        final TextView title = (TextView) layout.findViewById(R.id.tTitleDetails);
        final TextView content = (TextView) layout.findViewById(R.id.tContentDetails);
        TextView date = (TextView) layout.findViewById(R.id.tDateDetails);
        TextView where = (TextView) layout.findViewById(R.id.tWhereDetails);
        Task task = TaskList.getInstance(getActivity()).getTask(this.index);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        title.setText(task.getTitle());
        content.setText(task.getContent());

        title.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get API version:
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;

                // API 21 edo altuago
                if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    myTTS.speak(title.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                } else
                    myTTS.speak(title.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }});

        content.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Get API version:
                int currentapiVersion = android.os.Build.VERSION.SDK_INT;

                // API 21 edo altuago
                if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    myTTS.speak(content.getText().toString(), TextToSpeech.QUEUE_FLUSH, null, null);
                } else
                    myTTS.speak(content.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
            }});

        date.setText(formatter.format(task.getWhen()));
        where.setText(task.getWhere());

        return layout;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onTaskDetailsFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTaskDetailsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTaskDetailsFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onTaskDetailsFragmentInteraction(Uri uri);
    }

    // http://code.tutsplus.com/tutorials/android-sdk-using-the-text-to-speech-engine--mobile-8540
    public void onInit(int initStatus) {
        //check for successful instantiation
        if (initStatus == TextToSpeech.SUCCESS) {
            if(myTTS.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                myTTS.setLanguage(Locale.US);
        } else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(getActivity(), "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // the user has the necessary data - create the TTS
                myTTS = new TextToSpeech(getActivity(), this);
            }
            else {
                // no data - install it now
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }
}
