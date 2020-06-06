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
}