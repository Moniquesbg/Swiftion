package com.nhlstenden.swiftionparser.parsers;

import com.prowidesoftware.swift.model.field.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;

/****************************************************************************************
 * This class is used to build a json object from a MT940 file
 * The json object is built using the org.json library
 * The json object is then converted to a string and returned
 ****************************************************************************************/
public class MT940JsonBuilder {

    /**
     * This method is used to build a json object from a MT940 file
     * @param file the MT940 file to be converted to a json object
     * @return the json object as a string
     */
    public static String buildMT940Json(MT940 file) {
        try {
            JSONObject root = new JSONObject();

            // build file header
            root.put(ParserUtility.HEADER, buildFileHeader(file));

            // build rest of the file
            root.put(ParserUtility.ACCOUNT_INFORMATION, buildaccountInfo(file));

            // return the json object to a string
            String json = root.toString(4);
            System.out.println(json);

            return json;

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method is used to build the file header
     * @param file the MT940 file to be converted to a json object
     * @return the json object containing the file header
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildFileHeader(MT940 file) throws JSONException {
        JSONObject fileHeader = new JSONObject();

        fileHeader.put(ParserUtility.MESSAGETYPE, file.getMessageType());
        fileHeader.put(ParserUtility.SENDER, file.getSender());
        fileHeader.put(ParserUtility.RECIEVER, file.getReceiver());
        fileHeader.put(ParserUtility.MESSAGEPRIORITY, file.getMessagePriority());
        fileHeader.put(ParserUtility.APP_ID, file.getApplicationId());
        fileHeader.put(ParserUtility.SEQUENCE_NUMBER, file.getSequenceNumber());
        fileHeader.put(ParserUtility.SESSION_NUMBER, file.getSessionNumber());

        return fileHeader;
    }

    /**
     * This method is used to build the account information
     * @param file the MT940 file to be converted to a json object
     * @return the json object containing the account information
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildaccountInfo(MT940 file) throws JSONException {
        JSONObject accountInfo = new JSONObject();

        accountInfo.put(ParserUtility.TRANSACTION_REF_NUM, buildT20(file.getField20()));
        accountInfo.put(ParserUtility.ACCOUNT_IDENTIFICATION, buildT25(file.getField25()));
        accountInfo.put(ParserUtility.STATEMENT_NUMBER, buildT28C(file.getField28C()));
        accountInfo.put(ParserUtility.FINAL_OPENING_BALANCE, buildT60F(file.getField60F()));
        accountInfo.put(ParserUtility.TRANSACTIONS, buildTransactions(file));
        accountInfo.put(ParserUtility.FINAL_CLOSING_BALANCE, buildT62F(file.getField62F()));
        accountInfo.put(ParserUtility.AVAILABLE_BALANCE, buildT64(file.getField64()));

        return accountInfo;
    }

    /**
     * This method is used to build tag20
     * @param f20 the final closing balance field
     * @return the json object containing the final closing balance
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT20(Field20 f20) throws JSONException {
        JSONObject transactionRefNum = new JSONObject();

        transactionRefNum.put(ParserUtility.GENERAL_TAG, f20.getName());
        transactionRefNum.put(ParserUtility.TRANSACTION_VALUE, f20.getReference());

        return transactionRefNum;
    }

    /**
     * This method is used to build the account identification
     * @param f25 the account identification field
     * @return the json object containing the account identification
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT25(Field25 f25) throws JSONException {
        JSONObject accountIdentification = new JSONObject();

        accountIdentification.put(ParserUtility.GENERAL_TAG, f25.getName());
        accountIdentification.put(ParserUtility.ACCOUNT_VALUE, f25.getAccount());

        return accountIdentification;
    }

    /**
     * This method is used to build the Tag 28c
     * @param f28c the statement number field
     * @return the json object containing the field 28c content
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT28C(Field28C f28c) throws JSONException {
        JSONObject statentNum = new JSONObject();

        statentNum.put(ParserUtility.GENERAL_TAG, f28c.getName());
        statentNum.put(ParserUtility.STATEMENT_NUMBER, f28c.getStatementNumber());
        statentNum.put(ParserUtility.SEQUENCE_NUMBER, f28c.getSequenceNumber());

        return statentNum;
    }

    /**
     * This method is used to build tag 60F
     * @param f60f the tagg 60F field
     * @return the json object containing the final closing balance
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT60F(Field60F f60f) throws JSONException {
        JSONObject openingBalance = new JSONObject();

        openingBalance.put(ParserUtility.GENERAL_TAG, f60f.getName());
        openingBalance.put(ParserUtility.CD_MARK, f60f.getDCMark());
        openingBalance.put(ParserUtility.DATE, f60f.getDate());
        openingBalance.put(ParserUtility.CURRENCY, f60f.getCurrency());
        openingBalance.put(ParserUtility.AMOUNT, f60f.getAmount());

        return openingBalance;
    }

    /**
     * This method is used to build transactions from the file
     * @param file mt940 file
     * @return the json object containing the transactions
     * @throws JSONException when the json object cannot be created
     */
    private static JSONArray buildTransactions(MT940 file) throws JSONException {
        JSONArray transactions = new JSONArray();

        // equal num of field61 and field86
        for (int i = 0; i < file.getField61().size(); i++) {
            JSONObject transactionDetails = new JSONObject();


            Field61 f61 = file.getField61().get(i);
            Field86 f86 = file.getField86().get(i);

            // tag 61
            transactionDetails.put(ParserUtility.STATEMENT_LINE, buildT61(f61));

            // tag 86
            transactionDetails.put(ParserUtility.TRANSACTION_DETAILS, buildT86(f86));

            // append to tranactions jsonobject
            JSONObject transaction = new JSONObject();
            transaction.put(ParserUtility.TRANSACTION, transactionDetails);

            transactions.put(transaction);
        }

        return transactions;
    }

    /**
     * This method is used to build tag 61
     * @param f61 the tag 61 field
     * @return the json object containing the final closing balance
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT61(Field61 f61) throws JSONException {
        JSONObject statementLine = new JSONObject();

        statementLine.put(ParserUtility.GENERAL_TAG, f61.getName());
        statementLine.put(ParserUtility.VALUE_DATE, f61.getValueDate());
        statementLine.put(ParserUtility.ENTRY_DATE, f61.getEntryDate());
        statementLine.put(ParserUtility.CD_MARK, f61.getDebitCreditMark());
        statementLine.put(ParserUtility.TRANS_VALUE, f61.getAmount());
        statementLine.put(ParserUtility.TRANS_TYPE, f61.getTransactionType());
        statementLine.put(ParserUtility.TRANSACTION_REFERENCE, f61.getReferenceForTheAccountOwner());
        statementLine.put(ParserUtility.SUPPLEMENTARY_DETAILS, f61.getSupplementaryDetails());

        return statementLine;
    }

    /**
     * This method is used to build tag 86
     * @param f86 the tag 86 field
     * @return the json object containing the 86 tag content
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT86(Field86 f86) throws JSONException {
        JSONObject transactionDetail = new JSONObject();

        transactionDetail.put(ParserUtility.GENERAL_TAG, f86.getName());
        transactionDetail.put(ParserUtility.TRANSACTION_DETAILS, f86.getNarrative());

        return transactionDetail;
    }

    /**
     * This method is used to build tag 62F
     * @param f62f the tag 62F field
     * @return the json object containing the 62F tag content
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT62F(Field62F f62f) throws JSONException {
        JSONObject finalClosingBalance = new JSONObject();

        finalClosingBalance.put(ParserUtility.GENERAL_TAG, f62f.getName());
        finalClosingBalance.put(ParserUtility.CD_MARK, f62f.getDCMark());
        finalClosingBalance.put(ParserUtility.DATE, f62f.getDate());
        finalClosingBalance.put(ParserUtility.CURRENCY, f62f.getCurrency());
        finalClosingBalance.put(ParserUtility.AMOUNT, f62f.getAmount());

        return finalClosingBalance;
    }

    /**
     * This method is used to build tag 64
     * @param f64 the tag 64 field
     * @return the json object containing the 64 tag content
     * @throws JSONException when the json object cannot be created
     */
    private static JSONObject buildT64(Field64 f64) throws JSONException {
        JSONObject availableBalance = new JSONObject();

        availableBalance.put(ParserUtility.GENERAL_TAG, f64.getName());
        availableBalance.put(ParserUtility.CD_MARK, f64.getDCMark());
        availableBalance.put(ParserUtility.DATE, f64.getDate());
        availableBalance.put(ParserUtility.CURRENCY, f64.getCurrency());
        availableBalance.put(ParserUtility.AMOUNT, f64.getAmount());

        return availableBalance;
    }
}