public class DatastoreContract {
    DatastoreContract() {
    }

    /**
     * Used for Dependency injection for the Datastore
     */
    public static final class DBEntry implements BaseColumns {
        final static String TABLE_NAME = "DataStore";

        final static String _ID = BaseColumns._ID;
        final static String Key = "key";
        final static String Value = "value";
        final static String TTL = "ttl";
        final static String Dated = "dated";
    }
}
