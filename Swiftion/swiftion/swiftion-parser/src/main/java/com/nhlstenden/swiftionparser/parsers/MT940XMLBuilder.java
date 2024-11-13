package com.nhlstenden.swiftionparser.parsers;

import com.prowidesoftware.swift.model.mt.mt9xx.MT940;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;

/****************************************************************************************
 * This class is used to build an XML string from a MT940 file.
 ****************************************************************************************/
public class MT940XMLBuilder {

    /**
     * This method is used to build an XML string from a MT940 file.
     * @param file The MT940 file to be converted to XML.
     * @return The XML string.
     */
    public static String buildMT940XML(MT940 file) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            //Elements
            Element root = document.createElement(ParserUtility.FILETYPE);
            document.appendChild(root);

            Element header = document.createElement(ParserUtility.HEADER);
            root.appendChild(header);

            Element messageType = document.createElement(ParserUtility.MESSAGETYPE);
            messageType.appendChild((document.createTextNode(file.getMessageType())));
            header.appendChild(messageType);

            Element sender = document.createElement(ParserUtility.SENDER);
            sender.appendChild((document.createTextNode(file.getSender())));
            header.appendChild(sender);

            Element receiver = document.createElement(ParserUtility.RECIEVER);
            receiver.appendChild((document.createTextNode(file.getReceiver())));
            header.appendChild(receiver);

            Element messagePriority = document.createElement(ParserUtility.MESSAGEPRIORITY);
            messagePriority.appendChild((document.createTextNode(file.getMessagePriority())));
            header.appendChild(messagePriority);

            Element applicationId = document.createElement(ParserUtility.APP_ID);
            applicationId.appendChild((document.createTextNode(file.getApplicationId())));
            header.appendChild(applicationId);

            Element sequenceNumber = document.createElement(ParserUtility.SEQUENCE_NUMBER);
            sequenceNumber.appendChild((document.createTextNode(file.getSequenceNumber())));
            header.appendChild(sequenceNumber);

            Element sessionNumber = document.createElement(ParserUtility.SESSION_NUMBER);
            sessionNumber.appendChild((document.createTextNode(file.getSessionNumber())));
            header.appendChild(sessionNumber);
            // End Header

            Element accountInformation = document.createElement(ParserUtility.ACCOUNT_INFORMATION);
            root.appendChild(accountInformation);

            //Tag 20
            Element transactionRef = document.createElement(ParserUtility.TRANSACTION_REF_NUM);

            Element transactionTag = document.createElement(ParserUtility.GENERAL_TAG);
            transactionTag.appendChild((document.createTextNode(file.getField20().NAME)));
            transactionRef.appendChild(transactionTag);

            Element transactionValue = document.createElement(ParserUtility.TRANSACTION_VALUE);
            transactionValue.appendChild((document.createTextNode(file.getField20().getReference().replaceAll("\r", ""))));
            transactionRef.appendChild(transactionValue);

            accountInformation.appendChild(transactionRef);
            // End 20

            //Tag 25
            Element accountIdentification = document.createElement(ParserUtility.ACCOUNT_IDENTIFICATION);
            Element accountTag = document.createElement(ParserUtility.GENERAL_TAG);
            accountTag.appendChild((document.createTextNode(file.getField25().NAME)));
            accountIdentification.appendChild(accountTag);

            Element accountValue = document.createElement(ParserUtility.ACCOUNT_VALUE);
            accountValue.appendChild((document.createTextNode(file.getField25().getAccount().replaceAll("\r", ""))));
            accountIdentification.appendChild(accountValue);

            accountInformation.appendChild(accountIdentification);
            // End 25

            //Tag 28C
            Element statementNumber = document.createElement(ParserUtility.STATEMENT_NUMBER);
            Element statementTag = document.createElement(ParserUtility.GENERAL_TAG);

            statementTag.appendChild((document.createTextNode(file.getField28C().NAME)));
            statementNumber.appendChild(statementTag);

            Element statementValue = document.createElement(ParserUtility.SEQUENCE_NUMBER);
            statementValue.appendChild((document.createTextNode(file.getField28C().getStatementNumber().replaceAll("\r", ""))));
            statementNumber.appendChild(statementValue);

            Element SequenceValue = document.createElement(ParserUtility.SEQUENCE_VALUE);
            SequenceValue.appendChild((document.createTextNode(file.getField28C().getSequenceNumber())));
            statementNumber.appendChild(SequenceValue);

            accountInformation.appendChild(statementNumber);
            // End 28C

            //Tag 60F
            Element finalOpeningBalance = document.createElement(ParserUtility.FINAL_OPENING_BALANCE);

            Element openingTag = document.createElement(ParserUtility.GENERAL_TAG);
            openingTag.appendChild((document.createTextNode(file.getField60F().NAME)));
            finalOpeningBalance.appendChild(openingTag);

            Element CDType = document.createElement(ParserUtility.CD_MARK);
            CDType.appendChild((document.createTextNode(file.getField60F().getDCMark())));
            finalOpeningBalance.appendChild(CDType);

            Element date = document.createElement(ParserUtility.DATE);
            date.appendChild((document.createTextNode(file.getField60F().getDate())));
            finalOpeningBalance.appendChild(date);

            Element currency = document.createElement(ParserUtility.CURRENCY);
            currency.appendChild((document.createTextNode(file.getField60F().getCurrency())));
            finalOpeningBalance.appendChild(currency);

            Element amount = document.createElement(ParserUtility.AMOUNT);
            amount.appendChild((document.createTextNode(file.getField60F().getAmount().replaceAll("\r", ""))));
            finalOpeningBalance.appendChild(amount);

            accountInformation.appendChild(finalOpeningBalance);

