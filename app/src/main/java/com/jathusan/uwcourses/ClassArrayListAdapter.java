package com.jathusan.uwcourses;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class ClassArrayListAdapter extends ArrayAdapter<Class> {

    int resource;
    Context ctx;

    public ClassArrayListAdapter(Context context, int resource, List<Class> items) {
        super(context, resource, items);
        this.resource=resource;
        this.ctx = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout classView;

        Class clazz = getItem(position);

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
        TextView subtitle =(TextView)classView.findViewById(R.id.subjectListSubtitle);


        title.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "roboto_condensed_lt.ttf"));
        subtitle.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "roboto_condensed_lt_i.ttf"));

        title.setText(clazz.getSubject() + " " + clazz.getCatalog_number());
        subtitle.setText(clazz.getTitle());

        return classView;
    }

}
