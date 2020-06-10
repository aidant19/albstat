package albstat;

// aidan tokarski
// 6/10/20
// a class for storing player results

class PlayerResult {

    public String playerID;
    public int kills;
    public int deaths;

    public PlayerResult(String playerID, int kills, int deaths){
        this.playerID = playerID;
        this.kills = kills;
        this.deaths = deaths;
    }
}