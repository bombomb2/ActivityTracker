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
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST = 1;
    TextView step;
    TextFileManager textFileManager;
    int total_steps = 0; //총 걸음수를 저장하는 변수
    private int steps;
    StepMonitor moving_check;
    Intent step_count;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private ArrayList<StatItem> items = new ArrayList<>();
    int temp_steps;//움직임이 없는 동안의 걸음수를 저장하는 변수
    int now_steps;//현재 걸음수를 저장하는 변수
    private BroadcastReceiver MyStepReceiver = new BroadcastReceiver() {
        String location = "";
        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().equals("kr.ac.koreatech.msp.stepmonitor")) {
                //걸음 수를 판단 후  넘어오는 걸음 수에 대해 정확도를 위해 나누기 2를
                //하여 걸음 수의 정확도를 향상
                steps = intent.getIntExtra("steps", 0)/2;//
                now_steps += steps ;

            }
            else if(intent.getAction().equals("uncheck_step"))
            {
                //움직임 판단동안 측정된 걸음수를 현재 걸음수에 저장
                temp_steps = intent.getIntExtra("uncheck_step",0)/2;
                now_steps += temp_steps;
            }

            else if(intent.getAction().equals("com.dasom.activitytracker.time")) { // TimeMonitor로부터 시간및 이동 정보 수신
                long gap = intent.getLongExtra("gap", 0);
                boolean stay = intent.getBooleanExtra("stay", true);
                long nowTime = intent.getLongExtra("endTime", 0);
                long prevTime = intent.getLongExtra("startTime", 0);
                if(gap> 0) {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    String endTime = sdf.format(nowTime);
                    String startTime = sdf.format(prevTime);
                    if(stay) { // 현재 정지 상태
                        if(gap>=5) { //5분 이상 정지
                            textFileManager.save(startTime+"-"+endTime+": "+location+ ", "+gap + "분 정지\n"); //로그 저장
                            items.add(new StatItem(startTime, endTime, gap, location, stay)); // 목록에 추가
                        }
                    }
                    else { //현재 이동 상태
                        if(gap>=1) { //1분 이상 이동
                            textFileManager.save(startTime+"-"+endTime+": "+now_steps+ "걸음, "+gap + "분 이동\n"); //로그 저장
                            items.add(new StatItem(startTime, endTime, gap, now_steps + "걸음", stay)); //목록 추가
                            total_steps += now_steps; //총 걸음수 계산
                            step.setText("Steps: " + total_steps);
                            moving_check.setCount(0);
                            now_steps = 0;
                        }
                    }
                    adapter.notifyDataSetChanged(); //목록 추가 알림
                    recyclerView.scrollToPosition(adapter.getItemCount()-1); //목록이 화면 아래로 내려가면 자동으로 스크롤
                }
            }
            else if(intent.getAction().equals("com.dasom.activitytracker.location")) { //위치 정보가 수신되면
                location = intent.getStringExtra("location"); //위치 정보 저장
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
        step =  (TextView)findViewById(R.id.step);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("kr.ac.koreatech.msp.stepmonitor");
        intentFilter.addAction("com.dasom.activitytracker.time");
        intentFilter.addAction("com.dasom.activitytracker.location");
        intentFilter.addAction("uncheck_step");
        registerReceiver(MyStepReceiver, intentFilter);
        textFileManager = new TextFileManager();
        Intent hs = new Intent(this,HSMonitor.class);
        startService(hs);
        startService(new Intent(this, TimeMonitor.class));
        startService(new Intent(getApplicationContext(), IndoorService.class));
    }

    private void requestRuntimePermission() {
        //*******************************************************************
        // Runtime permission check
        //*******************************************************************
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
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
                    Manifest.permission.ACCESS_FINE_LOCATION )) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST);
            }

            else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION},
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

    private void setRecyclerView() { //RecyclerView 초기 구성
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerAdapter(items);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(MyStepReceiver);
    }
}
