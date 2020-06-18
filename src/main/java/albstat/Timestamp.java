package albstat;

// aidan tokarski
// 5/26/20
// timestamp converter for albion online

public class Timestamp {

    public int[] date;
    public double[] time;

    public Timestamp(String timestampString) {
        // this constructor can use both the albion format and the db format
        // albion format separates Date, Time with a 'T' (ASCII 84)
        // db format separates Data, Time with a ' ' (ASCII 32)
        this.date = new int[3];
        this.time = new double[3];
        timestampString = timestampString.replace("Z", "");
        String[] dateTimeStrings;
        if (timestampString.contains("T")){
            dateTimeStrings = timestampString.split("T");
        } else {
            dateTimeStrings = timestampString.split(" ");
        }
        String dateString = dateTimeStrings[0];
        String timeString = dateTimeStrings[1];
        String[] dateStrings = dateString.split("-");
        String[] timeStrings = timeString.split(":");
        for (int i = 0; i < 3; i++) {
            this.date[i] = Integer.parseInt(dateStrings[i]);
        }
        for (int i = 0; i < 3; i++) {
            this.time[i] = Double.parseDouble(timeStrings[i]);
        }
    }

    public boolean isBetween(Timestamp time1, Timestamp time2) {
        if (this.isAfter(time1) && this.isBefore(time2)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isBetween(String time1String, String time2String){
        Timestamp time1 = new Timestamp(time1String);
        Timestamp time2 = new Timestamp(time2String);
        return isBetween(time1, time2);
    }

    public boolean isAfter(Timestamp time) {
        if (this.date[0] != time.date[0]) {
            if (this.date[0] < time.date[0]) {
                return false;
            } else {
                return true;
            }
        } else if (this.date[1] != time.date[1]) {
            if (this.date[1] < time.date[1]) {
                return false;
            } else {
                return true;
            }
        } else if (this.date[2] != time.date[2]) {
            if (this.date[2] < time.date[2]) {
                return false;
            } else {
                return true;
            }
        }
        if (this.time[0] != time.time[0]) {
            if (this.time[0] < time.time[0]) {
                return false;
            } else {
                return true;
            }
        } else if (this.time[1] != time.time[1]) {
            if (this.time[1] < time.time[1]) {
                return false;
            } else {
                return true;
            }
        } else if (this.time[2] != time.time[2]) {
            if (this.time[2] < time.time[2]) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public boolean isBefore(Timestamp time) {
        if (this.date[0] != time.date[0]) {
            if (this.date[0] > time.date[0]) {
                return false;
            } else {
                return true;
            }
        } else if (this.date[1] != time.date[1]) {
            if (this.date[1] > time.date[1]) {
                return false;
            } else {
                return true;
            }
        } else if (this.date[2] != time.date[2]) {
            if (this.date[2] > time.date[2]) {
                return false;
            } else {
                return true;
            }
        }
        if (this.time[0] != time.time[0]) {
            if (this.time[0] > time.time[0]) {
                return false;
            } else {
                return true;
            }
        } else if (this.time[1] != time.time[1]) {
            if (this.time[1] > time.time[1]) {
                return false;
            } else {
                return true;
            }
        } else if (this.time[2] != time.time[2]) {
            if (this.time[2] > time.time[2]) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public String toString(){
        // returns this object in the db format
        return String.format("%d-%d-%d %.0f:%.0f:%f", this.date[0], this.date[1], this.date[2], this.time[0], this.time[1], this.time[2]);
    }
}