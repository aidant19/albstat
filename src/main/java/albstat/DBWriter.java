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

public class DBWriter {

    public static void writeDBFile(List<Match> matchList){

        try {
            FileWriter fileWriter = new FileWriter("DBFile.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (Match match : matchList) {
                bufferedWriter.write(match.DBString());
            }
            bufferedWriter.close();
        } catch (FileNotFoundException ex) {
            System.out.println("unable to open new match file");
        } catch (IOException ex) {
            System.out.println("error writing to match file");
        }
    }
}