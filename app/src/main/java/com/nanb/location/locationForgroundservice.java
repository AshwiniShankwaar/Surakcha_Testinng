package com.nanb.location;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.FileOutputStream;

public class locationForgroundservice extends Service {
    private String num = "";
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null && locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();

                num = "latitude "+latitude + " "+ "longitude "+longitude;
                try {
                    FileOutputStream fos = null;
                    String fileName = "SurakchaLocation.txt";
                    fos = openFileOutput(fileName, MODE_PRIVATE);
                    fos.write(num.getBytes());
                    fos.close();
                    //display file saved message
                    //Toast.makeText(getBaseContext(), "File saved successfully!"+ " "+getFilesDir(),Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    private void startLocationService() {
        String channel_id = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle("Location server");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Runnning");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channel_id) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(channel_id, "Location service", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setDescription("This channel is used by location service");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(300000);
        locationRequest.setFastestInterval(60000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        startForeground(Constants.LOCATION_SERVICE_ID,builder.build());
    }

    private void stoplocationservice(){
        LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(locationCallback);
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(action != null){
                if(action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                }else if(action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                    stoplocationservice();
                }
            }
        }
        return super.onStartCommand(intent,flags, startId);
    }
}
