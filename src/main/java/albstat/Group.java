package albstat;

// aidan tokarski
// 6/10/20
// a class for containing the group data from events

import java.util.ArrayList;

public class Group {

    ArrayList<MainHandSnapshot> snapshots;

    public Group() {
        this.snapshots = new ArrayList<MainHandSnapshot>();
    }

    public void addSnap(MainHandSnapshot m) {
        this.snapshots.add(m);
    }
}