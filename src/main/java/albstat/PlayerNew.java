package albstat;

// aidan tokarski
// 6/16/20
// a class for containing match data

public class PlayerNew extends JSONDefinedMap {

    // defines the amount of fields in this Map
    private static final int FIELDS = 5;

    private int keyNumber;

    public PlayerNew(int keyNumber) {
        super(FIELDS);
        this.keyNumber = keyNumber;
        setMapping();
    }

    public String put(String key, String newValue) {
        // custom put notifies that the playerID has been updated
        synchronized (values) {
            for (int i = 0; i < size; i++) {
                if (keys[i].compareTo(key) == 0 && key.compareTo("team") != 0) {
                    values[i] = newValue;
                    if (i == 0) {
                        values.notifyAll();
                        try {
                            values.wait();
                        } catch (InterruptedException e) {
                            System.out.println("sync player mapping interrupted");
                            System.out.println(e);
                            System.exit(0);
                        }
                    }
                }
            }
            return null;
        }
    }

    protected void setMapping() {
        // maps fields from the api (JSON) to fields in the map
        this.jsonMap = new JSONMap(FIELDS);
        keys[1] = "playerName";
        keys[2] = "team";
        keys[3] = "kills";
        keys[4] = "deaths";
        String resultsString = String.format("team%sResults", values[2] = (keyNumber < 5) ? "1" : "2");
        jsonMap.add(new String[] { resultsString, String.format("keySet%d", keyNumber % 5) }, keys[0] = "playerID");
        // a new thread is created to complete the following mappings once the playerID
        // has been retrieved
        Runnable asyncMap = new Runnable() {
            public void run() {
                try {
                    synchronized (values) {
                        while (values[0] == null) {
                            values.wait();
                        }
                        jsonMap.add(new String[] { resultsString, values[0], "Name" }, keys[1]);
                        jsonMap.add(new String[] { resultsString, values[0], "Kills" }, keys[3]);
                        jsonMap.add(new String[] { resultsString, values[0], "Deaths" }, keys[4]);
                        values.notifyAll();
                    }
                } catch (InterruptedException e) {
                    System.out.println("async player mapping interrupted");
                    System.out.println(e);
                    System.exit(0);
                }
            }
        };
        Thread asyncMapper = new Thread(asyncMap);
        asyncMapper.start();
    }
}