package com.nanb.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyReceiver extends BroadcastReceiver {
    LogfileCreate logfileCreate = new LogfileCreate();
    @Override
    public void onReceive(Context context, Intent intent) {
        String status = NetworkUtil.getConnectivityStatusString(context);
        if(status.isEmpty()) {
            logfileCreate.appendLog("Not connected to internet");
            Toast.makeText(context, "It seems that you are not connected to the internet, Please Check your internet connection.", Toast.LENGTH_LONG).show();
        }

    }
}
