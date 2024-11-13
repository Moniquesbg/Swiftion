package com.nhlstenden.swiftionwebapp.controllers.profile;

import com.nhlstenden.swiftionwebapp.controllers.user.UserController;
import com.nhlstenden.swiftionwebapp.utility_classes.Converter;
import com.nhlstenden.swiftionwebapp.utility_classes.MessageHandler;
import com.nhlstenden.swiftionwebapp.utility_classes.RequestBuilder;
import com.nhlstenden.swiftionwebapp.utility_classes.Validate;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*****************************************************************
 * The ProfileController class is responsible for everything that
 * is related to the profile page
 ****************************************************************/
@Controller
public class ProfileController {

    private Validate validate;

    @Autowired
    public ProfileController() {
        this.validate = new Validate();
    }

    /**
     * Displays the profile page with de data of the user.
     *
     * @param session session of the request
     * @param model mode lof the request
     * @return the profile page of the user
     */
    @GetMapping("/profile")
    public String index(HttpSession session, Model model) {

        JSONObject sessionData = UserController.getPermissions(session);
        String mode = sessionData.get("mode").toString();
        String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=get-user&params=%s",
                sessionData.get("user_id")), mode, UserController.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            System.out.println(response);
            String schema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/profileSchema.json" : "/static/schemaModels/xml/profileSchema.xsd";
            if (!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateLoginSchema", mode), mode));
                return "profile";
            }
        }

        try {
            MessageHandler.redirectMessageHandler(session, model, mode);
            model.addAttribute("data", Converter.convertOutputToObject(response, mode).get("data"));

            return "profile";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    /**
     * Updates the profile of a user.
     *
     * @param id id of user getting updated
     * @param username username of user getting updated
     * @param firstname firstname of user getting updated
     * @param lastname lastname of user getting updated
     * @param session HttpSession of the request
     * @return updates the user profile
     */
    @PostMapping("/profile/update/{id}")
    public String updateUsername(@PathVariable int id,
                                 @RequestParam String username,
                                 @RequestParam String firstname,
                                 @RequestParam String lastname,
                                 HttpSession session
    ) {
        JSONObject obj = UserController.getPermissions(session);

        if (!Validate.validateEmail(username)) {
            return "redirect:/profile?id=" + id;
        }

        try {
            String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=update-username&params=%d,%s,%s,%s", id, username, firstname, lastname), obj.get("mode").toString(), UserController.getPermissions(session).get("role").toString());

            if (obj.get("mode").toString().equalsIgnoreCase("json") ||
                    obj.get("mode").toString().equalsIgnoreCase("xml")) {
                System.out.println(response);
                String schema = obj.get("mode").toString().equalsIgnoreCase("json") ? "/static/schemaModels/json/profileSchema.json" : "/static/schemaModels/xml/webappMessage.xsd";
                if (!(validate.validateData(response, this.getClass().getResourceAsStream(schema), obj.get("mode").toString()))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(obj.get("mode").toString(),
                            "Data is niet correct volgens het verificatie schema.", "failed"));
                    return "redirect:/profile?id=" + id;
                }
            }

            JSONObject data = Converter.convertOutputToObject(response, obj.get("mode").toString());
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(obj.get("mode").toString(),
                            "Profiel is bijgewerkt.", data.get("status").toString()));
                }
            }
            return "redirect:/profile?id=" + id;
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }
}