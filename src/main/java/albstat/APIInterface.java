package albstat;

// aidan tokarski
// 5/26/20
// api interface module for albion online

import java.net.*;
import java.io.*;

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
        String URL = String.format(
                "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d", limit,
                offset);
        return getHTML(URL);
    }

    public String getEventHistory(String player1, String player2) {
        String URL = String.format("https://gameinfo.albiononline.com/api/gameinfo/events/%s/history/%s", player1,
                player2);
        return getHTML(URL);
    }

    public String getMatch(String matchID) {
        String URL = String.format("https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague/%s", matchID);
        return getHTML(URL);
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