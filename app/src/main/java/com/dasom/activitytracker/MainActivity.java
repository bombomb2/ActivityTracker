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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String BROADCAST_ACTION_ACTIVITY = "kr.ac.koreatech.msp.hslocationtracking";
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST = 1;
    TextView tv_content,movingText,step;
    BroadcastReceiver receiver_text;
    TextFileManager textFileManager;
    int total_steps;
    private int steps;
    EditText edit_rms;
    Button btn_rms;
    StepMonitor moving_check;
    Intent step_count;
    private RecyclerView.Adapter adapter;
    private ArrayList<StatItem> items = new ArrayList<>();
    int temp_steps;
    int now_steps;
    private BroadcastReceiver MyStepReceiver = new BroadcastReceiver() {
        String location = "";
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("kr.ac.koreatech.msp.stepmonitor")) {
                steps = intent.getIntExtra("steps", 0);
                temp_steps = intent.getIntExtra("steps2",0);
                now_steps += steps ;

            }
            else if(intent.getAction().equals("uncheck_step"))
            {
                temp_steps = intent.getIntExtra("uncheck_step",0)/2;
                now_steps += temp_steps;
            }
            else if(intent.getAction().equals(BROADCAST_ACTION_ACTIVITY)) {
                boolean moving = intent.getBooleanExtra("moving", false);
                if(moving) {
                    //temp_total_steps = moving_check.getCount();
                    movingText.setText("Moving");
                } else {
                    movingText.setText("NOT Moving");

                }
            }
            else if(intent.getAction().equals("com.dasom.activitytracker.time")) {
                long gap = intent.getLongExtra("gap", 0);
                boolean stay = intent.getBooleanExtra("stay", true);
                long nowTime = intent.getLongExtra("endTime", 0);
                long prevTime = intent.getLongExtra("startTime", 0);
                if(gap> 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    String endTime = sdf.format(nowTime);
                    String startTime = sdf.format(prevTime);
                    if(stay) {
                        textFileManager.save(gap + "초 정지\n");
                        items.add(new StatItem(startTime, endTime, gap + "초 정지", location, stay));
                    }
                    else {
                        textFileManager.save(gap + "초 이동\n");

                        items.add(new StatItem(startTime, endTime, gap + "초 이동", now_steps+"걸음", stay));
                        total_steps += now_steps;
                        step.setText("steps: " + total_steps);
                        moving_check.setCount(0);
                        now_steps = 0;
                    }
                    adapter.notifyDataSetChanged();
                }
            }
            else if(intent.getAction().equals("com.dasom.activitytracker.location")) {
                location = intent.getStringExtra("location");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRecyclerView();

        step_count = new Intent(this , StepCount.class);
        requestRuntimePermission();
        moving_check =  new StepMonitor(getApplicationContext());
        tv_content = (TextView)findViewById(R.id.tv_content);
        movingText = (TextView)findViewById(R.id.isMoving);
        step =  (TextView)findViewById(R.id.step);
        edit_rms = (EditText)findViewById(R.id.editText_rms);
        btn_rms = (Button)findViewById(R.id.button_rms);
        Button btn_remove = (Button)findViewById(R.id.btn_remove);
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textFileManager.delete();
                tv_content.setText(textFileManager.load());
            }
        });
        btn_rms.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                //Intent intent = new Intent("change_rms");
                //double temp = Double.parseDouble(edit_rms.getText().toString());
                //intent.putExtra("rms",temp);
               // sendBroadcast(intent);
                double temp = Double.parseDouble(edit_rms.getText().toString());
                edit_rms.setText("");
                moving_check.setRms(temp);
            }
        });
        IntentFilter intentFilter = new IntentFilter(BROADCAST_ACTION_ACTIVITY);
        intentFilter.addAction("kr.ac.koreatech.msp.stepmonitor");
        intentFilter.addAction("com.dasom.activitytracker.time");
        intentFilter.addAction("com.dasom.activitytracker.location");
        intentFilter.addAction("uncheck_step");
        registerReceiver(MyStepReceiver, intentFilter);
        textFileManager = new TextFileManager();
        Intent hs = new Intent(this,HSMonitor.class);
        startService(hs);
        startService(new Intent(this, TimeMonitor.class));
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

    private void setRecyclerView() {
        RecyclerView recyclerView;
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerAdapter(items);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
