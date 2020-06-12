package albstat;

// aidan tokarski
// 6/12/20
// a class for containing match fields

import java.util.ArrayList;

public class DBMatch{

    public String match_id;
    public char match_level;
    public char match_winner;
    public String match_start_time;
    public String match_end_time;
    public ArrayList<DBMatchPlayer> dbMatchPlayers;
    public ArrayList<DBSnapshot> dbSnapshots;

    public DBMatch(Match match, int starting_id){
        this.match_id = match.matchID;
        this.match_level = (char) match.level;
        this.match_winner = (char) match.winner;
        this.match_start_time = match.startTime.toString();
        this.match_end_time = match.endTime.toString();
        this.dbMatchPlayers = getDBMatchPlayers(match, starting_id);
        this.dbSnapshots = getDBSnapshots(match);
    }

    public ArrayList<DBMatchPlayer> getDBMatchPlayers(Match match, int starting_id){
        ArrayList<DBMatchPlayer> players = new ArrayList<>();
        for (String player : match.team1Players) {
            players.add(new DBMatchPlayer(starting_id++, player, match.matchID, 1));            
        }
        for (String player : match.team2Players) {
            players.add(new DBMatchPlayer(starting_id++, player, match.matchID, 2));            
        }
        return players;
    }

    public ArrayList<DBSnapshot> getDBSnapshots(Match match){
        ArrayList<DBSnapshot> snapshots = new ArrayList<>();
        for (Event event : match.events) {
            snapshots.add(new DBSnapshot(getMatchPlayerID(event.player1ID), 1, event.eventID, event.timestamp, event.player1Snapshot));
            snapshots.add(new DBSnapshot(getMatchPlayerID(event.player2ID), 2, event.eventID, event.timestamp, event.player2Snapshot));
            for (MainHandSnapshot groupSnap : event.group.snapshots) {
                snapshots.add(new DBSnapshot(getMatchPlayerID(groupSnap.playerID), 3, event.eventID, event.timestamp, groupSnap));
            }
            for (ParticipantSnapshot partiSnap : event.participants.snapshots) {
                snapshots.add(new DBSnapshot(getMatchPlayerID(partiSnap.playerID), 4, event.eventID, event.timestamp, partiSnap));
            }
        }
        return snapshots;
    }

    public int getMatchPlayerID(String player_id){
        for (DBMatchPlayer dbMatchPlayer : dbMatchPlayers) {
            if(dbMatchPlayer.player_id.compareTo(player_id) == 0){
                return dbMatchPlayer.match_player_id;
            }
        }
        return -1;
    }

    public String toString(){
        return String.format("('%s', '%d', '%d', '%s', '%s')", match_id, (int)match_level, (int)match_winner, match_start_time, match_end_time);
    }
}