package albstat;

// aidan tokarski
// 6/3/20
// albstat driver module

import java.util.ArrayList;

public class Driver{

    public static void main(String[] args) {
        APIInterface apiInterface = new APIInterface();
        int limit = 1000;
        int offset = 0;
        System.out.println(String.format("retrieving matches %d-%d", offset + 1, offset + limit));
        ArrayList<Match> matchList = apiInterface.getMatches(offset, limit);
    }
}