package albstat;

// aidan tokarski
// 6/12/20
// a class for containing player fields

public class DBMatchPlayer{

    public int match_player_id;
    public String player_id;
    public String match_id;
    public char team;

    public DBMatchPlayer(int match_player_id, String player_id, String match_id, int team){
        this.match_player_id = match_player_id;
        this.player_id = player_id;
        this.match_id = match_id;
        this.team = (char) team;
    }

    public String toString(){
        return String.format("('%s', '%s', '%s', '%d')", match_player_id, player_id, match_id, (int) team); 
    }
}