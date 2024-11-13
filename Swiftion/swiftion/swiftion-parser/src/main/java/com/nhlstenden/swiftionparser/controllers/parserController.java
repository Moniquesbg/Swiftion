package com.nhlstenden.swiftionparser.controllers;

import com.nhlstenden.swiftionparser.converter.Converter;
import com.prowidesoftware.swift.model.mt.mt9xx.MT940;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static com.nhlstenden.swiftionparser.parsers.MT940JsonBuilder.buildMT940Json;
import static com.nhlstenden.swiftionparser.parsers.MT940XMLBuilder.buildMT940XML;


/****************************************************************************************
 * This class is the controller for the parser. It handles the requests and returns the
 * response. it uses the annotation of @Restcontroller to make it a rest controller, so
 * it doesn't need views.
 ****************************************************************************************/
@RestController
public class parserController {

    /***
     * This method handles the POST request for parsing a MT940 file to JSON
     * @param file mt940 file
     * @return returns the response entity with the json string of the mt940 file
     */
    @PostMapping(value = "/parseJson", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> parseJson(@RequestBody MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            MT940 mt = new MT940(file.getInputStream());

            String mtJson = buildMT940Json(mt);
            System.out.println("JSON: " + mtJson);
            if (mtJson != null) {
                return new ResponseEntity<>(mtJson, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                        Converter.buildReturnMessage("json", "One of the fields cannot be parsed", "failed"),
                        headers,
                        HttpStatus.OK
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    /***
     * This method handles the POST request for parsing a MT940 file to XML
     * @param file mt940 file
     * @return returns the response entity with the xml string of the mt940 file
     */
    @PostMapping(value = "/parseXml", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<String> parseXML(@RequestBody MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);

            MT940 mt = new MT940(file.getInputStream());
            System.out.println(mt);
            String mtXml = buildMT940XML(mt);
            System.out.println("XML: " + mtXml);
            if (mtXml != null) {
                return new ResponseEntity<>(mtXml, headers, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(
                        Converter.buildReturnMessage("xml", "One of the fields cannot be parsed", "failed"),
                        headers,
                        HttpStatus.OK
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}