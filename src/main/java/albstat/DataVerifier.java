package albstat;

// aidan tokarski
// 6/10/20
// a class for verifying reported and cross-referenced data

import java.util.ArrayList;

public class DataVerifier {

    public static int verifyData(ArrayList<Event> events, MatchResult results) {
        // verifies match data by comparing the match results retrieved from the api
        // with results created from retrieved events
        MatchResult resultsReported = new MatchResult(results.matchID);
        resultsReported.setPlayers(getTeam1(results), getTeam2(results));
        for (Event event : events) {
            resultsReported.updateResult(event.player1ID, true);
            resultsReported.updateResult(event.player2ID, false);
        }
        return results.compare(resultsReported);
    }

    public static ArrayList<String> getTeam1(MatchResult results) {
        ArrayList<String> playerIDs = new ArrayList<String>();
        for (PlayerResult result : results.team1Results) {
            playerIDs.add(result.playerID);
        }
        return playerIDs;
    }

    public static ArrayList<String> getTeam2(MatchResult results) {
        ArrayList<String> playerIDs = new ArrayList<String>();
        for (PlayerResult result : results.team2Results) {
            playerIDs.add(result.playerID);
        }
        return playerIDs;
    }
}