package albstat;

// aidan tokarski
// 6/18/20
// a module which provides the same functionality as the APIInterface, but using cached files

// file writing
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

// util
import java.util.Set;
import java.util.ArrayList;

// json parsing
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

// html requests
import java.net.*;
import java.io.*;

public class APIInterfaceCached {

    public int matchesToParse;
    public int matchesParsed;
    public int duplicates;
    public String lastReport;

    public APIInterfaceCached() {
        this.matchesParsed = 0;
        this.matchesToParse = 0;
        this.duplicates = 0;
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

    public String getNewMatches(int offset, int limit) {
        String matchJSON = "";
        try {
            FileReader fileReader = new FileReader("match_cache.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String match = bufferedReader.readLine();
            if (match != null) {
                matchJSON += match;
            }
            match = bufferedReader.readLine();
            while (match != null) {
                matchJSON += "," + match;
                match = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("match_cache file not found");
        } catch (IOException ex) {
            System.out.println("error reading match_cache file");
        }
        return matchJSON;
    }

    public String getEventHistory(String player1, String player2) {
        JSONParser parser = new JSONParser();
        try {
            FileReader fileReader = new FileReader("event_cache.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String eventJSON = bufferedReader.readLine();
            while (eventJSON != null) {
                JSONArray events = (JSONArray) parser.parse(eventJSON);
                for (Object eventObj : events) {
                    JSONObject event = (JSONObject) eventObj;
                    String killerID = ((JSONObject) event.get("Killer")).get("Id").toString();
                    String victimID = ((JSONObject) event.get("Victim")).get("Id").toString();
                    if (player1.compareTo(killerID) == 0) {
                        if (player2.compareTo(victimID) == 0) {
                            bufferedReader.close();
                            return eventJSON;
                        }
                    } else if (player2.compareTo(killerID) == 0) {
                        if (player1.compareTo(victimID) == 0) {
                            bufferedReader.close();
                            return eventJSON;
                        }
                    }
                }
                eventJSON = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("event_cache file not found");
        } catch (IOException e) {
            System.out.println("error reading event_cache file");
        } catch (ParseException e) {
            System.out.println("failure to parse events_cache");
        }
        return "[]";
    }

    public void writeMatches(String matchJSON) {
        try {
            FileWriter fileWriter = new FileWriter("match_cache.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(matchJSON);
            bufferedWriter.close();
        } catch (FileNotFoundException e) {
            System.out.println("unable to open new match_cache file");
        } catch (IOException e) {
            System.out.println("error writing to match_cache file");
        }
    }

    public void writeEvents(ArrayList<String> eventJSON) {
        try {
            FileWriter fileWriter = new FileWriter("event_cache.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (String string : eventJSON) {
                bufferedWriter.write(string);
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
        } catch (FileNotFoundException ex) {
            System.out.println("unable to open new event_cache file");
        } catch (IOException ex) {
            System.out.println("error writing to event_cache file");
        }
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

    public String getItemName(String item_type) {
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