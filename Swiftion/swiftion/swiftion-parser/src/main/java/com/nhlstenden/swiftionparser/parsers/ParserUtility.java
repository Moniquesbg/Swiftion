package com.nhlstenden.swiftionparser.parsers;

/****************************************************************************************
 * This class contains all the constants for the tags used in the json and XML parser.
 ***************************************************************************************/
public class ParserUtility {
    // start tag
    public static final String FILETYPE = "MT940";

    // file header
    public static final String HEADER = "fileHeader"; // start
    public static final String MESSAGETYPE = "messageType";
    public static final String SENDER = "sender";
    public static final String RECIEVER = "receiver";
    public static final String MESSAGEPRIORITY = "messagePriority";
    public static final String APP_ID = "applicationId";
    public static final String SEQUENCE_NUMBER = "sequenceNumber";
    public static final String SESSION_NUMBER = "sessionNumber";

    // start of tags
    public static final String ACCOUNT_INFORMATION = "accountInformation";

    // GENERAL_TAGS
    public static final String GENERAL_TAG = "tag";
    public static final String DATE = "date";
    public static final String CURRENCY = "currency";
    public static final String AMOUNT = "amount";

    // TAG 20
    public static final String TRANSACTION_REF_NUM = "transactionReferenceNumber"; // start
    public static final String TRANSACTION_VALUE = "referenceNumber";

    // TAG 25
    public static final String ACCOUNT_IDENTIFICATION = "accountIdentification"; // start
    public static final String ACCOUNT_VALUE = "accountNumber";

    // TAG 28C
    public static final String STATEMENT_NUMBER = "statementNumber";
    public static final String SEQUENCE_VALUE = "sequenceNumber";

    // TAG 60F
    public static final String FINAL_OPENING_BALANCE = "finalOpeningBalance";

    // TRANSACTIONS
    public static final String TRANSACTIONS = "transactions";

    // TAG 61
    public static final String TRANSACTION = "transaction"; // start
    public static final String STATEMENT_LINE = "statementLine";
    public static final String VALUE_DATE = "valueDate";
    public static final String ENTRY_DATE = "entryDate";
    public static final String CD_MARK = "creditDebitType";
    public static final String TRANS_VALUE = "transactionValue";
    public static final String TRANS_TYPE = "transactionType";
    public static final String TRANSACTION_REFERENCE = "transactionReference";
    public static final String SUPPLEMENTARY_DETAILS = "supplementaryDetails";

    // TAG 86
    public static final String TRANSACTION_DETAILS = "transactionDetails";
    public static final String NARRATIVE = "narrative";

    // TAG 62F
    public static final String FINAL_CLOSING_BALANCE = "finalClosingBalance";

    // TAG 64
    public static final String AVAILABLE_BALANCE = "availableBalance";

    // output property XML
    public static final String OUTPUT_PROPERTY_P1 = "{http://xml.apache.org/xslt}indent-amount";
    public static final String OUTPUT_PROPERTY_P2 = "4";
}