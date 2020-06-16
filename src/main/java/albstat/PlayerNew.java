package albstat;

// aidan tokarski
// 6/16/20
// a class for containing match data

import java.util.Map;

public class PlayerNew extends JSONDefinedMap {

    // defines the amount of fields in this Map
    private static final int FIELDS = 5;

    private int keyNumber;

    public PlayerNew(int keyNumber) {
        super(FIELDS);
    }

    public PlayerNew(Map<String, String> map, int keyNumber) {
        super(map, FIELDS);
    }

    public String put(String key, String newValue) {
        // custom put notifies that the playerID has been updated
        for (int i = 0; i < size; i++) {
            if (keys[i].compareTo(key) == 0) {
                values[i] = newValue;
                if (i == 0) {
                    values.notifyAll();
                }
            }
        }
        return null;
    }

    protected void setMapping() {
        // maps fields from the api (JSON) to fields in the map
        this.jsonMap = new JSONMap(FIELDS);
        keys[2] = "team";
        values[2] = (keyNumber < 5) ? "1" : "2";
        String resultsString = String.format("team%sResults", values[2]);
        jsonMap.put(new String[] { resultsString, String.format("keySet%d", keyNumber) }, keys[0] = "playerID");
        synchronized (values) {
            try {
                while (values[0] == null) {
                    values.wait();
                }
                jsonMap.put(new String[] { resultsString, values[0], "Name" }, keys[1] = "playerName");
                jsonMap.put(new String[] { resultsString, values[0], "Kills" }, keys[3] = "kills");
                jsonMap.put(new String[] { resultsString, values[0], "Deaths" }, keys[4] = "deaths");
            } catch (InterruptedException e) {
                System.out.println("player mapping interrupted");
                System.out.println(e);
                System.exit(0);
            }
        }
    }
}