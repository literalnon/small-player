package com.example.donald.testapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.Vector;

public class ListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        findMp3File();

        ListView listView = (ListView) findViewById(R.id.listView);

        listView.setAdapter(new com.example.donald.testapp.ListAdapter(this));
    }

    public Vector<File> findFileInDirectory(File dir) {
        Vector<File> files = new Vector<>();
        for (File f : dir.listFiles())
            if (f.isFile()) {
                if (f.getName().contains(".mp3"))
                    files.add(f);
            } else if (f.isDirectory())
                files.addAll(findFileInDirectory(f));
        return files;
    }

    public void findMp3File() {
        if(MainActivity.mp3files == null)
            MainActivity.mp3files = findFileInDirectory(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
    }
}
