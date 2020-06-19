package albstat;

import java.util.ArrayList;

// aidan tokarski
// 6/14/20
// a class for bulding and handling data from the albion api as outlined by the albstat database

public class DataBuilder {

    // interface instances
    private APIInterface apiInterface;
    private DBInterface dbInterface;
    private JSONHandler jsonHandler;
    private APIInterfaceCached apiInterfaceCached;

    public DataBuilder() {
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
            String matchJSON = apiInterface.getNewMatches(offset, batchSize);
            jsonHandler.loadArray(matchJSON);
            System.out.println("matches retrieved, parsing matches");
            for (int i = 0; i < batchSize; i++) {
                MatchNew match = new MatchNew();
                jsonHandler.mapTo(match);
                if (match.get("level").compareTo("1") == 0) {
                    System.out.println("level 1 match found");
                } else {
                    getEvents(match);
                    addMatchToDB(match);
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
                String eventJSON = apiInterface.getEventHistory(match.getSubMap(i).get("playerID"),
                        match.getSubMap(j).get("playerID"));
                if (eventHandler.loadArray(eventJSON)) {
                    do {
                        if (new Timestamp(eventHandler.getValue("TimeStamp")).isBetween(match.get("timeStart"),
                                match.get("timeEnd"))) {
                            getSnapshots(match, eventHandler);
                        }
                    } while (eventHandler.loadNextObject());
                }
            }
        }
    }

    public void getSnapshots(MatchNew match, JSONHandler eventHandler) {
        int nParticipants = Integer.parseInt(eventHandler.getValue("numberOfParticipants"));
        int nGroupMembers = Integer.parseInt(eventHandler.getValue("groupMemberCount"));
        // create killer snapshot
        SnapshotNew nextSnap = new SnapshotNew(1, 0);
        eventHandler.mapTo(nextSnap);
        match.addSubMapping(nextSnap);
        // create victim snapshot
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
        }
    }

    public void addMatchToDB(MatchNew match) {
        int nextID = dbInterface.getNextMatchPlayerID();
        // sub maps 0-9 reserved for players
        // sub maps 10+ reserved for snapshots
        for (int i = 0; i < 10; i++) {
            String playerID = match.getSubMap(i).get("playerID");
            for (int j = 10; j < match.subMaps.size(); j++) {
                match.getSubMap(j).put(playerID, String.valueOf(nextID + i));
            }
        }
        dbInterface.addMatch(match);
    }

    public static void main(String[] args) throws Exception {
        DataBuilder builder = new DataBuilder();
        builder.getNewMatches(0, 1, 1);
    }
}