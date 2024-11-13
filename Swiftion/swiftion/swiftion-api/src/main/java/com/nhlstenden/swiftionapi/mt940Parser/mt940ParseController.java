package com.nhlstenden.swiftionapi.mt940Parser;

import com.nhlstenden.swiftionapi.auth.Authenticate;
import com.nhlstenden.swiftionapi.converter.Converter;
import com.nhlstenden.swiftionapi.database.mongodb.MongoDBConnector;
import com.nhlstenden.swiftionapi.database.mysql.assets.Convert;
import com.nhlstenden.swiftionapi.mt940Validator.Mt940Validator;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.xml.sax.SAXException;

import java.io.IOException;

import static com.nhlstenden.swiftionapi.database.mysql.assets.SwiftCopyQueryBuilder.addSwiftCopyDataToMysql;

/*****************************************************************
 * The mt940ParseController class is responsible for everything that
 * is related to parsing a .940 file to XML or JSON
 ****************************************************************/
@RestController
public class mt940ParseController {
    private final Authenticate auth;
    private final MongoDBConnector mongoDB;
    private final Mt940Validator validator;
    private Dotenv env;

    @Autowired
    public mt940ParseController() {
        this.auth = new Authenticate();
        this.mongoDB = new MongoDBConnector();
        this.validator = new Mt940Validator();
        this.env = Dotenv.load();
    }

    /**
     * end point for parsing a .940 to XML
     */
    @PostMapping(value = "/api/mt940/parse/xml", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> parseXML(
            @RequestPart MultipartFile file,
            @RequestPart String userId,
            @RequestHeader(value = "Authorization") String apiToken,
            @RequestHeader(value = "role") String role
    ) throws IOException, SAXException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (auth.checkToken(apiToken)) {
            WebClient client = WebClient.create();
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource());

            String response = client.post()
                    .uri(this.env.get("PARSER_PATH") + ":" + env.get("PARSER_PORT") + "/parseXml")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_XML)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                    .block();

            System.err.println("API: " + response);
            // validates xml response with schema, if false error message.
            if (response.contains("<status>failed</status>")) {
                if (validator.validateXML(response, this.getClass().getResourceAsStream(
                        "/static/schemaModels/xml/ApiMessage.xsd"))
                ) {
                    return new ResponseEntity<>(response, httpHeaders, HttpStatus.OK);
                }
            }

            if (!(validator.validateXML(response, this.getClass().getResourceAsStream(
                    "/static/schemaModels/xml/MT940Parsed.xsd")))
            ) {
                System.out.println("validating xml with schema failed");
                return new ResponseEntity<>(Converter.buildReturnMessage(
                        "xml",
                        "Heel de geparste XML is incorrect",
                        "failed"),
                        httpHeaders,
                        HttpStatus.FAILED_DEPENDENCY
                );
            }

            // add file to mongo and mysql
            if (addFileContentToDb(file, userId, role)) {
                return new ResponseEntity<>(Converter.buildReturnMessage(
                        "xml",
                        "MT940 bestand toegevoegd",
                        "success"),
                        httpHeaders,
                        HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(Converter.buildReturnMessage(
                        "xml",
                        "Bestand kan niet toegevoegd worden aan de database",
                        "failed"),
                        httpHeaders,
                        HttpStatus.FAILED_DEPENDENCY
                );
            }

        } else {

            return new ResponseEntity<>(Converter.buildReturnMessage(
                    "xml",
                    "Invalid API token",
                    "failed"),
                    httpHeaders,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    /**
     * end point for parsing a .940 to JSON
     * @param file file to parse
     * @param userId user id
     * @param apiToken api token
     * @return response entity
     * @throws IOException io exception
     * @throws JSONException json exception
     */
    @PostMapping(value = "/api/mt940/parse/json", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> parseJson(
            @RequestPart MultipartFile file,
            @RequestPart String userId,
            @RequestHeader(value = "Authorization") String apiToken,
            @RequestHeader(value = "role") String role
    ) throws IOException, JSONException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        if (auth.checkToken(apiToken)) {
            WebClient client = WebClient.create();
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("file", file.getResource());

            String response = client.post()
                    .uri(this.env.get("PARSER_PATH") + ":" + env.get("PARSER_PORT") + "/parseJson")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                    .block();

            System.err.println("parser response " + response);

            JSONObject obj = Convert.stringToJson(response);

            if (obj.has("status")) {
                System.out.println(obj);
                return new ResponseEntity(Converter.buildReturnMessage(
                        "json",
                        "Éen van de velden kan niet gelezen worden!",
                        "failed"),
                        httpHeaders,
                        HttpStatus.OK
                );
            }

            //validates the json with a json schema draft 07
            if (!(validator.validateJson(response, mt940ParseController.class.getResourceAsStream("/static/schemaModels/json/MT940Parsed.json")))) {
                System.out.println("Validating json with json schema failed");
                return new ResponseEntity<>(Converter.buildReturnMessage(
                        "json",
                        "Geparste JSON is incorrect",
                        "failed"),
                        httpHeaders,
                        HttpStatus.NOT_ACCEPTABLE
                );
            }

            // add file to mongo and mysql

            if (addFileContentToDb(file, userId, role)) {
                System.out.println("success");
                return new ResponseEntity<>(Converter.buildReturnMessage(
                        "json",
                        "MT940 bestand toegevoegd",
                        "success"), httpHeaders, HttpStatus.OK
                );
            } else {
                return new ResponseEntity<>(Converter.buildReturnMessage(
                        "json",
                        "Bestand kan niet toegevoegd worden aan de database",
                        "failed"),
                        httpHeaders,
                        HttpStatus.FAILED_DEPENDENCY
                );
            }
        } else {
            return new ResponseEntity<>(Converter.buildReturnMessage(
                    "json",
                    "invalid API token",
                    "failed"),
                    httpHeaders,
                    HttpStatus.UNAUTHORIZED
            );
        }
    }

    /**
     * add both files, once it has been validated to the datbaases
     * @param file .940 file
     * @param userId id of the user
     * @return true if it was both inserted succesfully, otherwise false
     */
    private Boolean addFileContentToDb(MultipartFile file, String userId, String role) {
        try {
            if (!addSwiftCopyDataToMysql(new MT940(file.getInputStream()), userId, role)) {
                return false;
            }
            if (!addFileToMongoDB(file, userId)) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * add file to mongoDb
     * @return true if it was succesfully inserted
     */
    public boolean addFileToMongoDB(MultipartFile file, String userId) {
        mongoDB.createCollectionSolution();
        mongoDB.insertMT940(file, userId);
        return true;
    }
}