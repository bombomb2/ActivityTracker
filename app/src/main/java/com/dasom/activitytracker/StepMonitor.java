package com.dasom.activitytracker;



import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;


public class StepMonitor implements SensorEventListener {
    private static final String LOGTAG = "HS_Location_Tracking";

    private Context context;
    private SensorManager mSensorManager;
    private Sensor mLinear;
    private float x, y, z;
    public static long[] time = new long[2];
    int count = 0;
    ArrayList<Double> arr_rms = new ArrayList<>();
    // 움직임 여부를 나타내는 bool 변수: true이면 움직임, false이면 안 움직임
    private boolean isMoving;

    // onStart() 호출 이후 onStop() 호출될 때까지 센서 데이터 업데이트 횟수를 저장하는 변수
    private int sensingCount;

    // 센서 데이터 업데이트 중 움직임으로 판단된 횟수를 저장하는 변수
    private int movementCount;

    // 움직임 여부를 판단하기 위한 3축 가속도 데이터의 RMS 값의 기준 문턱값
    private static double step_THRESHOLD = 1.0;
   public void setRms(double rms)
   {
       step_THRESHOLD = rms;
   }
   public double getRms(){return step_THRESHOLD;}
   public void setCount(int i){count = i;}
   public int getCount(){return count;}
    public StepMonitor(Context context) {
        this.context = context;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public void onStart() {
        // SensorEventListener 등록
        if (mLinear != null) {
            Log.d(LOGTAG, "Register Accel Listener!");
            mSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_GAME);
        }
        // 변수 초기화
        isMoving = false;
        sensingCount = 0;
        movementCount = 0;
    }

    public void onStop() {
        // SensorEventListener 등록 해제
        if (mSensorManager != null) {
            Log.d(LOGTAG, "Unregister Accel Listener!");
            mSensorManager.unregisterListener(this);
        }
        Log.d("check_count","count: " + count);
        double sum = 0;
        for(int i=0; i<arr_rms.size(); i++)
            sum+=arr_rms.get(i);
        double avr = sum / arr_rms.size();
        if(avr >step_THRESHOLD) {
            Log.d("test_sample","start");
            //time[0] = System.currentTimeMillis();//움직이기 시작한 시간
            isMoving = true;
            Log.d("test_sample", "time:" + (time[0]-time[1]));
        }
        else {
            Log.d("test_sample","stop");
            time[1] = System.currentTimeMillis();//움직이지 않았다고 판단되는 시간
            isMoving = false;
            Log.d("test_sample", "time:" + (time[1]-time[0]));
        }
        arr_rms.clear();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // 센서 데이터가 업데이트 되면 호출
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // 센서 업데이트 횟수 증가
            sensingCount++;

            //***** sensor data collection *****//
            // event.values 배열의 사본을 만들어서 values 배열에 저장
            float[] values = event.values.clone();

            // movement detection
            detectMovement(values);
        }
    }

    private void detectMovement(float[] values) {
        Log.d("test_sample3",step_THRESHOLD+"");

        x = values[0];
        y = values[1];
        z =values[2];
        double rms = Math.sqrt(x * x + y * y + z * z);
        arr_rms.add(rms);

        if(rms > step_THRESHOLD)
            count++;


        Log.d("check_count","count2: " + count);
    }

    // 일정 시간 동안 움직임 판단 횟수가 센서 업데이트 횟수의 50%를 넘으면 움직임으로 판단
    public boolean isMoving() {
        return isMoving;
    }

}