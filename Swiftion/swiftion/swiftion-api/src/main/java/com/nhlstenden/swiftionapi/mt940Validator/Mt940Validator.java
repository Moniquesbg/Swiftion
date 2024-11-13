package com.nhlstenden.swiftionapi.mt940Validator;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.validator.routines.IBANValidator;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;

/*****************************************************************
 * This class is the validator class. It validates the message on
 * JSON and XML format
 ****************************************************************/

public class Mt940Validator {

    /**
     * Method that validates the parsed json with a json schema draft 07. Validates on structure and content.
     * @param data
     * @param schemaPath
     * @return true if schema matches the response json. False if the schema doesn't match the response json.
     */
    public static boolean validateJson(String data, InputStream schemaPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        JsonSchema jsonSchema = schemaFactory.getSchema(schemaPath);
        JsonNode json = mapper.readTree(data);
        Set<ValidationMessage> errors = jsonSchema.validate(json);
        if (!errors.isEmpty()) {
            for (ValidationMessage error : errors) {
                System.out.println(error.getMessage());
            }
            return false;
        }
        return true;
    }

    /**
     * Method that checks if the iban is valid in the json
     * @param json
     * @return boolean true if iban is valid or false if iban is invalid
     * @throws JSONException
     */

    public boolean checkIban(String json) throws JSONException {

        JSONObject jsonObject = new JSONObject(json);
        JSONObject accountIdentification = jsonObject.getJSONObject("accountInformation").getJSONObject("accountIdentification");
        String ibanWithIso = accountIdentification.getString("accountNumber").replaceAll("\\s+", "").toUpperCase();
        int extractIso = ibanWithIso.length() - 3;
        String ibanWithoutIso = ibanWithIso.substring(0, extractIso);

        IBANValidator validator = IBANValidator.getInstance();
        System.out.println("iban " + ibanWithoutIso);
        return validator.isValid(ibanWithoutIso);
    }

    /**
     * Method that validates the parsed xml with a XSD schema. Validates on structure and content
     * @param data
     * @param schemaPath
     * @return true if schema matches the xml, false if the schema doesn't match the xml response
     * @throws IOException
     * @throws SAXException
     */

    public static boolean validateXML(String data, InputStream schemaPath) throws IOException, SAXException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(schemaPath));
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new DefaultHandler() {
            @Override
            public void warning(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void error(SAXParseException e) throws SAXException {
                throw e;
            }

            @Override
            public void fatalError(SAXParseException e) throws SAXException {
                throw e;
            }
        });
        validator.validate(new StreamSource(new StringReader(data)));
        return true;
    }

    /**
     * Method that checks the iban from the parsed xml
     * @param xml
     * @return true if iban is valid, false if iban is invalid.
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public boolean checkIbanXML(String xml) throws ParserConfigurationException, IOException, SAXException {

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            // Get the accountNumber element
            NodeList nodeList = doc.getElementsByTagName("accountNumber");
            Element element = (Element) nodeList.item(0);

            // Extract the account number value
            String ibanNumberWithIso = element.getTextContent();
            System.out.println("ibanNumberWithIso " + ibanNumberWithIso);
            int extractIso = ibanNumberWithIso.length() - 3;
            String ibanWithoutIso = ibanNumberWithIso.substring(0, extractIso);
            System.out.println("ibanWithoutIso " + ibanWithoutIso);
            IBANValidator validator = IBANValidator.getInstance();

            return validator.isValid(ibanWithoutIso);
    }
}








