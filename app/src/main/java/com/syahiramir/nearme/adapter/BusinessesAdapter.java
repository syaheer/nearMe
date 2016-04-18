package com.syahiramir.nearme.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.syahiramir.nearme.MapActivity;
import com.syahiramir.nearme.R;
import com.syahiramir.nearme.data.FeedBusinesses;

import java.util.List;

/**
 * Created by Syahir on 4/16/16.
 * adapter for businesses list
 */
public class BusinessesAdapter extends BaseAdapter {
    private final Activity activity;
    private LayoutInflater inflater;
    private final List<FeedBusinesses> feedBusinesses;

    public BusinessesAdapter (Activity activity, List<FeedBusinesses> feedBusinesses) {
        this.activity = activity;
        this.feedBusinesses = feedBusinesses;
    }

    @Override
    public int getCount() {
        return feedBusinesses.size();
    }

    @Override
    public Object getItem(int location) {
        return feedBusinesses.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) activity
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.feed_businesses, parent, false);

        ImageView storeImage = (ImageView) convertView.findViewById(R.id.storeImage);
        TextView storeName = (TextView) convertView.findViewById(R.id.storeName);
        TextView distance = (TextView) convertView.findViewById(R.id.distance);
        TextView businessType = (TextView) convertView.findViewById(R.id.businessType);
        TextView status = (TextView) convertView.findViewById(R.id.status);

        //setting typeface to follow Belly's example
        Typeface openSansSemibold = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Semibold.ttf");
        Typeface openSansRegular = Typeface.createFromAsset(activity.getAssets(), "fonts/OpenSans-Regular.ttf");
        storeName.setTypeface(openSansSemibold);
        distance.setTypeface(openSansRegular);
        businessType.setTypeface(openSansRegular);
        status.setTypeface(openSansSemibold);

        final FeedBusinesses businesses = feedBusinesses.get(position);

        Picasso.with(activity).load(businesses.getImageURL()).fit().into(storeImage);
        storeName.setText(businesses.getName());
        distance.setText(businesses.getDistance());
        businessType.setText(businesses.getCategory());
        status.setText(businesses.getIsClosed());
        if(businesses.getIsClosed().equals("OPEN")){
            status.setTextColor(ContextCompat.getColor(activity, R.color.openGreen));
        } else {
            status.setTextColor(ContextCompat.getColor(activity, R.color.closeGrey));
        }

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, MapActivity.class);
                intent.putExtra("name", businesses.getName());
                intent.putExtra("latitude", businesses.getLatitude());
                intent.putExtra("longitude", businesses.getLongitude());
                activity.startActivity(intent);
            }
        });

        return convertView;
    }

}