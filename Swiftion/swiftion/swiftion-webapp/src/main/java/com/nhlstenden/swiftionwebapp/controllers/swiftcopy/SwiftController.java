package com.nhlstenden.swiftionwebapp.controllers.swiftcopy;

import com.nhlstenden.swiftionwebapp.controllers.settings.PluginController;
import com.nhlstenden.swiftionwebapp.controllers.user.UserController;
import com.nhlstenden.swiftionwebapp.utility_classes.Converter;
import com.nhlstenden.swiftionwebapp.utility_classes.MessageHandler;
import com.nhlstenden.swiftionwebapp.utility_classes.RequestBuilder;
import com.nhlstenden.swiftionwebapp.utility_classes.Validate;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

/*****************************************************************
 * The SwiftController class is responsible for everything that
 * is related to the swift copy page
 ****************************************************************/
@Controller
public class SwiftController {

    private Dotenv env;
    private Validate validate;

    @Autowired
    public SwiftController() {
        this.env = Dotenv.load();
        this.validate = new Validate();
    }

    /**
     * Method that shows the overview of all the swift copy's
     * @param model model of the request
     * @param session HttpSession of the request
     * @return the swift copy overview
     */
    @GetMapping("/swift-copy-overview")
    public String Mt940Overview(Model model, HttpSession session) {
        String mode = UserController.getPermissions(session).get("mode").toString();
        getAllTransactions(model, session);
        model.addAttribute("auth", UserController.getPermissions(session));

        MessageHandler.redirectMessageHandler(session, model, mode);

        return "swift-copy-overview";
    }

