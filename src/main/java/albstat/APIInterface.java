package albstat;

// aidan tokarski
// 5/26/20
// api interface module for albion online

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class APIInterface {

    // static API endpoint helpers
    public static final String CL20Type = "CrystalLeague20v20";
    public static final String CL5Type = "CrystalLeague";

    // http client
    private static final HttpClient client = HttpClient.newBuilder().build();
    private static final Builder requestBuilder = HttpRequest.newBuilder();

    public static String getHTML(String url) {
        synchronized (requestBuilder) {
            try {
                HttpRequest request = requestBuilder.uri(URI.create(url)).build();
                HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
                while (response.statusCode() != 200) {
                    response = client.send(request, BodyHandlers.ofString());
                }
                return response.body();
            } catch (Exception e) {
                System.out.println(e);
                System.exit(1);
                return null;
            }
        }
    }

    public static synchronized CompletableFuture<HttpResponse<String>> getHTMLAsync(String url) {
        HttpRequest request = requestBuilder.uri(URI.create(url)).build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, BodyHandlers.ofString());
        return response;
    }

    public static String getNewMatches(int limit, int offset, String matchType) {
        String url = String.format(
                "https://gameinfo.albiononline.com/api/gameinfo/matches/crystalleague?limit=%d&offset=%d&matchType=%s",
                limit, offset, matchType);
        return getHTML(url);
    }

    public static CompletableFuture<HttpResponse<String>> getEventHistory(String player1ID, String player2ID) {
        String url = String.format("https://gameinfo.albiononline.com/api/gameinfo/events/%s/history/%s", player1ID,
                player2ID);
        return getHTMLAsync(url);
    }

    public static String getPlayer(String playerID) {
        String url = String.format("https://gameinfo.albiononline.com/api/gameinfo/players/%s", playerID);
        return getHTML(url);
    }

    public static class EventRequest implements Supplier<String> {

        final private String player1ID;
        final private String player2ID;

        public EventRequest(String player1ID, String player2ID) {
            this.player1ID = player1ID;
            this.player2ID = player2ID;
        }

        public String get() {
            boolean noResponse = true;
            HttpResponse<String> eventResponse = null;
            while (noResponse) {
                try {
                    eventResponse = APIInterface.getEventHistory(player1ID, player2ID).get();
                    while (eventResponse.statusCode() != 200) {
                        eventResponse = APIInterface.getEventHistory(player1ID, player2ID).get();
                    }
                } catch (Exception e) {
                    // not sure yet
                } finally {
                    noResponse = false;
                }
            }
            return eventResponse.body();
        }
    }
}