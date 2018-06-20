package com.dasom.activitytracker;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.Collections;

public class StepCount extends Service implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mLinear;
    private long lastTime;
    private int count;
    int count2;
    private float x, y, z;
    StepMonitor sm;
    ArrayList<Double> arr_rms = new ArrayList<>();

    private static double step_standard = 1.6;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        sm  = new StepMonitor(getApplicationContext());
        lastTime = System.currentTimeMillis();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // SensorEventListener 등록
        mSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        step_standard = sm.getRms();
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        // SensorEventListener 해제
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // 센서 데이터가 업데이트 되면 호출
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            count = 0;
            long currentTime = System.currentTimeMillis();//현재시간 저장
            long gabOfTime = (currentTime - lastTime) ;//측정 전의 시간과 현재시간의 차
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            double rms = Math.sqrt(x * x + y * y + z * z);
            arr_rms.add(rms);
            if(gabOfTime >= 1000)
            {
                lastTime = currentTime;
                if(arr_rms.size()>3) {
                    Descending descending = new Descending();
                    Collections.sort(arr_rms, descending);
                    arr_rms.remove(0);
                    arr_rms.remove(arr_rms.size() - 1);
                }
                double sum = 0;
                for(int i=0; i<arr_rms.size(); i++)
                    sum+=arr_rms.get(i);
                double avr = sum / arr_rms.size();
                arr_rms.clear();
                if(avr > step_standard) {
                    Intent intent = new Intent("kr.ac.koreatech.msp.stepmonitor"); //값을 브로드캐스트함
                    count++;
                    intent.putExtra("steps", count);
                    sendBroadcast(intent);
                }
            }

        }
    }


}