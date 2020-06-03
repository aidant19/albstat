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

    public Event(String eventID, String player1ID, String player2ID, String matchID, Timestamp timestamp) {
        this.eventID = eventID;
        this.player1ID = player1ID;
        this.player2ID = player2ID;
        this.matchID = matchID;
        this.timestamp = timestamp;
    }

    public String toString(){
        return String.format("%s, %s, %s, %s", eventID, player1ID, player2ID, timestamp);
    }
}