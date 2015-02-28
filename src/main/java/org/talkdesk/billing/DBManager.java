package org.talkdesk.billing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * Created by ashansa on 2/27/15.
 */
public class DBManager {

    private static Log log = LogFactory.getLog(DBManager.class);

    private String dbUrl = "jdbc:mysql://localhost:3306/billing_charges";
    private String userName = "root";
    private String password = "root";
    private String chargesTable = "Charges";
    private String callHistoryTable = "CallHistory";
    private Connection dbConnection = null;
    private Properties dbProperties = new Properties();

    public DBManager() throws IOException {
        loadDBInfo();
        createConnection();
    }

    private void loadDBInfo() throws IOException {
        InputStream input = null;
        try {
            input = getClass().getResourceAsStream("/db.properties");
            // load a properties file
            dbProperties.load(input);

        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean createConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            dbUrl = "jdbc:mysql://" + dbProperties.getProperty("host") + ":3306/" + dbProperties.getProperty("dbname");
            userName = dbProperties.getProperty("dbuser");
            password = dbProperties.getProperty("userPassword");
            dbConnection = DriverManager.getConnection(dbUrl, userName, password);
        } catch (Exception ex) {
            log.error("error while establishing the connection. " + ex);
        }
        return (dbConnection != null);
    }

    public ResultSet getChargeDetails(String countryCode) throws SQLException {
        PreparedStatement getChargeDetails = null;
        String getQuery = "select * from " + chargesTable + " where COUNTRY_CODE=" + "\"" + countryCode + "\"";

        try {
            getChargeDetails = dbConnection.prepareStatement(getQuery);
            ResultSet results = getChargeDetails.executeQuery();
            return results;
        } catch (SQLException e) {
            log.debug("Failed to retrieve the results");
            throw e;
        }
    }

    public void addCallEntriesToUser(String accountId, String customerNo, String talkdeskNo, String forwardedNo,
                                     double callDuration, double charge) throws SQLException {
        String insertQuery = "insert into " + callHistoryTable + " VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement addCallToUser = null;

            addCallToUser = dbConnection.prepareStatement(insertQuery);
            addCallToUser.setString(1, accountId);
            addCallToUser.setString(2, customerNo);
            addCallToUser.setString(3, talkdeskNo);
            addCallToUser.setString(4, forwardedNo);
            addCallToUser.setString(5, String.valueOf(callDuration));
            addCallToUser.setString(6, String.valueOf(charge));

            addCallToUser.executeUpdate();
        } catch (SQLException ex) {
            log.debug("Error in inserting call entry for user. " + ex);
            throw ex;
        }
    }

    public ResultSet getCallHistory(String accountId) throws SQLException {
        PreparedStatement callHistory = null;
        String getQuery = "select * from " + callHistoryTable + " where ACCOUNT_ID=" + "\"" + accountId + "\"";

        try {
            callHistory = dbConnection.prepareStatement(getQuery);
            ResultSet results = callHistory.executeQuery();
            return results;
        } catch (SQLException e) {
            log.debug("Failed to retrieve the results");
            throw e;
        }
    }

    public void populateDBs() throws SQLException, IOException {
        createCallHistoryTable();
        createChargesTable();
    }

    private void createCallHistoryTable() throws SQLException {
        PreparedStatement deleteTable = null;
        String deleteQuery = "DROP TABLE IF EXISTS " + callHistoryTable;

        try {
            deleteTable = dbConnection.prepareStatement(deleteQuery);
            deleteTable.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete table. " + e);
            throw e;
        }

        PreparedStatement createTable = null;
        String createQuery = "CREATE TABLE IF NOT EXISTS " + callHistoryTable + "(ACCOUNT_ID VARCHAR(225) NOT NULL, "
                + "CUSTOMER_NO VARCHAR(20) NOT NULL," + "TALKDESK_NO VARCHAR(20) NOT NULL," + "FORWARDED_NO VARCHAR(20),"
                + "DURATION VARCHAR(10) NOT NULL," + "CHARGE VARCHAR(10) NOT NULL" + ")";
        try {
            createTable = dbConnection.prepareStatement(createQuery);
            createTable.executeUpdate();
        } catch (SQLException e) {
            log.error("table not created");
            throw e;
        }
    }

    private void createChargesTable() throws SQLException, IOException {
        PreparedStatement deleteTable = null;
        String deleteQuery = "DROP TABLE IF EXISTS " + chargesTable;

        try {
            deleteTable = dbConnection.prepareStatement(deleteQuery);
            deleteTable.executeUpdate();
        } catch (SQLException e) {
            log.error("Failed to delete table. " + e);
            throw e;
        }

        PreparedStatement createTable = null;
        String createQuery = "CREATE TABLE IF NOT EXISTS " + chargesTable + "(COUNTRY_CODE VARCHAR(225) NOT NULL, "
                + "CHARGE VARCHAR(20) NOT NULL, " + "PREFIXES LONGTEXT NOT NULL" + ")";

        try {
            createTable = dbConnection.prepareStatement(createQuery);
            createTable.executeUpdate();
        } catch (SQLException e) {
            log.error("table not created");
            throw e;
        }

        BufferedReader br = new BufferedReader(new FileReader(dbProperties.getProperty("rates_file")));
        String line = br.readLine();
        while (line != null) {
            String[] splitted = line.split(",");
            String insertQuery = "insert into " + chargesTable + " VALUES (?,?,?)";
            try {
                PreparedStatement addEntry = null;

                addEntry = dbConnection.prepareStatement(insertQuery);
                addEntry.setString(1, splitted[0]);
                addEntry.setString(2, splitted[1]);
                addEntry.setString(3, line.replace(splitted[0].concat(",").concat(splitted[1].concat(",")), ""));
                addEntry.executeUpdate();
            } catch (Exception ex) {
                log.error("Error in inserting the country code charges. " + ex);
            }
            line = br.readLine();
        }
    }
}
