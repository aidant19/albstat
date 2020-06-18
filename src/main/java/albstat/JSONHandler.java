package albstat;

// aidan tokarski
// 6/14/20
// module for handling JSON using the simple JSON parser

// util
import java.util.Iterator;
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
    private Iterator arrayIterator;

    public JSONHandler() {
        this.parser = new JSONParser();
    }

    public boolean loadArray(String rawJSON) {
        // loads a JSONArray and its first object parsed from a string
        try {
            loadedArray = (JSONArray) parser.parse(rawJSON);
            arrayIterator = loadedArray.iterator();
            baseObject = (JSONObject) loadedArray.get(0);
            loadedObject = baseObject;
            return true;
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
            return false;
        } catch (IndexOutOfBoundsException e) {
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
        // loads a JSONObject stored within the top level object
        loadedObject = (JSONObject) loadedObject.get(key);
    }

    public void loadBaseObject() {
        // loads the top level object
        loadedObject = baseObject;
    }

    public boolean loadNextObject() {
        // loads the next JSONObject in the loaded array
        // returns false if there is no next object
        if (arrayIterator.hasNext()) {
            baseObject = (JSONObject) arrayIterator.next();
            loadedObject = baseObject;
            return true;
        } else {
            return false;
        }
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
                        if (address[i].contains("last")) {
                            JSONArray tempArray = (JSONArray) current;
                            current = tempArray.get(tempArray.size() - 1);
                        } else {
                            JSONArray tempArray = (JSONArray) current;
                            current = tempArray.get(Integer.parseInt(address[i].substring(1)));
                        }
                    } else if (address[i].contains("keySet")) {
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
        }
    }

    public void mapTo(JSONDefinedMap map) {
        // maps the loaded object to values addressed by a JSONDefinedMap
        for (Entry<String, String[]> entry : map.getJSONMap().entrySet()) {
            for (String string : entry.getValue()) {
                System.out.print(string + ",");
            }
            System.out.println();
            map.put(entry.getKey(), getValueFromChain(entry.getValue()));
        }
        if (map.getSubMap(0) != null) {
            for (JSONDefinedMap subMap : map.getSubMaps()) {
                for (Entry<String, String[]> entry : subMap.getJSONMap().entrySet()) {
                    for (String string : entry.getValue()) {
                        System.out.print(string + ",");
                    }
                    System.out.println();
                    subMap.put(entry.getKey(), getValueFromChain(entry.getValue()));
                }
            }
        }
    }
}
