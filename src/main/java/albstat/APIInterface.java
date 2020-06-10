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
    public int duplicates;

    public APIInterface() {
        this.matchesParsed = 0;
        this.matchesToParse = 0;
        this.duplicates = 0;
    }

    public void getMatches(int offset, int limit, ArrayList<Match> matchList) {
        String URL = String.format(
                "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d", limit,
                offset);
        String rawJSON = getHTML(URL);
        parseMatches(rawJSON, matchList);
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

    public void parseMatches(String rawJSON, ArrayList<Match> matchList) {

        ArrayList<Match> newMatchList = new ArrayList<Match>();
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(rawJSON);
            ArrayList<String> matchIDs = getMatchIDs(matchList);
            JSONArray array = (JSONArray) obj;
            for (Object matchObj : array) {
                JSONObject match = (JSONObject) matchObj;
                if (Integer.parseInt(match.get("crystalLeagueLevel").toString()) == 1) {
                    continue;
                } else if (matchIDs.contains(match.get("MatchId").toString())){
                    this.duplicates++;
                } else {
                    Timestamp startTime = new Timestamp(match.get("startTime").toString());
                    int winner = Integer.parseInt(match.get("winner").toString());
                    int level = Integer.parseInt(match.get("crystalLeagueLevel").toString());
                    String matchID = match.get("MatchId").toString();
                    JSONObject team1 = (JSONObject) match.get("team1Results");
                    JSONObject team2 = (JSONObject) match.get("team2Results");
                    Set<String> team1Players = (Set<String>) team1.keySet();
                    Set<String> team2Players = (Set<String>) team2.keySet();
                    JSONArray timeline1 = (JSONArray) match.get("team1Timeline");
                    JSONObject lastEvent = (JSONObject) timeline1.get(timeline1.size() - 1);
                    Timestamp endTime = new Timestamp(lastEvent.get("TimeStamp").toString());
                    MatchResult results = buildResult(match, matchID, team1Players, team2Players);
                    newMatchList.add(new Match(matchID, team1Players, team2Players, startTime, endTime, level, winner, results));
                }
            }
        } catch (ParseException pe) {
            System.out.println(pe + " (initial parse error)");
        }
        this.matchesToParse = newMatchList.size();
        System.out.printf("duplicates found: %d\n", this.duplicates);
        System.out.printf("matches to parse: %d\n", this.matchesToParse);
        crossReferenceMatches(newMatchList);
        matchList.addAll(newMatchList);
    }

    public void crossReferenceMatches(ArrayList<Match> matchList) {

        JSONParser parser = new JSONParser();
        for (Match match : matchList) {
            System.out.printf("matches parsed: %d\r", this.matchesParsed);
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
                                    match.addEvent(buildEvent(event, time));
                                }
                            }
                        }
                    } catch (ParseException pe) {
                        System.out.println(pe + " (cross-reference error)");
                    }
                }
            }
            int diff = match.verifyData();
            if(diff != 0){
                System.exit(1);
            } else {
                this.matchesParsed++;
            }
        }
    }

    public MatchResult buildResult(JSONObject match, String matchID, Set<String> team1Players, Set<String> team2Players){
        MatchResult results = new MatchResult(matchID);
        results.setPlayers(new ArrayList<String>(team1Players), new ArrayList<String>(team2Players));
        JSONObject team1Results = (JSONObject) match.get("team1Results");
        for (String player : team1Players) {
            int kills = Integer.parseInt(((JSONObject)team1Results.get(player)).get("Kills").toString());
            int deaths = Integer.parseInt(((JSONObject)team1Results.get(player)).get("Deaths").toString());
            results.setResult(player, kills, deaths);
        }
        JSONObject team2Results = (JSONObject) match.get("team2Results");
        for (String player : team2Players) {
            int kills = Integer.parseInt(((JSONObject)team2Results.get(player)).get("Kills").toString());
            int deaths = Integer.parseInt(((JSONObject)team2Results.get(player)).get("Deaths").toString());
            results.setResult(player, kills, deaths);
        }
        return results;
    }

    public Event buildEvent(JSONObject event, Timestamp timestamp) {

        String eventID = event.get("EventId").toString();
        JSONObject killer = (JSONObject) event.get("Killer");
        JSONObject victim = (JSONObject) event.get("Victim");
        JSONObject killerEquipment = (JSONObject) killer.get("Equipment");
        String player1ID = killer.get("Id").toString();
        JSONObject victimEquipment = (JSONObject) victim.get("Equipment");
        String player2ID = victim.get("Id").toString();
        Event newEvent = new Event(eventID, player1ID, player2ID, timestamp);
        newEvent.player1Snapshot = buildSnapshot(killerEquipment, eventID, player1ID);
        newEvent.player2Snapshot = buildSnapshot(victimEquipment, eventID, player2ID);
        newEvent.group = getGroup(event);
        newEvent.participants = getParticipants(event);
        return newEvent;
    }

    public Snapshot buildSnapshot(JSONObject equipment, String eventID, String playerID) {

        Snapshot snap = new Snapshot(playerID, eventID);
        snap.addMain(checkItemString((JSONObject) equipment.get("MainHand")));
        snap.addOff(checkItemString((JSONObject) equipment.get("OffHand")));
        snap.addHead(checkItemString((JSONObject) equipment.get("Head")));
        snap.addArmor(checkItemString((JSONObject) equipment.get("Armor")));
        snap.addShoe(checkItemString((JSONObject) equipment.get("Shoes")));
        snap.addCape(checkItemString((JSONObject) equipment.get("Cape")));
        return snap;
    }

    public String checkItemString(JSONObject item){
        if(item == null){
            return "_";
        } else {
            return item.get("Type").toString();
        }
    }

    public Participants getParticipants(JSONObject event){

        Participants participants = new Participants();
        JSONArray participantArray = (JSONArray) event.get("Participants");
        for (Object partiObj : participantArray) {
            JSONObject parti = (JSONObject) partiObj;
            JSONObject equipment = (JSONObject) parti.get("Equipment");
            String partiID = parti.get("Id").toString();
            participants.addSnap(buildParticipantSnapshot(equipment, partiID));
        }
        return participants;
    }

    public ParticipantSnapshot buildParticipantSnapshot(JSONObject equipment, String playerID) {

        ParticipantSnapshot snap = new ParticipantSnapshot(playerID);
        snap.addMain(checkItemString((JSONObject) equipment.get("MainHand")));
        snap.addOff(checkItemString((JSONObject) equipment.get("OffHand")));
        snap.addHead(checkItemString((JSONObject) equipment.get("Head")));
        snap.addArmor(checkItemString((JSONObject) equipment.get("Armor")));
        snap.addShoe(checkItemString((JSONObject) equipment.get("Shoes")));
        snap.addCape(checkItemString((JSONObject) equipment.get("Cape")));
        return snap;
    }

    public Group getGroup(JSONObject event) {

        Group group = new Group();
        JSONArray groupMembers = (JSONArray) event.get("GroupMembers");
        for (Object memberObj : groupMembers) {
            JSONObject member = (JSONObject) memberObj;
            JSONObject equipment = (JSONObject) member.get("Equipment");
            String memberID = member.get("Id").toString();
            group.addSnap(buildMainHandSnapshot(equipment, memberID));
        }
        return group;
    }

    public MainHandSnapshot buildMainHandSnapshot(JSONObject equipment, String playerID){

        MainHandSnapshot snap = new MainHandSnapshot(playerID, null);
        snap.addMain(checkItemString((JSONObject) equipment.get("MainHand")));
        return snap;
    }

    public ArrayList<String> getMatchIDs(ArrayList<Match> matchList){
        ArrayList<String> matchIDs = new ArrayList<String>();
        for (Match match : matchList) {
            matchIDs.add(match.matchID);
        }
        return matchIDs;
    }
}