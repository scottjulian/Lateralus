package net.scottjulian.lateralus.components.phone;


import android.provider.CallLog;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Phonecall implements Comparable {

    private String _name;
    private String _number;
    private long   _duration;
    private int    _type;

    private long _timestamp;
    private String _timeFormatted;

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String NAME_UNKOWN = "Unknown";
    public static final int INCOMING = CallLog.Calls.INCOMING_TYPE;
    public static final int OUTGOING = CallLog.Calls.OUTGOING_TYPE;


    public Phonecall(String name, String number, long duration, long timestamp, int type){
        _name = (name == null || name.isEmpty()) ? NAME_UNKOWN : name;
        _number = number.replace('+','\0');
        _duration = duration;
        _timestamp = timestamp;
        _type = (type == INCOMING) ? INCOMING : OUTGOING;
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        _timeFormatted = sdf.format(new Date(_timestamp));
    }

    public String getName(){
        return _name;
    }

    public String getNumber(){
        return _number;
    }

    public long getDuration(){
        return _duration;
    }

    public Boolean wasIncoming(){
        return (_type == INCOMING);
    }

    public Boolean wasOutgoing(){
        return (_type == OUTGOING);
    }

    public int getType(){
        return _type;
    }

    public String getTime() {
        return _timeFormatted;
    }

    public long getTimestamp() {
        return _timestamp;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof Phonecall){
            Phonecall s = (Phonecall) o;
            // most recent first, oldest last
            return (getTimestamp() > s.getTimestamp()) ? -1 : 1;
        }
        return 0;
    }
}