    /**
     * Method that adds a MT940 file to the applications.
     * @param file MT940 file
     * @param model model of the request
     * @param session HttpSession of the request
     * @return success message when succesfully submitted
     */
    @PostMapping(value = "/swift-copy-overview", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String addMt940file(
            @RequestParam("file") MultipartFile file,
            Model model,
            HttpSession session
    ) {
        // Check if user is allowed to access this path
        String userAllowed = UserController.checkUserPermission(session);

        if (userAllowed != null) {
            return userAllowed;
        }

        model.addAttribute("auth", UserController.getPermissions(session));
        JSONObject obj = null;
        String mode = UserController.getPermissions(session).get("mode").toString();
        //checks extension
        if (!(checkFileExtension(file))) {
            obj = Converter.convertOutputToObject(Validate.exception("errorWrongExtension", mode),mode);
            model.addAttribute("messageHandling", obj.get("data"));
            getAllTransactions(model, session);
            return "swift-copy-overview";
        }

        //checks if there is a mimetype and if it's an octet mime type
        if (!(file.getContentType().equals("application/octet-stream"))) {
            obj = Converter.convertOutputToObject(Validate.exception("errorWrongMimeType", mode),mode);
            model.addAttribute("messageHandling", obj);
            getAllTransactions(model, session);
            return "swift-copy-overview";
        }

        WebClient client = WebClient.create();
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        builder.part("file", file.getResource());
        builder.part("userId", String.format("%s", UserController.getPermissions(session).get("user_id")));
        String response = client.post()
                .uri(this.env.get("API_PATH") + ":" + this.env.get("API_PORT") + "/api/mt940/parse/" + mode)
                .header("Authorization", this.env.get("API_TOKEN"))
                .header("role", UserController.getPermissions(session).get("role").toString())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchangeToMono(clientResponse -> clientResponse.bodyToMono(String.class))
                .block();
        System.out.println("Response" + response);
        obj = Converter.convertOutputToObject(response, mode);

        if (obj.has("status")) {
            model.addAttribute("messageHandling", obj.get("data"));
        }

        getAllTransactions(model, session);
        return "swift-copy-overview";
    }

    /**
     * Method that shows a specific swift copy.
     * @param id id of swift copy
     * @param model model of request
     * @param session HttpSession of the request
     * @return the specific swift copy
     */
    @GetMapping("/view-swift-copy")
    public String viewAfschrift(@RequestParam String id, Model model, HttpSession session) {
        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            String costCenterResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-cost-centers", mode, UserController.getPermissions(session).get("role").toString());
            String currencyResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-currencies", mode, UserController.getPermissions(session).get("role").toString());
            String creditDebitResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-card-types", mode, UserController.getPermissions(session).get("role").toString());
            String transactionsresponse = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=get-statement&params=%s", id), mode, UserController.getPermissions(session).get("role").toString());

            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {

                String costCenterSchema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/costCenterSchema.json" : "/static/schemaModels/xml/costCenterSchema.xsd";
                if (!(validate.validateData(costCenterResponse, this.getClass().getResourceAsStream(costCenterSchema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(Validate.exception("errorCostCenterTransaction", mode), mode));
                    return "redirect:/swift-copy-overview";
                }

                String currencySchemaSchema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/currencySchema.json" : "/static/schemaModels/xml/currencySchema.xsd";
                if (!(validate.validateData(currencyResponse, this.getClass().getResourceAsStream(currencySchemaSchema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(Validate.exception("errorValidateAddCustomTransaction", mode), mode));

                    return "redirect:/swift-copy-overview";
                }

                String creditDebitSchema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/creditDebitTransactionSchema.json" : "/static/schemaModels/xml/creditDebitTransactionSchema.xsd";
                if (!(validate.validateData(creditDebitResponse, this.getClass().getResourceAsStream(creditDebitSchema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(Validate.exception("errorValidateGetTransactions", mode), mode));

                    return "redirect:/swift-copy-overview";
                }
                System.out.println("kaas " + transactionsresponse);
                String transactionsSchema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/viewSwiftCopy.json" : "/static/schemaModels/xml/viewSwiftCopy.xsd";
                if (!(validate.validateData(transactionsresponse, this.getClass().getResourceAsStream(transactionsSchema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(Validate.exception("errorValidateGetTransactions", mode), mode));

                    return "redirect:/swift-copy-overview";
                }
            }



            model.addAttribute("cost_center", Converter.convertOutputToObject(costCenterResponse, mode).getJSONArray("data"));
            model.addAttribute("currency", Converter.convertOutputToObject(currencyResponse, mode).getJSONArray("data"));
            model.addAttribute("transaction_types", Converter.convertOutputToObject(creditDebitResponse, mode).getJSONArray("data"));
            model.addAttribute("data", Converter.convertOutputToObject(transactionsresponse, mode).getJSONArray("data"));

            model.addAttribute("plugins", PluginController.getPlugins(session));
            model.addAttribute("auth", UserController.getPermissions(session));

            return "/view-swift-copy";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Method that can delete a swift copy
     * @param id id of swift copy
     * @param session HttpSession of the request
     * @return redirected to swift copy overview and the swift copy has been deleted
     */
    @PostMapping("/view-swift-copy/delete/{id}")
    public String viewAfschrift(@PathVariable int id, HttpSession session) {
        // Check if user is allowed to access this path
        String userAllowed = UserController.checkUserPermission(session);

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=delete-swift-copy&params=%s", id), mode, UserController.getPermissions(session).get("role").toString());
            JSONObject data = Converter.convertOutputToObject(response, mode);
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(mode, "Swift copy succesvol verwijderd", data.get("status").toString()));
                    return "redirect:/swift-copy-overview";
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        return String.format("redirect:/view-swift-copy?id=%d", id);
    }

    /**
     * Method to get all the transaction
     * @param model model of the request
     * @param session HttpSession of the request
     * @return all transactions
     */

    public Model getAllTransactions(Model model, HttpSession session) {
        String mode = UserController.getPermissions(session).get("mode").toString();
        String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure?name=get-mt940-overview"), mode, UserController.getPermissions(session).get("role").toString());
        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String schema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/swiftCopySchema.json" : "/static/schemaModels/xml/swiftCopySchema.xsd";
            if(!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode)))
            {
                System.out.println("Error: validatie met schema van het ophalen van alle afschriften ging fout");
                model.addAttribute("messageHandling", Converter.convertOutputToObject(Validate.exception("errorValidateGetMT940", mode) , mode).get("data"));
                return model;
            }
        }

        try {
            model.addAttribute("data", Converter.convertOutputToObject(response, mode).get("data"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    /**
     * Method that checks for the correct file extension.
     * @param file MT940 file
     * @return true or false if it has a valid extension
     */
    public boolean checkFileExtension(MultipartFile file) {
        //Checks if extension of the uploaded file is .940 or .sta
        String[] allowedExtension = new String[]{"940"};

        // Get the file name of the uploaded file
        String fileName = file.getOriginalFilename();

        // Get the last index of the dot (.) character in the file name
        int lastDotIndex = fileName.lastIndexOf('.');

        // Get the file extension from the file name
        String fileExtension = fileName.substring(lastDotIndex + 1);

        for (String extension : allowedExtension) {
            if (fileExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        return false;
    }

}