package com.dasom.activitytracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeMonitor extends Service {
    private  boolean prev_moving =false;
    private boolean now_moving = false;
    private boolean first = false;
    private long prev_time =0;
    private long now_time = 0;
    private BroadcastReceiver recieve_move = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals("kr.ac.koreatech.msp.hslocationtracking")) {
                now_moving = intent.getBooleanExtra("moving", false);
                long time = intent.getLongExtra("time", 0);

                if(!first) {
                    if (now_moving ^ prev_moving) {
                        now_time = time;
                        long gap = (now_time - prev_time) / 1000;
                        Intent intent2 = new Intent("com.dasom.activitytracker.time");
                        intent2.putExtra("gap", gap);
                        if(now_moving) {
                            Log.d("지금", gap+"분 체류");
                            intent2.putExtra("stay", true);
                            intent2.putExtra("startTime", prev_time);
                            intent2.putExtra("endTime", now_time);
                        }
                        else {
                            Log.d("지금", gap+"분 이동");
                            intent2.putExtra("stay", false);
                            intent2.putExtra("startTime", prev_time);
                            intent2.putExtra("endTime", now_time);
                        }
                        prev_moving = now_moving;
                        prev_time = now_time;

                        sendBroadcast(intent2);
                    }
                }
            }
        }
    };


    public TimeMonitor() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("kr.ac.koreatech.msp.hslocationtracking");
        registerReceiver(recieve_move, intentFilter);
        prev_time = getTime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
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
}
