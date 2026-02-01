package com.example.neartalk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        MyService.stopAlarm();
        Intent serviceIntent = new Intent(context, MyService.class);
        context.stopService(serviceIntent);
    }
}
