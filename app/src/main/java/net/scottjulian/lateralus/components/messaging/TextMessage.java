package net.scottjulian.lateralus.components.messaging;


import java.text.SimpleDateFormat;
import java.util.Date;


public class TextMessage implements Comparable{

    private String _name;
    private String _number;
    private String _msgText;
    private String _type;
    private String _timeFormatted;
    private long   _timestamp;
    private int _sent;

    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String MMS = "mms";
    public static final String SMS = "sms";
    public static final int SENT = 2;
    public static final int RECEIVED = 1;

    public TextMessage(String name, String number, String msgText, String type, long timestamp, int sent){
        _name = name;
        _number = number.replace('+','\0');
        _msgText = msgText;
        _timestamp = timestamp;
        _type = (type == SMS) ? SMS : MMS;
        _sent = (sent == SENT) ? SENT : RECEIVED;

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        _timeFormatted = sdf.format(new Date(_timestamp));
    }

    public String getName(){
        return _name;
    }

    public String getNumber(){
        return _number;
    }

    public String getMessageText(){
        return _msgText;
    }

    public String getTime(){
        return _timeFormatted;
    }

    public long getTimestamp(){
        return _timestamp;
    }

    public Boolean wasSent(){
        return _sent == SENT;
    }

    public Boolean wasReceived(){
        return _sent == RECEIVED;
    }

    public String getType(){
        return _type;
    }

    @Override
    public int compareTo(Object o) {
        if(o instanceof TextMessage){
            TextMessage tm = (TextMessage) o;
            // oldest first, most recent last
            return (getTimestamp() > tm.getTimestamp()) ? 1 : -1;
        }
        return 0;
    }

}
