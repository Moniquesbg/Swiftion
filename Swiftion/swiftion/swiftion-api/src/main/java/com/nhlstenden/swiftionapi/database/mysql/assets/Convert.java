package com.nhlstenden.swiftionapi.database.mysql.assets;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

public class Convert {

    /**
     * convert data retrieved from the database to json
     *
     * @param rs resultset from database query
     * @return json object from the resultset
     */
    public static JSONObject toJson(ResultSet rs, String objectName) {
        try {
            JSONObject root = new JSONObject();
            JSONArray jsonData = new JSONArray();
            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                int numColumns = rsmd.getColumnCount();
                JSONObject obj = new JSONObject();
                for (int i = 1; i <= numColumns; i++) {
                    String column_name = rsmd.getColumnLabel(i);
                    if (rs.getObject(column_name) != null) {
                        obj.put(column_name, rs.getObject(column_name));
                    } else {
                        obj.put(column_name, "");
                    }
                }
                jsonData.put(obj);
            }
            root.put(objectName, jsonData);
            root.put("status", "success");
            return root;
        } catch (Exception e) {
            System.err.println(e);
        }
        return null;
    }

    /**
     * convert data retrieved from the database to XML
     * NOTE : xml elements may not start with integer values!
     *
     * @param rs resultset from database query
     * @return XML object from the resultset
     */
    public static String toXml(ResultSet rs, String objectName) {
        String xmlString = "";
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            ResultSetMetaData rsmd = rs.getMetaData();
            Element root = document.createElement("root");

            while (rs.next()) {
                int numColumns = rsmd.getColumnCount();
                Element object = document.createElement(objectName);

                for (int i = 1; i <= numColumns; i++) {
                    String column_name = rsmd.getColumnLabel(i);
                    Element column = document.createElement(column_name);
                    column.appendChild(document.createTextNode(rs.getString(column_name)));
                    object.appendChild(column);
                }

                root.appendChild(object);
            }

            Element success = document.createElement("status");
            success.appendChild(document.createTextNode("success"));
            root.appendChild(success);
            document.appendChild(root);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new StringWriter());

            transformer.transform(domSource, streamResult);
            xmlString = streamResult.getWriter().toString();

        } catch (Exception e) {
            System.err.println(e);
            return null;
        }
        return xmlString;
    }

    public static JSONObject stringToJson(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        return obj;
    }
}
