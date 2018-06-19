package com.dasom.activitytracker;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class StepCount extends Service implements SensorEventListener {
    private static final String LOGTAG = "StepMonitor";

    private SensorManager mSensorManager;
    private Sensor mLinear;
    private float lastX;
    private float lastY;
    private float lastZ;
    private long lastTime;
    private float speed;
    private int count;
    private float x, y, z;


    private static final double step_standard = 1.3;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(LOGTAG, "onCreate()");

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // SensorEventListener 등록
        mSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // intent: startService() 호출 시 넘기는 intent 객체
        // flags: service start 요청에 대한 부가 정보. 0, START_FLAG_REDELIVERY, START_FLAG_RETRY
        // startId: start 요청을 나타내는 unique integer id

        Toast.makeText(this, "Activity Monitor 시작", Toast.LENGTH_SHORT).show();
        Log.d(LOGTAG, "onStartCommand()");


        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        Toast.makeText(this, "Activity Monitor 중지", Toast.LENGTH_SHORT).show();
        Log.d(LOGTAG, "onDestroy()");

        // SensorEventListener 해제
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // 센서 데이터가 업데이트 되면 호출
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            long currentTime = System.currentTimeMillis();//현재시간 저장
            long gabOfTime = (currentTime - lastTime);//측정 전의 시간과 현재시간의 차

            if (gabOfTime > 1000) { //  gap of time of step count //측정 시간을 500밀리초로 간격으로                L
                lastTime = currentTime;//지나간 현재 시간 저장

                x = event.values[0] ;
                y = event.values[1] ;
                z = event.values[2] ;

                speed = Math.abs(x+y+z - lastX - lastY - lastZ);//대략의 거리값을 측정 하기위한 변수

                Log.d("test","step:"+speed/gabOfTime*1000);
                if (speed/gabOfTime*1000>step_standard) {//거리값을 측정하여 1.1이상이면 움직였다고 판단
                    Intent intent = new Intent("kr.ac.koreatech.msp.stepmonitor"); //값을 브로드캐스트함
                     count++;
                    intent.putExtra("steps",count);
                    sendBroadcast(intent);
                } // end of if

            } // end of if
            lastX = event.values[0];
            lastY = event.values[1];
            lastZ = event.values[2];
        }
    }


    // a simple inference for step count


}