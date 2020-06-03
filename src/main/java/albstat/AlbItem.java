package albstat;

// aidan tokarski
// 6/2/20
// a class for containing albion item data

public class AlbItem {

    String itemID;
    int nUsed;
    int nWon;
    int nLost;
    int nUsedBoth;

    public AlbItem(String itemID) {
        this.itemID = itemID;
        this.nUsed = 0;
        this.nWon = 0;
        this.nLost = 0;
        this.nUsedBoth = 0;
    }

    public String toString(){
        return itemID;
    }
}