package albstat;

// aidan tokarski
// 6/2/20
// driver program for statistical analysis

import java.util.ArrayList;

public class Analyzer {

    ArrayList<AlbItem> weaponList;

    public Analyzer() {
        this.weaponList = new ArrayList<AlbItem>();
    }

    public void parseMatches(ArrayList<Match> matchList) {
        for (Match match : matchList) {
            this.parseMatch(match);
        }
    }

    public void updateItem(String itemID, boolean win) {
        for (AlbItem item : weaponList) {
            if (item.itemID.compareTo(itemID) == 0) {
                item.nUsed++;
                if (win) {
                    item.nWon++;
                } else {
                    item.nLost++;
                }
                return;
            }
        }
        AlbItem newItem = new AlbItem(itemID);
        newItem.nUsed++;
        if (win) {
            newItem.nWon++;
        } else {
            newItem.nLost++;
        }
        weaponList.add(newItem);
    }

    public void parseMatch(Match match) {

        ArrayList<Snapshot> parsedSnaps1 = new ArrayList<Snapshot>();
        ArrayList<String> parsedPlayers1 = new ArrayList<String>();
        ArrayList<Snapshot> parsedSnaps2 = new ArrayList<Snapshot>();
        ArrayList<String> parsedPlayers2 = new ArrayList<String>();
        for (Event event : match.events) {
            if (match.team1Players.contains(event.player1ID)) {
                if (!(parsedPlayers1.contains(event.player1ID))) {
                    parsedSnaps1.add(event.player1Snapshot);
                    parsedPlayers1.add(event.player1ID);
                }
                if (!(parsedPlayers2.contains(event.player2ID))) {
                    parsedSnaps2.add(event.player2Snapshot);
                    parsedPlayers2.add(event.player2ID);
                }
            } else {
                if (!(parsedPlayers2.contains(event.player1ID))) {
                    parsedSnaps2.add(event.player1Snapshot);
                    parsedPlayers2.add(event.player1ID);
                }
                if (!(parsedPlayers1.contains(event.player2ID))) {
                    parsedSnaps1.add(event.player2Snapshot);
                    parsedPlayers1.add(event.player2ID);
                }
            }
        }
        for (Snapshot snap : parsedSnaps1) {
            updateItem(snap.mainHandID, (match.winner == 1) ? true : false);
        }
        for (Snapshot snap : parsedSnaps2) {
            updateItem(snap.mainHandID, (match.winner == 2) ? true : false);
        }
    }

    public void printStats() {
        for (AlbItem item : weaponList) {
            System.out.println(String.format("%s\t%d\t%f%%\t", item.itemID, item.nUsed, item.nWon / (double) item.nUsed));
        }
    }
}