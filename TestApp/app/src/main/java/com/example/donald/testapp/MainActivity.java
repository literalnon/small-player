package com.example.donald.testapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.net.Uri;
import android.net.rtp.AudioStream;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    Context context;
    static Vector<File> mp3files;
    MediaPlayer file1, file2;
    //Vector<MediaPlayer> mediaPlayers;
    int curPlayer;
    Handler handler;
    Runnable runnable;
    final int REQUEST_CODE_FILE_1 = 1;
    final int REQUEST_CODE_FILE_2 = 2;
    AudioManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        curPlayer = -1;
        handler = new Handler();

        file1 = new MediaPlayer();
        file2 = new MediaPlayer();

        final Button btn_file_1 = (Button)findViewById(R.id.btn_file_1);
        Button btn_file_2 = (Button)findViewById(R.id.btn_file_2);
        final Button btn_play = (Button)findViewById(R.id.btn_play);
        btn_play.setClickable(false);

        manager = (AudioManager)getSystemService(AUDIO_SERVICE);

        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(manager.getStreamVolume(AudioManager.STREAM_MUSIC));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int r_code = (v.getId() == btn_file_1.getId()? REQUEST_CODE_FILE_1 : REQUEST_CODE_FILE_2);
                startActivityForResult(new Intent(context, ListActivity.class), r_code);
                btn_play.setClickable(true);
            }
        };

        btn_file_1.setOnClickListener(listener);
        btn_file_2.setOnClickListener(listener);

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(0);
                btn_play.setClickable(false);
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    manager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        mp3files = findMp3File();
    }

    MediaPlayer getPlayer(int pl_c){
        return pl_c == 0? file1:file2;
    }

    public  void play(final int next){
        if (curPlayer == -1)
            startPlay(next);
        else {
            stopPlay();
            startPlay(next);
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.removeCallbacks(runnable);
                play((curPlayer + 1) % 2);
            }
        };
        handler = new Handler();
        handler.postDelayed(runnable, getPlayer(curPlayer).getDuration() - 3000);
    }

    void startPlay(int next){
        try {
            MediaPlayer new_player = getPlayer(next);
            new_player.prepare();
            curPlayer = next;
            new_player.setVolume(0f, 0f);
            new_player.start();

            for(int vlm = 0; vlm < 15; ++vlm) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (Exception e) {
                }
                new_player.setVolume((float) vlm / 15, (float) vlm / 15);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    void stopPlay(){
        try {
            MediaPlayer old_player = getPlayer(curPlayer);
            for(int vlm = 15; vlm > 0; --vlm){
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (Exception e) {
                }
                old_player.setVolume((float)vlm / 15, (float)vlm / 15);
            }
            old_player.stop();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK)
            try {
                if (requestCode == REQUEST_CODE_FILE_1)
                    file1.setDataSource(mp3files.get(data.getIntExtra("file", 0)).getAbsolutePath());
                else if (requestCode == REQUEST_CODE_FILE_2)
                    file2.setDataSource(mp3files.get(data.getIntExtra("file", 1)).getAbsolutePath());
            }catch (Exception e){
                e.printStackTrace();
            }
    }


    public Vector<File> findFileInDirectory(File dir) {
        Vector<File> mp3Fls = new Vector<>();

        for (File f : dir.listFiles())
            if (f.isFile()) {
                if (f.getName().contains(".mp3"))
                    mp3Fls.add(f);
            } else if (f.isDirectory())
                mp3Fls.addAll(findFileInDirectory(f));
        return mp3Fls;
    }

    public Vector<File> findMp3File() {
        return findFileInDirectory(new File(Environment.getExternalStorageDirectory().getAbsolutePath()));
    }
};
