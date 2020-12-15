package com.nanb.location;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;

public class LogfileCreate {
    String logText = "";
    public void appendLog(String text,Context context)
    {
        File logFile = new File(context.getFilesDir(),"LogFile.txt");
        Log.d("filesize",String.valueOf(logFile.length()));
        if(logFile.length() > 10000){
            logFile.delete();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        //System.out.println(formatter.format(date));

        logText = formatter.format(date) + " "+ text;

        Log.d("Log data",  logText);

        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(logText);
            buf.newLine();
            buf.close();
            Log.d("path",logFile.getAbsolutePath());
        }
        catch (IOException e)
        {

            e.printStackTrace();
        }
    }
}
