package com.nanb.location;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class LogfileCreate {
    String logText = "";
    public void appendLog(String text)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        System.out.println(formatter.format(date));

        logText = formatter.format(date) + " "+ text;

        File logFile = new File(Environment.getRootDirectory(),"LogFile.file");
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
        }
        catch (IOException e)
        {// TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
