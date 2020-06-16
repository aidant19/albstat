package albstat;

// aidan tokarski
// 6/15/20
// a class for containing match data

import java.util.Map;
import java.util.Set;
import java.util.ArrayList;

public class MatchNew extends JSONDefinedMap {

    // defines the amount of fields in this Map
    private static final int FIELDS = 5;

    // underlying datasets
    public Set<String> team1Players;
    public Set<String> team2Players;
    public ArrayList<Event> events;
    private MatchResult results;

    public MatchNew() {
        super(FIELDS);
    }

    public MatchNew(Map<String, String> map) {
        super(map, FIELDS);
    }

    protected void setMapping(){
        // maps fields from the api (JSON) to fields in the map
        this.jsonMap = new JSONMap(FIELDS);
        jsonMap.put(new String[] {"MatchId"}, keys[0] = "matchID");
        jsonMap.put(new String[] {"crystalLeagueLevel"}, keys[1] = "level");
        jsonMap.put(new String[] {"winner"}, keys[2] = "winner");
        jsonMap.put(new String[] {"startTime"}, keys[3] = "timeStart");
        jsonMap.put(new String[] {"team1Timeline", ":last", "TimeStamp"}, keys[4] = "timeEnd");
    }

    public int verifyData(){
        return DataVerifier.verifyData(events, results);
    }
}