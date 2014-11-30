package com.apps.rufus.taptowake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class OnPowerButtonPressedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {

        Log.i("Hugh", "Power button is pressed.");

        //perform what you want here
    }
}