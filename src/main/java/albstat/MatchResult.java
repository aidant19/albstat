package albstat;

// aidan tokarski
// 6/10/20
// a class for storing player results

import java.util.ArrayList;

class MatchResult {

    public String matchID;
    public ArrayList<PlayerResult> team1Results;
    public ArrayList<PlayerResult> team2Results;

    public MatchResult(String matchID) {
        this.matchID = matchID;
    }

    public void addTeam1Result(PlayerResult result) {
        team1Results.add(result);
    }

    public void addTeam2Result(PlayerResult result) {
        team2Results.add(result);
    }

    public void setPlayers(ArrayList<String> team1IDs, ArrayList<String> team2IDs) {
        team1Results = new ArrayList<PlayerResult>();
        for (String playerID : team1IDs) {
            team1Results.add(new PlayerResult(playerID, 0, 0));
        }
        team2Results = new ArrayList<PlayerResult>();
        for (String playerID : team2IDs) {
            team2Results.add(new PlayerResult(playerID, 0, 0));
        }
    }

    public void setResult(String playerID, int kills, int deaths) {
        for (PlayerResult playerResult : team1Results) {
            if (playerID == playerResult.playerID) {
                playerResult.kills = kills;
                playerResult.deaths = deaths;
            }
        }
        for (PlayerResult playerResult : team2Results) {
            if (playerID == playerResult.playerID) {
                playerResult.kills = kills;
                playerResult.deaths = deaths;
            }
        }
    }

    public void updateResult(String playerID, boolean kill) {
        for (PlayerResult playerResult : team1Results) {
            if (playerID.compareTo(playerResult.playerID) == 0) {
                if (kill) {
                    playerResult.kills++;
                } else {
                    playerResult.deaths++;
                }
            }
        }
        for (PlayerResult playerResult : team2Results) {
            if (playerID.compareTo(playerResult.playerID) == 0) {
                if (kill) {
                    playerResult.kills++;
                } else {
                    playerResult.deaths++;
                }
            }
        }
    }

    public int compare(MatchResult results) {
        for (PlayerResult playerResult1 : this.team1Results) {
            for (PlayerResult playerResult2 : results.team1Results) {
                if (playerResult1.playerID.compareTo(playerResult2.playerID) == 0) {
                    int killDiff = playerResult1.kills - playerResult2.kills;
                    if (killDiff != 0) {
                        System.out.println("kill difference found");
                        System.out.println("match: " + matchID);
                        System.out.println("player: " + playerResult1.playerID);
                        System.out.println("expected: " + playerResult1.kills);
                        System.out.println("found: " + playerResult2.kills);
                        System.out.println(results);
                        System.out.println(this);
                        for (PlayerResult pResult : team2Results) {
                            System.out.printf("https://gameinfo.albiononline.com/api/gameinfo/events/%s/history/%s\n",
                                    playerResult1.playerID, pResult.playerID);
                        }
                        return 1;
                    }
                }
            }
        }
        for (PlayerResult playerResult1 : this.team2Results) {
            for (PlayerResult playerResult2 : results.team2Results) {
                if (playerResult1.playerID.compareTo(playerResult2.playerID) == 0) {
                    int killDiff = playerResult1.kills - playerResult2.kills;
                    if (killDiff != 0) {
                        System.out.println("kill difference found");
                        System.out.println("match: " + matchID);
                        System.out.println("player: " + playerResult1.playerID);
                        System.out.println("expected: " + playerResult1.kills);
                        System.out.println("found: " + playerResult2.kills);
                        System.out.println(results);
                        System.out.println(this);
                        for (PlayerResult pResult : team1Results) {
                            System.out.printf("https://gameinfo.albiononline.com/api/gameinfo/events/%s/history/%s\n",
                                    playerResult1.playerID, pResult.playerID);
                        }
                        return 1;
                    }
                }
            }
        }
        return 0;
    }

    public String toString() {
        String team1String = "", team2String = "";
        for (PlayerResult playerResult : team1Results) {
            team1String += String.format("%s:%d,%d ", playerResult.playerID, playerResult.kills, playerResult.deaths);
        }
        team1String = team1String.substring(0, team1String.length() - 1);
        for (PlayerResult playerResult : team2Results) {
            team2String += String.format("%s:%d,%d ", playerResult.playerID, playerResult.kills, playerResult.deaths);
        }
        team2String = team2String.substring(0, team2String.length() - 1);
        return String.format("%s\n%s\n%s", matchID, team1String, team2String);
    }
}