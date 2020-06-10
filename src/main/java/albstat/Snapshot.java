package albstat;

// aidan tokarski
// 6/2/20
// a class for containing snapshot (build) data

public class Snapshot {

    String playerID;
    String eventID;
    String mainHandID;
    String offHandID;
    String headID;
    String armorID;
    String shoeID;
    String capeID;

    public Snapshot(String playerID, String eventID) {
        this.playerID = playerID;
        this.eventID = eventID;
    }

    public void addMain(String m){
        this.mainHandID = m;
    }

    public void addOff(String o){
        this.offHandID = o;
    }

    public void addHead(String h){
        this.headID = h;
    }

    public void addArmor(String a){
        this.armorID = a;
    }

    public void addShoe(String s){
        this.shoeID = s;
    }

    public void addCape(String c){
        this.capeID = c;
    }

    public String toString(){
        return String.format("%s,%s,%s,%s,%s,%s", mainHandID, offHandID, headID, armorID, shoeID, capeID);
    }

    public static Snapshot buildFromStrings(String[] snapshotStrings, String playerID, String eventID){
        Snapshot snapshot = new Snapshot(playerID, eventID);
        snapshot.addMain(snapshotStrings[0]);
        snapshot.addOff(snapshotStrings[1]);
        snapshot.addHead(snapshotStrings[2]);
        snapshot.addArmor(snapshotStrings[3]);
        snapshot.addShoe(snapshotStrings[4]);
        snapshot.addCape(snapshotStrings[5]);
        return snapshot;
    }
}