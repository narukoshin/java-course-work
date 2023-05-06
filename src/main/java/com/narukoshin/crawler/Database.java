package com.narukoshin.crawler;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connection db;
    /**
     * This will show if the database is just created, or it's already existed.
     * */
    protected boolean justCreated;
    public Database(){
        // Database initialization
        // --------------------------------
        // It checks if the database file exists, if not, then creates it.
        initDatabase();
    }

    /**
     * It will create a new database if its doesn't exist
     * And will create tables that are needed for the crawler.
     * Also, it will create a database connection.
     *  */
    private void initDatabase()  {
        File f = new File("database.sqlite3");
        // If file doesn't exist then we are creating it
        if (!f.exists()) {
            // Creating a file.
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            // If a file exists or just created,
            // We can continue to create tables
            connectDatabase(f);

            // Installing all the tables we need for this application.
            try {
                databaseInstallation();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            // Setting that we just created the database.
            justCreated = true;
        } else {
            // If the database already exists, we can just connect to it.
            connectDatabase(f);
            // setting that the database already exists.
            justCreated = false;
        }
    }
    /**
     * Connecting to the database itself.
     *
     * @param f    Pointer to the database file.
     * */
    private void connectDatabase(File f){
        try {
            db = DriverManager.getConnection("jdbc:sqlite:" + f.getAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * This function will create all the tables we need for this application
     *
     * @throws SQLException if an error occurs in checking connection
     * */
    private void databaseInstallation() throws SQLException {
        // Checking if the database is connected successfully
        connectionCheck();
        try {
            Statement stmt = db.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS websites (id INTEGER PRIMARY KEY AUTOINCREMENT, website_url TEXT NOT NULL, added_at DATETIME DEFAULT CURRENT_TIMESTAMP);");
            stmt.execute("CREATE TABLE IF NOT EXISTS settings (id INTEGER PRIMARY KEY AUTOINCREMENT, setting_name TEXT NOT NULL, setting_value TEXT NOT NULL);");
        } catch (SQLException e){
            throw new SQLException(e);
        }
    }
    /**
     * Updating setting that is stored in the database
     *
     * @param name          the name of setting
     * @param value         the value of setting
     * @throws SQLException if an
        }
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        // There we will load all the found websites from the database if there are any.
        try {error occurs during checking the database connection or executing the statement
     * */
    public void updateSetting(String name, String value) throws SQLException {
        // Checking if we are connected to the database
        connectionCheck();
        try {
            Statement stmt = db.createStatement();
            stmt.execute("UPDATE settings SET setting_value = '"+ value.strip() +"' WHERE setting_name = '"+ name.strip() +"';");
        } catch (SQLException e){
            throw new SQLException(e);
        }
    }
    /**
     * Retrieving a setting from the database
     *
     * @param name          the name of setting
     * @throws SQLException if an error occurs during checking the database connection or executing the statement
     * */
    public String retrievingSetting(String name) throws SQLException{
        // Checking if we are connected to the database
        connectionCheck();
        try {
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT setting_value FROM settings WHERE setting_name = '" + name.strip() +"'");
            if (rs.next()) {
                return rs.getString("setting_value");
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        return null;
    }
    /**
     * Creates a new settings in the database
     *
     * @param name      the name of setting
     * @param value     the value of setting (Default value)
     * @throws SQLException if an error occurs during checking the database connection or executing the statement
     * */
    public void createSetting(String name, String value) throws SQLException {
        // Checking if we are connected to the database
        connectionCheck();
        try {
            Statement stmt = db.createStatement();
            stmt.execute("INSERT INTO settings (setting_name, setting_value) VALUES ('" + name + "', '" + value + "');");
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }
    /**
     * This method will insert a website into database on save button click.
     *
     * @param name      URL or name of the website
     * @throws SQLException     if an error occurs during checking the database connection or executing the statement
     * */
    public void insertWebsite(String name) throws SQLException {
        // Checking if we are connected to the database
        connectionCheck();
        try {
            // Before we are adding a website,
            // We need to check if that website isn't already in the database.
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT website_url FROM websites WHERE website_url = '" + name +"' LIMIT 1;");
            if (!rs.next()) {
                // Adding website to the database
                stmt = db.createStatement();
                stmt.execute("INSERT INTO websites (website_url) VALUES ('" + name + "')");
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }

    }
    /**
     * This function will check if the app connected to a database successfully
     *
     * @throws SQLException     if db connection is null or not an instance of sqlite
     * */
    private void connectionCheck() throws SQLException {
        if (!(db instanceof org.sqlite.SQLiteConnection)){
            throw new SQLException("db is not an instance of sqlite.SQLiteConnection");
        }
    }

    /**
     * Retrieving all the websites stored in the database
     *
     * @return list of all the websites stored in the database
     * @throws SQLException     if db connection is null or not, an instance of sqlite or the statements were not executed.
     * */
    public List retrieveWebsites() throws SQLException {
        // Checking if we are connected to the database
        connectionCheck();
        try {
            List<String> websites = new ArrayList();
            Statement stmt = db.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT website_url FROM websites;");
            while (rs.next()) {
                websites.add(rs.getString("website_url"));
            }
            return websites;
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }
}
