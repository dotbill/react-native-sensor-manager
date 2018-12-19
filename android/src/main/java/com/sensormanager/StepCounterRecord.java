package com.sensormanager;

import android.os.Bundle;
import android.os.SystemClock;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.support.annotation.Nullable;

import java.io.*;
import java.util.Date;
import java.util.Timer;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.lang.Integer;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.ReactApplicationContext;

public class StepCounterRecord implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mStepCounter;
    private long lastUpdate = 0;
    private int i = 0;
	private int delay;

	private ReactContext mReactContext;
	private Arguments mArguments;


    public StepCounterRecord(ReactApplicationContext reactContext) {
        mSensorManager = (SensorManager)reactContext.getSystemService(reactContext.SENSOR_SERVICE);
		mReactContext = reactContext;
    }

	public int start(int delay) {
		this.delay = delay;
        if ((mStepCounter = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)) != null) {
			mSensorManager.registerListener(this, mStepCounter, SensorManager.SENSOR_DELAY_FASTEST);
			return (1);
		}
		return (0);
	}

    public void stop() {
        mSensorManager.unregisterListener(this);
    }

	private void sendEvent(String eventName, @Nullable WritableMap params)
	{
		try {
			mReactContext 
				.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class) 
				.emit(eventName, params);
		} catch (RuntimeException e) {
			Log.e("ERROR", "java.lang.RuntimeException: Trying to invoke JS before CatalystInstance has been set!");
		}
	}

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
		WritableMap map = mArguments.createMap();
        long elapseRealtime = SystemClock.elapsedRealtime();;
        long today = System.currentTimeMillis();
        long timeReboot = today - elapseRealtime;
        int steps = (int) sensorEvent.values[0];

        if (mySensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            long curTime = System.currentTimeMillis();
            i++;
            if ((curTime - lastUpdate) > delay) {
                i = 0;
                map.putDouble("reboot", timeReboot);
                map.putDouble("today", today);
                map.putDouble("steps", steps);
                map.putDouble("timestamp", sensorEvent.timestamp);
				sendEvent("StepCounter", map);
                lastUpdate = curTime;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
