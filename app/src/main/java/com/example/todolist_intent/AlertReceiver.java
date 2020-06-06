package com.example.todolist_intent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

public class AlertReceiver extends BroadcastReceiver {


    //교수님 강의 자료를 퍼와서 조금 수정함.
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isEntering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        String location = intent.getStringExtra("location");
        // boolean getBooleanExtra(String name, boolean defaultValue)



        if(isEntering)
            Toast.makeText(context, location+" 지점에 접근중입니다..", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(context, location+" 지점에서 벗어납니다..", Toast.LENGTH_SHORT).show();
    }
}

