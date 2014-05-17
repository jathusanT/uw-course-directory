package com.jathusan.uwcourses;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class SubjectArrayListAdapter extends ArrayAdapter<Subject> {

    int resource;
    Context ctx;

    public SubjectArrayListAdapter(Context context, int resource, List<Subject> items) {
        super(context, resource, items);
        this.resource=resource;
        this.ctx = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout subjectView = new LinearLayout(getContext());

        try {
            Subject subject = getItem(position);

            if (convertView == null) {
                subjectView = new LinearLayout(getContext());
                String inflater = Context.LAYOUT_INFLATER_SERVICE;
                LayoutInflater vi;
                vi = (LayoutInflater) getContext().getSystemService(inflater);
                vi.inflate(resource, subjectView, true);
            } else {
                subjectView = (LinearLayout) convertView;
            }

            TextView title = (TextView) subjectView.findViewById(R.id.subjectListTitle);
            TextView subtitle = (TextView) subjectView.findViewById(R.id.subjectListSubtitle);

            title.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "roboto_condensed_lt.ttf"));
            subtitle.setTypeface(Typeface.createFromAsset(ctx.getAssets(), "roboto_condensed_lt.ttf"));

            title.setText(subject.getDescription());
            subtitle.setText("Course Code: " + subject.getSubject().toUpperCase());

        } catch (Exception e){
        }

        return subjectView;
    }

}
