package com.nanb.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

public class settingActivity extends AppCompatActivity {


    Switch locationserviceSwitcher;
    LogfileCreate logfileCreate = new LogfileCreate();
    private static final int Request_code = 6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        locationserviceSwitcher = (Switch) findViewById(R.id.switcher);
        if(islocationServiceAvailable()){
            locationserviceSwitcher.setChecked(true);
        }else{
            locationserviceSwitcher.setChecked(false);
        }
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        findViewById(R.id.contactselect).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),contactactivity.class));
            }
        });
        findViewById(R.id.termandcondtion).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Term and Condition",Toast.LENGTH_LONG).show();
            }
        });
        findViewById(R.id.privatepolicy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Private policy",Toast.LENGTH_LONG).show();
            }
        });

        locationserviceSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                       ActivityCompat.requestPermissions(settingActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
                    }else{
                        startLocationService();
                    }
                } else {
                    stopLocationService();
                }
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Request_code && grantResults.length > 0){
            if(grantResults[0 ] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            }else{
                logfileCreate.appendLog("Permission denied",this);
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void startLocationService(){
        if(!islocationServiceAvailable()){
            Intent intent = new Intent(getApplicationContext(), locationForgroundservice.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            logfileCreate.appendLog("Location service start",this);
            Toast.makeText(this,"Location service start",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(islocationServiceAvailable()){
            Intent intent  = new Intent(getApplicationContext(), locationForgroundservice.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            logfileCreate.appendLog("Location service start",this);
            Toast.makeText(this,"Location service stop",Toast.LENGTH_SHORT).show();
        }
    }
    private boolean islocationServiceAvailable(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for(ActivityManager.RunningServiceInfo service: activityManager.getRunningServices(Integer.MAX_VALUE)){
                if(locationForgroundservice.class.getName().equals(service.service.getClassName())){
                    if(service.foreground){
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }
}