package albstat;

// aidan tokarski
// 6/12/20
// for interfacing api requests and processing with the database

import java.sql.*;
import java.util.ArrayList;

public class DBInterface {

    private Connection con;
    // note: the connection is unique to each DBInterface instance
    // multi-threading requires multiple DBInterfaces

    public DBInterface() throws SQLException, SQLTimeoutException {
        // by default, this constructor creates a connection requiring commits
        this.con = connect();
        this.con.setAutoCommit(false);
    }

    public static Connection connect() throws SQLException, SQLTimeoutException {
        DBCredentials credentials = new DBCredentials();
        System.out.println("database connection established");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/albstat", credentials.getUser(),
                credentials.getPass());
    }

    public void rollbackClose() throws SQLException {
        // note that a close would accomplish the same goal;
        // the redundancy is used as a reminder
        con.rollback();
        con.close();
    }

    public void commitClose() throws SQLException {
        con.commit();
        con.close();
    }

    public void rollback() throws SQLException {
        con.rollback();
    }

    public void commit() throws SQLException {
        con.commit();
    }

    public ArrayList<String> getParsedMatchIDs() throws SQLException {
        // returns previously parsed matches for use with new api interface instances
        ArrayList<String> matchIDs = new ArrayList<String>();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT `match_id` FROM `match`");
        while (rs.next()) {
            matchIDs.add(rs.getString(1));
        }
        return matchIDs;
    }

    public void addMatches(ArrayList<Match> matchList) throws SQLException {
        for (Match match : matchList) {
            addMatch(match);
        }
    }

    public void addMatch(Match match) throws SQLException {
        // adds matches to the db with data retrieved from the api
        // note: the order in which data is added is in increasing complexity
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM `match_player` ORDER BY `match_player_id` DESC LIMIT 1");
        // retrieves the current highest match_player_id (the latest)
        int next_id;
        if (rs.next()) {
            next_id = rs.getInt(1) + 1;
        } else {
            next_id = 1;
        }
        DBMatch dbMatch = new DBMatch(match, next_id);
        // note: match_players are incrementally assigned ids from next_id
        stmt.executeUpdate(String.format(
                "INSERT INTO `match` (`match_id`, `match_level`, `match_winner`, `match_time_start`, `match_time_end`) VALUES %s",
                dbMatch));
        addMatchPlayers(dbMatch, stmt);
        addSnapshots(dbMatch, stmt);
    }

    public void addMatchPlayers(DBMatch dbMatch, Statement stmt) throws SQLException {
        for (DBMatchPlayer match_player : dbMatch.dbMatchPlayers) {
            stmt.executeUpdate(String.format(
                    "INSERT INTO `match_player` (`match_player_id`, `player_id`, `match_id`, `team`) VALUES %s",
                    match_player));
        }
    }

    public void addSnapshots(DBMatch dbMatch, Statement stmt) throws SQLException {
        for (DBSnapshot snapshot : dbMatch.dbSnapshots) {
            stmt.executeUpdate(String.format(
                    "INSERT INTO `snapshot` (`match_player_id`, `snapshot_type`, `event_id`, `timestamp`, `mainhand_type`, `mainhand_enchant`, `mainhand_tier`, `offhand_type`, `offhand_enchant`, `offhand_tier`, `head_type`, `head_enchant`, `head_tier`, `armor_type`, `armor_enchant`, `armor_tier`, `shoe_type`, `shoe_enchant`, `shoe_tier`, `cape_type`, `cape_enchant`, `cape_tier`) VALUES %s",
                    snapshot));
        }
    }

    public ArrayList<DBPlayer> getUnnamedPlayers() throws SQLException {
        // returns a list of players who appear in the snapshot table, but not the player table
        // for use with the api in retrieving player names
        ArrayList<DBPlayer> players = new ArrayList<>();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(
                "SELECT COUNT(`player_id`) FROM `match_player` WHERE `player_id` NOT IN (SELECT `player_id` FROM `player`)");
        if (rs.next()) {
            int parsed = 0;
            int entries = rs.getInt(1);
            System.out.printf("%d entries to check\n", entries);
            rs = stmt.executeQuery(
                    "SELECT `player_id` FROM `match_player` WHERE `player_id` NOT IN (SELECT `player_id` FROM `player`) ORDER BY `player_id`");
            String lastAdded = "";
            while (rs.next()) {
                String player = rs.getString(1);
                if (player.compareTo(lastAdded) != 0) {
                    players.add(new DBPlayer(player));
                    lastAdded = player;
                }
                parsed++;
                System.out.printf("progress: %d / %d (%.2f%%)\r", parsed, entries, 100 * parsed / (double) entries);
            }
            System.out.printf("progress: %d / %d (%.2f%%)\n", parsed, entries, 100 * parsed / (double) entries);
        }
        System.out.printf("unique players to parse: %d\n", players.size());
        return players;
    }

    public void addPlayer(DBPlayer player) throws SQLException {
        Statement stmt = con.createStatement();
        stmt.executeUpdate(String.format("INSERT INTO `player` (`player_id`, `player_name`) VALUES %s", player));
    }

    public ArrayList<DBItem> getUnnamedItems(String item_class) throws SQLException {
        // returns a list of items which appear in the snapshot table, but not any item table
        // for use with the api in retrieving item names
        ArrayList<DBItem> items = new ArrayList<>();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(String.format(
                "SELECT COUNT(`%s_type`) FROM `snapshot` WHERE `%s_type` != 'null' AND `%s_type` NOT IN (SELECT `%s_type` FROM `%s`)",
                item_class, item_class, item_class, item_class, item_class));
        if (rs.next()) {
            int parsed = 0;
            int entries = rs.getInt(1);
            rs = stmt.executeQuery(String.format(
                    "SELECT `%s_type` FROM `snapshot` WHERE `%s_type` != 'null' AND `%s_type` NOT IN (SELECT `%s_type` FROM `%s`) ORDER BY `%s_type`",
                    item_class, item_class, item_class, item_class, item_class, item_class));
            String lastAdded = "";
            while (rs.next()) {
                String item = rs.getString(1);
                if (item.compareTo(lastAdded) != 0) {
                    items.add(new DBItem(item, item_class));
                    lastAdded = item;
                }
                parsed++;
                System.out.printf("progress: %d / %d (%.2f%%)\r", parsed, entries, 100 * parsed / (double) entries);
            }
            System.out.printf("progress: %d / %d (%.2f%%)\n", parsed, entries, 100 * parsed / (double) entries);
        }
        System.out.printf("unique items to parse: %d\n", items.size());
        return items;
    }

    public void addItem(DBItem item) throws SQLException {
        // note: this requires the item_class to be known
        Statement stmt = con.createStatement();
        stmt.executeUpdate(String.format("INSERT INTO `%s` (`%s_type`, `%s_name`) VALUES %s", item.item_class,
                item.item_class, item.item_class, item));
    }

    public void updateSnapshots() throws SQLException {
        // this function is deprecated, for converting snapshots from the old version to the new
        Statement stmt = con.createStatement();
        Statement stmt2 = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM snapshot_old");
        if (rs.next()) {
            int parsed = 0;
            int entries = rs.getInt(1);
            rs = stmt.executeQuery("SELECT * FROM snapshot_old");
            while (rs.next()) {
                int match_player_id = rs.getInt(1);
                int snapshot_type = rs.getInt(2);
                int event_id = rs.getInt(3);
                Timestamp timestamp = new Timestamp(rs.getString(4));
                String mainhand_type = rs.getString(5);
                String offhand_type = rs.getString(6);
                String head_type = rs.getString(7);
                String armor_type = rs.getString(8);
                String shoe_type = rs.getString(9);
                String cape_type = rs.getString(10);
                stmt2.executeUpdate(String.format(
                        "INSERT INTO `snapshot` (`match_player_id`, `snapshot_type`, `event_id`, `timestamp`, `mainhand_type`, `mainhand_enchant`, `mainhand_tier`, `offhand_type`, `offhand_enchant`, `offhand_tier`, `head_type`, `head_enchant`, `head_tier`, `armor_type`, `armor_enchant`, `armor_tier`, `shoe_type`, `shoe_enchant`, `shoe_tier`, `cape_type`, `cape_enchant`, `cape_tier`) VALUES %s",
                        new DBSnapshot(match_player_id, snapshot_type, event_id, timestamp, mainhand_type, offhand_type,
                                head_type, armor_type, shoe_type, cape_type)));
                parsed++;
                System.out.printf("progress: %d / %d (%.2f%%)\r", parsed, entries, 100 * parsed / (double) entries);
                if (parsed % 20000 == 0) {
                    this.commit();
                }
            }
            System.out.printf("progress: %d / %d (%.2f%%)\n", parsed, entries, 100 * parsed / (double) entries);
        }
    }
}