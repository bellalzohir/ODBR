package csci435.csci435_odbr;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

/**
 * Created by Brendan Otten on 2/17/2016.
 * resource: http://developer.android.com/guide/topics/ui/accessibility/services.html
 */
public class AccessService extends AccessibilityService{
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if(event.getPackageName().equals(Globals.packageName)) {


            Toast.makeText(getBaseContext(), "Service: " + event.getPackageName(), Toast.LENGTH_SHORT).show();
            BugReport.getInstance().addUserEvent(event);
            Log.v("AccessService", "PONG");

            //Where do we store this data? It all seems pertinent, only keep data that is not part of our app so we ignore things
            //That have packageName == "csci435.csci435_odbr"

            //record event and take snapshot here?
        }
    }

    @Override
    public void onInterrupt() {

    }
}