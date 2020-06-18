package albstat;

// aidan tokarski
// 6/17/20
// a class for containing snapshot

import org.apache.commons.lang3.ArrayUtils;

public class SnapshotNew extends JSONDefinedMap {

    // defines the amount of fields in this Map
    private static final int FIELDS = 22;

    // map specific fields
    private int snapshotType;
    private int playerNumber;
    private String playerID;

    // reference strings
    protected static String[] itemClass = { "MainHand", "OffHand", "Head", "Armor", "Shoes", "Cape" };
    protected static String[] itemField = { "Type", "Enchant", "Tier" };

    public SnapshotNew(int snapshotType, int playerNumber) {
        super(FIELDS);
        this.snapshotType = snapshotType;
        this.playerNumber = playerNumber;
        setKeys();
        setMapping();
    }

    public String put(String key, String newValue) {
        // custom put notifies that the playerID has been updated
        if (key.contains("TypeFull")) {
            addItem(key.split(" ")[0], newValue);
        } else {
            for (int i = 0; i < size; i++) {
                if (keys[i].compareTo(key) == 0) {
                    if (keys[i].compareTo("playerID") == 0) {
                        playerID = newValue;
                    } else {
                        values[i] = newValue;
                    }
                }
            }
        }
        return null;
    }

    private void addItem(String itemClass, String itemStringFull) {
        // adds values by parsing an full item type
        if (itemStringFull == null) {
            put(itemClass + "Type", null);
            put(itemClass + "Enchant", "0");
            put(itemClass + "Tier", "0");
        } else if (itemStringFull.contains("@")) {
            // indicates item is enchanted
            put(itemClass + "Enchant", itemStringFull.substring(itemStringFull.length() - 1));
            put(itemClass + "Tier", itemStringFull.substring(1, 2));
            put(itemClass + "Type", itemStringFull.substring(3, itemStringFull.length() - 2));
        } else {
            // indicates item is not enchanted
            put(itemClass + "Enchant", "0");
            put(itemClass + "Tier", itemStringFull.substring(1, 2));
            put(itemClass + "Type", itemStringFull.substring(3));
        }
    }

    protected void setKeys() {
        keys[0] = "MatchPlayerID";
        keys[1] = "snapshotType";
        values[1] = String.valueOf(snapshotType);
        keys[2] = "eventID";
        keys[3] = "timestamp";
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                keys[4 + (j + (i * 3))] = itemClass[i] + itemField[j];
            }
        }
    }

    protected void setMapping() {
        // maps fields from the api (JSON) to keys or fields in the map
        this.jsonMap = new JSONMap(9);
        jsonMap.add(new String[] { "EventId" }, keys[2]);
        jsonMap.add(new String[] { "TimeStamp" }, keys[3]);
        String[] playerAddress;
        switch (snapshotType) {
            case 1:
                playerAddress = new String[] { "Killer" };
                break;
            case 2:
                playerAddress = new String[] { "Victim" };
                break;
            case 3:
                playerAddress = new String[] { "GroupMembers", String.format(":%d", playerNumber) };
                break;
            case 4:
                playerAddress = new String[] { "Participants", String.format(":%d", playerNumber) };
                break;
            default:
                playerAddress = new String[] { "" };
                System.out.println("no snapshot type defined");
                System.out.println(Thread.currentThread().getStackTrace());
                System.exit(0);
        }
        jsonMap.add(ArrayUtils.addAll(playerAddress, "Id"), "playerID");
        String[] equipmentAddress = ArrayUtils.addAll(playerAddress, "Equipment");
        for (int i = 0; i < 6; i++) {
            jsonMap.add(ArrayUtils.addAll(equipmentAddress, itemClass[i], "Type"), itemClass[i] + " TypeFull");
        }
    }
}