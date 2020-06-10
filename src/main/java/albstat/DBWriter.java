package albstat;

// aidan tokarski
// 6/5/20
// api interface module for albion online

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class DBWriter {

    public static void writeDBFile(List<Match> matchList, String filePath) {

        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (Match match : matchList) {
                bufferedWriter.write(match.DBString());
            }
            bufferedWriter.close();
        } catch (FileNotFoundException ex) {
            System.out.println("unable to open new db file");
        } catch (IOException ex) {
            System.out.println("error writing to db file");
        }
    }

    public static List<Match> readDBFile(String filePath) {
        List<Match> matchList = new ArrayList<Match>();
        try {
            FileReader fileReader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line = bufferedReader.readLine();
            while (line != null) {
                String matchString = line;
                String team1String = bufferedReader.readLine();
                String team2String = bufferedReader.readLine();
                String eventString = bufferedReader.readLine();
                matchList.add(Match.buildFromStrings(matchString, team1String, team2String, eventString));
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("db file not found");
        } catch (IOException ex) {
            System.out.println("error reading db file");
        }
        return matchList;
    }
}