package csci435.csci435_odbr;

import java.lang.reflect.Array;
import java.util.AbstractQueue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import org.json.JSONObject;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.view.accessibility.AccessibilityEvent;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.util.Log;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.accessibility.AccessibilityNodeInfo;

/**
 * Created by Rich on 2/11/16.
 */
public class BugReport {
    private static int colors[] = {Color.BLUE, Color.GREEN, Color.RED, Color.CYAN, Color.YELLOW, Color.MAGENTA};
    private static int MAX_ITEMS_TO_PRINT = 10;

    private HashMap<Sensor, SensorDataList> sensorData = new HashMap<Sensor, SensorDataList>();
    private HashMap<Sensor, Bitmap> sensorGraphs = new HashMap<Sensor, Bitmap>();
    //private Queue<Screenshots> screenshotsQueue = new LinkedList<Screenshots>();
    private ArrayList<Sensor> sensorList = new ArrayList<Sensor>();
    private List<Events> eventList = new ArrayList<Events>();
    private List<Screenshots> screenshotsList = new ArrayList<Screenshots>();
    private SparseArray<Bitmap> screenshots = new SparseArray<Bitmap>();
    private String title = "";
    private String reporterName = "";
    private String desiredOutcome = "";
    private String actualOutcome = "";
    private int eventCount = 0;

    private static BugReport ourInstance = new BugReport();

    public static BugReport getInstance() {
        return ourInstance;
    }

    private BugReport() {}

    public void clearReport() {
        sensorData.clear();
        sensorGraphs.clear();
        sensorList.clear();
        eventList.clear();
        screenshots.clear();
        title = "";
        reporterName = "";
        desiredOutcome = "";
        actualOutcome = "";
        eventCount = 0;
        screenshotsList.clear();

    }
    /*
    public void addUserEvent(AccessibilityEvent e) {
        eventList.add(new Events(e));
        addCount();
    }
    */

    public void addScreenshot(Screenshots screenshot){
        //add user screenshot to some data structure, can literally be just an array

        screenshotsList.add(screenshot);
        //addCount();
    }

    public void addPotentialScreenshot(Screenshots screenshot){
        screenshotsList.add(screenshot);
    }

    public Screenshots getPotentialScreenshot(int index){
        return screenshotsList.get(index);
    }

    public int getListSize(){
        return screenshotsList.size();
    }

    public void addCount(){
        eventCount++;
    }

    public void addSensorData(Sensor s, SensorEvent e) {
        if (!sensorList.contains(s)) {
            sensorList.add(s);
            sensorData.put(s, new SensorDataList());
        }
        sensorData.get(s).addData(e.timestamp, e.values.clone());
    }

    public void addScreenshot(Bitmap s) {
        //addCount();
        screenshots.put(eventCount, s);
        addCount();
    }

    public void printScreenshots(){
        for(int i = 0; i < screenshots.size(); i++){
            Log.v("Screenshots", "" + getScreenshotAtIndex(i));
        }
    }

    public void setDesiredOutcome(String s) { desiredOutcome = s;}

    public void setActualOutcome(String s) {actualOutcome = s;}

    public void setTitle(String s) {title = s;}

    public void setReporterName(String s) {reporterName = s;}

    /**
     * Returns its data as a formatted JSON file; currently outputs data to LogCat
     * @return
     */
    public JSONObject toJSON() {
        //Log Title, Reporter Name and Description
        Log.v("BugReport", "Reporter: " + reporterName);
        Log.v("BugReport", "Title: " + title);
        Log.v("BugReport", "What Should Happen: " + desiredOutcome);
        Log.v("BugReport", "What Does Happen: " + actualOutcome);

        //Log Sensor Data, each sensor capped at MAX_ITEMS_TO_PRINT
        for (Sensor s : sensorData.keySet()) {
            Log.v("BugReport", "|*************************************************|");
            Log.v("BugReport", "Data for Sensor: " + s.getName());
            SensorDataList data = sensorData.get(s);
            long timeStart = data.getTime(0);
            for (int i = 0; i < MAX_ITEMS_TO_PRINT && i < data.numItems(); i++) {
                Log.v("BugReport", "Time: " + (data.getTime(i) - timeStart) + "| " + "Data: " + makeSensorDataReadable(data.getValues(i)));
            }
            int printed = data.numItems() - MAX_ITEMS_TO_PRINT;
            Log.v("BugReport", "And " + (printed > 0 ? printed : 0) + " more");
            Log.v("BugReport", "|*************************************************|");
        }

        //Log UserEvent data
        for(int i = 0; i < eventList.size(); i++){
            Log.v("Event Number:", "" + i);
            eventList.get(i).printData();
        }
        return new JSONObject();
    }

    private String makeSensorDataReadable(float[] input) {
        String s = "";
        for (float f : input) {
            s +=  f + " | ";
        }
        return s;
    }


