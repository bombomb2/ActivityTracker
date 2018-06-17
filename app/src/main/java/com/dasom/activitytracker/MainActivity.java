package com.dasom.activitytracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private static final String BROADCAST_ACTION_ACTIVITY = "kr.ac.koreatech.msp.hslocationtracking";
    LocationManager locManager;
    ProximityReceiver receiver_proximity;
    boolean isPermitted = false;
    final int MY_PERMISSIONS_REQUEST = 1;
    Location_out[] location = new Location_out[2];
    TextView tv_content,movingText,step;
    WifiManager wifiManager;
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
                total_steps +=steps;
               step.setText("steps: " + total_steps);
            }
            else if(intent.getAction().equals(BROADCAST_ACTION_ACTIVITY)) {
                boolean moving = intent.getBooleanExtra("moving", false);
                if(moving) {
                    movingText.setText("Moving");
                    startService(step_count);
                } else {
                    movingText.setText("NOT Moving");
                    stopService(step_count);
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

        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        proximityStart();


        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        if(wifiManager.isWifiEnabled() == false)
            wifiManager.setWifiEnabled(true);
        Intent intent = new Intent(getApplicationContext(), IndoorService.class);
        startService(intent);
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
    private void proximityStart() {
        location[0] = new Location_out("운동장", 36.762581, 127.284527, 80);
        location[1] = new Location_out("대학본부 앞", 36.764215, 127.282173, 50);
        for(int i=0; i<=1; i++) {
            addProximity(i);
        }
    }

    private void addProximity(int count) {
        if(isPermitted){
            Intent intent = new Intent("com.dasom.activitytracker.proximity"+count); //브로드캐스트를 위한 인텐트 생성
            intent.putExtra("name", location[count].getName()); //현재 객체의 이름정보를 전달하여 Toast메세지에 정보를 표시할 수 있도록 함
            PendingIntent proximity = PendingIntent.getBroadcast(this, 0, intent, 0);

            try{
                locManager.addProximityAlert(location[count].getLatitude(), location[count].getLongitude(), location[count].getRadius(), -1, proximity); //proximityAlert 등록
            }
            catch (SecurityException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        receiver_proximity = new ProximityReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.dasom.activitytracker.proximity0");
        filter.addAction("com.dasom.activitytracker.proximity1");
        registerReceiver(receiver_proximity, filter);

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
        try {
            locManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        unregisterReceiver(receiver_proximity);
        unregisterReceiver(receiver_text);
        stopService(new Intent(getApplicationContext(), IndoorService.class));
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
