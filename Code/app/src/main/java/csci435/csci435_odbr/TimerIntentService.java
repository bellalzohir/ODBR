package csci435.csci435_odbr;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Brendan Otten on 3/29/2016.
 */
//Background process that checks every 5 seocnds if the user is still actively using the application
public class TimerIntentService extends IntentService {

    Intent localIntent;

    public TimerIntentService(){
        super("TimerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v("Timer", "hiding the visibility");
        localIntent = new Intent("csci435.csci435_odbr.TimerIntentService.send").putExtra("visibility", false);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

        Globals.time_last_event = System.currentTimeMillis();
        //while the user does something else in less than 5 seconds, keep the pause button invisible
        while(System.currentTimeMillis() - Globals.time_last_event < 5000){

        }

        localIntent = new Intent("csci435.csci435_odbr.TimerIntentService.send").putExtra("visibility", true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);

    }
}