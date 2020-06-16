package albstat;

import java.sql.SQLException;

// aidan tokarski
// 6/3/20
// albstat driver module

import java.util.ArrayList;

public class Driver {

    public static void getNewMatches(int limit, int offset, int total) throws Exception {
        for (; limit + offset <= total; offset += limit) {
            APIInterface apiInterface = new APIInterface();
            DBInterface dbInterface = new DBInterface();
            ArrayList<String> matchIDs = dbInterface.getParsedMatchIDs();
            /* ArrayList<Match> matchList = apiInterface.getNewMatches(offset, limit, matchIDs);
            try {
                dbInterface.addMatches(matchList);
                dbInterface.commitClose();
            } catch (SQLException e) {
                System.out.println(e);
                dbInterface.rollbackClose();
            } */
        }
    }

    public static void getPlayerNames() throws Exception {
        APIInterface apiInterface = new APIInterface();
        DBInterface dbInterface = new DBInterface();
        ArrayList<DBPlayer> players = dbInterface.getUnnamedPlayers();
        int parsed = 0;
        for (DBPlayer player : players) {
            player.setName(apiInterface.getPlayerName(player.player_id));
            parsed++;
            apiInterface.reportStatus(String.format("progress: %d / %d (%.2f%%)", parsed, players.size(),
                    100 * parsed / (double) players.size()), false, false);
            try {
                dbInterface.addPlayer(player);
                dbInterface.commit();
            } catch (SQLException e) {
                System.out.println(e);
                dbInterface.rollback();
            }
        }
        apiInterface.reportStatus(String.format("progress: %d / %d (%.2f%%)", parsed, players.size(),
                100 * parsed / (double) players.size()), false, true);
    }

    public static void getItemNames() throws Exception {

        String[] item_classes = { "mainhand", "offhand", "head", "armor", "shoe", "cape" };
        for (String item_class : item_classes) {
            APIInterface apiInterface = new APIInterface();
            DBInterface dbInterface = new DBInterface();
            int parsed = 0;
            ArrayList<DBItem> items = dbInterface.getUnnamedItems(item_class);
            for (DBItem item : items) {
                item.setName(apiInterface.getItemName(item.item_type));
                parsed++;
                apiInterface.reportStatus(String.format("progress: %d / %d (%.2f%%)", parsed, items.size(),
                        100 * parsed / (double) items.size()), false, false);
                try {
                    dbInterface.addItem(item);
                    dbInterface.commit();
                } catch (SQLException e) {
                    System.out.println(e);
                    dbInterface.rollback();
                }
            }
            apiInterface.reportStatus(String.format("progress: %d / %d (%.2f%%)", parsed, items.size(),
                    100 * parsed / (double) items.size()), false, true);
        }
    }

    public static void updateSnapshots() throws Exception {
        DBInterface dbInterface = new DBInterface();
        dbInterface.updateSnapshots();
        dbInterface.commitClose();
    }

    public static void main(String[] args) throws Exception {
        DataBuilder builder = new DataBuilder();
        builder.getNewMatches(0, 1, 1);
    }
}