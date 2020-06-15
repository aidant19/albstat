package albstat;

// aidan tokarski
// 6/14/20
// a class for bulding and handling data from the albion api as outlined by the albstat database

public class DataBuilder {

    private APIInterface apiInterface;
    private DBInterface dbInterface;

    public DataBuilder(){
        this.apiInterface = new APIInterface();
        this.dbInterface = new DBInterface();
    }
}