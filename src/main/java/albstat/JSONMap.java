package albstat;

// aidan tokarski
// 6/15/20
// a class for designating JSON fields to retrieve values from

import java.util.Map;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Set;
import java.util.Iterator;

public class JSONMap extends AbstractMap<String, String[]> {

    private int size;
    private int entryCount;
    private String[] keyNames; // name of the key the address maps to
    private String[][] Addresses; // JSON address

    public JSONMap(int size) {
        this.size = size;
        this.entryCount = 0;
        this.Addresses = new String[size][];
        this.keyNames = new String[size];
    }

    public boolean add(String[] address, String keyName) {
        Addresses[entryCount] = address;
        keyNames[entryCount] = keyName;
        entryCount++;
        return true;
    }

    public Set<Entry<String, String[]>> entrySet() {
        return new EntrySet();
    }

    final class EntrySet extends AbstractSet<Map.Entry<String, String[]>> {

        public final int size() {
            return size;
        }

        public Iterator<Entry<String, String[]>> iterator() {
            return new EntryIterator();
        }

        final class EntryIterator implements Iterator<Entry<String, String[]>> {

            private int index;

            EntryIterator() {
                this.index = 0;
            }

            public final Entry<String, String[]> next() {
                Entry<String, String[]> nextEntry = new SimpleEntry<>(keyNames[index], Addresses[index]);
                index++;
                return nextEntry;
            }

            public final boolean hasNext() {
                return index != entryCount;
            }
        }
    }
}