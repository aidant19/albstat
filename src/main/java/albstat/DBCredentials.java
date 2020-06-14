package albstat;

// aidan tokarski
// 6/12/20
// for getting db credentials stored in a file

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class DBCredentials {

    private String user;
    private String pass;

    public DBCredentials(){
        // this constructor assumes you have placed a cred.txt file in the proper location
        readCred();
    }

    private void readCred(){
        try {
            FileReader fileReader = new FileReader("cred.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            this.user = bufferedReader.readLine();
            this.pass = bufferedReader.readLine();
            bufferedReader.close();
        } catch (FileNotFoundException ex) {
            System.out.println("credential file not found");
        } catch (IOException ex) {
            System.out.println("error reading credentials");
        }
    }

    public String getUser(){
        return this.user;
    }

    public String getPass(){
        return this.pass;
    }
}