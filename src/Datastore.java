import org.json.JSONException;
import org.json.JSONObject;

import java.lang.instrument.Instrumentation;
import java.sql.*;
import java.util.Date;
import java.util.Scanner;

public class Datastore {

    Connection connection = null;
    Statement stmt = null;
    String sql;

    public Datastore() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:datastore.db");
            initializeDatastore();
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    void initializeDatastore() {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, DatastoreContract.DBEntry.TABLE_NAME, null);
            if (!tables.next()) {
                stmt = connection.createStatement();
                sql = "CREATE TABLE " + DatastoreContract.DBEntry.TABLE_NAME + " " +
                        "(" + DatastoreContract.DBEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        " " + DatastoreContract.DBEntry.Key + " TEXT NOT NULL, " +
                        " " + DatastoreContract.DBEntry.Value + " TEXT, " +
                        " " + DatastoreContract.DBEntry.TTL + " INTEGER," +
                        " " + DatastoreContract.DBEntry.Dated + " TEXT) ";
                stmt.executeUpdate(sql);
                stmt.close();
                System.out.println("Table Created!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void create(String key, JSONObject value) {
        try {
            long dated = new Date().getTime();
            stmt = connection.createStatement();
            sql = "INSERT INTO " + DatastoreContract.DBEntry.TABLE_NAME + " (" + DatastoreContract.DBEntry.Key + "," + DatastoreContract.DBEntry.Value + "," + DatastoreContract.DBEntry.Dated + ") " +
                    "VALUES ('" + key + "','" +
                    value.toString() + "','" + String.valueOf(dated) + "')";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void create(String key, JSONObject value, Integer ttl) {
        try {
            long dated = new Date().getTime();
            stmt = connection.createStatement();
            sql = "INSERT INTO " + DatastoreContract.DBEntry.TABLE_NAME + " (" + DatastoreContract.DBEntry.Key + "," + DatastoreContract.DBEntry.Value + "," + DatastoreContract.DBEntry.TTL + "," + DatastoreContract.DBEntry.Dated + ") " +
                    "VALUES ('" + key + "','" +
                    value.toString() + "'," + ttl + ",'" + String.valueOf(dated) + "')";
            stmt.executeUpdate(sql);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws JSONException {
        Datastore datastore = new Datastore();
        Scanner scanner = new Scanner(System.in);
        int choice;
        do {
            System.out.println("1. Create\n2. Read\n3. Delete\n4. Exit\nEnter choice: ");
            choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    System.out.println("1. Create without TTL\n2. Create with TTL\nEnter choice:");
                    int createChoice = scanner.nextInt();
                    String key, value;
                    int ttl;
                    do {
                        System.out.println("Enter key: ");
                        key = scanner.next();
                        if (key.length() > 32)
                            System.out.println("\nPlease enter key with less tha 32 Chars.\n\n");
                    } while (key.length() > 32);

                    System.out.println("Enter Value: ");
                    value = scanner.next();

                    if (createChoice == 1) {
                        datastore.create(key, new JSONObject(value));
                    } else if (createChoice == 2) {
                        System.out.println("Enter TTL (in seconds): ");
                        ttl = scanner.nextInt();
                        datastore.create(key, new JSONObject(value), ttl);
                    } else {
                        System.out.println("Invalid choice!");
                    }
                    break;
                case 4:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Please Enter Correct option!");
            }
        } while (choice != 4);
    }
}
