package albstat;

// aidan tokarski
// 5/26/20
// timestamp converter for albion online

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Timestamp {

    public LocalDateTime timestamp;
    public static final DateTimeFormatter FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Timestamp(String timestampString) {
        // this constructor can use both the albion format and the db format
        // albion format separates Date, Time with a 'T' (ASCII 84)
        // db format separates Data, Time with a ' ' (ASCII 32)
        timestampString = timestampString.replace("Z", "");
        timestampString = timestampString.replace(" ", "T");
        timestamp = LocalDateTime.parse(timestampString);
    }

    public static String convertString(String apiTime){
        // converts an api timestamp into a db timestamp
        apiTime = apiTime.replace("Z", "");
        apiTime = apiTime.replace("T", " ");
        return apiTime;
    }

    public boolean isBetween(Timestamp time1, Timestamp time2) {
        // checks if this timestamp is between time1 and time2 (inclusive)
        if (this.isAfter(time1) && this.isBefore(time2)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBetween(String time1String, String time2String){
        // checks if this timestamp is between time1 and time2 (inclusive)
        // time1 and time2 are converted from string
        Timestamp time1 = new Timestamp(time1String);
        Timestamp time2 = new Timestamp(time2String);
        return isBetween(time1, time2);
    }

    public boolean isAfter(Timestamp time) {
        // returns true if this timestamp is after the specified time (inclusive)
        return timestamp.isAfter(time.timestamp);
    }

    public boolean isBefore(Timestamp time) {
        // returns true if this timestamp is before the specified time (inclusive)
        return timestamp.isBefore(time.timestamp);
    }

    public String toString(){
        // returns this timestamp in the db format
        return timestamp.format(FORMAT);
    }
}