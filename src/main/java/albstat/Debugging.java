package albstat;

import java.util.ArrayList;
import java.util.Map.Entry;

// aidan tokarski
// 6/14/20
// a class for bulding and handling data from the albion api as outlined by the albstat database

public class Debugging {

    // interface instances
    private APIInterface apiInterface;
    private DBInterface dbInterface;
    private JSONHandler jsonHandler;
    private APIInterfaceCached apiInterfaceCached;

    public Debugging() {
        this.apiInterface = new APIInterface();
        this.dbInterface = new DBInterface();
        this.jsonHandler = new JSONHandler();
        this.apiInterfaceCached = new APIInterfaceCached();
    }

    public void getNewMatches(int offset, int batchSize, int total) {
        // primary data retrieval method
        if (offset + total > 10000) {
            // the api only allows for requests of the last 10,000 matches
            System.out.println("offset + total cannot be greater than 10000");
            System.exit(0);
        } else {
            System.out.println("retrieving parsed match ids");
            ArrayList<String> parsedMatchIDs = dbInterface.getParsedMatchIDs();
            System.out.printf("%d entries found\n", parsedMatchIDs.size());
            System.out.printf("requesting %d matches, offset %d from api\n", batchSize, offset);
            String matchJSON = apiInterfaceCached.getNewMatches(offset, batchSize);
            jsonHandler.loadArray(matchJSON);
            System.out.println("matches retrieved, parsing matches");
            ArrayList<MatchNew> matchList = new ArrayList<>();
            ArrayList<MatchNew> level1List = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                MatchNew match = new MatchNew();
                jsonHandler.mapTo(match);
                if (match.get("level").compareTo("1") == 0) {
                    level1List.add(match);
                } else {
                    getEvents(match);
                    matchList.add(match);
                }
                System.out.println(match);
                for (JSONDefinedMap subMap : match.getSubMaps()) {
                    System.out.println(subMap);
                }
                jsonHandler.loadNextObject();
            }
        }
    }

    public void getEvents(MatchNew match) {
        // retrieves the event history for all player combinations then finds events
        // which occurred in the timeframe of the match
        JSONHandler eventHandler = new JSONHandler();
        for (int i = 0; i < 5; i++) {
            for (int j = 5; j < 10; j++) {
                String eventJSON = apiInterfaceCached.getEventHistory(match.getSubMap(i).get("playerID"),
                        match.getSubMap(j).get("playerID"));
                if (eventHandler.loadArray(eventJSON)) {
                    do {
                        if (new Timestamp(eventHandler.getValue("TimeStamp")).isBetween(match.get("timeStart"),
                                match.get("timeEnd"))) {
                            addSnapshots(match, eventHandler);
                            return;
                        }
                    } while (eventHandler.loadNextObject());
                }
            }
        }
    }

    public void addSnapshots(MatchNew match, JSONHandler eventHandler) {
        int nParticipants = Integer.parseInt(eventHandler.getValue("numberOfParticipants"));
        int nGroupMembers = Integer.parseInt(eventHandler.getValue("groupMemberCount"));
        // create killer snapshot
        SnapshotNew nextSnap = new SnapshotNew(1, 0);
        eventHandler.mapTo(nextSnap);
        match.addSubMapping(nextSnap);
/*         // create victim snapshot
        nextSnap = new SnapshotNew(2, 0);
        eventHandler.mapTo(nextSnap);
        match.addSubMapping(nextSnap);
        // create group member snapshots
        for (int i = 0; i < nGroupMembers; i++) {
            nextSnap = new SnapshotNew(3, i);
            eventHandler.mapTo(nextSnap);
            match.addSubMapping(nextSnap);
        }
        // create participant snapshots
        for (int i = 0; i < nParticipants; i++) {
            nextSnap = new SnapshotNew(4, i);
            eventHandler.mapTo(nextSnap);
            match.addSubMapping(nextSnap);
        } */
    }

    public static void main(String[] args) throws Exception {
        JSONHandler jsonHandler = new JSONHandler();
        APIInterfaceCached apiInterface = new APIInterfaceCached();
        String eventJSON = apiInterface.getEventHistory("3N4psRbsRy-7Kyy8qfAEHw", "cENkabV8RteTCxAHr1YLwg");
        SnapshotNew newSnap = new SnapshotNew(1, 0);
        JSONMap snapMap = newSnap.getJSONMap();
        jsonHandler.loadArray(eventJSON);
        for (Entry entry : snapMap.entrySet()) {
            System.out.println(entry.getKey());
        }
    }
}