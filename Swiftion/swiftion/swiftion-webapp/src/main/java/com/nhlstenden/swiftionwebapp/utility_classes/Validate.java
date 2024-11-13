package com.nhlstenden.swiftionwebapp.utility_classes;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Set;
import java.util.regex.Pattern;

/*****************************************************************
 * This class is the validate class. It validates the data
 ****************************************************************/
public class Validate {

    /**
     * Exception handler
     * @param name name of the exception
     * @param mode mode of the request
     * @return exception message
     */
    public static String exception(String name, String mode) {
        return switch (name) {
            case "errorWrongExtension" ->
                    Converter.buildReturnMessage(mode, "Alleen MT940 bestanden zijn toegestaan: verkeerde bestands extensie", "failed");
            case "errorWrongMimeType" ->
                    Converter.buildReturnMessage(mode, "Alleen MT940 bestanden zijn toegestaan: verkeerde bestands mime type", "failed");
            case "errorPasswordLogin" ->
                    Converter.buildReturnMessage(mode, "Verkeerde wachtwoord", "failed");
            case "errorLogin" ->
                    Converter.buildReturnMessage(mode, "Gebruikersnaam en wachtwoord matchen niet", "failed");
            case "errorValidateEditPassword" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het wijzigen van het wachtwoord", "failed");
            case "errorValidateLoginSchema" ->
                    Converter.buildReturnMessage(mode, "Data validatie is verkeerd gegaan bij de login pagina", "failed");
            case "errorValidateProfileSchema" ->
                    Converter.buildReturnMessage(mode, "Data validatie is verkeerd gegaan bij de profiel pagina", "failed");
            case "errorValidateAddTransaction" ->
                    Converter.buildReturnMessage(mode, "Data validatie is verkeerd gegaan bij de transactie toevoeg pagina", "failed");
            case "errorEmptyFieldsAddTransaction" ->
                    Converter.buildReturnMessage(mode, "Kan custom transactie niet toevoegen, 1 van de velden is leeg.", "failed");
            case "errorValidateAddCustomTransaction" ->
                    Converter.buildReturnMessage(mode, "Data validatie is verkeerd gegaan bij de voeg kasgeld transactie pagina", "failed");
            case "errorCostCenterTransaction" ->
                    Converter.buildReturnMessage(mode, "Data validatie is verkeerd gegaan bij de voeg transactie pagina toe: kostplaats", "failed");
            case "errorValidateUpdateTransaction" ->
                    Converter.buildReturnMessage(mode, "Data validatie is verkeerd gegaan bij de wijzig transactie pagina", "failed");
            case "errorValidateGetTransactions" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het ophalen van alle transacties", "failed");
            case "errorValidateGetMT940" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het ophalen van alle afschriften", "failed");
            case "errorValidateGetCustomTransactions" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het ophalen van alle kasgeld transacties", "failed");
            case "errorGetIdCustomTransactions" ->
                    Converter.buildReturnMessage(mode, "Het ingevulde id bestaat niet", "failed");
            case "errorValidateDeleteTransactions" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het verwijderen van een transactie", "failed");
            case "errorValidateAddUserPage" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het laden van de gebruiker toevoeg pagina ", "failed");
            case "errorValidateGetSwiftCopies" ->
                    Converter.buildReturnMessage(mode, "Data validatie is fout gegaan bij het laden van alle afschriften ", "failed");
            default ->
                    Converter.buildReturnMessage(mode, "Onbekende error, neem contact op met de systeem administrator", "failed");
        };
    }

    /**
     * Method that validates a database api object.
     * @param obj api object
     * @return valid or invalid
     */
    public static Boolean validateDbApiObject(JSONObject obj){
        if (obj.has("status")) {
            if (obj.get("status").equals("success")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method that validates an email address.
     * @param email submitted email address
     * @return valid or invalid
     */
    public static Boolean validateEmail(String email) {
        boolean result = true;
        try {
            InternetAddress emailAddr = new InternetAddress(email);
            emailAddr.validate();
        } catch (AddressException ex) {
            result = false;
        }
        return result;
    }

    /**
     * Method that validates the password
     * @param password submitted password
     * @return valid or invalid
     */
    public static Boolean validatePassword(String password) {
        // regex
        // valid : abcdefg#G4!
        // invalid : abcdefg# G4! (space makes it invalid)
        //
        // atleast 1 lowercase letter
        // atleast 1 uppercase letter
        // atleast 1 number
        // atleast 1 special character
        // must be between 8 and 100 characters
        String passwordRegex = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])[\\w!@#$%]{8,100}$";

        return Pattern.compile(passwordRegex).matcher(password).matches();
    }

    /**
     * Method that validates the JSON or XML data.
     * @param data JSON or XML data
     * @param schemaPath schema path
     * @param mode mode of request
     * @return valid or invalid
     */
    public static Boolean validateData(String data, InputStream schemaPath, String mode) {
        return switch (mode.toLowerCase()) {
            case "json" -> validateJsonData(data, schemaPath);
            case "xml" -> validateXML(data, schemaPath);
            default -> false;
        };
    }

    /**
     * Method that validates JSON data
     * @param data JSON data
     * @param schemaPath schema path
     * @return valid or invalid
     */
    public static boolean validateJsonData(String data, InputStream schemaPath){
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema jsonSchema = schemaFactory.getSchema(schemaPath);
            JsonNode json = mapper.readTree(String.valueOf(data));
            Set<ValidationMessage> errors = jsonSchema.validate(json);
            if (!errors.isEmpty()) {
                for (ValidationMessage error : errors) {
                    System.out.println(error.getMessage());
                }
                return false;
            }
            System.out.println("Json validation successful");
            return true;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }

    }

    /**
     * Method that validates XML
     * @param data XML data
     * @param schemaPath schema path
     * @return valid or invalid
     */
    public static boolean validateXML(String data, InputStream schemaPath) {
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new StreamSource(schemaPath));
            Validator validator = schema.newValidator();
            validator.setFeature("http://apache.org/xml/features/validation/schema", true);
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
            System.out.println("XML validation successful");
            return true;
        }catch (Exception e){
            System.out.println("validation error: " +e.getMessage());
            return false;
        }
    }

}