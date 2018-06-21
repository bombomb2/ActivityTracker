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
    private Sensor mLinear; //Linear_Aecceleration을 위한 변수
    private long lastTime;//움직임이 없다고 판단되는 시간 저장하는 변수
    private int count;   //움직임 값을 저장하는 변수
    private float x, y, z; //가속도 센서의 x,y,z축 값을 저장하는 변수
    StepMonitor sm; //stepMonitor의 함수를 사용하기 위한 변수
    ArrayList<Double> arr_rms = new ArrayList<>(); //rms값들을 저장하는 변수

    private static double step_standard = 1.6; //움직임 판단 기준치


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {

        sm  = new StepMonitor(getApplicationContext());
        lastTime = System.currentTimeMillis(); //시작시 값 초기화
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // SensorEventListener 등록
        mSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        step_standard = sm.getRms(); //StepMonitor의 기준치와 동일화 하기 위한 초기화
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
            count = 0;//그 순간의 걸음 수 측정을 위한 초기화
            long currentTime = System.currentTimeMillis();//현재시간 저장
            long gabOfTime = (currentTime - lastTime) ;//측정 전의 시간과 현재시간의 차
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
            double rms = Math.sqrt(x * x + y * y + z * z);
            arr_rms.add(rms);
            if(gabOfTime >= 1000) //시간차가 1초 이상인 경우 실행
            {
                lastTime = currentTime;
                if(arr_rms.size()>3) { //최대, 최소값 제거를 위한 최소 배열 사이즈 제한
                    //배열 정렬 후 값들의  평준화를 위하여 최대값과 최소값을 제거
                    Descending descending = new Descending();
                    Collections.sort(arr_rms, descending);
                    arr_rms.remove(0);
                    arr_rms.remove(arr_rms.size() - 1);
                }
                double sum = 0;
                for(int i=0; i<arr_rms.size(); i++)
                    sum+=arr_rms.get(i);
                double avr = sum / arr_rms.size();//rms들의 평균값을 구함
                arr_rms.clear();
                if(avr > step_standard) {//평균값이 기준치를 넘기면 count하고 브로드캐스트를 날림
                    Intent intent = new Intent("kr.ac.koreatech.msp.stepmonitor"); //값을 브로드캐스트함
                    count++;
                    intent.putExtra("steps", count);
                    sendBroadcast(intent);
                }
            }

        }
    }


}