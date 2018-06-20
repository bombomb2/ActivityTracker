package com.dasom.activitytracker;




import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class StepMonitor implements SensorEventListener {
    private Context context;
    private SensorManager mSensorManager;
    private Sensor mLinear;
    private float x, y, z;
    public static long[] time = new long[2];
    int count = 0;
    ArrayList<Double> arr_rms = new ArrayList<>();
    // 움직임 여부를 나타내는 bool 변수: true이면 움직임, false이면 안 움직임
    private boolean isMoving;


    private static double step_THRESHOLD = 1.6
            ;
   public void setRms(double rms)
   {
       step_THRESHOLD = rms;
   }
   public double getRms(){return step_THRESHOLD;}
   public void setCount(int i){count = i;}
   public int getCount(){return count/2;}
    public StepMonitor(Context context) {
        this.context = context;

        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        mLinear = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
    }

    public void onStart() {
        // SensorEventListener 등록
        if (mLinear != null) {
            mSensorManager.registerListener(this, mLinear, SensorManager.SENSOR_DELAY_GAME);
        }
        // 변수 초기화
        isMoving = false;
        count = 0;
    }

    public void onStop() {
        // SensorEventListener 등록 해제
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
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
        if(avr >step_THRESHOLD) {
            isMoving = true;
        }
        else {
            isMoving = false;
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
            float[] values = event.values.clone();
            detectMovement(values);
        }
    }

    private void detectMovement(float[] values) {

        x = values[0];
        y = values[1];
        z =values[2];
        double rms = Math.sqrt(x * x + y * y + z * z);
        arr_rms.add(rms);

        if(rms > step_THRESHOLD)
            count++;
    }

    // 일정 시간 동안 움직임 판단 횟수가 센서 업데이트 횟수의 50%를 넘으면 움직임으로 판단
    public boolean isMoving() {
        return isMoving;
    }

}

class Descending implements Comparator<Double> {
    @Override
    public int compare(Double o1, Double o2)
    {
        return o2.compareTo(o1);
    }
}