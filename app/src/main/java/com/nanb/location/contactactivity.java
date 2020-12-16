package com.nanb.location;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class contactactivity extends AppCompatActivity {
    private ArrayList<String> alist = new ArrayList<String>();

    ListView contactlist;
    String TAG = "Contact:";
    LogfileCreate logfileCreate = new LogfileCreate();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactactivity);
        contactlist = (ListView) findViewById(R.id.contactlist);
        get();
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),settingActivity.class));
            }
        });
        findViewById(R.id.Save).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Log.d(TAG,String.valueOf(alist.size()));
                logfileCreate.appendLog("List size: "+String.valueOf(alist.size()),getApplicationContext());
                // Toast.makeText(MainActivity.this, String.valueOf(alist.size()), Toast.LENGTH_SHORT).show();

                for(String nmb : alist){
                    Log.d(TAG,nmb);
                    logfileCreate.appendLog("Contact List: "+nmb,getApplicationContext());
                }
                createFile();
                alist.clear();
                startActivity(new Intent(getApplicationContext(),settingActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(getApplicationContext(),settingActivity.class));
    }

    private void createFile() {

        File gpxfile = new File(getApplicationContext().getFilesDir(), "Surakchacontact.txt");
        FileWriter writer = null;
        try {
            writer = new FileWriter(gpxfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = alist.size();
        // System.out.println("size of arraylist is"+size);
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
    public void get(){
        Cursor cursor  = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null);
        startManagingCursor(cursor);

        String[] from = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone._ID

        };

        //Toast.makeText(getApplicationContext(),String.valueOf(from.length),Toast.LENGTH_LONG).show();
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
                findViewById(R.id.Save).setVisibility(View.VISIBLE);
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


}