package albstat;

// aidan tokarski
// 6/10/20
// a class for containing just a mainhand (for use with groups)

public class MainHandSnapshot extends Snapshot {

    public MainHandSnapshot(String playerID, String eventID) {
        super(playerID, eventID);
        this.offHandID = "_";
        this.armorID = "_";
        this.headID = "_";
        this.shoeID = "_";
        this.capeID = "_";
    }
}