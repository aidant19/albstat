package albstat;

// aidan tokarski
// 6/10/20
// a class for containing the group data from events

import java.util.ArrayList;

public class Group {

    ArrayList<MainHandSnapshot> snapshots;

    public Group() {
        this.snapshots = new ArrayList<MainHandSnapshot>();
        for (int i = 0; i < 5; i++) {
            snapshots.add(new MainHandSnapshot(null, null));
        } 
    }

    public void addSnap(MainHandSnapshot m) {
        this.snapshots.remove(0);
        this.snapshots.add(m);
    }

    public String toString() {
        String snapshotsString = "";
        for (MainHandSnapshot mainHandSnapshot : snapshots) {
            snapshotsString += mainHandSnapshot.toString() + ",";
        }
        snapshotsString = snapshotsString.substring(0, snapshotsString.length() - 1);
        return snapshotsString;
    }

    public static Group buildFromStrings(String[] groupStrings, String eventID) {
        Group group = new Group();
        for (int i = 0; i < 5; i++) {
            MainHandSnapshot snap = new MainHandSnapshot(groupStrings[0 + (i * 7)], eventID);
            snap.addMain(groupStrings[1 + (i * 7)]);
            group.addSnap(snap);
        }
        return group;
    }
}