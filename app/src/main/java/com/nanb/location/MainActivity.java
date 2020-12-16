package com.nanb.location;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

   String locationdata = "",TAG="contact data",latitudedata="",longitudedata="",AddressData="";
   LogfileCreate logfileCreate = new LogfileCreate();
   Long backpresstime;
   Toast backtoast;
    private static final int Request_code = 6;
    private ArrayList<String> listOfLines = new ArrayList<>();
    private BroadcastReceiver MyReceiver = null;
    TextView lat,lon,add;
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 10000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyReceiver = new MyReceiver();
        broadcastIntent();
        checkpermission();
        locationservice();


        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},Request_code);
        }else{
            startLocationService();
        }

        lat = findViewById(R.id.latitude);
        lon = findViewById(R.id.longitude);
        add = findViewById(R.id.address);

       File file = new File(MainActivity.this.getFilesDir(),"SurakchaLocation.txt");
       if(file.exists()){
           setdata();
       }

        findViewById(R.id.setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),settingActivity.class));
            }
        });


        findViewById(R.id.btsms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS},1);
                    }else{
                        try{
                            String phnnbr = "8709337435";
                            File file = new File(MainActivity.this.getFilesDir(),"Surakchacontact.txt");
                            if(file.exists()){
                                loadcontact();
                                for(String nmb : listOfLines){
                                    Log.d(TAG,nmb);
                                    String Locationhyperlink =  "https://www.google.com/maps/search/?api=1&query="+latitudedata+","+longitudedata;
                                    Log.d("http link",Locationhyperlink);
                                    SmsManager smsManager = SmsManager.getDefault();
                                    ArrayList<String> parts = smsManager.divideMessage("Latitude: "+latitudedata+"\nLongitude: "+longitudedata+"\nAddress: "+AddressData+"\nGoogle map Link: "+Locationhyperlink);
                                    smsManager.sendMultipartTextMessage(phnnbr,null,parts,null,null);
                                }
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"Please select contacts.",Toast.LENGTH_LONG).show();
                            }
                            listOfLines.clear();

                        }catch (Exception e){
                            Log.d("smsManager",e.getMessage());
                        }
                    }
                }
                //Toast.makeText(MainActivity.this, String.valueOf(listOfLines.size()), Toast.LENGTH_SHORT).show();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                handler.postDelayed(runnable, delay);
                File file = new File(MainActivity.this.getFilesDir(),"SurakchaLocation.txt");
                if(file.exists()){
                    setdata();
                }
            }
        }, delay);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        File file = new File(MainActivity.this.getFilesDir(),"SurakchaLocation.txt");
        if(file.exists()){
            setdata();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File file = new File(MainActivity.this.getFilesDir(),"SurakchaLocation.txt");
        if(file.exists()){
            setdata();
        }
    }

    private void setdata() {
        loadlocationdata();
        getLatituteandlongitute();
        lat.setText("Latitude: "+latitudedata);
        lon.setText("Longitude: "+longitudedata);
        add.setText("Address: "+AddressData);
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
    private void startLocationService(){
        if(!islocationServiceAvailable()){
            Intent intent = new Intent(getApplicationContext(), locationForgroundservice.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            logfileCreate.appendLog("Location service start",this);
            //Toast.makeText(this,"Location service start",Toast.LENGTH_SHORT).show();
        }
    }
    private void getLatituteandlongitute() {
        Log.d(TAG, "getLatituteandlongitute: " + locationdata);
       int lastpartoflatitude = locationdata.indexOf(",");
       int lastpartoflongitude = locationdata.indexOf(", Address: ");
       AddressData = locationdata.substring(lastpartoflongitude+11);
       latitudedata = locationdata.substring(10,lastpartoflatitude);
       longitudedata = locationdata.substring(lastpartoflatitude+13,lastpartoflongitude);
        //Log.d(TAG, String.valueOf(lastpartoflatitude) +" "+ String.valueOf(lastpartoflongitude) + " "+ latitudedata+" "+longitudedata);
    }

    private void locationservice() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            Toast.makeText(getApplicationContext(),"Location service is not enable",Toast.LENGTH_SHORT).show();
            new AlertDialog.Builder(this,R.style.CustomAlertDailog)
                    .setTitle("Location Service")
                    .setMessage("Let us help the application to determine the exert Location.")
                    .setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Disagree",null)
                    .show();
        }
    }

    private void broadcastIntent() {
        registerReceiver(MyReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    private void checkpermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)+ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)+ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)+ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{
                        Manifest.permission.READ_CONTACTS,Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.SEND_SMS
                },Request_code);
            }
        }
    }

    private void loadcontact() {
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new FileReader(new File(MainActivity.this.getFilesDir(),"Surakchacontact.txt")));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            line = bufReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (line != null) { listOfLines.add(line);
            try {
                line = bufReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bufReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void loadlocationdata() {
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new FileReader(new File(MainActivity.this.getFilesDir(),"SurakchaLocation.txt")));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            line = bufReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (line != null) { locationdata = line;
            try {
                line = bufReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            bufReader.close();
        } catch (IOException e) {
            e.printStackTrace();
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