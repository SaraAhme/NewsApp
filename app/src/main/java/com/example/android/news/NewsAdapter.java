package com.example.android.news;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by alslam on 12/05/2018.
 */

public class NewsAdapter extends ArrayAdapter<NewsFeed> {
    List<NewsFeed> list;
    Context context;

    public NewsAdapter(@NonNull Context context, @NonNull List<NewsFeed> objects) {
        super(context, 0, objects);
        this.list = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_news, parent, false);
        }

        final NewsFeed newsFeed = getItem(position);
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.Title);
        TextView type = (TextView) convertView.findViewById(R.id.Type);
        TextView auther = (TextView) convertView.findViewById(R.id.Auther);
        TextView date = (TextView) convertView.findViewById(R.id.Date);
        TextView secNam=(TextView) convertView.findViewById(R.id.secNam);
        // Populate the data into the template view using the data object
        title.setText(newsFeed.getTitle());
        if (newsFeed.getType() != null)
            type.setText(newsFeed.getType());
        if (newsFeed.getAuther() != null)
            auther.setText(newsFeed.getAuther());
        if (newsFeed.getDate() != null)
            date.setText(newsFeed.getDate());
        if (newsFeed.getsecNam() != null)
            secNam.setText(newsFeed.getsecNam());
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(newsFeed.getWeburl()));
                context.startActivity(intent);
            }
        });
        return convertView;
    }
}
