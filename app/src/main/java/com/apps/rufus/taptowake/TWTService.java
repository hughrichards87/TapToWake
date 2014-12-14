package com.apps.rufus.taptowake;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.app.Service;
import android.os.PowerManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.ImageView;


public class TWTService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor proximity;
    private Sensor gravity;

    private PowerManager powerManager;
   // private DevicePolicyManager policyManager;

    private static final float shakeThreshold = 0.001f;
    private static final float screenOffShakeThreshold = 0.07f;
    private static final long seconds = 1000;
    private static final long coolDownPeriod = 500;

    private final static float gravitySquared = SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH;

    private class Dimension {
        public static final int X = 0;
        public static final int Y = 1;
        public static final int Z = 2;
    }

    private long lastShake;
    private long lastGravity;
    private float lastShakeValue;
    private float lastgravityValue;
    private float[] gravityForAcc = new float[3];
    private float[] acceleration = new float[3];
    private boolean isProximityCovered;



    private WindowManager windowManager;
    private ImageView invisbleView;
    private WindowManager.LayoutParams params;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
      //  policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                proximity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                gravity,
                SensorManager.SENSOR_DELAY_NORMAL);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        invisbleView = new ImageView(this);
        invisbleView.setImageResource(R.drawable.ic_launcher);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;

        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.y = 0;//size.y;
        params.x = size.x;
        params.height = 0;
        params.width = 0;
        invisbleView.setKeepScreenOn(true);
        windowManager.addView(invisbleView, params);

        lastShake = System.currentTimeMillis();
        lastShakeValue =0f;


        Log.i("Hugh", "SERVICE IS RUNNING");

        //sticky so it will get restarted if it stops
        return Service.START_STICKY;
    }


    @Override
    public void onDestroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType())
        {
            case Sensor.TYPE_GRAVITY:
                getGravity(event);
                break;
            case Sensor.TYPE_ACCELEROMETER:
                getAccelerometer(event);
                break;
            case Sensor.TYPE_PROXIMITY:
                getProximity(event);
                break;
        }
    }

    private void getGravity(SensorEvent event) {
        float[] values = event.values;
        // gravity strength
        float x = values[Dimension.X];
        float y = values[Dimension.Y];
        float z = values[Dimension.Z];

        long actualTime = System.currentTimeMillis();
        if (Math.abs(lastGravity - actualTime) > coolDownPeriod) {
            lastGravity = actualTime;
        }

    }

    private void getProximity(SensorEvent event) {

        Boolean newValue = (event.values[Dimension.X] < event.sensor.getMaximumRange());

        if (isProximityCovered != newValue) {
            isProximityCovered = newValue;
           // Log.i("Hugh", "screen is covered " + Boolean.toString(isProximityCovered) + " " + Float.toString(event.sensor.getMaximumRange()));
           /* if (isProximityCovered) {
                //Log.i("Hugh", "screen is covered " + Boolean.toString(isProximityCovered));
                turnScreenOff();
            }
            else
            {
                turnScreenOn();
            }
            */
        }
    }

    private float square(float x){
        return x*x;
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;

        final float alpha = 0.8f;

        // Isolate the force of gravity with the low-pass filter.
        gravityForAcc[0] = alpha * gravityForAcc[0] + (1 - alpha) * event.values[0];
        gravityForAcc[1] = alpha * gravityForAcc[1] + (1 - alpha) * event.values[1];
        gravityForAcc[2] = alpha * gravityForAcc[2] + (1 - alpha) * event.values[2];

        // Remove the gravity contribution with the high-pass filter.
        acceleration[0] = event.values[0] - gravityForAcc[0];
        acceleration[1] = event.values[1] - gravityForAcc[1];
        acceleration[2] = event.values[2] - gravityForAcc[2];

        // Movement
      //  float x = values[Dimension.X];
      //  float y = values[Dimension.Y];
      //  float z = values[Dimension.Z];
       float accelationSquareRoot = (float)Math.sqrt(square(acceleration[0]) +square(acceleration[1])+ square(acceleration[2]))
           / (gravitySquared);


        //kept as might need it later

        long actualTime = System.currentTimeMillis();
        //Log.i("Hugh", Long.toString(actualTime) + " " + Long.toString(lastShake) + " " + Long.toString(Math.abs(lastShake - actualTime)));
        if (Math.abs(lastShake - actualTime) > coolDownPeriod)
        {
           // Log.i("Hugh", "inside " + Long.toString(actualTime) + " " + Long.toString(lastShake) + " " + Long.toString(Math.abs(lastShake - actualTime)));
            lastShake = actualTime;

            float difference = Math.abs( accelationSquareRoot - lastShakeValue);
            Log.i("Hugh", Float.toString(difference));
            if (
                        (isProximityCovered == false && powerManager.isInteractive() == true && difference >= shakeThreshold)
                    || (isProximityCovered == false && powerManager.isInteractive() == false && difference >= screenOffShakeThreshold)
                        )
            {
                if (powerManager.isInteractive()){
                    if (!invisbleView.getKeepScreenOn()) {
                        windowManager.removeView(invisbleView);
                        invisbleView.setKeepScreenOn(true);
                        windowManager.addView(invisbleView, params);
                    }
                }
                else {
                    if (!isProximityCovered) {
                       // Log.i("Hugh", "turn screen on?");
                        turnScreenOn();
                    }
                }
            }
            else
            {
                if (invisbleView.getKeepScreenOn()) {
                    windowManager.removeView(invisbleView);
                    invisbleView.setKeepScreenOn(false);
                    windowManager.addView(invisbleView, params);
                }
            }

        }
    }

    private void turnScreenOn(){
        //  Log.i("Hugh", "PowerManager.PARTIAL_WAKE_LOCK " + Boolean.toString(powerManager.isWakeLockLevelSupported(PowerManager.PARTIAL_WAKE_LOCK)));
        //  Log.i("Hugh", "PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK " + Boolean.toString(powerManager.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)));
        //  Log.i("Hugh", "PowerManager.FULL_WAKE_LOCK " + Boolean.toString(powerManager.isWakeLockLevelSupported(PowerManager.FULL_WAKE_LOCK)));
        //   Log.i("Hugh", "PowerManager.SCREEN_BRIGHT_WAKE_LOCK " + Boolean.toString(powerManager.isWakeLockLevelSupported(PowerManager.SCREEN_BRIGHT_WAKE_LOCK)));
          Log.i("Hugh", "PowerManager.SCREEN_DIM_WAKE_LOCK " + Boolean.toString(powerManager.isWakeLockLevelSupported(PowerManager.SCREEN_DIM_WAKE_LOCK)));

        if (invisbleView.getKeepScreenOn() == false) {
            windowManager.removeView(invisbleView);
            invisbleView.setKeepScreenOn(true);
            windowManager.addView(invisbleView, params);
        }
        int flags =  PowerManager.SCREEN_DIM_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP;
               // | PowerManager.ON_AFTER_RELEASE
        PowerManager.WakeLock wl = powerManager.newWakeLock(flags,
                "TapToWake");
        wl.acquire();
        wl.release();

    }

    //broken
    private void turnScreenOff(){
        if (invisbleView.getKeepScreenOn()) {
            windowManager.removeView(invisbleView);
            invisbleView.setKeepScreenOn(false);
            windowManager.addView(invisbleView, params);
        }
       // int flags =  PowerManager.PARTIAL_WAKE_LOCK;
        //PowerManager.WakeLock wl = powerManager.newWakeLock(flags,
       //         "TapToWake");
        //wl.acquire();
        //wl.release();


       // policyManager.lockNow();

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
