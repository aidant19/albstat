package albstat;

// aidan tokarski
// 6/15/20
// defines a basic Map which contains a JSONMap

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.Iterator;

public abstract class JSONDefinedMap extends AbstractMap<String, String> {

    // api mappings
    protected JSONMap jsonMap;

    // base data
    protected String[] keys;
    protected String[] values;
    protected int size;

    // sub maps
    protected ArrayList<JSONDefinedMap> subMaps;

    protected JSONDefinedMap(int size) {
        this.size = size;
        values = new String[size];
        keys = new String[size];
        subMaps = new ArrayList<>(0);
    }

    public String put(String key, String newValue) {
        for (int i = 0; i < size; i++) {
            if (keys[i].compareTo(key) == 0) {
                values[i] = newValue;
            }
        }
        return null;
    }

    protected abstract void setMapping();
    // used to set the mapping of JSON locations to keys in the base map
    // also sets the keys in this map

    protected void setSubMapping() {}
    // by default does not perform any submapping

    public String[] getJSONAddress(String key) {
        return jsonMap.get(key);
    }

    public JSONMap getJSONMap(){
        return jsonMap;
    }

    public JSONDefinedMap getSubMap(int index) {
        return null;
    }

    public ArrayList<JSONDefinedMap> getSubMaps() {
        if (this.subMaps.size() == 0) {
            return new ArrayList<>(0);
        } else {
            return this.subMaps;
        }
    }

    public Set<Entry<String, String>> entrySet() {
        return new EntrySet();
    }

    final class EntrySet extends AbstractSet<Entry<String, String>> {

        public final int size() {
            return size;
        }

        public Iterator<Entry<String, String>> iterator() {
            return new EntryIterator();
        }

        final class EntryIterator implements Iterator<Entry<String, String>> {

            private int index;

            EntryIterator() {
                this.index = 0;
            }

            public final Entry<String, String> next() {
                Entry<String, String> nextEntry = new SimpleEntry<>(keys[index], values[index]);
                index++;
                return nextEntry;
            }

            public final boolean hasNext() {
                return index != size;
            }
        }
    }

    public String toString() {
        String toString = "(";
        for (String value : values) {
            toString += String.format("'%s',", value);
        }
        toString = toString.substring(0, toString.length() - 1);
        toString += ")";
        return toString;
    }
}