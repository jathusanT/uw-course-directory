package com.jathusan.uwcourses;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class NavDrawerArrayListAdapter extends ArrayAdapter<String> {

    int resource;
    Context ctx;

    public NavDrawerArrayListAdapter(Context context, int resource, List<String> items) {
        super(context, resource, items);
        this.resource=resource;
        this.ctx = context;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout classView;

        String item = getItem(position);

        if(convertView==null) {
            classView = new LinearLayout(getContext());
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi;
            vi = (LayoutInflater)getContext().getSystemService(inflater);
            vi.inflate(resource, classView, true);
        } else {
            classView = (LinearLayout) convertView;
        }

        TextView title =(TextView)classView.findViewById(R.id.subjectListTitle);

        title.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "roboto_lt.ttf"));
        title.setTextColor(Color.DKGRAY);
        title.setText(item);

        return classView;
    }

}
