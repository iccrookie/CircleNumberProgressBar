package com.circlenumberprogressbar.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.iccrookie.circlenumberprogressbar.CircleNumberProgressBar;
import com.iccrookie.circlenumberprogressbar.OnProgressBarListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    CircleNumberProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (CircleNumberProgressBar) findViewById(R.id.progress_bar);
        final Timer timer = new Timer();
        progressBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.incrementProgressBy(1);
                            }
                        });
                    }
                },1000,50);
            }
        });

        progressBar.setOnProgressBarListener(new OnProgressBarListener() {
            @Override
            public void onProgressChange(float current, float max) {
                Log.e(TAG,String.format("current:%s, max:%s",current,max));
            }
        });

    }
}
