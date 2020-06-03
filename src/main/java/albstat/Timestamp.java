package albstat;

// aidan tokarski
// 5/26/20
// timestamp converter for albion online

public class Timestamp {
    // takes in YYYY-MM-DDTHH:MM:SS.SSSSSSSSS (9 decimal places)

    public int[] date;
    public double[] time;

    public Timestamp(String timestampString) {
        this.date = new int[3];
        this.time = new double[3];
        timestampString = timestampString.replace("Z", "");
        String[] dateTimeStrings = timestampString.split("T");
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

        if (this.time[0] < time.time[0]) {
            return false;
        } else if (this.time[1] < time.time[1]) {
            return false;
        } else if (this.time[2] < time.time[2]) {
            return false;
        } else {
            return true;
        }
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

        if (this.time[0] > time.time[0]) {
            return false;
        } else if (this.time[1] > time.time[1]) {
            return false;
        } else if (this.time[2] > time.time[2]) {
            return false;
        } else {
            return true;
        }
    }

    public String toString(){
        return String.format("%d-%d-%dT%.0f:%.0f:%f", this.date[0], this.date[1], this.date[2], this.time[0], this.time[1], this.time[2]);
    }
}