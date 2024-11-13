package com.nhlstenden.swiftionapi.database.mapper;

import com.nhlstenden.swiftionapi.converter.Converter;

/*****************************************************************
 * The mapper class is responsible for mapping a name to a
 * procedure call, preparing a query and
 * returning the correct procedure call.
 ****************************************************************/
public class Mapper {

    /**
     * if a condition isn't met in a request to the API, return an error
     *
     * @param name name of the error
     * @return a json string which returns the error
     */
    public static String exception(String name, boolean json, boolean xml) {
        String mode;
        if (json) {
            mode = "json";
        } else {
            mode = "xml";
        }

        return switch (name) {
            case "name-error" ->
                    Converter.buildReturnMessage(
                            mode,
                            "unknown name parameter, please try again with a different name",
                            "failed"
                    );
            case "auth-error" ->
                    Converter.buildReturnMessage(
                            mode,
                            "unable to authenticate, please try to authenticate with a valid api key",
                            "failed"
                    );
            case "mysql-error" ->
                    Converter.buildReturnMessage(
                            mode,
                            "something went wrong with calling the procedure, please contact your system admin",
                            "failed"
                    );
            case "format-error" ->
                    Converter.buildReturnMessage(
                            mode,
                            "no format was specified for retrieving the data",
                            "failed"
                    );
            case "parse-error" ->
                    Converter.buildReturnMessage(
                            mode,
                            "no data format is specified, or both have been specified as true. Please select XML or JSON as a parameter",
                            "failed"
                    );
            default ->
                    Converter.buildReturnMessage(
                            mode,
                            "unknown error occured, please write a detailed report of what happened and send it to the system administrator",
                            "failed"
                    );
        };
    }

    /**
     * request a procedure that doesn't need any parameter, e.g. : retrieving all users for user-overview
     *
     * @param name name of the procedure
     * @return procedure call
     */
    public static String nameToProcedure(String name) {
        if (name == null) {
            return null;
        }

        return switch (name) {
            case "test-roles" -> "{CALL `testGetRoles`()}";
            case "get-roles" -> "{CALL `getRoles`()}";
            case "get-users" -> "{CALL `getUsers`()}";
            case "get-mt940-overview" -> "{CALL `getMt940Overview`()}";
            case "get-cost-centers" -> "{CALL `getCostCenters`()}";
            case "get-currencies" -> "{CALL `getCurrencies`()}";
            case "get-card-types" -> "{CALL `getCardTypes`()}";
            case "get-custom-transactions" -> "{CALL `getCustomTransactions`()}";
            case "get-mt940-transactions" -> "{CALL `getMt940Transactions`()}";
            case "get-plugins" -> "{CALL `getPlugins`()}";
            case "get-cost-center-mt940-transaction" -> "{CALL `getSwiftCopyTransactionCostCenter`()}";
            case "get-cost-center-custom-transaction" -> "{CALL `getCustomTransactionCostCenter`()}";
            case "get_communaction" -> "{CALL `getCommunication`()}";

            default -> null;
        };
    }

    /**
     * convert names to procedure, this method must make use of parameters!
     *
     * @param name       name of the procedure
     * @param parameters parameters (max parameters is 3)
     * @return the correct procedure call
     */
    public static String nameToProcedure(String name, String[] parameters) {
        if (parameters.length == 1) {
            return storedProcedureOneParameter(name, parameters);
        } else if (parameters.length == 2) {
            return storedProcedureTwoParameters(name, parameters);
        } else if (parameters.length > 2) {
            return storedProcedureManyParameters(name, parameters);
        } else {
            return null;
        }
    }

