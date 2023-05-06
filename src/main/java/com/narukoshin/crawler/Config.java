package com.narukoshin.crawler;

import java.sql.SQLException;
import java.util.List;

public class Config {
    /**
     * Database connection
     * */
    private Database db;
    /**
     * A query string containing search parameter
     */
    static String queryString = "site:.lv";
    /**
     * A cookie value that will be added in the request
     */
    static String cookieValue = "CONSENT=PENDING+374; SID=WQhvDWKzEO9Lnxwm6GPA89qDgGfjqG-HPpX7GEStYumFL0Zi9k_hQUFHm8f6RZukUOCS1A.; __Secure-1PSID=WQhvDWKzEO9Lnxwm6GPA89qDgGfjqG-HPpX7GEStYumFL0ZiN0oi4_S4H0ixVI98Nh37tQ.; __Secure-3PSID=WQhvDWKzEO9Lnxwm6GPA89qDgGfjqG-HPpX7GEStYumFL0Zi-eqN4NcjYD2XAzZ1Dc7EPw.; HSID=AioHdDacFzjIcWDzX; SSID=AKGes7vEaNj-pmhe-; APISID=Mc5q5XY-KX_pjYF1/AOihTYgjgxUqG0k9s; SAPISID=ha5p5g_bA_MAakxh/AP0HNYnli5MaDEU6i; __Secure-1PAPISID=ha5p5g_bA_MAakxh/AP0HNYnli5MaDEU6i; __Secure-3PAPISID=ha5p5g_bA_MAakxh/AP0HNYnli5MaDE…oBe_xv5Jmb7K98SYgN0Afktd6bD0n6LP6pgngc7wwQ6eBGIA2IzoA; NID=511=fbGkDHLbQRPLAEkFf-oF-KZxtKw_6bTtfWn4WF9yE6n2j7yKu_LQx1KbD0NYYla51HczH-bKCxceJu79PnuYLTPgfrYrCF0x3qjRPlH-25co4sj7vsRfeccRaIq3bLQD1jeJJoPuvfO7l7VnT8yN39AoeGSp9k_ygJkG1hJMZEoEt6ShFEwZlSY84w8NTVeheQz3u2hG; 1P_JAR=2023-5-4-21; GOOGLE_ABUSE_EXEMPTION=ID=5d49c86260660a48:TM=1683229680:C=r:IP=85.254.89.244-:S=bd4VdWSuBRy9Xxll7lVlkB8; OGPC=19022519-1:; OGP=-19022519:; DV=k7GoBFuRbdMgQAgxTl7ANXbg_wmJfljjuvLUHP2URQAAAAA; OTZ=7015500_44_48_123900_44_436380";
    /**
     * Bool value that will indicate if the crawler is currently running or stopped.
     */
    static boolean isRunning = false;
    /**
     * Additional Google parameters for better matching, for example, filtering by the latest updates by this week.
     */
    static String additionalParameters = "&tbs=qdr:w";
    /**
     * Available settings to change
     * */
    private final String[] availableSettings = {
            "queryString",
            "cookieValue",
            "additionalParameters"
    };

    private static Config configInstance;

    /**
     * Setting a static config instance
     *
     * @param config    Config instance
     * */
    public static void setInstance(Config config){
        configInstance = config;
    }

    /**
     * This method is to get a static config instance with all existing values.
     *
     * @return      Returning a static config instance
     * */
    public static Config getInstance() {
       return configInstance;
    }

    /**
     * Setting the database instance
     *
     * @param db    the database instance
     */
    public void setDatabase(Database db) {
        this.db = db;
    }

    /**
     * Changing the status of the crawler.
     *
     * @param status    the new status of the crawler
     */
    public static void setRunning(boolean status) {
        isRunning = status;
    }

    /**
     * This method will create basic settings in the database
     *
     * @throws SQLException     if an error occurs during creating the settings
     * */
    protected void createSettings() throws SQLException {
        // If the database is just created,
        // There will be no settings in the database.
        // We have to create them now.
        if (db.justCreated){
            // Adding settings with default settings
            try {
                db.createSetting("queryString", queryString);
                db.createSetting("cookieValue", cookieValue);
                db.createSetting("additionalParameters", additionalParameters);
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }
    }

    /**
     * Getting the setting value from the database
     *
     * @param name      name of the setting to retrieve
     * */
    public String getSetting(String name) throws SQLException {
        try {
            return db.retrievingSetting(name);
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    /**
     * This method updates the setting values
     *
     * @param name          the name of the setting
     * @param value         the value to set
     * @throws SQLException if an error occurs during setting the setting.
     * */
    public void updateSetting(String name, String value) throws SQLException {
        // Checking if the setting is in the allowed list to change.
        for (String element : availableSettings) {
            if (element.equals(name)) {
                // Updating the value in the database.
                this.db.updateSetting(name, value);
                return;
            }
        }
    }
    /**
     * On-save-button click all the websites in the table will be saved in the database.
     *
     * @param name      URL or name of the website to save
     * @throw SQLException if an error occurs during adding the website to the database
     * */
    public void addWebsite(String name) throws SQLException {
        this.db.insertWebsite(name);
    }

    /**
     * When the application starts,
     * It will load all the websites stored in the database and insert in the table to keep progress.
     *
     * */
    public List getWebsites() throws SQLException {
        return this.db.retrieveWebsites();
    }
}
