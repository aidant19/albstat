package albstat;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

// aidan tokarski
// 6/14/20
// a class for bulding and handling data from the albion api as outlined by the albstat database

public class DataBuilder {

    // interface instances
    private DBInterface dbInterface;

    // fields
    public int matchesToParse;
    public int matchesParsed;
    public int duplicates;
    public String lastReport;

    public DataBuilder() {
        this.dbInterface = new DBInterface();
        this.matchesParsed = 0;
        this.matchesToParse = 0;
        this.duplicates = 0;
    }

    public void getNewMatches(int offset, String matchType) {
        // primary data retrieval method
        if (offset > 999) {
            // the api only allows for requests of the last 1,000 matches
            System.out.println("offset cannot be greater than 999");
            System.exit(0);
        } else {
            int counter = 0;
            System.out.println("retrieving parsed match ids");
            ArrayList<String> parsedMatchIDs = dbInterface.getParsedMatchIDs();
            System.out.printf("%d entries found\n", parsedMatchIDs.size());
            System.out.printf("requesting %d matches\n", offset);
            String matchJSON = APIInterface.getNewMatches(0, offset, matchType);
            JSONHandler jsonHandler = new JSONHandler();
            System.out.println("finding earliest unparsed match...");
            if (jsonHandler.loadArrayReverse(matchJSON)) {
                do {
                    reportStatus(String.format("matches parsed: %d", counter), false, false);
                    if (initVerify(jsonHandler)) {
                        Match match = new Match();
                        jsonHandler.mapTo(match);
                        if (!(parsedMatchIDs.contains(match.get("matchID")))) {
                            // adding the new match id to prevent api changes from
                            // causing requests to overlap
                            parsedMatchIDs.add(match.get("matchID"));
                            synchronized (dbInterface) {
                                if (match.get("level").compareTo("1") == 0) {
                                    addLevel1MatchToDB(match);
                                } else {
                                    new MatchRequest(match).run();
                                }
                            }
                        }
                    }
                    counter++;
                } while (jsonHandler.loadPreviousObject());
            }
        }
        reportStatus(String.format("matches parsed: %d", offset), false, true);
    }

    public class MatchRequest implements Runnable {

        private Match match;

        public MatchRequest(Match match) {
            this.match = match;
        }

        public void run() {
            getEvents(match);
            if (DataVerifier.verify(match)) {
                addMatchToDB(match);
            }
        }
    }

    public boolean initVerify(JSONHandler jsonHandler) {
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
        ArrayList<CompletableFuture<String>> eventQueries = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            for (int j = 5; j < 10; j++) {
                String player1ID = match.getSubMap(i).get("playerID");
                String player2ID = match.getSubMap(j).get("playerID");
                // create new event request
                CompletableFuture<String> eventQuery = CompletableFuture
                        .supplyAsync(new EventRequest(player1ID, player2ID));
                // add event query to list of queries
                eventQueries.add(eventQuery);
            }
        }
        try {
            for (CompletableFuture<String> eventQuery : eventQueries) {
                if (eventHandler.loadArray(eventQuery.get())) {
                    do {
                        if (new Timestamp(eventHandler.getValue("TimeStamp")).isBetween(match.get("timeStart"),
                                match.get("timeEnd"))) {
                            getSnapshots(match, eventHandler);
                        }
                    } while (eventHandler.loadNextObject());
                }
            }
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public class EventRequest implements Supplier<String> {

        private String player1ID;
        private String player2ID;

        public EventRequest(String player1ID, String player2ID) {
            this.player1ID = player1ID;
            this.player2ID = player2ID;
        }

        public String get() {
            return APIInterface.getEventHistory(player1ID, player2ID);
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
        // int nextID = dbInterface.getNextMatchPlayerID();
        int nextID = 0;
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

    public void getPlayerNames(JSONHandler jsonHandler) {
        ArrayList<String> UnnamedIDs = dbInterface.getUnnamedPlayerIDs();
        for (String id : UnnamedIDs) {
            jsonHandler.loadObject(APIInterface.getPlayer(id));
            dbInterface.addPlayer(id, jsonHandler.getValue("Name"));
        }
    }

    public void updateTimeStamps() {
        // for updating timestamps which were truncated
        ArrayList<String> eventIDs = dbInterface.getUniqueEvents();
    }

    public void reportStatus(String status, boolean error, boolean last) {
        String newReport;
        if (lastReport == null) {
            newReport = status;
        } else if (error && lastReport.contains(status)) {
            if (lastReport.contains(": ")) {
                int statusCount = Integer.parseInt(lastReport.split(": ")[1]);
                newReport = String.format("%s: %d", status, statusCount + 1);
            } else {
                newReport = String.format("%s: 2", status);
            }
        } else if (!error) {
            if (lastReport.contains(status.split(":")[0])) {
                newReport = status;
            } else {
                System.out.println(lastReport);
                newReport = status;
            }
        } else {
            System.out.println(lastReport);
            newReport = status;
        }
        if (last) {
            System.out.println(newReport);
        } else {
            System.out.print(newReport + "\r");
        }
        lastReport = newReport;
    }

    public static void main(String[] args) throws Exception {
        DataBuilder builder = new DataBuilder();
        // offset, batchSize, total
        String matchType = APIInterface.CL20Type;
        builder.getNewMatches(999, matchType);
        // builder.getPlayerNames(new JSONHandler());
    }
}