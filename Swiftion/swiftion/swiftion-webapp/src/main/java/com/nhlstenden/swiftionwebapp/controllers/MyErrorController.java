package com.nhlstenden.swiftionwebapp.controllers;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*****************************************************************
 * The MyErrorController class is responsible for everything that
 * is related to the custom error pages
 ****************************************************************/
@Controller
public class
MyErrorController implements ErrorController {

    /**
     * Method that handles errors
     * @param request the request
     * @return the error
     */
    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);

        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());

            if(statusCode == HttpStatus.NOT_FOUND.value()) {
                return "error/error-404";
            }
            else if(statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                return "error/error-500";
            }
        }
        return "error/error";
    }

    /**
     * Method that handles when a user gets on a forbidden page.
     * @return forbidden page
     */
    @RequestMapping("/error-forbidden")
    public String handleforbidden() {
        return "error/error-403";
    }
}