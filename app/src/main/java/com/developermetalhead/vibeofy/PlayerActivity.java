package com.developermetalhead.vibeofy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gauravk.audiovisualizer.visualizer.BarVisualizer;
import com.gauravk.audiovisualizer.visualizer.BlastVisualizer;

import java.io.File;
import java.util.ArrayList;

public class PlayerActivity extends AppCompatActivity {




    Button btnPlay,btnNext,btnPrevious,btnFastForward,btnFastBackward;
    TextView txtSongName,txtSongStart,txtSongEnd;
    SeekBar seekMusicBar;

    BlastVisualizer blastVisualizer;

    ImageView imageView;
    String songName;
    public static final String EXTRA_NAME = "song_name";
    static MediaPlayer mediaPlayer;
    int position;
    ArrayList<File> mySongs;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(blastVisualizer!=null)
        {
            blastVisualizer.release();
        }
        super.onDestroy();
    }

    Thread updateSeekbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        getSupportActionBar().setTitle("Vibeofy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        btnPrevious=findViewById(R.id.btnPrevious);
        btnNext=findViewById(R.id.btnNext);
        btnFastBackward=findViewById(R.id.btnFastBackward);
        btnFastForward=findViewById(R.id.btnFastForward);
        btnPlay=findViewById(R.id.btnPlay);

        txtSongName=findViewById(R.id.txtSong);
        txtSongStart=findViewById(R.id.txtSongStart);
        txtSongEnd=findViewById(R.id.txtSongEnd);

        seekMusicBar = findViewById(R.id.seekBar);
        blastVisualizer = findViewById(R.id.blast);

        imageView = findViewById(R.id.imgView);

        if(mediaPlayer != null)
        {
            mediaPlayer.start();;
            mediaPlayer.release();
        }

        Intent intent = getIntent();
        Bundle bundle=intent.getExtras();

        mySongs = (ArrayList)bundle.getParcelableArrayList("songs");
        String sName = intent.getStringExtra("songname");
        position = bundle.getInt("pos",0);
        txtSongName.setSelected(true);
        Uri uri = Uri.parse(mySongs.get(position).toString());
        songName = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
        txtSongName.setText(songName);


        mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
        mediaPlayer.start();
        seekMusicBar.setMax(mediaPlayer.getDuration());

        seekMusicBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekMusicBar.getProgress());

            }
        });

        updateSeekbar = new Thread()
        {
            @Override
            public void run() {
                int totalDuration = mediaPlayer.getDuration();
                int currentPosition= 0;
                while (currentPosition<totalDuration)
                {
                    try {
                        sleep(500);
                        currentPosition = mediaPlayer.getCurrentPosition();
                        seekMusicBar.setProgress(currentPosition);

                    }
                    catch (InterruptedException | IllegalStateException e)
                    {
                        e.printStackTrace();
                    }
                }

            }
        };

        updateSeekbar.start();
        seekMusicBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
        seekMusicBar.getThumb().setColorFilter(getResources().getColor(R.color.white),PorterDuff.Mode.SRC_IN);



        String endTime = createTime(mediaPlayer.getDuration());
        txtSongEnd.setText(endTime);

        final Handler handler = new Handler();
        final int delay = 1000;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String duration = createTime(mediaPlayer.getDuration());
                String currentTime = createTime(mediaPlayer.getCurrentPosition());
                txtSongStart.setText(currentTime);
                txtSongEnd.setText(duration);
                handler.postDelayed(this,delay);
            }
        },delay);



        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle);
                    mediaPlayer.pause();
                }
                else
                {
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle);
                    mediaPlayer.start();


                    TranslateAnimation movAnim =  new TranslateAnimation(-25,25,-25,25);
                    movAnim.setInterpolator(new AccelerateInterpolator());
                    movAnim.setDuration(400);
                    movAnim.setFillEnabled(true);
                    movAnim.setFillAfter(true);
                    movAnim.setRepeatMode(Animation.REVERSE);
                    movAnim.setRepeatCount(1);
                    imageView.startAnimation(movAnim);

                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                btnNext.performClick();
            }
        });

        int audioSessionId = mediaPlayer.getAudioSessionId();
        if(audioSessionId != -1)
        {
            blastVisualizer.setAudioSessionId(audioSessionId);
        }

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position+1)%mySongs.size());
                Uri uri= Uri.parse(mySongs.get(position).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(),uri);

                btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle);
                mediaPlayer.start();
                seekMusicBar.setMax(mediaPlayer.getDuration());
                songName = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
                txtSongName.setText(songName);



                startAnimation(imageView,360f);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        btnNext.performClick();
                    }
                });

            }
        });
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                mediaPlayer.release();
                position = ((position-1)<0)?(mySongs.size()-1):position-1;
                Uri uri= Uri.parse(mySongs.get(position).toString());
                mediaPlayer = mediaPlayer.create(getApplicationContext(),uri);
                mediaPlayer.start();
                seekMusicBar.setMax(mediaPlayer.getDuration());
                btnPlay.setBackgroundResource(R.drawable.ic_baseline_pause_circle);
                songName = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
                txtSongName.setText(songName);


                startAnimation(imageView,-360f);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        btnNext.performClick();
                    }
                });
            }
        });
        btnFastForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()+10000);
                }

            }
        });
        btnFastBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying())
                {
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition()-10000);
                }

            }
        });


    }

    public void startAnimation(View view,Float degree)
    {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageView,"rotation",0f,degree);
        objectAnimator.setDuration(1000);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(objectAnimator);
        animatorSet.start();


    }
    public String createTime(int duration)
    {
        String time = "";
        int min = duration/1000/60;
        int sec = duration/1000%60;
        time = time+min+":";
        if(sec<10)
        {
            time+="0";
        }
        time+=sec;
        return time;


    }
}