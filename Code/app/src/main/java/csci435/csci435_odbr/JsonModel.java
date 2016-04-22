package csci435.csci435_odbr;

/**
 * Created by danielpark on 4/21/16.
 */
public class JsonModel {

    private String device_type = "Device: " + android.os.Build.DEVICE;
    private String os_version = "OS Version: "+ System.getProperty("os.version");
    private String app_name = "";
    private String app_version = "";
    private String title = "";
    private String name = "";
    private String description = "";
    private double report_start_time = 11;
    private double report_end_time = 11;
    private String accelerometer_stream = "";
    private String gyroscope_stream = "";

    private String events = "";

    class Eventset {
        public String screenshots;
        public String event_start;
        public String event_end;
    }

}
