package com.dasom.activitytracker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class IndoorService extends Service {

    private static final String TAG = "IndoorService";
    WifiManager wifiManager;
    List<ScanResult> scanList;

    int count = 2, rssi = 6;
    Timer timer = new Timer();
    TimerTask timerTask = null;
    Location_in[] location;
    Vibrator vib;

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
                checkProximity();
        }
    };

    // 등록 장소 근접 여부를 판단하고 그에 따라 알림을 주기 위한 메소드
    private void checkProximity() {
        scanList = wifiManager.getScanResults();
        for (int i = 0; i < count; i++)
            location[i].setProximate(false);


        // 등록된 top1 AP가 스캔 결과에 있으며, 그 RSSI가 top1 rssi 값보다 10이하로 작을 때
        // 등록된 장소 근처에 있는 것으로 판단
        for (int j = 0; j < count; j++) {
            for (int i = 1; i < scanList.size(); i++) {
                ScanResult result = scanList.get(i); //rssi 값이 가장 센 세개의 ap를 비교하여 rssi값의 차이가 +- 5 이하면 근처에 있는것으로 판단
                if ( (location[j].getTop1APId().equals(result.BSSID) && Math.abs(result.level - location[j].getTop1rssi())  <=  rssi )||
                        (location[j].getTop2APId().equals(result.BSSID) && Math.abs(result.level - location[j].getTop2rssi())  <=  rssi )
                        ||(location[j].getTop3APId().equals(result.BSSID) && Math.abs(result.level - location[j].getTop3rssi())  <=  rssi))
                    location[j].setProximate(true);
            }
        }

        boolean isProximate = false;
        for (int i = 0; i < count; i++) {
            if (location[i].isProximate()) { // 등록한 위치에 있는경우
                isProximate = true;
                // 진동 패턴
                long[] pattern = {0, 200};
                // pattern 변수로 지정된 방식으로 진동한다, -1: 반복 없음. 한번의 진동 패턴 수행 후 완료
                     vib.vibrate(pattern, -1);
                sendBroadcast(new Intent("com.dasom.activitytracker.WRITE_FILE")); // 파일생성을 알리기 위한 브로드캐스트 생성

                Intent intent2 = new Intent("com.dasom.activitytracker.location");
                intent2.putExtra("location", location[i].getlocationName());
                sendBroadcast(intent2);
            }
        }
        if(!isProximate) { // 등록한 위치가 아닌경우
            Intent intent2 = new Intent("com.dasom.activitytracker.location");
            intent2.putExtra("location", "실내");
            sendBroadcast(intent2);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        vib = (Vibrator)getSystemService(VIBRATOR_SERVICE);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);
        location = new Location_in[2];

        IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
        Log.d("123","123");

        //각 위치의 ap정보를 객체로 생성
        //location[0] = new Location_in("401호", "18:80:90:c6:7b:22", -51, "18:80:90:c6:7b:21", -50, "18:80:90:c6:7b:2d", -50);
        location[0] = new Location_in("401호", "64:e5:99:8e:4c:58", -49, "00:40:5a:e4:b2:fb", -52, "64:e5:99:8e:4c:58", -52); //실은 방
        location[1] = new Location_in("다산홀", " 20:3a:07:9e:a6:c0", -66, "20:3a:07:9e:a6:c5", -62, "a4:18:75:58:77:d1", -58);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent: startService() 호출 시 넘기는 intent 객체
        // flags: service start 요청에 대한 부가 정보. 0, START_FLAG_REDELIVERY, START_FLAG_RETRY
        // startId: start 요청을 나타내는 unique integer id
        // 주기적으로 wifi scan 수행하기 위한 timer 가동
        startTimerTask();

        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        stopTimerTask();
        unregisterReceiver(mReceiver);
    }

    private void startTimerTask() {
        // TimerTask 생성한다
        timerTask = new TimerTask() {
            @Override
            public void run() {
                wifiManager.startScan();
            }
        };

        // TimerTask를 Timer를 통해 실행시킨다
        timer.schedule(timerTask, 1000, 15000); // 1초 후에 타이머를 구동하고 15초마다 반복한다
        //*** Timer 클래스 메소드 이용법 참고 ***//
        // 	schedule(TimerTask task, long delay, long period)
        // http://developer.android.com/intl/ko/reference/java/util/Timer.html
        //***********************************//
    }

    private void stopTimerTask() {
        // 1. 모든 태스크를 중단한다
        if(timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private String getTime(){ //현재 날짜와 시간을 반환해줌
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        String time = sdf.format(date);

        return time;
    }
}
