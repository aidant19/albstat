package albstat;

// aidan tokarski
// 6/10/20
// a class for containing the group data from events

import java.util.ArrayList;

public class Participants {

    ArrayList<Snapshot> snapshots;

    public Participants() {
        this.snapshots = new ArrayList<Snapshot>();
    }

    public void addSnap(Snapshot m){
        this.snapshots.add(m);
    }
}