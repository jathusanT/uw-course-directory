package com.jathusan.uwcourses;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchActivity extends Activity {

    private TextView instructions;
    private EditText code;
    private Button goButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.jathusan.uwcourses.R.layout.activity_search);
        setupActionBar();
        overridePendingTransition(0,0);

        instructions = (TextView) findViewById(com.jathusan.uwcourses.R.id.instructions);
        instructions.setTypeface(Typeface.createFromAsset(getAssets(), "roboto_lt.ttf"));

        code = (EditText) findViewById(com.jathusan.uwcourses.R.id.ccodeenter);
        code.clearFocus();

        goButton = (Button) findViewById(com.jathusan.uwcourses.R.id.searchButton);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String query = code.getText().toString();

                if (query.contains(" ")){
                    query = query.replace(" ", "");
                }

                String subject = query;
                String codeNumber = "";

                if (query != null && !query.isEmpty() && !"".equals(query)) {

                    Pattern p = Pattern.compile("\\d.*");
                    Matcher m = p.matcher(query);
                    if (m.find()){
                        codeNumber = m.group();
                    }

                    subject = query.replace(codeNumber,"");
                }

                if (subject.contains(" ")) subject.replace(" ","");
                if (codeNumber.contains(" ")) codeNumber.replace(" ","");
                if ("".equals(codeNumber) && "".equals(subject)) return;

                if (codeNumber.equals("")) {
                    Intent searchClass = new Intent(getApplicationContext(), ClassesListActivity.class);
                    searchClass.putExtra("subject", subject.toUpperCase());
                    searchClass.putExtra("description", "Subject Search: \"" + subject.toUpperCase() + "\"");
                    startActivity(searchClass);
                    finish();
                } else{
                    Intent classInfo = new Intent(getApplicationContext(), ClassInfoActivity.class);
                    classInfo.putExtra("search", true);
                    classInfo.putExtra("subject", subject.toUpperCase());
                    classInfo.putExtra("catalog_number", codeNumber.toUpperCase());
                    startActivity(classInfo);
                    finish();
                }
            }
        });

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.jathusan.uwcourses.R.menu.search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}
