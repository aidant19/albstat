package albstat;

// aidan tokarski
// 6/3/20
// albstat driver module

import java.util.ArrayList;

public class Driver{

    public static void main(String[] args) {
        APIInterface apiInterface = new APIInterface();
        int limit = 5;
        int offset = 0;
        System.out.println(String.format("retrieving matches %d-%d", offset, limit));
        ArrayList<Match> matchList1 = apiInterface.getMatches(offset, limit);
        DBWriter.writeDBFile(matchList1, "DBFile.txt");
        ArrayList<Match> matchList2 = new ArrayList<Match>(DBWriter.readDBFile("DBFile.txt"));
        DBWriter.writeDBFile(matchList2, "DBFile2.txt");
    }
}