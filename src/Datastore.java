import org.json.JSONException;
import org.json.JSONObject;

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

    public boolean create(String key, JSONObject value) {
        try {
            if (!isKeyExists(key)) {
                long dated = new Date().getTime();
                stmt = connection.createStatement();
                sql = "INSERT INTO " + DatastoreContract.DBEntry.TABLE_NAME + " (" + DatastoreContract.DBEntry.Key + "," + DatastoreContract.DBEntry.Value + "," + DatastoreContract.DBEntry.Dated + ") " +
                        "VALUES ('" + key + "','" +
                        value.toString() + "','" + String.valueOf(dated) + "')";
                stmt.executeUpdate(sql);
                stmt.close();
                return true;
            } else return false;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public JSONObject read(String key) {
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + DatastoreContract.DBEntry.TABLE_NAME + " WHERE " + DatastoreContract.DBEntry.Key + "='" + key + "';");
            while (rs.next()) {
                int ttl = rs.getInt(DatastoreContract.DBEntry.TTL);
                if (ttl != 0) {
                    long createdTime = Long.parseLong(rs.getString(DatastoreContract.DBEntry.Dated));
                    long currentTime = new Date().getTime();
                    if (((currentTime - createdTime) / 60) < ttl)
                        return new JSONObject(rs.getString(DatastoreContract.DBEntry.Value));
                    else
                        return null;
                } else {
                    return new JSONObject(rs.getString(DatastoreContract.DBEntry.Value));
                }
            }
            stmt.close();
        } catch (SQLException | JSONException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    boolean isTTL(String key) {
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + DatastoreContract.DBEntry.TABLE_NAME + " WHERE " + DatastoreContract.DBEntry.Key + "='" + key + "';");
            while (rs.next()) {
                int ttl = rs.getInt(DatastoreContract.DBEntry.TTL);
                if (ttl != 0) {
                    long createdTime = Long.parseLong(rs.getString(DatastoreContract.DBEntry.Dated));
                    long currentTime = new Date().getTime();
                    if (((currentTime - createdTime) / 60) < ttl)
                        return true;
                    else
                        return false;
                } else {
                    return true;
                }
            }
            stmt.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    boolean isKeyExists(String key) {
        try {
            stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + DatastoreContract.DBEntry.TABLE_NAME + " WHERE " + DatastoreContract.DBEntry.Key + "='" + key + "';");
            stmt.close();
            if (rs.next()) {
                return false;
            } else
                return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean delete(String key) {
        try {
            if (isTTL(key)) {
                stmt = connection.createStatement();
                int rs = stmt.executeUpdate("DELETE FROM " + DatastoreContract.DBEntry.TABLE_NAME + " WHERE " + DatastoreContract.DBEntry.Key + "='" + key + "';");
                stmt.close();
                if (rs == 0) return false;
                else return true;
            } else {
                return false;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
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
                        try {
                            if (datastore.create(key, new JSONObject(value)))
                                System.out.println("Key-value created successfully!");
                            else System.out.println("Key already exists!");
                        } catch (JSONException e) {
                            System.out.println("Incorrect JSON Format!");
                        }
                    } else if (createChoice == 2) {
                        System.out.println("Enter TTL (in seconds): ");
                        ttl = scanner.nextInt();
                        try {
                            datastore.create(key, new JSONObject(value), ttl);
                        } catch (JSONException e) {
                            System.out.println("Incorrect JSON Format!");
                        }
                    } else {
                        System.out.println("Invalid choice!");
                    }
                    break;
                case 2:
                    System.out.println("Enter Key to read: ");
                    String searchKey;
                    searchKey = scanner.next();
                    JSONObject searchValue = datastore.read(searchKey);
                    if (searchValue != null) {
                        System.out.println(searchValue.toString());
                    } else {
                        System.out.println("Key does not exists or expired!");
                    }
                    break;
                case 3:
                    System.out.println("Enter Key to delete: ");
                    String deleteKey;
                    deleteKey = scanner.next();
                    if (datastore.delete(deleteKey))
                        System.out.println("Key deleted successfully!");
                    else
                        System.out.println("Key does not exists or expired!");
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
