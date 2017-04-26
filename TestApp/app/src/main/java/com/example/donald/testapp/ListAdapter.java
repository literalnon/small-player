package com.example.donald.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.TwoStatePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.LogRecord;

/**
 * Created by Donald on 22.04.2017.
 */
public class ListAdapter extends BaseAdapter {

    LayoutInflater inflater;
    Activity context;

    public ListAdapter(Activity context){
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return MainActivity.mp3files.size();
    }

    @Override
    public Object getItem(int position) {
        return MainActivity.mp3files.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView textView = ((TextView) view.findViewById(android.R.id.text1));
        textView.setText(MainActivity.mp3files.get(position).getName().toString());

        view.setTag(position);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("file_id", (int) v.getTag());
                context.setResult(context.RESULT_OK, intent);
                context.finish();
            }
        });

        return view;
    }
}
