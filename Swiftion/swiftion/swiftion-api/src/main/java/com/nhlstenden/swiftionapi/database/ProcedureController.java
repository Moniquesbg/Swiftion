package com.nhlstenden.swiftionapi.database;

import com.nhlstenden.swiftionapi.auth.Authenticate;
import com.nhlstenden.swiftionapi.database.mapper.Mapper;
import com.nhlstenden.swiftionapi.database.mysql.StoredProcedure;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/*****************************************************************
 * The ProcedureController class is responsible for everything that
 * is related to the database.
 * For instantiating a connection, to executing a stored procedure
 ****************************************************************/
@RestController
public class ProcedureController {

    /**
     * procedure without parameter values for stored procedure
     * @param name name of the procedure
     * @param json boolean for json format
     * @param xml boolean for xml format
     * @param apiKey api key for authentication
     * @return a responseEntity in either JSON or XMl format
     */
    @PostMapping("/api/database/procedure")
    public ResponseEntity<String> procedureNoPM(
            @RequestParam("name") String name,
            @RequestParam(value = "json", defaultValue = "false") Boolean json,
            @RequestParam(value = "xml", defaultValue = "false") Boolean xml,
            @RequestHeader(value = "Authorization", defaultValue = "") String apiKey,
            @RequestHeader(value = "role", defaultValue = "") String role
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON); // http headers for setting it to JSON

        // check if parameters are correctly set
        ResponseEntity<String> parameterCheck = this.checkRequestParameters(apiKey, name, httpHeaders, json, xml, null);

        if (parameterCheck != null) {
            // if one of the conditions is not met in checkRequestParmaters(), the error will be shown in JSON format
            return parameterCheck;
        }

        // map the name to a procedurecall
        String procedureCall = Mapper.nameToProcedure(name);

        // return the correct data based on the name of the procedure, xml or json format
        // (if both true - json will be picked) and http headers.
        return this.getData(procedureCall,null, xml, json, httpHeaders, role);
    }

    /**
     * procedure with parameter values for stored procedure
     * @param params array of parameter values
     * @param name name of the procedure
     * @param json boolean for json format
     * @param xml boolean for xml format
     * @param apiKey api key for authentication
     * @return a responseEntity in either JSON or XMl format
     */
    @PostMapping("/api/database/procedure-parameter")
    public ResponseEntity<String> procedurePM(
            @RequestParam("params") String[] params,
            @RequestParam("name") String name,
            @RequestParam(value = "json", defaultValue = "false") Boolean json,
            @RequestParam(value = "xml", defaultValue = "false") Boolean xml,
            @RequestHeader(value = "Authorization", defaultValue = "") String apiKey,
            @RequestHeader(value = "role", defaultValue = "") String role
    ) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON); // http headers for setting it to JSON

        // check if parameters are correctly set
        ResponseEntity<String> pmCheck = this.checkRequestParameters(apiKey, name, httpHeaders, json, xml, params);

        // check if all parameters are correct, if null, everything checked is correct
        if (pmCheck != null) {
            return pmCheck;
        }

        // if one of the conditions is not met in checkRequestParmaters(), the error will be shown in JSON format
        String procedureCall = Mapper.nameToProcedure(name, params);

        // return the correct data based on the name of the procedure, xml or json format
        // (if both true - json will be picked) and http headers.
        return this.getData(procedureCall, params, xml, json, httpHeaders, role);
    }

    // ########## start utility methods ########## //

    /**
     * check if the request parameters are correctly set, used by 'procedurePM' and 'procedureNoPM'
     *
     * @param apiKey      apikey to authenticate with
     * @param name        name of the procedure
     * @param httpHeaders used to set the format to JSON
     * @return null if no errors were generated, otherwise return new ResponseEntity
     */
    private ResponseEntity<String> checkRequestParameters(
            String apiKey,
            String name,
            HttpHeaders httpHeaders,
            Boolean json,
            Boolean xml,
            String[] params
    ) {
        // check if authenticated correctly
        Authenticate auth = new Authenticate();
        if (!auth.checkToken(apiKey)) {
            return new ResponseEntity<>(Mapper.exception("auth-error", json, xml),
                    httpHeaders,
                    HttpStatus.FORBIDDEN
            );
        }

        // check if the name given has an association in Mapper.nametoprocedure, if so set the procedurecall
        if (params == null) {
            if (Mapper.nameToProcedure(name) == null) {
                return new ResponseEntity<>(Mapper.exception("name-error", json, xml),
                        httpHeaders,
                        HttpStatus.BAD_REQUEST
                );
            }
        } else {
            if (Mapper.nameToProcedure(name, params) == null) {
                return new ResponseEntity<>(Mapper.exception("name-error", json, xml),
                        httpHeaders,
                        HttpStatus.BAD_REQUEST
                );
            }
        }

        // if no format (json/xml) is specified, execute parse error
        if (!json && !xml) {
            return new ResponseEntity<>(Mapper.exception("parse-error", json, xml),
                    httpHeaders,
                    HttpStatus.BAD_REQUEST
            );
        }

        return null;
    }

    /**
     * retrieve the data from MySQL, used by 'procedurePM' and 'procedureNoPM'
     *
     * @param procedureCall retrieved from the main method, this can be a procedure call with 0 or multiple parameters
     * @param httpHeaders   used to specify if the format returned should be JSON or XML
     * @return ResponseEntity formatted in XMl or JSON
     */
    private ResponseEntity<String> getData(
            String procedureCall,
            String[] params,
            boolean xml,
            boolean json,
            HttpHeaders httpHeaders,
            String role
    ) {
        String data = StoredProcedure.call(procedureCall, params,"data", json, xml, role);
        if (data != null) {
            if (json) {
                return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
            } else if (xml) {
                httpHeaders.setContentType(MediaType.TEXT_XML);
                return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(Mapper.exception("format-error", json, xml),
                        httpHeaders,
                        HttpStatus.BAD_REQUEST
                );
            }
        } else {
            return new ResponseEntity<>(Mapper.exception("mysql-error", json, xml),
                    httpHeaders,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // ########## end utility methods ########## //
}