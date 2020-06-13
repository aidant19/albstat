package albstat;

import java.sql.SQLException;

// aidan tokarski
// 6/3/20
// albstat driver module

import java.util.ArrayList;

public class Driver {

    public static void main(String[] args) throws Exception {
        int limit = 10;
        int offset = 0;
        for (; limit + offset < 10000; offset += limit) {
            APIInterface apiInterface = new APIInterface();
            DBInterface dbInterface = new DBInterface();
            ArrayList<String> matchIDs = dbInterface.getParsedMatchIDs();
            System.out.println(matchIDs.get(0));
            ArrayList<Match> matchList = apiInterface.getMatches(offset, limit, matchIDs);
            try {
                dbInterface.addMatches(matchList);
                dbInterface.close();
            } catch (SQLException e) {
                System.out.println(e);
                dbInterface.uncommit();
            }
        }
    }
}