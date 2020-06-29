package albstat;

// aidan tokarski
// 6/12/20
// for interfacing api requests and processing with the database

// modified by:
// jordan williams
// 6/29/20
// + automatic snapshot weighing

import java.sql.*;
import java.util.ArrayList;

public class DBInterface {

    private final Connection con;
    // note: the connection is unique to each DBInterface instance
    // multi-threading requires multiple DBInterfaces

    public DBInterface() {
        // by default, this constructor creates a connection requiring commits
        this.con = connect();
    }

    public static Connection connect() {
        // note: the connection is setup to require commits
        final DBCredentials credentials = new DBCredentials();
        Connection con;
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/albstat", credentials.getUser(),
                    credentials.getPass());
            con.setAutoCommit(false);
            System.out.println("database connection established");
            return con;
        } catch (final SQLException e) {
            System.out.println("could not connect to database");
            System.exit(0);
            return null;
        }
    }

    public void rollbackClose() throws SQLException {
        // note that just a close would accomplish the same goal;
        // the redundancy is used as a reminder of both actions occuring
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

    public ArrayList<String> getParsedMatchIDs() {
        // returns previously parsed matches for use with new api interface instances
        final ArrayList<String> matchIDs = new ArrayList<String>();
        try {
            final Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT `match_id` FROM `match`");
            while (rs.next()) {
                matchIDs.add(rs.getString(1));
            }
            rs = stmt.executeQuery("SELECT `match_id` FROM `match1`");
            while (rs.next()) {
                matchIDs.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            System.out.println("error retrieving parsed matches");
            System.out.println(e);
        }
        return matchIDs;
    }

    public int getNextMatchPlayerID() {
        // retrieves the current highest match_player_id (the latest)
        try {
            final Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery("SELECT * FROM `match_player` ORDER BY `match_player_id` DESC LIMIT 1");
            if (rs.next()) {
                return rs.getInt(1) + 1;
            } else {
                return 1;
            }
        } catch (final SQLException e) {
            System.out.println(e);
            System.exit(0);
            return 0;
        }
    }

    public void addMatch(final Match match) {
        // adds a new match to the db
        try {
            final Statement stmt = con.createStatement();
            stmt.executeUpdate(String.format(
                    "INSERT INTO `match` (`match_id`, `match_level`, `match_winner`, `match_time_start`, `match_time_end`) VALUES %s",
                    match
                )
            );
            for (int i = 0; i < 10; i++) {
                addMatchPlayer((Player) match.getSubMap(i));
            }
            for (int i = 10; i < match.subMaps.size(); i++) {
                addSnapshot((Snapshot) match.getSubMap(i));
            }
            commit();

            // Calculate weights for new snapshots.
            weighSnapshots();

        } catch (final SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public void addMatchPlayer(final Player player) throws SQLException {
        final Statement stmt = con.createStatement();
        stmt.executeUpdate(
                String.format("INSERT INTO `match_player` (`player_id`, `match_id`, `team`) VALUES %s", player));
    }

    public void addSnapshot(final Snapshot snapshot) throws SQLException {
        final Statement stmt = con.createStatement();
        stmt.executeUpdate(String.format(
                "INSERT INTO `snapshot` (`match_player_id`, `snapshot_type`, `event_id`, `timestamp`, `mainhand_type`, `mainhand_enchant`, `mainhand_tier`, `offhand_type`, `offhand_enchant`, `offhand_tier`, `head_type`, `head_enchant`, `head_tier`, `armor_type`, `armor_enchant`, `armor_tier`, `shoe_type`, `shoe_enchant`, `shoe_tier`, `cape_type`, `cape_enchant`, `cape_tier`) VALUES %s",
                snapshot));
    }

    public void addLevel1Match(final Match match) {
        try {
            final Statement stmt = con.createStatement();
            stmt.executeUpdate(String.format(
                    "INSERT INTO `match1` (`match_id`, `match_level`, `match_winner`, `match_time_start`, `match_time_end`) VALUES %s",
                    match));
            for (int i = 0; i < 10; i++) {
                addLevel1MatchPlayer((Player) match.getSubMap(i));
            }
            commit();
        } catch (final SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public void addLevel1MatchPlayer(final Player player) throws SQLException {
        final Statement stmt = con.createStatement();
        stmt.executeUpdate(
                String.format("INSERT INTO `match1_player` (`player_id`, `match_id`, `team`) VALUES %s", player));
    }

    public ArrayList<String> getUnnamedPlayerIDs() {
        // for updating players whose names were not retrieved
        final ArrayList<String> unnamedIDs = new ArrayList<>();
        try {
            final Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery(
                    "SELECT player_id FROM match_player WHERE player_id NOT IN (SELECT player_id FROM player) GROUP BY player_id");
            while (rs.next()) {
                unnamedIDs.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return unnamedIDs;
    }

    public void addPlayer(final String playerID, final String playerName){
        try {
        final Statement stmt = con.createStatement();
        stmt.executeUpdate(
                String.format("INSERT INTO `player` (`player_id`, `player_name`) VALUES ('%s','%s')", playerID, playerName));
        commit();
        } catch (final SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public ArrayList<String> getUniqueEvents(){
        // originally created for updating truncated timestamps
        ArrayList<String> eventIDs = new ArrayList<>();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT event_id FROM snapshot GROUP BY event_id");
            while (rs.next()) {
                eventIDs.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return eventIDs;
    }

    public void updateSnapshotTime(String eventID, String timestamp){
        // for updating snapshot timestamps
        // originally created for updating truncated timestamps
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate(
                String.format("UPDATE snapshot SET timestamp = '%s' WHERE event_id = '%s'", timestamp, eventID));
            commit();
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    
    public void weighSnapshots() {
        // Calculates weights for new snapshots in the DB.
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(`snapshot_id`) FROM `snapshot` WHERE `weight` IS null"
            );
            // Count number of new snapshots that are unweighted.
            int numNewSnapshotsUnweighted = rs.getInt(1);

            // Calculate and add weights to snapshot table.
            rs = stmt.executeQuery(
                    "CALL albstat.group_fullbuild();"
            );
            int numNewSnapshotsWeighted = rs.getInt(1);

            // Verify that number of new snapshots weighted == number of new snapshots added to DB.
            assert numNewSnapshotsWeighted == numNewSnapshotsUnweighted: "New snapshots weighted != new snapshots added to DB";

        } catch (AssertionError|SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
}