package com.jathusan.uwcourses;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class SubjectListActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, SwipeRefreshLayout.OnRefreshListener{

    private CharSequence mTitle;

    private String format = "json";
    private String response = "";

    public List<Subject> subjectList = new ArrayList<Subject>();

    private boolean requestFailed = false;
    private boolean updating = false;

    private ListView listView;
    private SwipeRefreshLayout refreshLayout;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SubjectArrayListAdapter subjectArrayAdapter;
    private TextView noInternet;
    private TextView noInternetSub;
    private TextView updatingText;
    private TextView updatingTextSub;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jathusan.uwcourses.R.layout.activity_main);
        overridePendingTransition(0,0);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(com.jathusan.uwcourses.R.id.navigation_drawer);
        mTitle = "uWaterloo Course Directory";

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                com.jathusan.uwcourses.R.id.navigation_drawer,
                (DrawerLayout) findViewById(com.jathusan.uwcourses.R.id.drawer_layout));

        setupActionBar();

        listView = (ListView) findViewById(com.jathusan.uwcourses.R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                Intent classList = new Intent(getApplicationContext(), ClassesListActivity.class);
                classList.putExtra("subject", subjectList.get(i).getSubject());
                classList.putExtra("description", subjectList.get(i).getDescription());
                startActivity(classList);
            }
        });

        subjectArrayAdapter = new SubjectArrayListAdapter(getApplicationContext(), com.jathusan.uwcourses.R.layout.list_row, subjectList);
        listView.setAdapter(subjectArrayAdapter);

        noInternet = (TextView) findViewById(com.jathusan.uwcourses.R.id.noInternet);
        noInternetSub = (TextView) findViewById(com.jathusan.uwcourses.R.id.nointernetSub);

        updatingText = (TextView) findViewById(com.jathusan.uwcourses.R.id.updatingMain);
        updatingTextSub = (TextView) findViewById(com.jathusan.uwcourses.R.id.updatingSub);

        progressBar = (ProgressBar) findViewById(com.jathusan.uwcourses.R.id.progressBar);

        // we dont want to see this text at launch
        updatingText.setVisibility(View.GONE);
        updatingTextSub.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        updatingText.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));
        updatingTextSub.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));

        noInternet.setVisibility(View.GONE);
        noInternetSub.setVisibility(View.GONE);
        noInternet.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));
        noInternetSub.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));

        refreshLayout = (SwipeRefreshLayout) findViewById(com.jathusan.uwcourses.R.id.swipe_container);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorScheme(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_red_dark,
                android.R.color.holo_orange_dark);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {}

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    refreshLayout.setEnabled(true);
                else
                    refreshLayout.setEnabled(false);
            }
        });

        // start an initial data pull
        new subjectJSONPullTask().execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            final int actionbarTitle = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
            TextView actionBarText = (TextView) findViewById(actionbarTitle);
            actionBarText.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_condensed.ttf"));
        } catch (Exception e){
            Log.e("Exception", "Could not find action bar title.");
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(com.jathusan.uwcourses.R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                break;
            case 2:
                Intent search = new Intent (getApplicationContext(), SearchActivity.class);
                startActivity(search);
                break;
            case 3:
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"j3thiruc@uwaterloo.ca"});
                i.putExtra(Intent.EXTRA_SUBJECT, "UW Course Directory App");
                try {
                    startActivity(Intent.createChooser(i, "Send Email With..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "No Email Clients Installed", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void setupActionBar(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(Color.parseColor(Common.actionBarColour));
        }

        ActionBar ab = getActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor(Common.actionBarColour)));
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public void onRefresh() {
        // start updating data
        try {
            if (!updating) {
                new subjectJSONPullTask().execute();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 5000);
            }
        } catch (Exception e){
            Log.e("Subject Pull Task", "Caught Exception");
        }
    }

    private class subjectJSONPullTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected void onPreExecute() {

            updating = true;
            requestFailed = false;

            super.onPreExecute();
            refreshLayout.setRefreshing(true);

            if (subjectList != null){
                subjectList.clear();
                updateSubjectList();
            }

            noInternet.setVisibility(View.GONE);
            noInternetSub.setVisibility(View.GONE);

            progressBar.setVisibility(View.VISIBLE);
            updatingText.setVisibility(View.VISIBLE);
            updatingTextSub.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet("https://api.uwaterloo.ca/v2/codes/subjects." + format + "?key=" + Common.apiKey);
                HttpResponse httpResponse = client.execute(request);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                response = builder.toString();

                if (response != null) {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray allSubjects = jsonObject.getJSONArray("data");

                    for (int i = 0; i < allSubjects.length(); i ++){
                        JSONObject jsonSubject = allSubjects.getJSONObject(i);
                        Subject currSubject = new Subject();

                        currSubject.setGroup(jsonSubject.getString("group"));
                        currSubject.setUnit(jsonSubject.getString("unit"));
                        currSubject.setDescription(jsonSubject.getString("description"));
                        currSubject.setSubject(jsonSubject.getString("subject"));

                        subjectList.add(currSubject);
                    }

                }

            } catch (Exception e){
                Log.e("Subject Async JSON Task", "Exception when executing http response.");
                requestFailed = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            refreshLayout.setRefreshing(false);

            updatingText.setVisibility(View.GONE);
            updatingTextSub.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);

            if (requestFailed){
                noInternet.setVisibility(View.VISIBLE);
                noInternetSub.setVisibility(View.VISIBLE);
            } else {
                updateSubjectList();
            }

            updating = false;

        }
    }

    private void updateSubjectList(){
        if (subjectArrayAdapter != null){
            subjectArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(com.jathusan.uwcourses.R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder alert_box = new AlertDialog.Builder(SubjectListActivity.this);
            alert_box.setTitle("Confirm Exit");
            alert_box.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    finish();
                }
            });

            alert_box.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface arg0, int arg1) {
                    // do nothing
                }
            });

            alert_box.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == com.jathusan.uwcourses.R.id.action_search) {
            Intent search = new Intent (getApplicationContext(), SearchActivity.class);
            startActivity(search);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(com.jathusan.uwcourses.R.layout.fragment_main, container, false);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((SubjectListActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }



}
