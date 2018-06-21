package com.dasom.activitytracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

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

            if (intent.getAction().equals("kr.ac.koreatech.msp.hslocationtracking")) { // 사용자의 움직임 정보 수신
                now_moving = intent.getBooleanExtra("moving", false);
                long time = intent.getLongExtra("time", 0);

                if(!first) {
                    if (now_moving ^ prev_moving) { //사용자가 정지했다가 이동 혹은 이동하였다가 정지 상태
                        now_time = time;
                        long gap = (now_time - prev_time) / 1000; // 두 시간 차이를 구함
                        Intent intent2 = new Intent("com.dasom.activitytracker.time"); //메인 액티비티에 출력하기 위한 브로드캐스트 발생
                        intent2.putExtra("gap", gap);
                        if(now_moving) {
                            intent2.putExtra("stay", true);
                            intent2.putExtra("startTime", prev_time);
                            intent2.putExtra("endTime", now_time);
                        }
                        else {
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
        prev_time = getTime(); //처음에는 출발시간을 현재 시간으로 설정
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
