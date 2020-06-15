package albstat;

// aidan tokarski
// 6/14/20
// module for handling JSON using the simple JSON parser

// util
import java.util.Map;

// simple json modules
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class JSONHandler {

    // parser instance
    private JSONParser parser;

    // loadable objects
    private JSONArray loadedArray; // extends List
    private JSONObject loadedObject; // extends Map

    public JSONHandler() {
        this.parser = new JSONParser();
    }

    public void loadArray(String rawJSON) {
        // loads a JSONArray and its first object parsed from a string
        try {
            loadedArray = (JSONArray) parser.parse(rawJSON);
            loadedObject = (JSONObject) loadedArray.get(0);
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public void loadObject(String rawJSON){
        // loads a JSONObject parsed from a string
        try {
            loadedObject = (JSONObject) parser.parse(rawJSON);
        } catch (ParseException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    public boolean loadNextObject() {
        // loads the next JSONObject in the loaded array
        // returns false if there is not next object
        try {
            loadedObject = (JSONObject) loadedArray.iterator().next();
            return true;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public String getValue(String key) {
        // returns the String form of a key value
        return loadedObject.get(key).toString();
    }

    public String getValueFromChain(String[] keyChain) {
        // for getting values from a chain of nested objects
        // returns the String form of a key value
        // returns null if it cannot find the key
        try {
            JSONObject object = new JSONObject();
            int i;
            for (i = 0; i < keyChain.length - 2; i++) {
                object = (JSONObject) loadedObject.get(keyChain[i]);
            }
            return object.get(keyChain[i + 1]).toString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public void mapTo(Map map){
        
    }
}