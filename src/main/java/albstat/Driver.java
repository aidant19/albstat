package albstat;

// aidan tokarski
// 6/3/20
// albstat driver module

import java.util.ArrayList;

public class Driver{

    public static void main(String[] args) {
        int limit = 100;
        for(int i=0;i<1000;i+=limit){
            APIInterface apiInterface = new APIInterface();
            int offset = i;
            ArrayList<Match> matchList = new ArrayList<Match>(TextWriter.readDBFile("DBFile.txt"));
            System.out.println(String.format("retrieving matches %d-%d", offset, limit+offset));
            apiInterface.getMatches(offset, limit, matchList);
            TextWriter.writeDBFile(matchList, "DBFile.txt");
            System.out.println();
        }
    }
}