    public Bitmap drawSensorData(Sensor s) {
        if (sensorGraphs.containsKey(s)) {
            return sensorGraphs.get(s);
        }

        Globals.height = Globals.height / 2;
        Bitmap b = Bitmap.createBitmap(Globals.width, Globals.height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        SensorDataList data = sensorData.get(s);
        Paint color = new Paint();
        color.setColor(Color.BLACK);
        color.setStrokeWidth(5);
        c.drawARGB(255, 200, 200, 200);
        c.drawLine(0, 0, 0, Globals.height, color);
        c.drawLine(0, Globals.height / 2, Globals.width, Globals.height / 2, color);
        color.setStrokeWidth(3);

        long timeMod = data.getElapsedTime(data.numItems() - 1) / Globals.width;
        timeMod = timeMod > 0 ? timeMod : 1;
        for (int k = 0; k < data.sizeOfValueArray() && k < colors.length; k++) {
            float valueMod = data.meanValue(k) / (Globals.height / 2);
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
        Globals.height = Globals.height * 2;
        return b;
    }


    /*
    public ArrayList<LineGraphSeries<DataPoint>> getValuesAsPoints(Sensor s) {
        ArrayList<LineGraphSeries<DataPoint>> series = new ArrayList<LineGraphSeries<DataPoint>>();
        SensorDataList data = sensorData.get(s);
        for (int k = 0; k < data.sizeOfValueArray(); k++) {
            LineGraphSeries<DataPoint> line = new LineGraphSeries<DataPoint>();
            for (int i = 0; i < data.numItems(); i++) {
                line.appendData(new DataPoint(data.getTime(i), data.getValues(i)[k]), true, data.numItems());
            }
            series.add(line);
        }
        return series;
    } */


    /* Getters */
    public List<Events> getUserEvents() {
        return eventList;
    }
    public SparseArray<Bitmap> getScreenshots() {
        return screenshots;
    }
    public Events getEventAtIndex(int ndx) {
        return eventList.get(ndx);
    }
    public Bitmap getScreenshotAtIndex(int ndx) {
        return screenshots.get(ndx);
    }

    public String getReporterName() {
        return reporterName;
    }
    public String getTitle() {
        return title;
    }
    public int numSensors() {return sensorData.keySet().size();}
    public int numEvents() {return eventCount;}
    public Sensor getSensor(int pos) {return sensorList.get(pos);}
}


class Events {
    private Long timeStamp;
    private int eventType;
    private AccessibilityNodeInfo source;
    private CharSequence packageName;
    private Rect boundsInParent;
    private Rect boundsInScreen;

    public Events(AccessibilityEvent e){
        packageName = e.getPackageName();
        eventType = e.getEventType();
        timeStamp = e.getEventTime();
        source = e.getSource();
        boundsInParent = new Rect();
        boundsInScreen = new Rect();
        source.getBoundsInParent(boundsInParent);
        source.getBoundsInScreen(boundsInScreen);
    }


    public Rect getScreenRect() {
        return boundsInScreen;
    }


    public int[] getTransformedBoundsInScreen(int width, int height) {
        int[] location = new int[2];
        location[0] = boundsInScreen.centerX() * width / Globals.width;
        location[1] = boundsInScreen.centerY() * height / Globals.height;
        return location;
    }

    public String getViewDesc() {
        CharSequence className = source.getClassName();
        char stopChar = '.';
        int start = className.length() - 1;
        while (start > 0 && !(stopChar == className.charAt(start))) {
            start--;
        }

        CharSequence desc = source.getContentDescription();
        if (desc == null) {
            desc = source.getText();
        }
        return (String) className.subSequence(start + 1, className.length()) + " " + desc;
    }


    public void printData(){

        Log.v("Event: time", "" + timeStamp);
        Log.v("Event: type", "" + eventType);
        Log.v("Event: package name", "" + packageName);
        Log.v("Event: source", "" + source);
        Log.v("Event: bounds in parent", "" + boundsInParent);
        Log.v("Event: bounds in screen", "" + boundsInScreen);
    }

}


class SensorDataList {
    private ArrayList<Long> timestamps;
    private ArrayList<float[]> values;
    private float[] valueSums;
    private int numItems;

    public SensorDataList() {
        timestamps = new ArrayList<Long>();
        values = new ArrayList<float[]>();
        numItems = 0;
    }

    public void addData(long timestamp, float[] value) {
        ++numItems;
        timestamps.add(timestamp);
        values.add(value);
        if (numItems == 1) {
            valueSums = new float[value.length];
        }
        for (int i = 0; i < value.length; i++) {
            valueSums[i] += value[i];
        }
    }

    public long getTime(int index) {
        return timestamps.get(index);
    }

    public long getElapsedTime(int index) {
        return timestamps.get(index) - timestamps.get(0);
    }


    public float meanValue(int index) {
        return valueSums[index] / numItems;
    }

    public float stDev(int index) {
        float stdev = 0;
        float mean = meanValue(index);
        for (int i = 0; i < numItems; i++) {
            float f = values.get(i)[index];
            stdev += (f - mean) * (f - mean);
        }
        return (float) Math.sqrt(stdev / numItems);
    }

    public float[] getValues(int index) {
        return values.get(index);
    }

    public int numItems() {
        return numItems;
    }

    public int sizeOfValueArray() {
        return values.get(0).length;
    }
}