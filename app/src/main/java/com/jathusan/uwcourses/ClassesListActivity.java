package com.jathusan.uwcourses;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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


public class ClassesListActivity extends Activity {

    private String subject = null;
    private String description = null;

    private String response = "";
    private boolean requestFailed = false;

    private List<Class> classList = new ArrayList<Class>();

    private ListView listView;
    private ProgressBar progressBar;

    private TextView noInternet;
    private TextView noInternetSub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0,0);
        setContentView(com.jathusan.uwcourses.R.layout.activity_classes_list);

        getExtras();
        setupActionBar();

        listView = (ListView) findViewById(com.jathusan.uwcourses.R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> av, View view, int i, long l) {
                Intent classInfo = new Intent(getApplicationContext(), ClassInfoActivity.class);
                classInfo.putExtra("subject", classList.get(i).getSubject());
                classInfo.putExtra("description", classList.get(i).getDescription());
                classInfo.putExtra("academic_level", classList.get(i).getAcademic_level());
                classInfo.putExtra("catalog_number", classList.get(i).getCatalog_number());
                classInfo.putExtra("course_id", classList.get(i).getCourse_id());
                classInfo.putExtra("units", classList.get(i).getUnits());
                classInfo.putExtra("class", classList.get(i).getClass());
                classInfo.putExtra("title", classList.get(i).getTitle());
                startActivity(classInfo);
            }
        });

        progressBar = (ProgressBar) findViewById(com.jathusan.uwcourses.R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        noInternet = (TextView) findViewById(com.jathusan.uwcourses.R.id.noInternet);
        noInternetSub = (TextView) findViewById(com.jathusan.uwcourses.R.id.nointernetSub);

        noInternet.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));
        noInternetSub.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));

        new ClassInfoJSONPullTask().execute();
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

    private void getExtras(){
        if (getIntent().getExtras() != null){

            if (getIntent().getExtras().containsKey("subject")) {
                subject = getIntent().getExtras().getString("subject");
            }

            if (getIntent().getExtras().containsKey("description")) {
                description = getIntent().getExtras().getString("description");
            }
        }
    }

    private void setupActionBar(){
        // If the user is using KitKat, tint the statusbar.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setStatusBarTintColor(Color.parseColor(Common.actionBarColour));
        }

        ActionBar ab = getActionBar();
        ab.setBackgroundDrawable(new ColorDrawable(Color.parseColor(Common.actionBarColour)));
        ab.setDisplayHomeAsUpEnabled(true);

        if (description != null){
            ab.setTitle(description);
        }
    }

    private class ClassInfoJSONPullTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet("https://api.uwaterloo.ca/v2/courses/" + subject + ".json?key=" + Common.apiKey);

                HttpResponse httpResponse = client.execute(request);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }
                response = builder.toString();

                if (response != null) {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray allClasses = jsonObject.getJSONArray("data");

                    for (int i = 0; i < allClasses.length(); i ++){
                        JSONObject jsonSubject = allClasses.getJSONObject(i);
                        Class currClass = new Class();

                        currClass.setSubject(jsonSubject.getString("subject"));
                        currClass.setAcademic_level(jsonSubject.getString("academic_level"));
                        currClass.setCatalog_number(jsonSubject.getString("catalog_number"));
                        currClass.setCourse_id(jsonSubject.getString("course_id"));
                        currClass.setDescription(jsonSubject.getString("description"));
                        currClass.setTitle(jsonSubject.getString("title"));
                        currClass.setUnits(jsonSubject.getString("units"));

                        classList.add(currClass);
                    }

                }

            } catch (Exception e){
                Log.e("Class Async JSON Task", "Exception when executing http response.");
                requestFailed = true;
            }

            return null;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            noInternet.setVisibility(View.GONE);
            noInternetSub.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            progressBar.setVisibility(View.GONE);

            if (requestFailed){
                noInternet.setVisibility(View.VISIBLE);
                noInternetSub.setVisibility(View.VISIBLE);
            } else if (classList.size() == 0){
                noInternet.setText("No Courses Available");
                noInternetSub.setText("for " + description);
                noInternet.setVisibility(View.VISIBLE);
                noInternetSub.setVisibility(View.VISIBLE);
            } else {
                //set the list view
                ClassArrayListAdapter classArrayAdapter = new ClassArrayListAdapter(getApplicationContext(), com.jathusan.uwcourses.R.layout.list_row, classList);
                listView.setAdapter(classArrayAdapter);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.jathusan.uwcourses.R.menu.classes_list, menu);
        return true;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        onBackPressed();
        return true;
    }
}
