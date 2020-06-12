package albstat;

// aidan tokarski
// 6/12/20
// a class for containing snapshot fields

public class DBSnapshot {
    
    public int match_player_id;
    public char snapshot_type;
    public int event_id;
    public String timestamp;
    public String mainhand_type;
    public String offhand_type;
    public String head_type;
    public String armor_type;
    public String shoe_type;
    public String cape_type;

    public DBSnapshot(int match_player_id, int snapshot_type, String event_id, Timestamp timestamp, Snapshot snapshot){
        this.match_player_id = match_player_id;
        this.snapshot_type = (char) snapshot_type;
        this.event_id = Integer.parseInt(event_id);
        this.timestamp = timestamp.toString();
        this.mainhand_type = snapshot.mainHandID;
        this.offhand_type = snapshot.offHandID;
        this.head_type = snapshot.headID;
        this.armor_type = snapshot.armorID;
        this.shoe_type = snapshot.shoeID;
        this.cape_type = snapshot.capeID;
    }

    public String toString(){
        return String.format("('%d', '%d', '%d', '%s', '%s', '%s', '%s', '%s', '%s', '%s')", match_player_id, (int) snapshot_type, event_id, timestamp, mainhand_type, offhand_type, head_type, armor_type, shoe_type, cape_type); 
    }
}