package albstat;

import java.util.ArrayList;

// aidan tokarski
// 6/14/20
// a class for bulding and handling data from the albion api as outlined by the albstat database

public class DataBuilder {

    // interface instances
    private APIInterface apiInterface;
    private DBInterface dbInterface;
    private JSONHandler jsonHandler;

    public DataBuilder() {
        this.apiInterface = new APIInterface();
        this.dbInterface = new DBInterface();
        this.jsonHandler = new JSONHandler();
    }

    public void getNewMatches(int offset, int batchSize, int total) {
        // primary data retrieval method
        if (offset + total > 10000) {
            // the api only allows for requests of the last 10,000 matches
            System.out.println("offset + total cannot be greater than 10000");
            System.exit(0);
        } else {
            System.out.println("retrieving parsed match ids");
            ArrayList<String> parsedMatchIDs = dbInterface.getParsedMatchIDs();
            System.out.printf("%d entries found\n", parsedMatchIDs.size());
            System.out.printf("requesting %d matches, offset %d from api\n", batchSize, offset);
            jsonHandler.loadArray(apiInterface.getNewMatches(offset, batchSize));
            System.out.println("matches retrieved, parsing matches");
            ArrayList<MatchNew> matchList = new ArrayList<>();
            for (int i = 0; i < batchSize; i++) {
                MatchNew match = new MatchNew();
                jsonHandler.mapTo(match);
                System.out.println(match);
                /*
                    for (JSONDefinedMap subMap : match.getSubMaps()) {
                    System.out.println(subMap);
                } */
            }
        }
    }
}