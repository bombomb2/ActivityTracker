package com.dasom.activitytracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ProximityReceiver extends BroadcastReceiver {
    TextFileManager textFileManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        textFileManager = new TextFileManager();
        boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        // boolean getBooleanExtra(String name, boolean defaultValue)
        String name = intent.getStringExtra("name"); //MainActivity에서 보낸 객체의 이름정보 수신
        if(isEntering) {
            Toast.makeText(context, name + "에 접근중입니다..", Toast.LENGTH_LONG).show();
            textFileManager.save(getTime() + name+ "에 접근했습니다.\n");
            context.sendBroadcast(new Intent("com.dasom.activitytracker.WRITE_FILE")); // 파일생성을 알리기 위한 브로드캐스트 생성
        }
        else {
            Toast.makeText(context, name + "에서 벗어납니다..", Toast.LENGTH_LONG).show();
            textFileManager.save(getTime() + name+ "에서 벗어났습니다.\n");
            context.sendBroadcast(new Intent("com.dasom.activitytracker.WRITE_FILE")); // 파일생성을 알리기 위한 브로드캐스트 생성
        }
    }

    private String getTime(){ //현재 날짜와 시간을 반환해줌
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
        String time = sdf.format(date);

        return time;
    }
}
