package albstat;

// aidan tokarski
// 6/12/20
// for interfacing api requests and processing with the database

import java.sql.*;
import java.util.ArrayList;

public class DBInterface {

    public Connection con;

    public DBInterface() throws SQLException, SQLTimeoutException {
        this.con = connect();
        this.con.setAutoCommit(false);
    }

    public static Connection connect() throws SQLException, SQLTimeoutException {
        DBCredentials credentials = new DBCredentials();
        System.out.println("database connection established");
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/albstat", credentials.getUser(), credentials.getPass());
    }

    public void uncommit() throws SQLException {
        con.rollback();
        con.close();
    }

    public void close() throws SQLException {
        con.commit();
        con.close();
    }

    public ArrayList<String> getParsedMatchIDs() throws SQLException {
        
        ArrayList<String> matchIDs = new ArrayList<String>();
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT `match_id` FROM `match`");
        while (rs.next()){
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

        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM `match_player` ORDER BY `match_player_id` DESC LIMIT 1");
        int next_id = 1;
        if(rs.next()){
            next_id = rs.getInt(1) + 1;
        }
        DBMatch dbMatch = new DBMatch(match, next_id);
        stmt.executeUpdate(String.format("INSERT INTO `match` (`match_id`, `match_level`, `match_winner`, `match_time_start`, `match_time_end`) VALUES %s", dbMatch));
        addMatchPlayers(dbMatch, stmt);
        addSnapshots(dbMatch, stmt);
    }

    public void addMatchPlayers(DBMatch dbMatch, Statement stmt) throws SQLException {
        for (DBMatchPlayer match_player : dbMatch.dbMatchPlayers) {
            stmt.executeUpdate(String.format("INSERT INTO `match_player` (`match_player_id`, `player_id`, `match_id`, `team`) VALUES %s", match_player));
        }
    }

    public void addSnapshots(DBMatch dbMatch, Statement stmt) throws SQLException {
        for (DBSnapshot snapshot : dbMatch.dbSnapshots) {
            stmt.executeUpdate(String.format("INSERT INTO `snapshot` (`match_player_id`, `snapshot_type`, `event_id`, `timestamp`, `mainhand_type`, `offhand_type`, `head_type`, `armor_type`, `shoe_type`, `cape_type`) VALUES %s", snapshot));
        }
    }
}