package albstat;

// aidan tokarski
// 5/26/20
// a class for containing and cross-referencing match data

import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

public class Match {

    String matchID;
    Timestamp startTime;
    Timestamp endTime;
    int winner;
    int level;
    Set<String> team1Players;
    Set<String> team2Players;
    ArrayList<Event> events;

    public Match(String matchID, Set<String> team1Players, Set<String> team2Players, Timestamp startTime,
            Timestamp endTime, int level, int winner) {
        this.matchID = matchID;
        this.team1Players = team1Players;
        this.team2Players = team2Players;
        this.startTime = startTime;
        this.endTime = endTime;
        this.level = level;
        this.winner = winner;
        this.events = new ArrayList<Event>();
    }

    public void addEvent(Event e) {
        e.setMatchID(this.matchID);
        this.events.add(e);
    }

    public String DBString(){
        String team1String = "", team2String = "", eventString = "";
        for (String player : this.team1Players) {
            team1String += player + " ";
        }
        team1String = team1String.substring(0, team1String.length() - 1);
        for (String player : this.team2Players) {
            team2String += player + " ";
        }
        team2String = team2String.substring(0, team2String.length() - 1);
        for (Event event : this.events) {
            eventString += event.toString() + " ";
        }
        if (eventString.length() > 1){
            eventString = eventString.substring(0, eventString.length() - 1);
        }
        String startTimeString = startTime.toString();
        String endTimeString = endTime.toString();
        String levelString = String.valueOf(level);
        String winnerString = String.valueOf(winner);
        String matchString = String.format("%s %s %s %s %s", matchID, startTimeString, endTimeString, levelString, winnerString);
        return String.format("%s\n%s\n%s\n%s\n", matchString, team1String, team2String, eventString);
    }

    public static Match buildFromStrings(String matchString, String team1String, String team2String, String eventString){
        String[] matchStrings = matchString.split(" ");
        String matchID = matchStrings[0];
        Timestamp startTime = new Timestamp(matchStrings[1]);
        Timestamp endTime = new Timestamp(matchStrings[2]);
        int level = Integer.parseInt(matchStrings[3]);
        int winner = Integer.parseInt(matchStrings[4]);
        Set<String> team1Players = new HashSet<String>(Arrays.asList(team1String.split(" ")));
        Set<String> team2Players = new HashSet<String>(Arrays.asList(team2String.split(" ")));
        Match match = new Match(matchID, team1Players, team2Players, startTime, endTime, level, winner);
        String[] eventStrings = eventString.split(" ");
        for (String string : eventStrings) {
            match.addEvent(Event.buildFromStrings(string, matchID));
        }
        return match;
    }
}