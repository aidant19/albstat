package albstat;

// aidan tokarski
// 6/12/20
// a class for containing snapshot fields

public class DBSnapshot {

    // db fields
    public int match_player_id;
    public char snapshot_type;
    public int event_id;
    public String timestamp;
    public DBItem mainhand;
    public DBItem offhand;
    public DBItem head;
    public DBItem armor;
    public DBItem shoe;
    public DBItem cape;

    public DBSnapshot(int match_player_id, int snapshot_type, String event_id, Timestamp timestamp, Snapshot snapshot) {
        // constructor for use with api interface
        this.match_player_id = match_player_id;
        this.snapshot_type = (char) snapshot_type;
        this.event_id = Integer.parseInt(event_id);
        this.timestamp = timestamp.toString();
        this.mainhand = new DBItem(snapshot.mainHandID);
        this.offhand = new DBItem(snapshot.offHandID);
        this.head = new DBItem(snapshot.headID);
        this.armor = new DBItem(snapshot.armorID);
        this.shoe = new DBItem(snapshot.shoeID);
        this.cape = new DBItem(snapshot.capeID);
    }

    public DBSnapshot(int match_player_id, int snapshot_type, int event_id, Timestamp timestamp,
            String mainhand_type, String offhand_type, String head_type, String armor_type,
            String shoe_type, String cape_type) {
        // this constructor is deprecated, for use with converting old db format to new
        this.match_player_id = match_player_id;
        this.snapshot_type = (char) snapshot_type;
        this.event_id = event_id;
        this.timestamp = timestamp.toString();
        this.mainhand = new DBItem(mainhand_type);
        this.offhand = new DBItem(offhand_type);
        this.head = new DBItem(head_type);
        this.armor = new DBItem(armor_type);
        this.shoe = new DBItem(shoe_type);
        this.cape = new DBItem(cape_type);
    }

    public String toString() {
        // returns this object as the db fields specified by the snapshot table
        return String.format(
                "('%d', '%d', '%d', '%s', '%s', '%d', '%d', '%s', '%d', '%d', '%s', '%d', '%d', '%s', '%d', '%d', '%s', '%d', '%d', '%s', '%d', '%d')",
                match_player_id, (int) snapshot_type, event_id, timestamp, mainhand.item_type,
                (int) mainhand.item_enchant, (int) mainhand.item_tier, offhand.item_type, (int) offhand.item_enchant,
                (int) offhand.item_tier, head.item_type, (int) head.item_enchant, (int) head.item_tier, armor.item_type,
                (int) armor.item_enchant, (int) armor.item_tier, shoe.item_type, (int) shoe.item_enchant,
                (int) shoe.item_tier, cape.item_type, (int) cape.item_enchant, (int) cape.item_tier);
    }
}