    /**
     * used for stored procedures that carry -- 1 -- parameter
     *
     * @param name       name of the API path
     * @param parameters parameters of the stored procedures
     * @return the correct stored procedure call
     */
    private static String storedProcedureOneParameter(String name, String[] parameters) {
        return switch (name) {
            case "update-communication" ->
                    String.format(
                            "{CALL `updateCommunication`(%s)}",
                            prepareQuery(parameters)
                    );
            case "get-user-login" ->
                    String.format(
                            "{CALL `getUserLogin()`(%s)}",
                            prepareQuery(parameters)
                    );
            case "get-statement" ->
                    String.format(
                            "{CALL `getStatement`(%s)}",
                            prepareQuery(parameters)
                    );
            case "get-custom-transaction" ->
                    String.format(
                            "{CALL `getCustomTransaction`(%s)}",
                            prepareQuery(parameters)
                    );
            case "delete-custom-transaction" ->
                    String.format(
                            "{CALL `deleteCustomTransaction`(%s)}",
                            prepareQuery(parameters)
                    );
            case "delete-swift-copy" ->
                    String.format(
                            "{CALL `deleteSwiftCopy`(%s)}",
                            prepareQuery(parameters)
                    );
            case "get-transaction" ->
                    String.format(
                            "{CALL `getTransaction`(%s)}",
                            prepareQuery(parameters)
                    );
            case "get-user" ->
                    String.format(
                            "{CALL `getUser`(%s)}",
                            prepareQuery(parameters)
                    );
            case "get-current-password" ->
                    String.format(
                            "{CALL `getCurrentPassword`(%s)}",
                            prepareQuery(parameters)
                    );
            case "insert-cost-center" ->
                    String.format(
                            "{CALL `insertCostCenter`(%s)}",
                            prepareQuery(parameters)
                    );
            case "delete-cost-center" ->
                    String.format("{CALL `deleteCostCenter`(%s)}",
                            prepareQuery(parameters)
                    );
            default -> null;
        };
    }

    /**
     * used for stored procedures that carry -- 2 -- parameters
     *
     * @param name       name of the API path
     * @param parameters parameters of the stored procedures
     * @return the correct stored procedure call
     */
    private static String storedProcedureTwoParameters(String name, String[] parameters) {
        return switch (name) {
            case "update-user-role" ->
                    String.format(
                            "{CALL `updateUserRole`(%s)}",
                            prepareQuery(parameters)
                    );
            case "update-password" ->
                    String.format(
                            "{CALL `updatePassword`(%s)}",
                            prepareQuery(parameters)
                    );
            case "update-plugin" ->
                    String.format(
                            "{CALL `updatePlugin`(%s)}",
                            prepareQuery(parameters)
                    );
            default -> null;
        };
    }

    /**
     * used for stored procedures that carry -- MANY (more then 2) -- parameters
     *
     * @param name       name of the API path
     * @param parameters parameters of the stored procedures
     * @return the correct stored procedure call
     */
    private static String storedProcedureManyParameters(String name, String[] parameters) {
        return switch (name) {
            case "insert-custom-transaction" ->
                    String.format(
                            "{CALL `insertCustomTransaction`(%s)}",
                            prepareQuery(parameters)
                    );
            case "update-custom-transaction" ->
                    String.format(
                            "{CALL `updateCustomTransaction`(%s)}",
                            prepareQuery(parameters)
                    );
            case "update-transaction" ->
                    String.format(
                            "{CALL `updateTransaction`(%s)}",
                            prepareQuery(parameters)
                    );
            case "update-username" ->
                    String.format(
                            "{CALL `updateUsername`(%s)}",
                            prepareQuery(parameters)
                    );
            case "insert-user" ->
                    String.format(
                            "{CALL `insertUser`(%s)}",
                            prepareQuery(parameters)
            );
            case "update-user" ->
                    String.format(
                            "{CALL `updateUser`(%s)}",
                            prepareQuery(parameters)
                    );
            default -> null;
        };
    }

    /**
     * prepare a query (used for prepared statements)
     * @param params that are used for the prepared statement
     * @return a prepared query
     */
    private static String prepareQuery(String[] params) {
        String result = "";
        for (int i = 0; i < params.length; i++) {
            if (i == params.length - 1) {
                result += "?";
            } else {
                result += "?,";
            }
        }
        return result;
    }
}