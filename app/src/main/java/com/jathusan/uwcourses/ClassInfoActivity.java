package com.jathusan.uwcourses;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class ClassInfoActivity extends Activity {

    //private String[] actionBarColors = {"#0259af", "#e33e3e", "#00933B"};

    private String subject= "";
    private String description= "";
    private String academic_level= "";
    private String catalog_number= "";
    private String course_id= "";
    private String units= "";
    private String theTitle= "";
    private String response = "";
    private String prerequisites;
    private String offerings = "";
    private String antireqs= "";
    private String coreqs= "";
    private List<String> termsOffered = new ArrayList<String>();
    private List<String> components = new ArrayList<String>();
    private boolean requestFailed = false;

    private boolean online = false;
    private boolean onlineOnly = false;
    private boolean stj = false;
    private boolean stjOnly = false;
    private boolean renison = false;
    private boolean renisonOnly = false;
    private boolean grebel = false;
    private boolean grebelOnly = false;

    private String url = null;

    private TextView title;
    private TextView subtitle;
    private TextView info;
    private TextView moreinfo;
    private TextView prereq;

    boolean isSearch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jathusan.uwcourses.R.layout.activity_class_info);
        overridePendingTransition(0,0);

        title = (TextView) findViewById(com.jathusan.uwcourses.R.id.title);
        subtitle = (TextView) findViewById(com.jathusan.uwcourses.R.id.subtitle);
        info = (TextView) findViewById(com.jathusan.uwcourses.R.id.info);
        moreinfo = (TextView) findViewById(com.jathusan.uwcourses.R.id.moreinfo);
        prereq = (TextView) findViewById(com.jathusan.uwcourses.R.id.prereq);

        title.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_condensed.ttf"));
        subtitle.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_condensed_lt.ttf"));
        info.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_condensed_lt.ttf"));
        moreinfo.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_condensed_lt.ttf"));
        prereq.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_condensed_lt.ttf"));

        getExtras();
        setupActionBar();
        setupUI();
        new prereqJSONPullTask().execute();
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

    private class prereqJSONPullTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                HttpClient client = new DefaultHttpClient();
                HttpGet request = new HttpGet("https://api.uwaterloo.ca/v2/courses/"+subject+"/"+catalog_number+".json?key="+ Common.apiKey);

                HttpResponse httpResponse = client.execute(request);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"));
                StringBuilder builder = new StringBuilder();

                for (String line = null; (line = reader.readLine()) != null;) {
                    builder.append(line).append("\n");
                }

                response = builder.toString();

                if (response != null) {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONObject currObject = jsonObject.getJSONObject("data");

                    if (isSearch){
                        theTitle = currObject.getString("title");
                        academic_level = currObject.getString("academic_level");
                        description = currObject.getString("description");
                        units = currObject.getString("units");
                        course_id = currObject.getString("course_id");
                    }

                    prerequisites = currObject.getString("prerequisites");
                    antireqs = currObject.getString("antirequisites");
                    coreqs = currObject.getString("corequisites");
                    url = currObject.getString("url");

                    JSONObject offerings = currObject.getJSONObject("offerings");

                    online = offerings.getBoolean("online");
                    stj = offerings.getBoolean("st_jerome");
                    renison = offerings.getBoolean("renison");
                    grebel = offerings.getBoolean("conrad_grebel");
                    onlineOnly = offerings.getBoolean("online_only");
                    stjOnly = offerings.getBoolean("st_jerome_only");
                    renisonOnly = offerings.getBoolean("renison_only");
                    grebelOnly = offerings.getBoolean("conrad_grebel_only");

                    JSONArray instructions = currObject.getJSONArray("instructions");
                    for (int i = 0; i < instructions.length(); i++){
                        components.add(instructions.getString(i));
                    }

                    JSONArray terms = currObject.getJSONArray("terms_offered");
                    for (int i =0; i < terms.length(); i++){
                        termsOffered.add(terms.getString(i));
                    }
                }

            } catch (Exception e){
                Log.e("Prereq Async JSON Task", "Exception when executing http response.");
                requestFailed = true;
            }

            return null;

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (requestFailed){
                prereq.setText("");
                return;
            }

            if (isSearch){
                isSearch = false;
                setupUI();
            }

            prerequisites = (prerequisites == null || prerequisites.equals("null")) ? "None Available" : prerequisites;
            antireqs = (antireqs == null || antireqs.equals("null")) ? "None Available" : antireqs;
            coreqs = (coreqs == null || coreqs.equals("null")) ? "None Available" : coreqs;

            buildOfferings();

            String newComponents = (components.size() == 0) ? "Unknown" : components.toString();
            newComponents = newComponents.replace("[","");
            newComponents = newComponents.replace("]","");
            String newTermsOffered = (termsOffered.size() == 0) ? "Unknown" : termsOffered.toString();
            newTermsOffered = newTermsOffered.replace("[","");
            newTermsOffered = newTermsOffered.replace("]","");
            newTermsOffered = newTermsOffered.replace("F","Fall");
            newTermsOffered = newTermsOffered.replace("W","Winter");
            newTermsOffered = newTermsOffered.replace("S","Spring");

            prereq.setText(Html.fromHtml("<b>Components: </b> " + newComponents + "<br><br><b>Terms Offered</b>: "+ newTermsOffered
                    +"<br><br><b>Prerequisites:</b> " + prerequisites + "<br><br><b>Antirequisites:</b> " + antireqs +"<br><br><b>Corequisites:</b> "+coreqs
                    + "<br><br><b>Additional Offerings: </b>" + offerings + "<br><br><b>Website: " + url));
        }
    }

    private void buildOfferings(){
        offerings = "";

        if (grebelOnly || onlineOnly || renisonOnly || stjOnly){
            offerings = (grebelOnly) ? "Conrad Grebel Only (Not available on campus)" : "";
            offerings = (onlineOnly) ? "Online Only (Not available on campus)" : "";
            offerings = (renisonOnly) ? "Renison Only (Not available on campus)" : "";
            offerings = (stjOnly) ? "St. Jerome's Only (Not available on campus)" : "";
        }

        if (online){
            offerings += "Online";
        }
        if (stj){
            if (offerings.length() > 0){
                offerings += ", ";
            }
            offerings += "St. Jeromes";
        }

        if (renison){
            if (offerings.length() > 0){
                offerings += ", ";
            }
            offerings += "Renison";
        }

        if (grebel){
            if (offerings.length() > 0){
                offerings += ", ";
            }
            offerings += "Conrad Grebel";
        }

        if (offerings == null || "".equals(offerings)){
            offerings = "None Available";
        }
    }

    private void getExtras(){
        if (getIntent().getExtras() != null){

            if (getIntent().getExtras().containsKey("subject")) {
                subject = getIntent().getExtras().getString("subject");
            }

            if (getIntent().getExtras().containsKey("catalog_number")) {
                catalog_number = getIntent().getExtras().getString("catalog_number");
            }

            if (getIntent().getExtras().containsKey("search")) {
                isSearch = getIntent().getExtras().getBoolean("search");
            }

            if (getIntent().getExtras().containsKey("title")) {
                theTitle = getIntent().getExtras().getString("title");
            }

            if (getIntent().getExtras().containsKey("academic_level")) {
                academic_level = getIntent().getExtras().getString("academic_level");
            }

            if (getIntent().getExtras().containsKey("description")) {
                description = getIntent().getExtras().getString("description");
            }

            if (getIntent().getExtras().containsKey("course_id")) {
                course_id = getIntent().getExtras().getString("course_id");
            }


            if (getIntent().getExtras().containsKey("units")) {
                units = getIntent().getExtras().getString("units");
            }
        }
    }

    private void setupUI(){
        if (!isSearch) {
            //capitalize first letter
            academic_level = academic_level.substring(0, 1).toUpperCase() + academic_level.substring(1);
            description = (description == null || "".equals(description)) ? "None Available" : description;

            title.setText(theTitle);
            subtitle.setText(Html.fromHtml("<b>Course ID:</b> " + course_id));
            info.setText(Html.fromHtml("<b>Units:</b> " + units + "    |    <b>Academic Level:</b> " + academic_level));
            moreinfo.setText(Html.fromHtml("<b>Course Description:</b> <br>" + description));
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
        ab.setDisplayHomeAsUpEnabled(true);

        ab.setTitle("Course Search");

        if (subject != null && catalog_number != null){
            ab.setTitle(subject + " " + catalog_number);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.jathusan.uwcourses.R.menu.class_info, menu);
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
