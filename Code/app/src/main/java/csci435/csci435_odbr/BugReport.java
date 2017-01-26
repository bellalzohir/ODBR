package csci435.csci435_odbr;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

/**
 * Created by Richard Bonett on 2/11/16.
 * Singleton class containing all information for a specific bug report.
 * The BugReport contains a list of the events, a list for each sensor's data, as well as
 * descriptions useful for the report.
 */
public class BugReport {
    public static transient int colors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};

    private transient HashMap<String, Bitmap> sensorGraphs = new HashMap<String, Bitmap>();

    private HashMap<String, SensorDataList> sensorData = new HashMap<String, SensorDataList>();
    private List<ReportEvent> eventList = new ArrayList<ReportEvent>();
    private transient HashMap<Long, Integer> orientations = new HashMap<Long, Integer>();
    private String app_name = Globals.packageName;
    private String device_type = android.os.Build.MODEL;
    private String description_actual_outcome = "";
    private String description_desired_outcome = "";
    private String name = "";
    private String title = "";
    private int os_version = android.os.Build.VERSION.SDK_INT;


    private static transient BugReport ourInstance = new BugReport();


    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {
        clearReport();
    }

    //resets the data, called after report is submitted
    public void clearReport() {
        sensorData.clear();
        sensorGraphs.clear();
        eventList.clear();
        title = "";
        name = "";
        description_desired_outcome = "";
        description_actual_outcome = "";
    }


    public void addEvent(ReportEvent e) {
        eventList.add(e);
    }

    //adds a sensor 'event' to a specific sensor
    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorData.containsKey(s.getName())) {
            sensorData.put(s.getName(), new SensorDataList(s.getType()));
        }
        sensorData.get(s.getName()).addData(e.timestamp, e.values.clone());
    }

    public void addOrientation(long time, int orientation) {
        orientations.put(time, orientation);
    }

    public void setDescription_desired_outcome(String s) {
        description_desired_outcome = s;
    }

    public void setDescription_actual_outcome(String s) {
        description_actual_outcome = s;
    }

    public void setTitle(String s) {
        title = s;
    }

    public void setName(String s) {
        name = s;
    }

    public void setAppName(String s) {
        app_name = s;
    }

    /**
     * Returns a Bitmap representing the sensor's data over the course of the report. The graph
     * is formatted with a horizontal line representing the mean value and other lines representing
     * the deviation from the mean at any given time during the report
     * @param s the sensor
     * @return Bitmap of the sensor data
     */
    public Bitmap drawSensorData(String s) {
        if (sensorGraphs.containsKey(s)) {
            return sensorGraphs.get(s);
        }

        int height = Globals.height / 2;
        Bitmap b = Bitmap.createBitmap(Globals.width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        SensorDataList data = sensorData.get(s);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(5);
        c.drawARGB(255, 200, 200, 200);
        c.drawLine(0, 0, 0, height, color);
        c.drawLine(0, height / 2, Globals.width, height / 2, color);
        color.setStrokeWidth(3);

        long timeMod = data.getElapsedTime(data.numItems() - 1) / Globals.width;
        timeMod = timeMod > 0 ? timeMod : 1;
        for (int k = 0; k < data.numItems() && k < colors.length; k++) {
            float valueMod = data.meanValue(k) / (height / 2);
            valueMod = valueMod > 0 ? valueMod : 1;
            color.setColor(colors[k]);
            float startX = data.getElapsedTime(0) / timeMod;
            float startY = data.getValues(0)[k] / valueMod;
            for (int i = 1; i < data.numItems(); i++) {
                float endX = data.getElapsedTime(i) / timeMod;
                float endY = data.getValues(i)[k] / valueMod;
                c.drawLine(startX, startY, endX, endY, color);
                startX = endX;
                startY = endY;
            }
        }
        sensorGraphs.put(s, b);
        return b;
    }

    /* Getters */
    public String getName() {
        return name;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription_desired_outcome(){
        return description_desired_outcome;
    }
    public String getDescription_actual_outcome(){
        return description_actual_outcome;
    }
    public List<ReportEvent> getEventList() {
        return eventList;
    }
    public int numEvents() {
        return eventList.size();
    }
    public ReportEvent getEventAtIndex(int ndx) {
        return eventList.get(ndx);
    }
    public HashMap<String, SensorDataList> getSensorData() {
        return sensorData;
    }
}


/**
 * A SensorDataList contains the values of a particular sensor over time
 */
class SensorDataList {
    private ArrayList<SensorDataContainer> values;
    private String[] valueDescriptions;
    private transient float[] valueSums;
    private transient int numItems;

    public SensorDataList(int sensorType) {
        values = new ArrayList<SensorDataContainer>();
        numItems = 0;
        valueDescriptions = Globals.sensorDescription.get(sensorType, new String[] {""});
    }

    public void addData(long timestamp, float[] value) {
        ++numItems;
        values.add(new SensorDataContainer(timestamp, value));
        if (numItems == 1) {
            valueSums = new float[value.length];
        }
        for (int i = 0; i < value.length; i++) {
            valueSums[i] += value[i];
        }
    }

    public long getTime(int index) {
        return values.get(index).time;
    }

    public long getElapsedTime(int index) {
        return values.get(index).time - values.get(0).time;
    }

    public float meanValue(int index) {
        return valueSums[index] / numItems;
    }

    public float[] getValues(int index) {
        return values.get(index).values;
    }

    public int numItems() {
        return numItems;
    }


    class SensorDataContainer {
        public long time;
        public float[] values;
        public SensorDataContainer(long time, float[] values) {
            this.time = time;
            this.values = values;
        }
    }
}