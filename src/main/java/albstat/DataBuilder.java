package albstat;

import java.util.ArrayList;
import java.util.Set;

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

    public void getNewMatches(int offset) {
        // primary data retrieval method
        if (offset > 9999) {
            // the api only allows for requests of the last 10,000 matches
            System.out.println("offset cannot be greater than 9999");
            System.exit(0);
        } else {
            int counter = 0;
            System.out.println("retrieving parsed match ids");
            ArrayList<String> parsedMatchIDs = dbInterface.getParsedMatchIDs();
            System.out.printf("%d entries found\n", parsedMatchIDs.size());
            System.out.printf("requesting %d matches\n", offset);
            String matchJSON = apiInterface.getNewMatches(0, offset);
            System.out.println("finding earliest unparsed match...");
            if (jsonHandler.loadArrayReverse(matchJSON)) {
                do {
                    apiInterface.reportStatus(String.format("matches parsed: %d", counter), false, false);
                    if (initVerify()) {
                        Match match = new Match();
                        jsonHandler.mapTo(match);
                        if (!(parsedMatchIDs.contains(match.get("matchID")))) {
                            // adding the new match id to prevent api changes from
                            // causing requests to overlap
                            parsedMatchIDs.add(match.get("matchID"));
                            if (match.get("level").compareTo("1") == 0) {
                                addLevel1MatchToDB(match);
                            } else {
                                getEvents(match);
                                if (DataVerifier.verify(match)) {
                                    addMatchToDB(match);
                                }
                            }
                        }
                    }
                    counter++;
                } while (jsonHandler.loadPreviousObject());
            }
        }
        apiInterface.reportStatus(String.format("matches parsed: %d", offset), false, true);
    }

    public boolean initVerify() {
        // verifies that a match has 5 players on each team
        jsonHandler.loadSubObject("team1Results");
        Set<String> team1Players = jsonHandler.getKeySet();
        jsonHandler.loadBaseObject();
        jsonHandler.loadSubObject("team2Results");
        Set<String> team2Players = jsonHandler.getKeySet();
        jsonHandler.loadBaseObject();
        if (DataVerifier.verifyPlayers(team1Players, team2Players)) {
            return true;
        } else {
            System.out.printf("player count discrepancy @ match %s\n", jsonHandler.getValue("MatchId"));
            System.out.printf("review at: https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague/%s",
                    jsonHandler.getValue("MatchId"));
            return false;
        }
    }

    public void getEvents(Match match) {
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

    public void getSnapshots(Match match, JSONHandler eventHandler) {
        int nParticipants = Integer.parseInt(eventHandler.getValue("numberOfParticipants"));
        int nGroupMembers = Integer.parseInt(eventHandler.getValue("groupMemberCount"));
        // create killer snapshot
        Snapshot nextSnap = new Snapshot(1, 0);
        eventHandler.mapTo(nextSnap);
        match.addSubMapping(nextSnap);
        // create victim snapshot
        nextSnap = new Snapshot(2, 0);
        eventHandler.mapTo(nextSnap);
        match.addSubMapping(nextSnap);
        // create group member snapshots
        for (int i = 0; i < nGroupMembers; i++) {
            nextSnap = new Snapshot(3, i);
            eventHandler.mapTo(nextSnap);
            match.addSubMapping(nextSnap);
        }
        // create participant snapshots
        for (int i = 0; i < nParticipants; i++) {
            nextSnap = new Snapshot(4, i);
            eventHandler.mapTo(nextSnap);
            match.addSubMapping(nextSnap);
        }
    }

    public void addMatchToDB(Match match) {
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

    public void addLevel1MatchToDB(Match match) {
        dbInterface.addLevel1Match(match);
    }

    public static void main(String[] args) throws Exception {
        DataBuilder builder = new DataBuilder();
        // offset, batchSize, total
        builder.getNewMatches(6500);
    }
}