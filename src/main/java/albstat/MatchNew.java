package albstat;

// aidan tokarski
// 6/15/20
// a class for containing match data

import java.util.Map;
import java.util.ArrayList;

public class MatchNew extends JSONDefinedMap {

    // defines the amount of fields in this Map
    private static final int FIELDS = 7;

    // underlying datasets
    private ArrayList<JSONDefinedMap> subMaps;

    public MatchNew() {
        super(FIELDS);
    }

    public MatchNew(Map<String, String> map) {
        super(map, FIELDS);
    }

    protected void setMapping() {
        // maps fields from the api (JSON) to fields in the map
        this.jsonMap = new JSONMap(FIELDS);
        jsonMap.put(new String[] { "MatchId" }, keys[0] = "matchID");
        jsonMap.put(new String[] { "crystalLeagueLevel" }, keys[1] = "level");
        jsonMap.put(new String[] { "winner" }, keys[2] = "winner");
        jsonMap.put(new String[] { "startTime" }, keys[3] = "timeStart");
        jsonMap.put(new String[] { "team1Timeline", ":last", "TimeStamp" }, keys[4] = "timeEnd");
        jsonMap.put(new String[] { "team1Results", "keySet" }, keys[5] = "team1IDs");
        jsonMap.put(new String[] { "team2Results", "keySet" }, keys[6] = "team2IDs");
        setSubMapping();
    }

    protected void setSubMapping() {
        for (int i = 0; i < 10; i++) {
            subMaps.add(new PlayerNew(i));
        }
    }

    public JSONDefinedMap getSubMap(int index) {
        return subMaps.get(index);
    }

    public ArrayList<JSONDefinedMap> getSubMaps() {
        return subMaps;
    }
}