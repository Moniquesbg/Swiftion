package com.nhlstenden.swiftionapi.database.mysql.assets;

import com.nhlstenden.swiftionapi.database.mysql.StoredProcedure;
import com.prowidesoftware.swift.model.field.*;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;

import java.util.ArrayList;
import java.util.List;

public class SwiftCopyQueryBuilder {


    // insert swift copy
    public static Boolean addSwiftCopyDataToMysql(MT940 mt, String userId, String role){
        ArrayList<String> procedureCalls = new ArrayList<>();

        // add variables that are later used when calling stored procedures
        procedureCalls.add(getSwiftCopyVariables());

        // table 60f_opening_balance
        procedureCalls.add(build60f(mt.getField60F()));

        // table 62f
        procedureCalls.add(build62f(mt.getField62F()));

        // table 64
        procedureCalls.add(build64(mt.getField64()));

        // table swift_copy
        procedureCalls.add(buildSwiftCopyBase(mt, userId));

        // table 61 (transactions)
        procedureCalls.add(buildTransaction(mt));

        System.err.println(transactionBuilder(procedureCalls));

        return StoredProcedure.executeTransaction(transactionBuilder(procedureCalls), role);
    }


    private static String buildSwiftCopyBase(MT940 f, String userId){
        String swiftCopyCall = String.format(
                "CALL `insertSwiftCopyBase`(@swCopyId, %s, '%s', '%s', '%s', @id62f, @id60f, @id64);",
                userId,
                f.getField20().getReference(),
                f.getField25().getAccount(),
                f.getField28C().getStatementNumber()
        );
        return swiftCopyCall;
    }

    private static String build60f(Field60F f){

        String table60F = String.format(
                "CALL `insert60F`(@id60f, %d, %d, '%s', '%s');",
                getDcMarkId(f.getDCMark()),
                getCurrencyTypeId(f.getCurrency()),
                f.getDate(),
                f.getAmount()
        );

        return table60F;
    }

    private static String build62f(Field62F f){

        String table62F = String.format(
                "CALL `insert62F`(@id62f, %d, %d, '%s', '%s');",
                getDcMarkId(f.getDCMark()),
                getCurrencyTypeId(f.getCurrency()),
                f.getDate(),
                f.getAmount()
        );

        return table62F;
    }

    private static String build64(Field64 f){

        String table64 = String.format(
                "CALL `insert64`(@id64, %d, %d, '%s', '%s');",
                getDcMarkId(f.getDCMark()),
                getCurrencyTypeId(f.getCurrency()),
                f.getDate(),
                f.getAmount()
        );

        return table64;
    }

    private static String buildTransaction(MT940 f){
        StringBuilder transactionCalls = new StringBuilder();
        for (int i = 0; i < f.getField61().size(); i++) {
            Field61 t = f.getField61().get(i);
            Field86 n = f.getField86().get(i);

            String transactionCall = String.format(
                    "CALL `insert61`(@swCopyId, '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                    getDcMarkId(t.getDebitCreditMark()),
                    t.getValueDate(),
                    t.getEntryDate(),
                    t.getAmount(),
                    t.getTransactionType(),
                    t.getIdentificationCode(),
                    t.getReferenceForTheAccountOwner(),
                    t.getReferenceOfTheAccountServicingInstitution(),
                    t.getSupplementaryDetails(),
                    getT86Info(n.getNarrative())
                    );
            transactionCalls.append(transactionCall);
        }
        return transactionCalls.toString();
    }

    private static Integer getDcMarkId(String mark){
        return switch (mark.toLowerCase()) {
            case "d" -> 1;
            case "c" -> 2;
            default -> null;
        };
    }

    private static Integer getCurrencyTypeId(String currencyType){
        return switch (currencyType.toLowerCase()) {
            case "eur" -> 1;
            case "usd" -> 2;
            default -> null;
        };
    }

    private static String getSwiftCopyVariables(){
        String variables =
                "SELECT COALESCE((SELECT MAX(id) + 1 as id FROM `60f_opening_balance`), 1) INTO @id60f;" +
                "SELECT COALESCE((SELECT MAX(id) + 1 as id FROM `62f_closing_balance`), 1) INTO @id62f;" +
                "SELECT COALESCE((SELECT MAX(id) + 1 as id FROM `64_closing_available_balance`), 1) INTO @id64;" +
                "SELECT COALESCE((SELECT MAX(id) + 1 as id FROM `swift_copy`), 1) INTO @swCopyId;";

        return variables;
    }
    private static String transactionBuilder(ArrayList<String> procedureCalls){
        StringBuilder transaction = new StringBuilder();
        transaction.append("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE;");
        transaction.append("START TRANSACTION;");
        for (String procedureCall : procedureCalls) {
            transaction.append(procedureCall);
        }
        transaction.append("COMMIT;");
        return transaction.toString();
    }

    private static String getT86Info(String narrative){
        StringBuilder result = new StringBuilder();
        String[] lineSplitted = narrative.split("/");
        for (String s : lineSplitted) {
            s = s.replaceAll("\\p{C}", "");
            result.append(s).append("\n");
        }
        return result.toString();
    }
}