package albstat;

// aidan tokarski
// 5/26/20
// api interface module for albion online

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;
import java.net.*;
import java.io.*;
import java.util.Set;
import java.util.ArrayList;

public class APIInterface {

    public int matchesToParse;
    public int matchesParsed;

    public APIInterface() {
        this.matchesParsed = 0;
        this.matchesToParse = 0;
    }

    public ArrayList<Match> getMatches(int offset, int limit) {
        String URL = String.format(
                "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d", limit,
                offset);
        String rawJSON = getHTML(URL);
        ArrayList<Match> matchList = parseMatches(rawJSON);
        return matchList;
    }

    public String getHTML(String urlToRead) {
        try {
            StringBuilder result = new StringBuilder();
            URL url = new URL(urlToRead);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            rd.close();
            return result.toString();
        } catch (Exception e) {
            System.out.println("API Failure");
            return getHTML(urlToRead);
        }
    }

    public ArrayList<Match> parseMatches(String rawJSON) {

        JSONParser parser = new JSONParser();
        ArrayList<Match> matchList = new ArrayList<Match>();
        try {
            Object obj = parser.parse(rawJSON);
            JSONArray array = (JSONArray) obj;
            for (Object matchObj : array) {
                JSONObject match = (JSONObject) matchObj;
                if (Integer.parseInt(match.get("crystalLeagueLevel").toString()) == 1) {
                    continue;
                } else {
                    Timestamp startTime = new Timestamp(match.get("startTime").toString());
                    int winner = Integer.parseInt(match.get("winner").toString());
                    JSONObject team1 = (JSONObject) match.get("team1Results");
                    JSONObject team2 = (JSONObject) match.get("team2Results");
                    Set<String> team1Players = (Set<String>) team1.keySet();
                    Set<String> team2Players = (Set<String>) team2.keySet();
                    JSONArray timeline1 = (JSONArray) match.get("team1Timeline");
                    JSONObject lastEvent = (JSONObject) timeline1.get(timeline1.size() - 1);
                    Timestamp endTime = new Timestamp(lastEvent.get("TimeStamp").toString());
                    matchList.add(new Match(team1Players, team2Players, startTime, endTime, winner));
                }
            }
        } catch (ParseException pe) {
            System.out.println(pe + " (initial parse error)");
        }
        this.matchesToParse = matchList.size();
        System.out.println(String.format("matches to parse: %d", this.matchesToParse));
        crossReferenceMatches(matchList);
        return matchList;
    }

    public void crossReferenceMatches(ArrayList<Match> matchList) {

        JSONParser parser = new JSONParser();
        for (Match match : matchList) {
            for (String player1 : match.team1Players) {
                for (String player2 : match.team2Players) {
                    String events = getHTML(String.format(
                            "https://gameinfo.albiononline.com/api/gameinfo/events/%s/history/%s", player1, player2));
                    try {
                        if (events != null) {
                            Object obj = parser.parse(events);
                            JSONArray eventHistory = (JSONArray) obj;
                            for (Object eventObj : eventHistory) {
                                JSONObject event = (JSONObject) eventObj;
                                Timestamp time = new Timestamp(event.get("TimeStamp").toString());
                                if (time.isBetween(match.startTime, match.endTime)) {
                                    match.addEvent(buildEvent(event, null, time));
                                }
                            }
                        }
                    } catch (ParseException pe) {
                        System.out.println(pe + "\t\t (cross-reference error)");
                    }
                }
            }
        }
    }

    public Event buildEvent(JSONObject event, String matchID, Timestamp timestamp) {

        String eventID, player1ID, player2ID;
        eventID = event.get("EventId").toString();
        JSONObject killer = (JSONObject) event.get("Killer");
        JSONObject killerEquipment = (JSONObject) killer.get("Equipment");
        player1ID = killer.get("Id").toString();
        JSONObject victim = (JSONObject) event.get("Victim");
        JSONObject victimEquipment = (JSONObject) victim.get("Equipment");
        player2ID = victim.get("Id").toString();
        Event newEvent = new Event(eventID, player1ID, player2ID, matchID, timestamp);
        newEvent.player1Snapshot = buildSnapshot(killerEquipment, eventID, player1ID);
        newEvent.player2Snapshot = buildSnapshot(victimEquipment, eventID, player2ID);
        return newEvent;
    }

    public Snapshot buildSnapshot(JSONObject equipment, String eventID, String playerID) {
        Snapshot snap = new Snapshot(null, playerID, eventID);
        snap.addMain(((JSONObject) equipment.get("MainHand")).get("Type").toString());
        snap.addOff(((JSONObject) equipment.get("OffHand")).get("Type").toString());
        snap.addHead(((JSONObject) equipment.get("Head")).get("Type").toString());
        snap.addArmor(((JSONObject) equipment.get("Armor")).get("Type").toString());
        snap.addShoe(((JSONObject) equipment.get("Shoes")).get("Type").toString());
        snap.addCape(((JSONObject) equipment.get("Cape")).get("Type").toString());
        return snap;
    }
}