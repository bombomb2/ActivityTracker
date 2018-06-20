package com.dasom.activitytracker;


import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HSMonitor extends Service {
    private static final String LOGTAG = "HS_Location_Tracking";
    private static final String BROADCAST_ACTION_ACTIVITY = "kr.ac.koreatech.msp.hslocationtracking";
    AlarmManager am;
    PendingIntent pendingIntent;

    private PowerManager.WakeLock wakeLock;
    private CountDownTimer timer;

    private StepMonitor accelMonitor;
    private long period = 5000;
    private static final long activeTime = 1000;
    private static final long periodForMoving = 5000;
    private static final long periodIncrement = 5000;
    private static final long periodMax = 30000;

    private gpsListener mgpsListener;
    private LocationManager locManager;
    WifiManager wifiManager;

    private long startMoveTime = 0 ;
    private long endMoveTime = 0;
    private boolean isInside = false;

    // Alarm 시간이 되었을 때 안드로이드 시스템이 전송해주는 broadcast를 받을 receiver 정의
    // 움직임 여부에 따라 다음 alarm이 발생하도록 설정한다.
    private BroadcastReceiver AlarmReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("kr.ac.koreatech.msp.hsalarm")) {
                Log.d(LOGTAG, "Alarm fired!!!!");
                //-----------------
                // Alarm receiver에서는 장시간에 걸친 연산을 수행하지 않도록 한다
                // Alarm을 발생할 때 안드로이드 시스템에서 wakelock을 잡기 때문에 CPU를 사용할 수 있지만
                // 그 시간은 제한적이기 때문에 애플리케이션에서 필요하면 wakelock을 잡아서 연산을 수행해야 함
                //-----------------

                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HS_Wakelock");
                // ACQUIRE a wakelock here to collect and process accelerometer data and control location updates
                wakeLock.acquire();

                accelMonitor = new StepMonitor(context);
                accelMonitor.onStart();

                timer = new CountDownTimer(activeTime, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        Log.d(LOGTAG, "1-second accel data collected!!");
                        // stop the accel data update
                        Intent step = new Intent("uncheck_step");
                        step.putExtra("uncheck_step",accelMonitor.getCount());
                        sendBroadcast(step);
                        accelMonitor.onStop();
                        boolean moving = accelMonitor.isMoving();
                        // 움직임 여부에 따라 GPS location update 요청 처리
                        if(moving) {
                            startService(new Intent(getApplicationContext(),StepCount.class));
                            startMoveTime = getTime();
                            Log.d("시간", "이동시작시간: "+ startMoveTime);

                                    // 화면에 정보 표시를 위해 activity의 broadcast receiver가 받을 수 있도록 broadcast 전송
                                    Intent intent = new Intent(BROADCAST_ACTION_ACTIVITY);
                                    intent.putExtra("moving", moving);
                                    intent.putExtra("time", startMoveTime);
                                    // broadcast 전송
                                    sendBroadcast(intent);
                                    endMoveTime = 0;

                         stopProximity();
                        } else {
                            stopService(new Intent(getApplicationContext(),StepCount.class));
                            endMoveTime = getTime();
                            Log.d("시간", "이동정지시간: "+ endMoveTime);

                                    Intent intent = new Intent(BROADCAST_ACTION_ACTIVITY);
                                    intent.putExtra("moving", moving);
                                    intent.putExtra("time" , endMoveTime);
                                    // broadcast 전송
                                    sendBroadcast(intent);

                            startProximity();
                            stopService(new Intent(getApplicationContext() , StepCount.class));
                        }
                        // 움직임 여부에 따라 다음 alarm 설정
                        setNextAlarm(moving);

                        // 화면에 위치 데이터를 표시할 수 있도록 브로드캐스트 전송

                        // When you finish your job, RELEASE the wakelock
                        wakeLock.release();
                        wakeLock = null;
                    }
                };
                timer.start();
            }
        }
    };

    public String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
        Date currentTime = new Date();
        String dTime = formatter.format(currentTime);
        return dTime;
    }

    private void setNextAlarm(boolean moving) {

        // 움직임이면 5초 period로 등록
        // 움직임이 아니면 5초 증가, max 30초로 제한
        if(moving) {
            Log.d(LOGTAG, "MOVING!!");
            period = periodForMoving;
        } else {
            Log.d(LOGTAG, "NOT MOVING!!");
            period = period + periodIncrement;
            if(period >= periodMax) {
                period = periodMax;
            }
        }
        Log.d(LOGTAG, "Next alarm: " + period);

        // 다음 alarm 등록
        Intent in = new Intent("kr.ac.koreatech.msp.hsalarm");
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, in, 0);
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period , pendingIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        Log.d(LOGTAG, "onCreate");

        // Alarm 발생 시 전송되는 broadcast를 수신할 receiver 등록
        IntentFilter intentFilter = new IntentFilter("kr.ac.koreatech.msp.hsalarm");
        registerReceiver(AlarmReceiver, intentFilter);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mgpsListener = new gpsListener();

        // AlarmManager 객체 얻기
        am = (AlarmManager)getSystemService(ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent: startService() 호출 시 넘기는 intent 객체
        // flags: service start 요청에 대한 부가 정보. 0, START_FLAG_REDELIVERY, START_FLAG_RETRY
        // startId: start 요청을 나타내는 unique integer id

        Log.d(LOGTAG, "onStartCommand");

        // Alarm이 발생할 시간이 되었을 때, 안드로이드 시스템에 전송을 요청할 broadcast를 지정
        Intent in = new Intent("kr.ac.koreatech.msp.hsalarm");
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, in, 0);

        // Alarm이 발생할 시간 및 alarm 발생시 이용할 pending intent 설정
        // 설정한 시간 (5000-> 5초, 10000->10초) 후 alarm 발생
        am.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + period, pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {

        try {
            // Alarm 발생 시 전송되는 broadcast 수신 receiver를 해제
            unregisterReceiver(AlarmReceiver);
        } catch(IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        // AlarmManager에 등록한 alarm 취소
        am.cancel(pendingIntent);

        // release all the resources you use
        if(timer != null)
            timer.cancel();
        if(wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    private class gpsListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Location loc_now = new Location("현재위치");
            loc_now.setLatitude(location.getLatitude());
            loc_now.setLongitude(location.getLongitude());

            Log.d("gps", "위도: "+loc_now.getLatitude());
            Log.d("gps","경도: "+loc_now.getLongitude());
            Log.d("gps","정확도: "+      loc_now.getAccuracy());

            if(loc_now.hasAccuracy()) {
                Location loc_field = new Location("운동장");
                loc_field.setLatitude(36.762581);
                loc_field.setLongitude(127.284527);

                Location loc_head = new Location("대학본부 앞");
                loc_head.setLatitude(36.764215); //50
                loc_head.setLongitude(127.282173);

                float distance_field = loc_now.distanceTo(loc_field);
                float distance_head = loc_now.distanceTo(loc_head);

                if(distance_field <= 80) {
                    Intent intent2 = new Intent("com.dasom.activitytracker.location");
                    intent2.putExtra("location", "운동장");
                    sendBroadcast(intent2);
                }

                else if(distance_head <= 50) {
                    Intent intent2 = new Intent("com.dasom.activitytracker.location");
                    intent2.putExtra("location", "대학본부 앞");
                    sendBroadcast(intent2);
                }

                else {
                    Intent intent2 = new Intent("com.dasom.activitytracker.location");
                    intent2.putExtra("location", "실외");
                    sendBroadcast(intent2);
                }
            }

            else {
                startService(new Intent(getApplicationContext(), IndoorService.class));
            }

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


    private long getTime(){ //현재 날짜와 시간을 반환해줌
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String time = sdf.format(new Date(System.currentTimeMillis()));
        try {
            date = sdf.parse(time);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    private void startProximity() {
        try {
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, mgpsListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if(wifiManager.isWifiEnabled() == false)
            wifiManager.setWifiEnabled(true);
    }

    private void stopProximity() {

        try {
            locManager.removeUpdates(mgpsListener);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if(isServiceRunning())
        stopService(new Intent(getApplicationContext(), IndoorService.class));
    }

    public boolean isServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (IndoorService.class.getName().equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

}
