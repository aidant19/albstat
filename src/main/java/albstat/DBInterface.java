package albstat;

// aidan tokarski
// 6/12/20
// for interfacing api requests and processing with the database

// modified by:
// jordan williams
// 6/29/20
// + automatic snapshot weighing

// util
import java.sql.*;
import java.util.ArrayList;

import org.postgresql.util.PSQLException;

// credential reading
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

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
            con = DriverManager.getConnection("jdbc:postgresql://25.1.197.128:5432/albion", credentials.getUser(),
                    credentials.getPass());
            con.setAutoCommit(false);
            con.setSchema("albstat");
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
        con.setSchema("albstat");
    }

    public void commit() throws SQLException {
        con.commit();
    }

    public ArrayList<String> getParsedMatchIDs() {
        // returns previously parsed matches for use with new api interface instances
        final ArrayList<String> matchIDs = new ArrayList<String>();
        try {
            final Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id FROM match");
            while (rs.next()) {
                matchIDs.add(rs.getString(1));
            }
            /*
             * rs = stmt.executeQuery("SELECT `match_id` FROM `match1`"); while (rs.next())
             * { matchIDs.add(rs.getString(1)); }
             */
        } catch (final SQLException e) {
            System.out.println("error retrieving parsed matches");
            System.out.println(e);
        }
        return matchIDs;
    }

    public int getNextMatchPlayerID() {
        // retrieves the current highest id (the latest)
        try {
            final Statement stmt = con.createStatement();
            final ResultSet rs = stmt.executeQuery("SELECT * FROM match_player ORDER BY id DESC LIMIT 1");
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

    public boolean addMatch(final Match match) {
        // adds a new match to the db
        // convert winner to boolean
        // returns true if successful
        try {
            match.put("winner", String.valueOf(Integer.valueOf(match.get("winner")) - 1));
            final Statement stmt = con.createStatement();
            stmt.executeUpdate(
                    String.format("INSERT INTO match (id, level, winner, time_start, time_end) VALUES %s", match));
            for (int i = 0; i < 10; i++) {
                addMatchPlayer((Player) match.getSubMap(i));
            }
            for (int i = 10; i < match.subMaps.size(); i++) {
                addSnapshot((Snapshot) match.getSubMap(i));
            }
            // Calculate weights for new snapshots.
            // weighSnapshots();
            commit();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            System.out.println(e);
            try {
                rollback();
            } catch (Exception e2) {
                System.exit(1);
            }
            return false;
        }
    }

    public void addMatchPlayer(final Player player) throws SQLException, PSQLException {
        // convert team to boolean
        player.values[2] = String.valueOf(Integer.valueOf(player.get("team")) - 1);
        final Statement stmt = con.createStatement();
        stmt.executeUpdate(String.format("INSERT INTO match_player (player_id, match_id, team) VALUES %s", player));
    }

    public void addSnapshot(final Snapshot snapshot) throws SQLException, PSQLException {
        final Statement stmt = con.createStatement();
        stmt.executeUpdate(String.format(
                "INSERT INTO kill_event (type, event_id, match_player_id, timestamp, mainhand_type, mainhand_enchant, mainhand_tier, offhand_type, offhand_enchant, offhand_tier, head_type, head_enchant, head_tier, chest_type, chest_enchant, chest_tier, shoe_type, shoe_enchant, shoe_tier, cape_type, cape_enchant, cape_tier) VALUES %s",
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
                    "SELECT player_id FROM match_player WHERE player_id NOT IN (SELECT id FROM player) GROUP BY player_id");
            while (rs.next()) {
                unnamedIDs.add(rs.getString(1));
            }
        } catch (final SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return unnamedIDs;
    }

    public void addPlayer(final String playerID, final String playerName) {
        try {
            final Statement stmt = con.createStatement();
            stmt.executeUpdate(String.format("INSERT INTO player (id, name) VALUES ('%s','%s')", playerID,
                    playerName));
            commit();
        } catch (final SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public ArrayList<String> getUniqueEvents() {
        // originally created for updating truncated timestamps
        ArrayList<String> eventIDs = new ArrayList<>();
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT event_id FROM snapshot GROUP BY event_id");
            while (rs.next()) {
                eventIDs.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return eventIDs;
    }

    public void updateSnapshotTime(String eventID, String timestamp) {
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
            ResultSet rs = stmt.executeQuery("SELECT COUNT(`snapshot_id`) FROM `snapshot` WHERE `weight` IS null");
            // Count number of new snapshots that are unweighted.
            if (rs.next()) {
                int numNewSnapshotsUnweighted = rs.getInt(1);

                // Calculate and add weights to snapshot table.
                rs = stmt.executeQuery("CALL albstat.group_fullbuild();");
                if (rs.next()) {
                    int numNewSnapshotsWeighted = rs.getInt(1);

                    // Verify that number of new snapshots weighted == number of new snapshots added
                    // to DB.
                    assert numNewSnapshotsWeighted == numNewSnapshotsUnweighted
                            : "New snapshots weighted != new snapshots added to DB";
                }
            }

        } catch (AssertionError | SQLException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }

    private static class DBCredentials {

        private String user;
        private String pass;

        public DBCredentials() {
            // this constructor assumes you have placed a cred.txt file in the proper
            // location
            readCred();
        }

        private void readCred() {
            try {
                FileReader fileReader = new FileReader("cred.txt");
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                this.user = bufferedReader.readLine();
                this.pass = bufferedReader.readLine();
                bufferedReader.close();
            } catch (FileNotFoundException ex) {
                System.out.println("credential file not found");
            } catch (IOException ex) {
                System.out.println("error reading credentials");
            }
        }

        public String getUser() {
            return this.user;
        }

        public String getPass() {
            return this.pass;
        }
    }
}