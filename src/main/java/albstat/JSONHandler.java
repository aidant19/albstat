package albstat;

// aidan tokarski
// 6/14/20
// module for handling JSON using the simple JSON parser


import java.util.Set;

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

    public JSONHandler() {
        this.parser = new JSONParser();
    }

    public void loadArray(String rawJSON) {
        // loads a JSONArray and its first object parsed from a string
        try {
            loadedArray = (JSONArray) parser.parse(rawJSON);
            baseObject = (JSONObject) loadedArray.get(0);
            loadedObject = baseObject;
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
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

    public void loadSubObject(String key){
        // loads a JSONObject stored within the top level object
        loadedObject = (JSONObject) loadedObject.get(key);
    }

    public void loadBaseObject(){
        // loads the top level object
        loadedObject = baseObject;
    }

    public boolean loadNextObject() {
        // loads the next JSONObject in the loaded array
        // returns false if there is not next object
        try {
            baseObject = (JSONObject) loadedArray.iterator().next();
            loadedObject = baseObject;
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public JSONObject getObject(String key){
        return (JSONObject) loadedObject.get("key");
    }

    public String getValue(String key) {
        // returns the String form of a key value
        return loadedObject.get(key).toString();
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
                for (i = 1; i < address.length - 1; i++) {
                    if(address[i].contains(":")){
                        if(address[i].contains("last")){
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
                            if(index == counter){
                                current = ((JSONObject) current).get(keySet.remove(key));
                                break;
                            }
                        }
                    } else {
                        current = ((JSONObject) current).get(address[i]);
                    }
                }
                return ((JSONObject) current).get(address[address.length - 1]).toString();
            }
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void mapTo(JSONDefinedMap map) {
        // maps the loaded object to values addressed by a JSONDefinedMap
        Set<String> keys = map.keySet();
        for (String key : keys) {
            map.replace(key, getValueFromChain(map.getJSONAddress(key)));
        }
        for (JSONDefinedMap subMap : map.getSubMaps()){
            keys = subMap.keySet();
            for (String key : keys) {
                map.replace(key, getValueFromChain(map.getJSONAddress(key)));
            }
        }
    }
}
