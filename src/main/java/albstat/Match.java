package albstat;

// aidan tokarski
// 5/26/20
// a class for containing and cross-referencing match data

import java.util.Set;
import java.util.ArrayList;

public class Match {

    String matchID;
    Timestamp startTime;
    Timestamp endTime;
    int winner;
    int level;
    Set<String> team1Players;
    Set<String> team2Players;
    ArrayList<Event> events;
    MatchResult results;

    public Match(String matchID, Set<String> team1Players, Set<String> team2Players, Timestamp startTime,
            Timestamp endTime, int level, int winner, MatchResult results) {
        this.matchID = matchID;
        this.team1Players = team1Players;
        this.team2Players = team2Players;
        this.startTime = startTime;
        this.endTime = endTime;
        this.level = level;
        this.winner = winner;
        this.events = new ArrayList<Event>();
        this.results = results;
    }

    public void addEvent(Event e) {
        e.setMatchID(this.matchID);
        this.events.add(e);
    }

    public int verifyData(){
        return DataVerifier.verifyData(events, results);
    }
}