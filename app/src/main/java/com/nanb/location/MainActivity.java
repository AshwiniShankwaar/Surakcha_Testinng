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
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

   String locationdata = "",TAG="contact data";

    private static final int Request_code = 6;
    private ArrayList<String> alist = new ArrayList<String>();
    private ArrayList<String> listOfLines = new ArrayList<>();
    ListView contactlist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkpermission();

        contactlist = (ListView) findViewById(R.id.contactlist);

                findViewById(R.id.save_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,String.valueOf(alist.size()));
                // Toast.makeText(MainActivity.this, String.valueOf(alist.size()), Toast.LENGTH_SHORT).show();

                for(String nmb : alist){
                    Log.d(TAG,nmb);
                }
                createFile();
                alist.clear();
            }
        });
        findViewById(R.id.btsms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadcontact();
                loadlocationdata();
                for(String nmb : listOfLines){
                    //Log.d(TAG,nmb);
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(nmb,null,locationdata,null,null);

                }
                listOfLines.clear();
                //Toast.makeText(MainActivity.this, String.valueOf(listOfLines.size()), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btpick_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                //startActivityForResult(intent, REQUEST_CODE);
                get();
            }
        });

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION
                    },100);
                }else{
                    startLocationService();
                }
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                stopLocationService();
            }
        });
        /*findViewById(R.id.show).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            loadlocationdata();
                Log.d("location",locationdata);
            }
        });*/

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

    public void get(){
        Cursor cursor  = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        startManagingCursor(cursor);

        String[] from = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone._ID

        };

        int[] to = {
                android.R.id.text1,android.R.id.text2
        };
        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2,cursor,from,to);
        contactlist.setAdapter(simpleCursorAdapter);
        contactlist.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        contactlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.setBackgroundColor(getResources().getColor(R.color.purple_200));
                Cursor pCur = (Cursor) parent.getAdapter().getItem(position);
                String phoneNo = pCur.getString(pCur.getColumnIndex(
                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                if(alist.contains(phoneNo)){
                    alist.remove(phoneNo);
                    view.setBackgroundColor(getResources().getColor(R.color.design_default_color_background));
                }else {
                    alist.add(phoneNo);
                }
                //Toast.makeText(MainActivity.this, phoneNo , Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createFile() {

        File gpxfile = new File(MainActivity.this.getFilesDir(), "Surakchacontact.txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(gpxfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = alist.size();
        System.out.println("size of arraylist is"+size);
        try {
            for (int i=0;i<size;i++) {
                String str = alist.get(i).toString();
                writer.write(str);
                writer.write("\n");
            }
            writer.close();
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == Request_code && grantResults.length > 0){
            if(grantResults[0 ] == PackageManager.PERMISSION_GRANTED){
                startLocationService();
            }else{
                Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show();
            }
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

    private void startLocationService(){
        if(!islocationServiceAvailable()){
            Intent intent = new Intent(getApplicationContext(), locationForgroundservice.class);
            intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location service start",Toast.LENGTH_SHORT).show();
        }
    }

    private void stopLocationService(){
        if(islocationServiceAvailable()){
            Intent intent  = new Intent(getApplicationContext(), locationForgroundservice.class);
            intent.setAction(Constants.ACTION_STOP_LOCATION_SERVICE);
            startService(intent);
            Toast.makeText(this,"Location service stop",Toast.LENGTH_SHORT).show();
        }
    }

}