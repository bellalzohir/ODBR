package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Brendan Otten on 3/23/2016.
 */
public class SnapshotIntentService extends IntentService {

    String filename;
    static OutputStream os;
    Process sh;


    public SnapshotIntentService() {
        super("SnapshotIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Take screenshot, allows us to define other scenarios just incase we need to here

        //implemented for getEvent
        Bundle extras = intent.getExtras();
        int index = extras.getInt("index");
        takeScreenShot(index);

    }


    private void takeScreenShot(int i) {
        Log.v("Snapshot", "Started");

        //Run SU process here, we are in background thread.
        //Log.v("Screenshot", "Screenshot async occuring");
        long timestamp = 0;
        if (Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState())) {

            // we check if external storage is\ available, otherwise
            // display an error message to the user using Toast Message
            File sdCard = Environment.getExternalStorageDirectory();
            File directory = new File(sdCard.getAbsolutePath() + "/ScreenShots");
            directory.mkdirs();

            //filename = "screenshotlol" + i + ".png";
            //File yourFile = new File(directory, filename);

            filename = "screenshotnew" + Globals.screenshot_index + ".png";
            try {
                sh = Runtime.getRuntime().exec("su", null, null);
                os = sh.getOutputStream();
                os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename +"\n").getBytes("ASCII"));
                os.flush();

                timestamp = System.currentTimeMillis();
                Screenshots screenshots = new Screenshots(filename, timestamp);
                BugReport.getInstance().addPotentialScreenshot(screenshots);
                //int j = 0;
                //while(Globals.recording){
                    //Process sh = Runtime.getRuntime().exec("su", null, null);
                    //filename = "screenshotlol" + Globals.screenshot_index + ".png";

                    //os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename +"\n").getBytes("ASCII"));
                    //os.flush();
                    //Log.v("Screenshot", "Screenshot fired at: " + System.currentTimeMillis());
                    //os.close();

                    //add to stack here, possibly implement a wait function.
                //}
                //os.close();
                    /*
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    Bitmap b = BitmapFactory.decodeFile(yourFile.getAbsolutePath(), options);
                    BugReport.getInstance().addScreenshot(b); //screenshot is added here so shouldn't be a problem
                    */
                //i++;
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            Log.v("Screenshot", "ERROR");

        }
        //Tell program to restore the widget
        Intent localIntent = new Intent("csci435.csci435_odbr.SnapshotIntentService.send").putExtra("timestamp", timestamp);
        localIntent.putExtra("filename", filename);
        // Broadcasts the Intent to receivers in this app.
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
        //Globals.screenshot = 0;
    }

    public static void writeBytes(){
        try {
            String filename = "screenshot" + Globals.screenshot_index + ".png";
            os.write(("/system/bin/screencap -p " + "/sdcard/ScreenShots/" + filename + "\n").getBytes("ASCII"));
            os.flush();
            long timestamp = System.currentTimeMillis();
            Screenshots screenshots = new Screenshots(filename, timestamp);
            BugReport.getInstance().addPotentialScreenshot(screenshots);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void finishWriting() {
        try {
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
