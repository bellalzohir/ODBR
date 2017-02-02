package semeru.odbr;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Brendan Otten on 4/20/2016.
 * Replay service that iterates over the ReportEvents and utilizes sendevent to send the getEvent lines to re-enact
 * the input traces. Just a feature to allow the tester to replay the events they put in, gives a visual representation
 * for how the developer will be able to re-enact their reported bug
 */
public class ReplayService extends IntentService {

    Process su_replay;
    OutputStream os;

    public ReplayService() {
        super("ReplayService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        //we're going to have to do an SU thing here, but for now, lets just log something every 10 seconds
        ExecutorService service = Executors.newSingleThreadExecutor();

        try {
            su_replay = Runtime.getRuntime().exec("su", null, null);
            os = su_replay.getOutputStream();
            service.submit(new ReplayEvent());
        } catch(Exception e){}
    }


    class ReplayEvent implements Runnable {
        private long wait_before = 1000; //Milliseconds to wait before starting inputs
        private long wait_after = 2000; //Milliseconds to wait after returning to report
        @Override
        public void run() {
            try {
                Thread.sleep(wait_before);
                long previousEventTime = BugReport.getInstance().getStartTime();
                ArrayList<SendEventBundle> events = preprocessEvents();
                long waitUntil = 0;
                long curTime;


                HashMap<String, DataOutputStream> devices = new HashMap<String, DataOutputStream>();
                for (String device : getDevices()) {
                    //Grant permissions to directly write to device
                    os.write(("chmod 777 " + device + " \n").getBytes("ASCII"));
                    os.flush();
                    devices.put(device, new DataOutputStream(new BufferedOutputStream(
                            new FileOutputStream(new File(device)))));
                }

                DataOutputStream out;
                for (SendEventBundle bundle : events) {
                    out = devices.get(bundle.device);
                    while ((curTime = System.currentTimeMillis()) < waitUntil) {/* <(^_^)> */}
                    for (byte[] cmd : bundle.commands) {
                        out.write(cmd);
                    }
                    out.flush();
                    waitUntil = curTime + (bundle.timeMillis - previousEventTime);
                    previousEventTime = bundle.timeMillis;
                }
                for (DataOutputStream outputStream : devices.values()) {
                    outputStream.close();
                }
                os.close();
                su_replay.waitFor();
                Thread.sleep(wait_after);
            } catch (Exception e) {
                Log.e("ReplayService", "Unable to replay event: " + e.getMessage());
            }

            Intent record_intent = new Intent(ReplayService.this, ReportActivity.class);
            record_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            record_intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(record_intent);
        }


        public String[] getDevices() {
            Set<String> devices = new HashSet<String>();
            for (ReportEvent event : BugReport.getInstance().getEventList()) {
                devices.add(event.getDevice());
            }
            return devices.toArray(new String[devices.size()]);
        }


        public ArrayList<SendEventBundle> preprocessEvents() {
            ArrayList<SendEventBundle> events =  new ArrayList<SendEventBundle>();
            ArrayList<GetEvent> buffer = new ArrayList<GetEvent>();
            String device = "";
            long time = 0;
            for (ReportEvent event : BugReport.getInstance().getEventList()) {
                device = event.getDevice();
                for (GetEvent e : event.getInputEvents()) {
                    if (buffer.isEmpty() || time == e.getTimeMillis()) {
                        buffer.add(e);
                    }
                    else {
                        events.add(makeBundle(buffer, device));
                        buffer.clear();
                        buffer.add(e);
                    }
                    time = e.getTimeMillis();
                }
            }
            if (!buffer.isEmpty()) {
                events.add(makeBundle(buffer, device));
                buffer.clear();
            }
            return events;
        }


        public SendEventBundle makeBundle(ArrayList<GetEvent> events, String device) {
            byte[][] eventBundle = new byte[events.size()][];
            for (int i = 0; i < events.size(); ++i) {
                try {
                    eventBundle[i] = events.get(i).getBytes();
                } catch (Exception err) {
                    Log.e("ReplayService", "Unexpected error in preprocess: " + err.getMessage());
                }
            }
            return new SendEventBundle(device, eventBundle, events.get(0).getTimeMillis());
        }


        class SendEventBundle {
            public String device;
            public byte[][] commands;
            public long timeMillis;

            public SendEventBundle(String device, byte[][] commands, long timeMillis) {
                this.device = device;
                this.commands = commands;
                this.timeMillis = timeMillis;
            }
        }

    }
}
