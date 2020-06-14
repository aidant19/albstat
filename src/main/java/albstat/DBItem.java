package albstat;

// aidan tokarski
// 6/14/20
// a class for containing item fields

public class DBItem {

    // db fields
    public String item_type;
    public String item_name;
    public char item_enchant;
    public char item_tier;

    // program specifier
    public String item_class;

    public DBItem(String item_type_full) {
        // constructor using albion api naming scheme
        // for use by api interface
        this.item_name = null;
        this.item_class = null;
        this.parseType(item_type_full);
    }

    public DBItem(String item_type, String item_class) {
        // constructor using shortened naming scheme, and identifying item_class
        // for use by db interface
        this.item_name = null;
        this.item_class = item_class;
        this.item_type = item_type;
    }

    public void parseType(String item_type_full) {
        if (item_type_full == null){
            this.item_type = null;
            this.item_enchant = '\u0000';
            this.item_tier = '\u0000';
        }
        else if (item_type_full.compareTo("null") == 0) {
            // this condition may be removed in future versions
            // converts from old to new naming scheme in db
            this.item_type = null;
            this.item_enchant = '\u0000';
            this.item_tier = '\u0000';
        } else if (item_type_full.contains("@")) {
            // indicates item is enchanted
            item_enchant = (char) Integer.parseInt(item_type_full.substring(item_type_full.length() - 1));
            item_tier = (char) ((item_type_full.charAt(1)) - '0');
            item_type = item_type_full.substring(3, item_type_full.length() - 2);
        } else {
            // indicates item is not enchanted
            item_enchant = 0;
            item_tier = (char) ((item_type_full.charAt(1)) - '0');
            item_type = item_type_full.substring(3);
        }
    }

    public void setName(String n) {
        // for use with the api interface
        try {
            item_name = n.split("'s ")[1];
        } catch (Exception e) {
            System.out.println(n);
            System.out.println(e);
        }
    }

    public String toString() {
        // returns this object as the db fields specified in the item tables
        return String.format("('%s', '%s')", item_type, item_name);
    }
}