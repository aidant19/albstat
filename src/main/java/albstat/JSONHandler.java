package albstat;

// aidan tokarski
// 6/14/20
// module for handling JSON using the simple JSON parser

// util
import java.util.ListIterator;
import java.util.Set;
import java.util.Map.Entry;

// simple json modules
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class JSONHandler {

    // parser instance
    private JSONParser parser;

    // loadable objects
    private JSONArray loadedArray; // extended List
    private JSONObject baseObject; // extended Map
    private JSONObject loadedObject; // extended Map

    // array iterator
    private ListIterator listIterator;

    public JSONHandler() {
        this.parser = new JSONParser();
    }

    public boolean loadArray(String rawJSON) {
        // loads a JSONArray and its first object
        // returns true if the first object was loaded
        // returns false if the array is empty
        try {
            loadedArray = (JSONArray) parser.parse(rawJSON);
            listIterator = loadedArray.listIterator();
            if (listIterator.hasNext()) {
                baseObject = (JSONObject) listIterator.next();
                loadedObject = baseObject;
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
            return false;
        }
    }

    public boolean loadArrayReverse(String rawJSON) {
        // loads a JSONArray and its last object
        // returns true if the last object was loaded
        // returns false if the array is empty
        try {
            loadedArray = (JSONArray) parser.parse(rawJSON);
            listIterator = loadedArray.listIterator(loadedArray.size());
            if (listIterator.hasPrevious()) {
                baseObject = (JSONObject) listIterator.previous();
                loadedObject = baseObject;
                return true;
            } else {
                return false;
            }
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
            return false;
        }
    }

    public void loadObject(String rawJSON) {
        // loads a JSONObject parsed from a string
        try {
            baseObject = (JSONObject) parser.parse(rawJSON);
            loadedObject = baseObject;
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public void loadSubObject(String key) {
        // loads a JSONObject stored within the current loaded object
        loadedObject = (JSONObject) loadedObject.get(key);
    }

    public void loadBaseObject() {
        // loads the top level object
        loadedObject = baseObject;
    }

    public boolean loadNextObject() {
        // loads the next JSONObject in the loaded array
        // returns false if there is no next object
        if (listIterator.hasNext()) {
            baseObject = (JSONObject) listIterator.next();
            loadedObject = baseObject;
            return true;
        } else {
            return false;
        }
    }

    public boolean loadPreviousObject() {
        // loads the previous JSONObject in the loaded array
        // returns false if there is no previous object
        if (listIterator.hasPrevious()) {
            baseObject = (JSONObject) listIterator.previous();
            loadedObject = baseObject;
            return true;
        } else {
            return false;
        }
    }

    public boolean hasNext() {
        return this.listIterator.hasNext();
    }

    public boolean hasPrevious() {
        return this.listIterator.hasPrevious();
    }

    public JSONObject getObject(String key) {
        return (JSONObject) loadedObject.get("key");
    }

    public String getValue(String key) {
        // returns the String form of a key value
        try {
            return loadedObject.get(key).toString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String getValueFromChain(String[] address) {
        // for getting values from a chain of nested objects
        // returns the String form of a key value
        // returns null if it cannot find the key
        try {
            if (address.length == 1) {
                return loadedObject.get(address[0]).toString();
            } else {
                Object current = loadedObject.get(address[0]);
                int i;
                for (i = 1; i < address.length; i++) {
                    if (address[i].contains(":")) {
                        // returns an object/field from an array
                        if (address[i].contains("last")) {
                            JSONArray tempArray = (JSONArray) current;
                            current = tempArray.get(tempArray.size() - 1);
                        } else {
                            JSONArray tempArray = (JSONArray) current;
                            current = tempArray.get(Integer.parseInt(address[i].substring(1)));
                        }
                    } else if (address[i].contains("keySet")) {
                        // returns an object/field by key index
                        Set<String> keySet = ((JSONObject) current).keySet();
                        int counter = 0;
                        int index = Integer.parseInt(address[i].substring(6));
                        for (String key : keySet) {
                            if (index == counter) {
                                current = key;
                                break;
                            }
                            counter++;
                        }
                    } else {
                        current = ((JSONObject) current).get(address[i]);
                    }
                }
                return current.toString();
            }
        } catch (NullPointerException e) {
            return null;
        } catch (ClassCastException e){
            for (String string : address) {
                System.out.println(string);
            }
            e.printStackTrace();
            System.exit(0);
            return null;
        }
    }

    public Set<String> getKeySet(){
        return loadedObject.keySet();
    }

    public void mapTo(JSONDefinedMap map) {
        // maps the loaded object to values addressed by a JSONDefinedMap
        for (Entry<String, String[]> entry : map.getJSONMap().entrySet()) {
            map.put(entry.getKey(), getValueFromChain(entry.getValue()));
        }
        // maps the loaded object to values addressed in sub maps
        if (map.getSubMap(0) != null) {
            for (JSONDefinedMap subMap : map.getSubMaps()) {
                for (Entry<String, String[]> entry : subMap.getJSONMap().entrySet()) {
                    subMap.put(entry.getKey(), getValueFromChain(entry.getValue()));
                }
            }
        }
    }
}
