package albstat;

// aidan tokarski
// 6/10/20
// a class for verifying reported and cross-referenced data

public class DataVerifier {

    public static boolean verify(Match match) {
        String[] playerIDs = new String[10];
        int[] killsReported = new int[10];
        int[] killsFound = new int[10];
        // retrieves match reported kills
        for (int i = 0; i < 10; i++) {
            killsReported[i] = ((Player) match.getSubMap(i)).kills;
            playerIDs[i] = match.getSubMap(i).get("playerID");
        }
        // retrieves kills found in snapshots
        for (int i = 10; i < match.subMaps.size(); i++) {
            if (Integer.parseInt(match.getSubMap(i).get("snapshotType")) == 1) {
                for (int j = 0; j < 10; j++) {
                    if (((Snapshot) match.getSubMap(i)).playerID.compareTo(playerIDs[j]) == 0) {
                        killsFound[j]++;
                    }
                }
            }
        }
        // verifies that the reported and found kills are the same
        for (int i = 0; i < 10; i++) {
            if (killsReported[i] != killsFound[i]) {
                System.out.printf("kill discrepancy @ match: %s\n", match.get("matchID"));
                System.out.printf("player: %s reported: %d found %d\n", playerIDs[i], killsReported[i], killsFound[i]);
                return false;
            }
        }
        return true;
    }
}