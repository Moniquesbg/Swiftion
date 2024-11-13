package com.nhlstenden.swiftionapi.database.mysql;

import com.nhlstenden.swiftionapi.database.mysql.assets.Convert;

import java.sql.*;

/**
 * class that is responsible for executing queries/ calling procedures towards the mysql database
 */
public class StoredProcedure {

    /**
     * call a procedure from the database
     *
     * @param procedureCall procedure call to make
     * @param objectName    name of the object, e.g. : user, role, swift_copy etc.
     * @param json          should it be formatted using JSON (true/false)
     * @param xml           should it be formatted using XML (true/false)
     * @return a string formatted in JSON or XML format, if specified.
     */
    public static String call(String procedureCall, String[] params, String objectName, Boolean json, Boolean xml,String role) {
        Connection conn = new MySQL().getConnection(role);
        ResultSet resultSet;

        // execute query/call
        try {
            // stored procedure that is made in phpmyadmin
            PreparedStatement statement = conn.prepareCall(procedureCall);
            if (params != null) {
                setPreparedStatement(statement, params);
            }
            resultSet = statement.executeQuery();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e);
            return null;
        }

        if (json) {
            return Convert.toJson(resultSet, objectName).toString();
        } else if (xml) {
            return Convert.toXml(resultSet, objectName);
        } else {
            return null;
        }
    }

    /**
     * set the prepared statement query if a procedure contains any parameters
     * @param stmt statement to set the statement to
     * @param params parameters of the stored procedure
     */
    public static void setPreparedStatement(PreparedStatement stmt, String[] params) throws SQLException {
        for (int i = 1; i < params.length+1; i++) {
            stmt.setString(i, params[i-1]);
        }
    }

    /**
     * execute a transaction query. This can also be a normal query, however no results will be returned
     * @param transaction query to execute
     * @return true if query succeeded, otherwise false
     */
    public static Boolean executeTransaction(String transaction, String role) {
        Connection conn = new MySQL().getConnection(role);

        // execute query/call
        try {
            // stored procedure that is made in phpmyadmin
            PreparedStatement statement = conn.prepareStatement(transaction);
            statement.executeQuery();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e);
            return false;
        }
        return true;
    }
}