package albstat;

// aidan tokarski
// 6/12/20
// a class for containing player fields

public class DBPlayer{

    public String player_id;
    public String player_name;

    public DBPlayer(String player_id){
        this.player_id = player_id;
        this.player_name = null;
    }

    public DBPlayer(String player_id, String player_name){
        this.player_id = player_id;
        this.player_name = player_name;
    }

    public void setName(String n){
        this.player_name = n;
    }

    public String toString(){
        return String.format("('%s', '%s')", player_id, player_name); 
    }
}