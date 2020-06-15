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
    public String lastReport;

    public APIInterface() {
        this.matchesParsed = 0;
        this.matchesToParse = 0;
        this.duplicates = 0;
    }

    public String getNewMatches(int offset, int limit) {
        String URL = String.format(
                "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d", limit,
                offset);
        return getHTML(URL);
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
            reportStatus("API Failure", true, false);
            return getHTML(urlToRead);
        }
    }

    public ArrayList<Match> parseMatches(String rawJSON, ArrayList<String> matchIDs) {

        ArrayList<Match> matchList = new ArrayList<Match>();
        JSONParser parser = new JSONParser();
        try {
            JSONArray rawMatchArray = (JSONArray) parser.parse(rawJSON);
            for (Object matchObj : rawMatchArray) {
                JSONObject match = (JSONObject) matchObj;
                if (Integer.parseInt(match.get("crystalLeagueLevel").toString()) == 1) {
                    continue;
                } else if (matchIDs.contains(match.get("MatchId").toString())) {
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
                    matchList.add(
                            new Match(matchID, team1Players, team2Players, startTime, endTime, level, winner, results));
                }
            }
        } catch (ParseException pe) {
            reportStatus(pe + " (initial parse error)", true, false);
        }
        this.matchesToParse = matchList.size();
        System.out.printf("duplicates found: %d\n", this.duplicates);
        System.out.printf("matches to parse: %d\n", this.matchesToParse);
        crossReferenceMatches(matchList);
        if (lastReport != null) {
            System.out.println();
        }
        return matchList;
    }

    public void crossReferenceMatches(ArrayList<Match> matchList) {

        ArrayList<Match> errorList = new ArrayList<Match>();
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
                                    match.addEvent(buildEvent(event, time));
                                }
                            }
                        }
                    } catch (ParseException pe) {
                        reportStatus(pe + " (cross-reference error)", true, false);
                    }
                }
            }
            if (match.verifyData() != 0) {
                errorList.add(match);
                reportStatus(String.format("data verification error: @%s", match.matchID), true, false);
            }
            this.matchesParsed++;
            reportStatus(String.format("%s: %d", "matches parsed", this.matchesParsed), false, false);
        }
        for (Match match : errorList) {
            matchList.remove(match);
        }
    }

    public MatchResult buildResult(JSONObject match, String matchID, Set<String> team1Players,
            Set<String> team2Players) {
        MatchResult results = new MatchResult(matchID);
        results.setPlayers(new ArrayList<String>(team1Players), new ArrayList<String>(team2Players));
        JSONObject team1Results = (JSONObject) match.get("team1Results");
        for (String player : team1Players) {
            int kills = Integer.parseInt(((JSONObject) team1Results.get(player)).get("Kills").toString());
            int deaths = Integer.parseInt(((JSONObject) team1Results.get(player)).get("Deaths").toString());
            results.setResult(player, kills, deaths);
        }
        JSONObject team2Results = (JSONObject) match.get("team2Results");
        for (String player : team2Players) {
            int kills = Integer.parseInt(((JSONObject) team2Results.get(player)).get("Kills").toString());
            int deaths = Integer.parseInt(((JSONObject) team2Results.get(player)).get("Deaths").toString());
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

    public String checkItemString(JSONObject item) {
        if (item == null) {
            return null;
        } else {
            return item.get("Type").toString();
        }
    }

    public Participants getParticipants(JSONObject event) {

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

    public MainHandSnapshot buildMainHandSnapshot(JSONObject equipment, String playerID) {

        MainHandSnapshot snap = new MainHandSnapshot(playerID, null);
        snap.addMain(checkItemString((JSONObject) equipment.get("MainHand")));
        return snap;
    }

    public String getPlayerName(String playerID) {
        JSONParser parser = new JSONParser();
        String playerJSON = getHTML(
                String.format("https://gameinfo.albiononline.com/api/gameinfo/players/%s", playerID));
        try {
            JSONObject player = (JSONObject) parser.parse(playerJSON);
            return player.get("Name").toString();
        } catch (ParseException pe) {
            reportStatus(pe + " (player name error)", true, false);
            return null;
        }
    }

    public String getItemName(String item_type){
        JSONParser parser = new JSONParser();
        String itemJSON = getHTML(
                String.format("https://gameinfo.albiononline.com/api/gameinfo/items/T4_%s/data", item_type));
        try {
            JSONObject item = (JSONObject) parser.parse(itemJSON);
            return ((JSONObject) item.get("localizedNames")).get("EN-US").toString();
        } catch (ParseException pe) {
            reportStatus(pe + " (item name error)", true, false);
            return null;
        }
    }

    public void reportStatus(String status, boolean error, boolean last) {
        String newReport;
        if (lastReport == null) {
            newReport = status;
        } else if (error && lastReport.contains(status)) {
            if (lastReport.contains(": ")) {
                int statusCount = Integer.parseInt(lastReport.split(": ")[1]);
                newReport = String.format("%s: %d", status, statusCount + 1);
            } else {
                newReport = String.format("%s: 2", status);
            }
        } else if (!error) {
            if (lastReport.contains(status.split(":")[0])) {
                newReport = status;
            } else {
                System.out.println(lastReport);
                newReport = status;
            }
        } else {
            System.out.println(lastReport);
            newReport = status;
        }
        if (last) {
            System.out.println(newReport);
        } else {
            System.out.print(newReport + "\r");
        }
        lastReport = newReport;
    }
}