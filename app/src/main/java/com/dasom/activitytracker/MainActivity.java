package com.dasom.activitytracker;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String BROADCAST_ACTION_ACTIVITY = "kr.ac.koreatech.msp.hslocationtracking";
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST = 1;
    TextView tv_content,movingText,step;
    BroadcastReceiver receiver_text;
    TextFileManager textFileManager;
    int total_steps;
    private int steps;
    StepMonitor moving_check;
    Intent step_count;
    private BroadcastReceiver MyStepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("kr.ac.koreatech.msp.stepmonitor")) {
                steps = intent.getIntExtra("steps", 0);
                int temp_total_steps = 0;
                temp_total_steps += steps/2;
                Log.d("test",moving_check.gaptime()+"");

                    total_steps += temp_total_steps;
                    total_steps += steps;

               step.setText("steps: " + total_steps);
                //Log.d("test_sample", total_steps+"");
            }
            else if(intent.getAction().equals(BROADCAST_ACTION_ACTIVITY)) {
                boolean moving = intent.getBooleanExtra("moving", false);
                if(moving) {
                    movingText.setText("Moving");
                } else {
                    movingText.setText("NOT Moving");

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        step_count = new Intent(this , StepCount.class);
        requestRuntimePermission();
        moving_check =  new StepMonitor(getApplicationContext());
        tv_content = (TextView)findViewById(R.id.tv_content);
        movingText = (TextView)findViewById(R.id.isMoving);
        step =  (TextView)findViewById(R.id.step);
        Button btn_remove = (Button)findViewById(R.id.btn_remove);
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textFileManager.delete();
                tv_content.setText(textFileManager.load());
            }
        });
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION_ACTIVITY);
        intentFilter.addAction("kr.ac.koreatech.msp.stepmonitor");
        registerReceiver(MyStepReceiver, intentFilter);
        textFileManager = new TextFileManager();
        Intent hs = new Intent(this,HSMonitor.class);
        startService(hs);
    }

    private void requestRuntimePermission() {
        //*******************************************************************
        // Runtime permission check
        //*******************************************************************
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE )) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
            }

            else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION )) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
            }

            else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION )) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
            }

            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
            }
        } else {
            //권한이 있는 것
            isPermitted = true;
        }
        //*********************************************************************
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // read_external_storage-related task you need to do.
                    // ACCESS_FINE_LOCATION 권한을 얻음
                    isPermitted = true;
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // 권한을 얻지 못 하였으므로 location 요청 작업을 수행할 수 없다
                    // 적절히 대처한다
                    isPermitted = false;
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        tv_content.setText(textFileManager.load());

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.dasom.activitytracker.WRITE_FILE");

        receiver_text = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                tv_content.setText(textFileManager.load()); //브로드캐스트가 발생하면 텍스트 파일을 불러와서 TextView에 출력
            }
        };

        registerReceiver(receiver_text, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
         unregisterReceiver(receiver_text);
    }
}
