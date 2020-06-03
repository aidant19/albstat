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

    public static String getHTML(String urlToRead) {

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
            System.out.print("\nAPI Failure");
            return getHTML(urlToRead);
        }
    }

    public static ArrayList<Match> parseMatches(String rawJSON) {

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
                    Timestamp startTime, endTime;
                    int winner;
                    startTime = new Timestamp(match.get("startTime").toString());
                    JSONArray timeline1 = (JSONArray) match.get("team1Timeline");
                    JSONObject lastEvent = (JSONObject) timeline1.get(timeline1.size() - 1);
                    endTime = new Timestamp(lastEvent.get("TimeStamp").toString());
                    winner = Integer.parseInt(match.get("winner").toString());
                    JSONObject team1 = (JSONObject) match.get("team1Results");
                    Set<String> team1Players = (Set<String>) team1.keySet();
                    JSONObject team2 = (JSONObject) match.get("team2Results");
                    Set<String> team2Players = (Set<String>) team2.keySet();
                    matchList.add(new Match(team1Players, team2Players, startTime, endTime, winner));
                }
            }
        } catch (ParseException pe) {
            System.out.println("position: " + pe.getPosition());
            System.out.println(pe);
        }
        crossReferenceMatches(matchList);
        return matchList;
    }

    public static void crossReferenceMatches(ArrayList<Match> matchList) {

        JSONParser parser = new JSONParser();
        int counter = 0;
        for (Match match : matchList) {
            counter++;
            System.out.print(String.format("\rmatch progress: %d/%d", counter, matchList.size()));
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
                        System.out.println("position: " + pe.getPosition());
                        System.out.println(pe);
                    }
                }
            }
        }
    }

    public static Event buildEvent(JSONObject event, String matchID, Timestamp timestamp) {

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

    public static Snapshot buildSnapshot(JSONObject equipment, String eventID, String playerID) {
        Snapshot snap = new Snapshot(null, playerID, eventID);
        snap.addMain(parseItem(((JSONObject) equipment.get("MainHand"))));
        snap.addOff(parseItem(((JSONObject) equipment.get("OffHand"))));
        snap.addHead(parseItem(((JSONObject) equipment.get("Head"))));
        snap.addArmor(parseItem(((JSONObject) equipment.get("Armor"))));
        snap.addShoe(parseItem(((JSONObject) equipment.get("Shoes"))));
        snap.addCape(parseItem(((JSONObject) equipment.get("Cape"))));
        return snap;
    }

    public static String parseItem(JSONObject item) {

        if (item == null) {
            return "";
        } else {
            String itemString = item.get("Type").toString();
            String[] itemStrings1 = itemString.split("_");
            itemStrings1[0] = "";
            String noTier = "";
            for (String string : itemStrings1) {
                if (string == null) {
                    continue;
                }
                noTier += string;
            }
            String[] itemStrings2 = noTier.split("@");
            return itemStrings2[0];
        }
    }

    public static void main(String[] args) {
        ArrayList<Match> matchList = new ArrayList<Match>();
        int limit = 100;
        int total = 1000;
        for (int i = 0; i < total; i += limit) {
            System.out.println(String.format("retrieving matches %d-%d", i, i + limit - 1));
            String URL = String.format(
                    "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d", limit, i);
            ArrayList<Match> newMatches = parseMatches(getHTML(URL));
            System.out.println(String.format("matches %d-%d retrieved", i, i + limit - 1));
            System.out.println(String.format("analyzing matches %d-%d", i, i + limit - 1));
            Analyzer analyzer = new Analyzer();
            analyzer.parseMatches(newMatches);
            analyzer.printStats();
            matchList.addAll(newMatches);
        }
        System.out.println("\nall matches retrieved");
        System.out.println("analyzing all matches\n");
        Analyzer analyzer = new Analyzer();
        analyzer.parseMatches(matchList);
        analyzer.printStats();
    }
}