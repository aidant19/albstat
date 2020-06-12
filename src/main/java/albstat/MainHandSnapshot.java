package albstat;

// aidan tokarski
// 6/10/20
// a class for containing just a mainhand (for use with groups)

public class MainHandSnapshot extends Snapshot {

    public MainHandSnapshot(String playerID, String eventID) {
        super(playerID, eventID);
        this.offHandID = null;
        this.armorID = null;
        this.headID = null;
        this.shoeID = null;
        this.capeID = null;
    }

    public String toString(){
        return String.format("%s,%s,%s,%s,%s,%s,%s", playerID, mainHandID, offHandID, headID, armorID, shoeID, capeID);
    }
}