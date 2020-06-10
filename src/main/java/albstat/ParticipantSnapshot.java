package albstat;

// aidan tokarski
// 6/10/20
// a class for containing snapshot data of participants

public class ParticipantSnapshot extends Snapshot {

    public ParticipantSnapshot(String playerID){
        super(playerID, null);
    }

    public String toString(){
        return String.format("%s,%s,%s,%s,%s,%s,%s", playerID, mainHandID, offHandID, headID, armorID, shoeID, capeID);
    }
}