package com.dasom.activitytracker;



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
    private float lastX;
    private float lastY;
    private float lastZ;
    private float speed;
    private float x, y, z;
    private long lastTime;
    private float last_speed;
    public static long[] time = new long[2];
    ArrayList<Double> speed1 = new ArrayList<>();
    // 움직임 여부를 나타내는 bool 변수: true이면 움직임, false이면 안 움직임
    private boolean isMoving;

    // onStart() 호출 이후 onStop() 호출될 때까지 센서 데이터 업데이트 횟수를 저장하는 변수
    private int sensingCount;

    // 센서 데이터 업데이트 중 움직임으로 판단된 횟수를 저장하는 변수
    private int movementCount;

    // 움직임 여부를 판단하기 위한 3축 가속도 데이터의 RMS 값의 기준 문턱값
    private static double step_THRESHOLD = 1.1;
   public void setRms(double rms)
   {
       step_THRESHOLD = rms;
   }
   public double getRms(){return step_THRESHOLD;}
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
        long currentTime = System.currentTimeMillis();//현재시간 저장
        long gabOfTime = (currentTime - lastTime);//측정 전의 시간과 현재시간의 차
        Log.d("test_sample3",step_THRESHOLD+"");

        x = values[0];
        y = values[1];
        z =values[2];
        double rms = Math.sqrt(x * x + y * y + z * z);
        speed1.add(rms);
        if(gabOfTime>=1000)
        {
            lastTime = currentTime;
            double sum = 0;
            for(int i=0; i<speed1.size(); i++)
                sum+=speed1.get(i);
            double avr = sum / speed1.size();
            speed1.clear();
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
        }
        /*
        if (gabOfTime > 1000) { //  gap of time of step count //측정 시간을 500밀리초로 간격으로
            lastTime = currentTime;//지나간 현재 시간 저장
        }
            x = values[0];
            y = values[1];
            z = values[2];

            speed = Math.abs(x + y + z - lastX - lastY - lastZ);//대략의 거리값을 측정 하기위한 변수


            if ((speed /gabOfTime*1000)> step_THRESHOLD ) {//거리값을 측정하여 1.1이상이면 움직였다고 판단
                movementCount++;
                time[0] = System.currentTimeMillis();
            }// end of if

            Log.d("test",time[0]-time[1]+"");

        // end of if
        //Log.d("test",movementCount+"");
        //Log.d("test",sensingCount+"sensing");
        lastX = values[0];
        lastY = values[1];
        lastZ = values[2];
        last_speed = speed;
        /*double rms = Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
        //Log.d(LOGTAG, "rms: " + rms);

        // 怨꾩궛??rms 媛믪쓣 threshold 媛믨낵 鍮꾧탳?섏뿬 ?吏곸엫?대㈃ count 蹂??利앷?
        if(rms > 1.1) {
            movementCount++;
        }
            */
    }

    // 일정 시간 동안 움직임 판단 횟수가 센서 업데이트 횟수의 50%를 넘으면 움직임으로 판단
    public boolean isMoving() {
        /*if((time[0]-time[1])<=3000) {
            isMoving = true;
        } else if((time[1]-time[0])>=5000) {
            isMoving = false;
        }*/
        return isMoving;
    }

    public long gaptime()
    {
            return Math.abs(time[1]-time[0]);
    }
}