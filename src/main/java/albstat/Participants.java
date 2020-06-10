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

    public void addSnap(ParticipantSnapshot m) {
        this.snapshots.add(m);
    }

    public String toString() {
        String snapshotsString = "";
        for (ParticipantSnapshot participantSnapshot : snapshots) {
            snapshotsString += participantSnapshot.toString() + ",";
        }
        if (snapshotsString.length() > 0) {
            snapshotsString = snapshotsString.substring(0, snapshotsString.length() - 1);
        }
        return snapshotsString;
    }

    public static Participants buildFromStrings(String[] partiStrings, String eventID) {
        Participants participants = new Participants();
        for (int i = 0; i < partiStrings.length / 7; i++) {
            ParticipantSnapshot snap = new ParticipantSnapshot(partiStrings[0 + (i * 7)]);
            snap.addMain(partiStrings[1 + (i * 7)]);
            snap.addOff(partiStrings[2 + (i * 7)]);
            snap.addHead(partiStrings[3 + (i * 7)]);
            snap.addArmor(partiStrings[4 + (i * 7)]);
            snap.addShoe(partiStrings[5 + (i * 7)]);
            snap.addCape(partiStrings[6 + (i * 7)]);
            participants.addSnap(snap);
        }
        return participants;
    }
}