package com.example.pet.pixabay;

import com.bumptech.glide.Glide;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;


public class ImageAdapter extends BaseAdapter {
    private Context mContext;
    private int imageTotal;
    private ArrayList<String> mThumbIds;


    public ImageAdapter(Context c, ArrayList<String> getmThumbIds) {
        mContext = c;
        mThumbIds = getmThumbIds;
        imageTotal = mThumbIds.size();
    }

    public int getCount() {
        imageTotal = mThumbIds.size();
        return imageTotal;
    }

    @Override
    public String getItem(int position) {
        return mThumbIds.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            imageView = new ImageView(mContext);
            int screenWidth = ((Activity) mContext).getWindowManager()
                    .getDefaultDisplay().getWidth();
            LinearLayout.LayoutParams params = new LinearLayout
                    .LayoutParams(screenWidth / 2, 400 / 2);
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setPadding(0, 0, 0, 5);
        } else {
            imageView = (ImageView) convertView;
        }
        String url = getItem(position);
        Glide
                .with(imageView.getContext())
                .load(url)
                .thumbnail(Glide.with(mContext).load(R.drawable.loader))
                .into(imageView);
        return imageView;
    }

}

