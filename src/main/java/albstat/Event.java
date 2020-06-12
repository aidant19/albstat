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
}