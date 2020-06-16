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

    EntrySet mappings;

    public JSONMap(int size) {
        this.mappings = new EntrySet(size);
    }

    public String put(String[] address, String keyName) {
        mappings.add(address, keyName);
        return null;
    }

    public Set<Entry<String, String[]>> entrySet() {
        return mappings;
    }

    final class EntrySet extends AbstractSet<Map.Entry<String, String[]>> {

        private int size;
        private int entryCount;
        private String[] keyNames; // name of the key the address maps to
        private String[][] Addresses; // JSON address

        public EntrySet(int size) {
            this.size = size;
            this.entryCount = 0;
            this.Addresses = new String[size][];
            this.keyNames = new String[size];
        }

        public final int size() {
            return size;
        }

        public boolean add(String[] address, String keyName) {
            Addresses[entryCount] = address;
            keyNames[entryCount] = keyName;
            entryCount++;
            return true;
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