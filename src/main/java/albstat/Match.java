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
    Set<String> team1Players;
    Set<String> team2Players;
    ArrayList<Event> events;
    int playersParsed;

    public Match(String matchID, Set<String> team1Players, Set<String> team2Players, Timestamp startTime, Timestamp endTime, int winner) {
        this.matchID = matchID;
        this.team1Players = team1Players;
        this.team2Players = team2Players;
        this.startTime = startTime;
        this.endTime = endTime;
        this.winner = winner;
        this.events = new ArrayList<Event>();
        this.playersParsed = 0;
    }

    public void addEvent(Event e){
        e.setMatchID(this.matchID);
        this.events.add(e);
    }

    public String toString(){
        String team1String = "", team2String = "", eventString = "";
        for (String player : this.team1Players) {
            team1String += player + " ";
        }
        for (String player : this.team2Players) {
            team2String += player + " ";
        }
        for (Event event : this.events) {
            eventString += event.toString() + "\n";
        }
        return String.format("%s - %s\n%d\n%s\n%s\nevents:\n%s", startTime, endTime, winner, team1String, team2String, eventString);
    }
}