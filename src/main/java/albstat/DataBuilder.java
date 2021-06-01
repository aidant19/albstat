package albstat;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

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

    public String getMatchListJSON(int limit, int offset, String matchType) {
        // primary data retrieval method
        // returns JSON retrieved from albion api of recent matches
        if (limit > 1000) {
            // the api only allows for requests in batches of 1,000 matches
            System.out.println("limit (batch size) cannot be greater than 1000");
            System.exit(0);
        } else if (limit + offset > 10000) {
            // api requests cannot go back further than 10,000 matches
            System.out.println("limit (batch size) + offset cannot be greater than 10000");
            System.exit(0);
        } else {
            System.out.printf("requesting %d matches, beginning at %d\n", limit, offset);
        }
        return APIInterface.getNewMatches(limit, offset, matchType);
    }

    public ArrayList<String> getStoredMatchIDs() {
        // returns previously parsed match ids in database
        System.out.println("retrieving stored match ids");
        ArrayList<String> storedMatchIDs = dbInterface.getParsedMatchIDs();
        System.out.printf("%d stored entries retrieved\n", storedMatchIDs.size());
        return storedMatchIDs;
    }

    public boolean playerCountCheck(JSONHandler jsonHandler) {
        // verifies that the loaded match has the correct amount of players on each team

        // get team 1 player ids
        jsonHandler.loadSubObject("team1Results");
        Set<String> team1Players = jsonHandler.getKeySet();

        // reload match object
        jsonHandler.loadBaseObject();

        // get team 2 player ids
        jsonHandler.loadSubObject("team2Results");
        Set<String> team2Players = jsonHandler.getKeySet();

        // reload match object
        jsonHandler.loadBaseObject();

        // verifies that both teams contain 5 players
        boolean matchCheck = true;
        if (team1Players.size() != 5) {
            matchCheck = false;
        } else if (team2Players.size() != 5) {
            matchCheck = false;
        }

        // if match passes the check, proceed, else return an error
        if (matchCheck) {
            return true;
        } else {
            System.out.printf("player count discrepancy @ match %s\n", jsonHandler.getValue("MatchId"));
            System.out.printf("review at: https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague/%s\n",
                    jsonHandler.getValue("MatchId"));
            return false;
        }
    }

    public ArrayList<Match> findMatchesToParse(String matchListJSON) {
        // method for parsing the returned match list from the api
        ArrayList<Match> matchList = new ArrayList<>();

        // get previously parsed match ids to skip
        ArrayList<String> storedMatchIDs = getStoredMatchIDs();

        // initialize jsonHandler which reads data retrieved from api
        JSONHandler jsonHandler = new JSONHandler();

        // begin data processing
        System.out.println("finding earliest unparsed match...");

        // matches loaded in reverse order (oldest-latest)
        if (jsonHandler.loadArrayReverse(matchListJSON)) {
            do {
                // performs a player count check
                if (playerCountCheck(jsonHandler)) {
                    // initialize match object and map values
                    Match match = new Match();
                    jsonHandler.mapTo(match);
                    // check if the match has already been parsed
                    if (!(storedMatchIDs.contains(match.get("matchID")))) {
                        // level 1 matches are just added immediately (they have no kills/deaths)
                        if (match.get("level").equals("1")) {
                            // addLevel1MatchToDB(match);
                        } else {
                            // add the non-level1 matches to the list of matches to parse
                            matchList.add(match);
                        }
                    }
                }
            } while (jsonHandler.loadPreviousObject());
        }
        return matchList;
    }

    public void retrieveEventData(ArrayList<Match> matchList) {
        // retrieves events for matches (kills/deaths)
        // inputted matches should already have associated match data from the api
        // this method simply retrieves all of the events it can find for the match
        // this method also builds all of the snapshots from those events
        int parseCount = 0;
        int parseTotal = matchList.size();
        for (Match match : matchList) {
            reportStatus(String.format("matches parsed: %d/%d", parseCount, parseTotal), false, false);
            getEvents(match);
            parseCount++;
        }
        reportStatus(String.format("matches parsed: %d", parseCount, parseTotal), false, true);
    }

    public void getEvents(Match match) {
        // retrieves the event history for all player combinations then finds events
        // which occurred in the timeframe of the match
        ArrayList<CompletableFuture<String>> eventJSONList = new ArrayList<>();
        JSONHandler eventHandler = new JSONHandler();
        int requestedCount = 0;
        int receivedCount = 0;

        for (int i = 0; i < 5; i++) {
            for (int j = 5; j < 10; j++) {
                String player1ID = match.getSubMap(i).get("playerID");
                String player2ID = match.getSubMap(j).get("playerID");
                eventJSONList.add(CompletableFuture.supplyAsync(new APIInterface.EventRequest(player1ID, player2ID)));
                requestedCount++;
                reportStatus(String.format("events received/requested: %d/%d", receivedCount, requestedCount), false,
                        false);
            }
        }

        for (CompletableFuture<String> eventJSONResponse : eventJSONList) {
            try {
                String eventJSON = eventJSONResponse.get();
                receivedCount++;
                reportStatus(String.format("events received/requested: %d/%d", receivedCount, requestedCount), false,
                        false);
                if (eventHandler.loadArray(eventJSON)) {
                    do {
                        if (new Timestamp(eventHandler.getValue("TimeStamp")).isBetween(match.get("timeStart"),
                                match.get("timeEnd"))) {
                            getSnapshots(match, eventHandler);
                        }
                    } while (eventHandler.loadNextObject());
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        reportStatus(String.format("events received/requested: %d/%d", receivedCount, requestedCount), false, false);
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

    public boolean verifyKillsDeaths(Match match) {
        String[] playerIDs = new String[10];
        int[] killsReported = new int[10];
        int[] killsFound = new int[10];
        // retrieves match reported kills
        for (int i = 0; i < 10; i++) {
            killsReported[i] = ((Player) match.getSubMap(i)).kills;
            playerIDs[i] = match.getSubMap(i).get("playerID");
        }
        // retrieves kills found in snapshots
        for (int i = 10; i < match.subMaps.size(); i++) {
            if (Integer.parseInt(match.getSubMap(i).get("snapshotType")) == 1) {
                for (int j = 0; j < 10; j++) {
                    if (((Snapshot) match.getSubMap(i)).playerID.compareTo(playerIDs[j]) == 0) {
                        killsFound[j]++;
                    }
                }
            }
        }
        // verifies that the reported and found kills are the same
        for (int i = 0; i < 10; i++) {
            if (killsReported[i] != killsFound[i]) {
                System.out.printf("kill discrepancy @ match: %s\n", match.get("matchID"));
                System.out.printf("player: %s reported: %d found %d\n", playerIDs[i], killsReported[i], killsFound[i]);
                return false;
            }
        }
        return true;
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
        String matchType = APIInterface.CL5Type;
        String matchListJSON = builder.getMatchListJSON(1000, 9000, matchType);
        ArrayList<Match> matchList = builder.findMatchesToParse(matchListJSON);
        ArrayList<Match> errorList = new ArrayList<>();
        builder.retrieveEventData(matchList);
        for (int i = 0; i < matchList.size(); i++) {
            if (!builder.verifyKillsDeaths(matchList.get(i))) {
                errorList.add(matchList.remove(i));
            } else {
                builder.addMatchToDB(matchList.get(i));
            }
        }
        //builder.getPlayerNames(new JSONHandler());
    }
}