            // End 60F
            Element transactions = document.createElement(ParserUtility.TRANSACTIONS);
            accountInformation.appendChild(transactions);

            //Tag 61
            for (int i = 0; i < file.getField61().size(); i++) {
                Element transaction = document.createElement(ParserUtility.TRANSACTION);

                Element statementLine = document.createElement(ParserUtility.STATEMENT_LINE);

                Element transstatementTag = document.createElement(ParserUtility.GENERAL_TAG);
                transstatementTag.appendChild((document.createTextNode(file.getField61().get(i).getName())));
                statementLine.appendChild(transstatementTag);

                Element valueDate = document.createElement(ParserUtility.VALUE_DATE);
                valueDate.appendChild((document.createTextNode(file.getField61().get(i).getValueDate())));
                statementLine.appendChild(valueDate);

                Element entryDate = document.createElement(ParserUtility.ENTRY_DATE);
                entryDate.appendChild((document.createTextNode(file.getField61().get(i).getEntryDate())));
                statementLine.appendChild(entryDate);
                Element CDmark = document.createElement(ParserUtility.CD_MARK);
                CDmark.appendChild((document.createTextNode(file.getField61().get(i).getDebitCreditMark())));
                statementLine.appendChild(CDmark);

                Element transValue = document.createElement(ParserUtility.TRANS_VALUE);
                transValue.appendChild((document.createTextNode(file.getField61().get(i).getAmount())));
                statementLine.appendChild(transValue);

                Element transType = document.createElement(ParserUtility.TRANS_TYPE);
                transType.appendChild((document.createTextNode(file.getField61().get(i).getTransactionType())));
                statementLine.appendChild(transType);

                Element transRef = document.createElement(ParserUtility.TRANSACTION_REFERENCE);
                transRef.appendChild((document.createTextNode(file.getField61().get(i).getReferenceForTheAccountOwner())));
                statementLine.appendChild(transRef);

                Element supplementaryDetails = document.createElement(ParserUtility.SUPPLEMENTARY_DETAILS);
                supplementaryDetails.appendChild((document.createTextNode(file.getField61().get(i).getSupplementaryDetails())));
                statementLine.appendChild(supplementaryDetails);
                transaction.appendChild(statementLine);
                // End 61

                //Tag 86
                Element transactionDetails = document.createElement(ParserUtility.TRANSACTION_DETAILS);
                Element transDetailsTag = document.createElement(ParserUtility.GENERAL_TAG);
                transDetailsTag.appendChild((document.createTextNode(file.getField86().get(i).getName())));
                transactionDetails.appendChild(transDetailsTag);

                Element transDetailsValue = document.createElement(ParserUtility.NARRATIVE);
                transDetailsValue.appendChild((document.createTextNode(file.getField86().get(i).getNarrative())));
                transactionDetails.appendChild(transDetailsValue);
                transaction.appendChild(transactionDetails);

                // End 86
                transactions.appendChild(transaction);
            }

            //Tag 62F
            Element finalClosingBalance = document.createElement(ParserUtility.FINAL_CLOSING_BALANCE);

            Element closingTag = document.createElement(ParserUtility.GENERAL_TAG);
            closingTag.appendChild((document.createTextNode(file.getField62F().NAME)));
            finalClosingBalance.appendChild(closingTag);

            Element creditDebitType62 = document.createElement(ParserUtility.CD_MARK);
            creditDebitType62.appendChild((document.createTextNode(file.getField62F().getDCMark())));
            finalClosingBalance.appendChild(creditDebitType62);

            Element date62 = document.createElement(ParserUtility.DATE);
            date62.appendChild((document.createTextNode(file.getField62F().getDate())));
            finalClosingBalance.appendChild(date62);

            Element currency62 = document.createElement(ParserUtility.CURRENCY);
            currency62.appendChild((document.createTextNode(file.getField62F().getCurrency())));
            finalClosingBalance.appendChild(currency62);

            Element amount62 = document.createElement(ParserUtility.AMOUNT);
            amount62.appendChild((document.createTextNode(file.getField62F().getAmount().replaceAll("\r", ""))));
            finalClosingBalance.appendChild(amount62);

            accountInformation.appendChild(finalClosingBalance);
            // End 62F

            // Tag 64
            Element forwardAvailableBalance = document.createElement(ParserUtility.AVAILABLE_BALANCE);

            Element forwardTag = document.createElement(ParserUtility.GENERAL_TAG);
            forwardTag.appendChild((document.createTextNode(file.getField64().NAME)));
            forwardAvailableBalance.appendChild(forwardTag);

            Element creditDebitType64 = document.createElement(ParserUtility.CD_MARK);
            creditDebitType64.appendChild((document.createTextNode(file.getField64().getDCMark())));
            forwardAvailableBalance.appendChild(creditDebitType64);

            Element date64 = document.createElement(ParserUtility.DATE);
            date64.appendChild((document.createTextNode(file.getField64().getDate())));
            forwardAvailableBalance.appendChild(date64);

            Element currency64 = document.createElement(ParserUtility.CURRENCY);
            currency64.appendChild((document.createTextNode(file.getField64().getCurrency())));
            forwardAvailableBalance.appendChild(currency64);

            Element amount64 = document.createElement(ParserUtility.AMOUNT);
            amount64.appendChild((document.createTextNode(file.getField64().getAmount().replaceAll("\r", ""))));
            forwardAvailableBalance.appendChild(amount64);

            accountInformation.appendChild(forwardAvailableBalance);
            // End 64

            // create the xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(ParserUtility.OUTPUT_PROPERTY_P1, ParserUtility.OUTPUT_PROPERTY_P2);

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new StringWriter());

            transformer.transform(domSource, streamResult);
            String xmlString = streamResult.getWriter().toString();
            System.err.println(xmlString);

            return xmlString;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}