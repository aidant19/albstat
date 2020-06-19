package albstat;

// aidan tokarski
// 6/15/20
// a class for containing match data

import java.util.ArrayList;

public class MatchNew extends JSONDefinedMap {

    // defines the amount of fields in this Map
    private static final int FIELDS = 5;

    public MatchNew() {
        super(FIELDS);
        setKeys();
        setMapping();
    }

    protected void setKeys() {
        keys[0] = "matchID";
        keys[1] = "level";
        keys[2] = "winner";
        keys[3] = "timeStart";
        keys[4] = "timeEnd";
    }

    protected void setMapping() {
        // maps fields from the api (JSON) to fields in the map
        this.jsonMap = new JSONMap(FIELDS);
        jsonMap.add(new String[] { "MatchId" }, keys[0]);
        jsonMap.add(new String[] { "crystalLeagueLevel" }, keys[1]);
        jsonMap.add(new String[] { "winner" }, keys[2]);
        jsonMap.add(new String[] { "startTime" }, keys[3]);
        jsonMap.add(new String[] { "team1Timeline", ":last", "TimeStamp" }, keys[4]);
        setSubMapping();
    }

    public String put(String key, String newValue) {
        // custom put converts timestamp format
        for (int i = 0; i < size; i++) {
            if (keys[i].compareTo(key) == 0) {
                if (i == 3 || i == 4) {
                    values[i] = Timestamp.convertString(newValue);
                } else {
                    values[i] = newValue;
                }
            }
        }
        return null;
    }

    protected void setSubMapping() {
        for (int i = 0; i < 10; i++) {
            subMaps.add(new PlayerNew(i));
        }
    }

    public void addSubMapping(JSONDefinedMap m) {
        subMaps.add(m);
    }

    public JSONDefinedMap getSubMap(int index) {
        return subMaps.get(index);
    }

    public ArrayList<JSONDefinedMap> getSubMaps() {
        return subMaps;
    }
}