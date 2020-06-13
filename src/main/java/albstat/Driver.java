package albstat;

import java.sql.SQLException;

// aidan tokarski
// 6/3/20
// albstat driver module

import java.util.ArrayList;

public class Driver {

    public static void getNewMatches() throws Exception {
        int limit = 10;
        int offset = 0;
        for (; limit + offset < 10000; offset += limit) {
            APIInterface apiInterface = new APIInterface();
            DBInterface dbInterface = new DBInterface();
            ArrayList<String> matchIDs = dbInterface.getParsedMatchIDs();
            ArrayList<Match> matchList = apiInterface.getMatches(offset, limit, matchIDs);
            try {
                dbInterface.addMatches(matchList);
                dbInterface.close();
            } catch (SQLException e) {
                System.out.println(e);
                dbInterface.uncommit();
            }
        }
    }

    public static void getPlayerNames() throws Exception {
        APIInterface apiInterface = new APIInterface();
        DBInterface dbInterface = new DBInterface();
        ArrayList<DBPlayer> players = dbInterface.getUniqueMatchPlayers();
        int parsed = 0;
        for (DBPlayer player : players) {
            player.setName(apiInterface.getPlayerName(player.player_id));
            parsed++;
            apiInterface.reportStatus(String.format("progress: %d / %d (%.2f%%)", parsed, players.size(), 100 * parsed / (double) players.size()), false, false);
            try{
                dbInterface.addPlayer(player);
                dbInterface.commit();
            } catch (SQLException e) {
                System.out.println(e);
                dbInterface.rollback();
            }
        }
        apiInterface.reportStatus(String.format("progress: %d / %d (%.2f%%)", parsed, players.size(), 100 * parsed / (double) players.size()), false, false);
    }

    public static void main(String[] args) throws Exception {
        getNewMatches();
    }
}