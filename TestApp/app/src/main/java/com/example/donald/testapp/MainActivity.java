package com.example.donald.testapp;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.media.SyncParams;
import android.net.Uri;
import android.net.rtp.AudioStream;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.CharacterPickerDialog;
import android.util.TimeUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Permission;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

public class MainActivity extends AppCompatActivity {

    Context context;
    static Vector<File> mp3files;

    final int REQUEST_CODE_FILE_1 = 1;
    final int REQUEST_CODE_FILE_2 = 2;
    int id_1 = 0, id_2 = 1, cur_id;
    SimpleExoPlayer player;

    Handler handlerWaitEndTrack;
    Runnable runnableWhatDoingWhenEnd;

    AudioManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= 23)
            this.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

        final Button btn_file_1 = (Button)findViewById(R.id.btn_file_1);
        final Button btn_file_2 = (Button)findViewById(R.id.btn_file_2);

        final Button btn_play = (Button)findViewById(R.id.btn_play);
        btn_play.setClickable(false);

        manager = (AudioManager)getSystemService(AUDIO_SERVICE);
        context = this;

        SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setMax(manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekBar.setProgress(manager.getStreamVolume(AudioManager.STREAM_MUSIC));

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int r_code = (v.getId() == btn_file_1.getId()? REQUEST_CODE_FILE_1 : REQUEST_CODE_FILE_2);
                startActivityForResult(new Intent(context, ListActivity.class), r_code);
                v.setClickable(false);
                btn_play.setClickable(true);
            }
        };

        btn_file_1.setOnClickListener(listener);
        btn_file_2.setOnClickListener(listener);

        btn_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_play.setClickable(false);
                play();
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

        runnableWhatDoingWhenEnd = new Runnable() {
            @Override
            public void run() {
                playNextTrack(cur_id, player);
                handlerWaitEndTrack.postDelayed(runnableWhatDoingWhenEnd, getDuration() - 15000);
            }
        };
    }

    public  void play(){
        SimpleExoPlayer firstPlayer = createExoPlayer();
        firstPlayer.prepare(GetMediaSourseFromTrackId(id_1));
        firstPlayer.setPlayWhenReady(true);

        cur_id = id_1;
        player = firstPlayer;

        Thread crossFadeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                handlerWaitEndTrack = new Handler();
                handlerWaitEndTrack.postDelayed(runnableWhatDoingWhenEnd, getDuration() - 15000);
            }
        });

        crossFadeThread.run();
    }

    public SimpleExoPlayer createExoPlayer(){
        return ExoPlayerFactory.newSimpleInstance(context,
                new DefaultTrackSelector(
                        new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter())
                ), new DefaultLoadControl());
    }

    public MediaSource GetMediaSourseFromTrackId(int track_id){
        com.google.android.exoplayer2.upstream.DataSource.Factory dataSourseFactory =
                new DefaultDataSourceFactory(this, Util.getUserAgent(this, "small-player"), new DefaultBandwidthMeter());

        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        return new ExtractorMediaSource(
                Uri.fromFile(mp3files.elementAt(track_id))
                , dataSourseFactory, extractorsFactory, null, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK)
            try {
                if (requestCode == REQUEST_CODE_FILE_1)
                    id_1 = data.getIntExtra("file_id", 0);
                else if (requestCode == REQUEST_CODE_FILE_2)
                    id_2 = data.getIntExtra("file_id", 1);
            }catch (Exception e){
                e.printStackTrace();
            }
    }

    public long playNextTrack(int id, SimpleExoPlayer firstTrack){
        int f_id = id;
        int s_id = (id == id_1? id_2 : id_1);

        SimpleExoPlayer secondTrack = createExoPlayer();
        secondTrack.prepare(GetMediaSourseFromTrackId(s_id));
        secondTrack.setPlayWhenReady(true);
        secondTrack.setVolume(0);

        for(int i = 0; i <= 15; ++i){
            try{
                TimeUnit.SECONDS.sleep(1);
            }catch (Exception e){}

            secondTrack.setVolume((float)i / 15);
            firstTrack.setVolume((float)(15 - i) / 15);
        }

        cur_id = s_id;
        firstTrack.release();
        player = secondTrack;
        return secondTrack.getDuration();
    }

    long getDuration(){
        MediaPlayer fake_player = new MediaPlayer();
        try {
            fake_player.setDataSource(mp3files.get(cur_id).getAbsolutePath());
            fake_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            fake_player.prepare();
        }catch (Exception e){}
        return fake_player.getDuration();
    }
};
