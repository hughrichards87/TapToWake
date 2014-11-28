package com.apps.rufus.taptowake;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

    private static final int shakeThreshold = 2;
    private static final int coolDownPeriodBetweenShakes = 200;
    private final static float gravitySquared = SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH;
    private class Dimension {
        public static final int X = 0;
        public static final int Y = 1;
        public static final int Z = 2;
    }

    private SensorManager sensorManager;
    Sensor accelerometer;
    Sensor gravity;
    Sensor proximity;

    private long lastUpdate;

    private TextView xAccelerometerView;
    private TextView yAccelerometerView;
    private TextView zAccelerometerView;
    private TextView normAccelerometerView;
    private TextView proximityView;
    private TextView xGravityView;
    private TextView yGravityView;
    private TextView zGravityView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        xAccelerometerView = (TextView)findViewById(R.id.accelerometer_x);
        yAccelerometerView = (TextView)findViewById(R.id.accelerometer_y);
        zAccelerometerView = (TextView)findViewById(R.id.accelerometer_z);
        normAccelerometerView = (TextView)findViewById(R.id.accelerometer_norm);

        xGravityView = (TextView)findViewById(R.id.gravity_x);
        yGravityView = (TextView)findViewById(R.id.gravity_y);
        zGravityView = (TextView)findViewById(R.id.gravity_z);

        proximityView = (TextView)findViewById(R.id.proximity_value);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        lastUpdate = System.currentTimeMillis();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Sensor.TYPE_GRAVITY  // which way is down
        // Sensor.TYPE_MAGNETIC_FIELD //This to turn it off when case is closed
        // Sensor.TYPE_ACCELEROMETER //how it has moved
        // Sensor.TYPE_PROXIMITY // am i covered?

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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void getProximity(SensorEvent event) {
        float[] values = event.values;
        // gravity strength
        float value = values[Dimension.X];
        proximityView.setText(Float.toString(value));

    }

    private void getGravity(SensorEvent event) {
        float[] values = event.values;
        // gravity strength
        float x = values[Dimension.X];
        float y = values[Dimension.Y];
        float z = values[Dimension.Z];

        xGravityView.setText(Float.toString(x));
        yGravityView.setText(Float.toString(y));
        zGravityView.setText(Float.toString(z));
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[Dimension.X];
        float y = values[Dimension.Y];
        float z = values[Dimension.Z];
        float accelationSquareRoot = (x * x + y * y + z * z)
                / (gravitySquared);

        xAccelerometerView.setText(Float.toString(x));
        yAccelerometerView.setText(Float.toString(y));
        zAccelerometerView.setText(Float.toString(z));
        normAccelerometerView.setText(Float.toString(accelationSquareRoot));

        //kept as might need it later
        long actualTime = event.timestamp;
        if (accelationSquareRoot >= shakeThreshold)
        {
            if (actualTime - lastUpdate > coolDownPeriodBetweenShakes)
            {
                lastUpdate = actualTime;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                gravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this,
                proximity,
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}
