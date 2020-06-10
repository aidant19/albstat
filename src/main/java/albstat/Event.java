package albstat;

// aidan tokarski
// 6/1/20
// a class for containing event data

public class Event {

    String eventID;
    String player1ID;
    String player2ID;
    String matchID;
    Timestamp timestamp;
    Snapshot player1Snapshot;
    Snapshot player2Snapshot;
    Group group;
    Participants participants;

    public Event(String eventID, String player1ID, String player2ID, Timestamp timestamp) {
        this.eventID = eventID;
        this.player1ID = player1ID;
        this.player2ID = player2ID;
        this.timestamp = timestamp;
    }

    public void setMatchID(String m) {
        this.matchID = m;
    }

    public String toString() {
        String headerString = String.format("%s,%s,%s,%s,%d", eventID, player1ID, player2ID, timestamp,
                participants.snapshots.size());
        return String.format("%s,%s,%s,%s,%s", headerString, player1Snapshot.toString(), player2Snapshot.toString(),
                group.toString(), participants.toString());
    }

    public static Event buildFromStrings(String eventString, String matchID) {
        try {
            String[] eventStrings = eventString.split(",");
            String eventID = eventStrings[0];
            String player1ID = eventStrings[1];
            String player2ID = eventStrings[2];
            Timestamp timestamp = new Timestamp(eventStrings[3]);
            Event event = new Event(eventID, player1ID, player2ID, timestamp);
            event.setMatchID(matchID);
            String[] snapshot1Strings = new String[6];
            String[] snapshot2Strings = new String[6];
            System.arraycopy(eventStrings, 5, snapshot1Strings, 0, 6);
            System.arraycopy(eventStrings, 11, snapshot2Strings, 0, 6);
            event.player1Snapshot = Snapshot.buildFromStrings(snapshot1Strings, player1ID, eventID);
            event.player2Snapshot = Snapshot.buildFromStrings(snapshot2Strings, player2ID, eventID);
            String[] groupStrings = new String[35];
            System.arraycopy(eventStrings, 17, groupStrings, 0, 35);
            event.group = Group.buildFromStrings(groupStrings, eventID);
            int participants = Integer.parseInt(eventStrings[4]);
            String[] partiStrings = new String[participants * 7];
            System.arraycopy(eventStrings, 52, partiStrings, 0, participants * 7);
            event.participants = Participants.buildFromStrings(partiStrings, eventID);
            return event;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(matchID + " : " + eventString);
            e.printStackTrace();
            System.exit(1);
            return null;
        }
    }
}