package albstat;

// aidan tokarski
// 6/15/20
// a class for containing

import java.util.Map;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.Iterator;

public class MatchNew extends AbstractMap<String, String> {

    public Set<String> team1Players;
    public Set<String> team2Players;
    public ArrayList<Event> events;
    public MatchResult results;

    public MatchNew() {}

    public MatchNew(Map<String, String> map) {
        for (Entry<String, String> entry: map.entrySet()) {
            this.replace(entry.getKey(), entry.getValue());
        }
    }

    public Set<Entry<String, String>> entrySet() {
        return new EntrySet();
    }

    final class EntrySet extends AbstractSet<Map.Entry<String, String>> {

        private String[] keys = { "matchID", "level", "winner", "timeStart", "timeEnd" };

        public final int size() {
            return 5;
        }

        public Iterator<Entry<String, String>> iterator() {
            return new EntryIterator();
        }

		final class EntryIterator implements Iterator<Map.Entry<String, String>> {

            private int index;

            EntryIterator() {
                this.index = 0;
            }

            public final Entry<String, String> next() {
                return new SimpleEntry<String, String>(keys[index++], null);
            }

            public final boolean hasNext() {
                return index != 4;
            }
        }
    }

    public void addEvent(Event e) {
        e.setMatchID(this.get("matchID"));
        this.events.add(e);
    }

    public int verifyData() {
        return DataVerifier.verifyData(events, results);
    }
}