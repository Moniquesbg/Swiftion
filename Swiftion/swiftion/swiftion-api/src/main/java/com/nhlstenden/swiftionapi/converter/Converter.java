package com.nhlstenden.swiftionapi.converter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/*****************************************************************
 * This class is the converter class. It converts the message to a
 * json or xml string. to send it over and Webclient
 ****************************************************************/

public class Converter {
    /**
     * build a return message that will be shown on the view
     * @param mode, either XML or JSON
     * @param message message to be build
     * @param status either success or failure
     * @return message that was build
     */
    public static String buildReturnMessage(String mode, String message, String status) {
        String messageReturn = null;
        if (mode.equalsIgnoreCase("json")) {
            messageReturn = buildJSONReturnMessage(message, status);
        } else if (mode.equalsIgnoreCase("xml")) {
            messageReturn = buildXMLReturnMessage(message, status);
        }

        return messageReturn;
    }

    /**
     * build a return message in JSON format
     * @param message message to be build in JSON format
     * @param status status to be build in JSON format
     * @return the JSON object as a string
     */
    public static String buildJSONReturnMessage(String message, String status) {
        return String.format("{\"data\":[{\"message\":\"%s\"}],\"status\":\"%s\"}", message, status);
    }

    /**
     * build a return message in XML format
     * @param message message to be build in XML format
     * @param status status to be build in XML format
     * @return the XML object as a string
     */
    public static String buildXMLReturnMessage(String message, String status) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();

            Element root = document.createElement("root");
            document.appendChild(root);

            Element data = document.createElement("data");
            root.appendChild(data);

            Element messageElement = document.createElement("message");
            messageElement.appendChild(document.createTextNode(message));
            data.appendChild(messageElement);

            Element statusElement = document.createElement("status");
            statusElement.appendChild(document.createTextNode(status));
            root.appendChild(statusElement);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4" );

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new StringWriter());
            transformer.transform(domSource, streamResult);
            System.out.println(streamResult.getWriter().toString());
            return streamResult.getWriter().toString();
        }catch (Exception e){
            System.out.println(e.getMessage());
        }

        return null;
    }

}