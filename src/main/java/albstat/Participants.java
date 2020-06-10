package albstat;

// aidan tokarski
// 6/10/20
// a class for containing the participants data from events

import java.util.ArrayList;

public class Participants {

    ArrayList<ParticipantSnapshot> snapshots;

    public Participants() {
        this.snapshots = new ArrayList<ParticipantSnapshot>();
    }

    public void addSnap(ParticipantSnapshot m){
        this.snapshots.add(m);
    }

    public String toString() {
        String snapshotsString = "";
        for (ParticipantSnapshot participantSnapshot : snapshots) {
            snapshotsString += participantSnapshot.toString() + ",";
        }
        if(snapshotsString.length() > 0){
            snapshotsString = snapshotsString.substring(0, snapshotsString.length() - 1);
        }
        return snapshotsString;
    }
}