package com.example.todolist_intent;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Vibrator;
import android.widget.Toast;

public class AlertReceiver extends BroadcastReceiver {


    @Override

    public void onReceive(Context context, Intent intent) {


        boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        String location = intent.getStringExtra("location");
        intent.putExtra("alert", 1);
        // boolean getBooleanExtra(String name, boolean defaultValue)

        if(isEntering) {
            Toast.makeText(context, "이 근처에 할 일이 있어요!", Toast.LENGTH_LONG).show();

        }
        else
            Toast.makeText(context, location+" 지점에서 벗어납니다..", Toast.LENGTH_SHORT).show();

    }
}


