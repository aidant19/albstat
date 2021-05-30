package albstat;

// aidan tokarski
// 5/26/20
// api interface module for albion online

import java.net.*;
import java.io.*;

public class APIInterface {

    public static final String CL20Type = "CrystalLeague20v20";
    public static final String CL5Type = "CrystalLeague";

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
            return getHTML(urlToRead);
        }
    }

    public static String getNewMatches(int offset, int limit, String matchType) {
        String URL = String.format(
                "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d&matchType=%s", limit,
                offset, matchType);
        return getHTML(URL);
    }

    public static String getEventHistory(String player1, String player2) {
        String URL = String.format("https://gameinfo.albiononline.com/api/gameinfo/events/%s/history/%s", player1,
                player2);
        return getHTML(URL);
    }

    public static String getPlayer(String playerID) {
        String URL = String.format("https://gameinfo.albiononline.com/api/gameinfo/players/%s", playerID);
        return getHTML(URL);
    }
}