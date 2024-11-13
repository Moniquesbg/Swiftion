package com.nhlstenden.swiftionwebapp.controllers.user;

import com.nhlstenden.swiftionwebapp.utility_classes.*;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {

    private Validate validate;

    @Autowired
    public UserController() {
        this.validate = new Validate();
    }

    /**
     * Method that shows the page to create a new user.
     * @param model
     * @param session
     * @return
     */
    @GetMapping("/add-user")
    public String addUser(Model model, HttpSession session) {
        String mode = this.getPermissions(session).get("mode").toString();
        String rolesResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-roles", mode, this.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String schema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/rolesOverviewSchema.json"
                    : "/static/schemaModels/xml/rolesOverviewSchema.xsd";
            if (!(validate.validateData(rolesResponse, this.getClass().getResourceAsStream(schema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddUserPage", mode), mode));
                return "add-user";
            }
        }

        try {
            model.addAttribute("roles", Converter.convertOutputToObject(rolesResponse, mode).getJSONArray("data"));

            MessageHandler.redirectMessageHandler(session, model, mode);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        return "add-user";
    }

    /**
     * Method that adds new user to the application.
     * @param email
     * @param firstname
     * @param lastname
     * @param password
     * @param roleId
     * @param session
     * @param model
     * @return
     */
    @PostMapping("/add-user/insert")
    public String insertUser(
            @RequestParam("email") String email,
            @RequestParam("voornaam") String firstname,
            @RequestParam("achternaam") String lastname,
            @RequestParam("wachtwoord") String password,
            @RequestParam("rol") int roleId,
            HttpSession session,
            Model model
    ) {
        String mode = this.getPermissions(session).get("mode").toString();

        if (email.isEmpty() || firstname.isEmpty() || lastname.isEmpty() || password.isEmpty() || roleId < 0) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "U moet alle velden invullen.", "failed"));
            return "redirect:/add-user";
        }

        if (!Validate.validateEmail(email)) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "Geen geldige email!", "failed"));
            return "redirect:/add-user";
        }

        if (!Validate.validatePassword(password)) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "Geen geldige wachtwoord! Minstens één hoofdletter, één nummer en de wachtwoord moet minstens 8 karakters lang zijn. Ook mag het geen white spaces hebben", "failed"));
            return "redirect:/add-user";
        }

        String passwordHashed = Hash.generateBcryptHash(password);
        String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=insert-user&params=%d,%s,%s,%s,%s", roleId, email, passwordHashed, firstname, lastname), mode, this.getPermissions(session).get("role").toString());
        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String schema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/webappMessage.json"
                    : "/static/schemaModels/xml/webappMessage.xsd";

            if (!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("viewTransactionsUpdateSchema", mode), mode).get("data"));
                return "user-overview";
            }
        }

        JSONObject data = Converter.convertOutputToObject(response, mode);
        System.out.println(data);
        if (data.has("status")) {
            if (data.get("status").equals("success")) {
                session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                        mode, "Gebruiker succesvol toegevoegd!", data.get("status").toString()));
            }
        }
        return "redirect:/user-overview";
    }

    /**
     * Method that shows the password edit page.
     * @param session
     * @param model
     * @return
     */
    @GetMapping("/edit-password")
    public String editPassword(HttpSession session, Model model) {
        JSONObject obj = UserController.getPermissions(session);
        try {
            model.addAttribute("userId", obj.get("user_id"));
            MessageHandler.redirectMessageHandler(session, model, obj.get("mode").toString());
            return "edit-password";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Method that updates the password.
     * @param currentPw
     * @param newPw
     * @param newPwRepeat
     * @param session
     * @return
     */
    @PostMapping("/edit-password/update/{id}")
    public String updatePassword(
            @RequestParam("current-pw") String currentPw,
            @RequestParam("new-pw") String newPw,
            @RequestParam("new-pw-repeat") String newPwRepeat,
            HttpSession session
    ) {
        String mode = this.getPermissions(session).get("mode").toString();

        if (currentPw.isEmpty() || newPw.isEmpty() || newPwRepeat.isEmpty()){
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "De velden mogen niet leeg zijn", "failed"));
            return "redirect:/edit-password";
        }

        JSONObject sessionObj = UserController.getPermissions(session);
        String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=get-current-password&params=%s", sessionObj.get("user_id")), sessionObj.get("mode").toString(), this.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String schema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/editPasswordSchema.json"
                    : "/static/schemaModels/xml/editPasswordSchema.xsd";

            if (!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode))) {
                session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(
                        Validate.exception("errorValidateEditPassword", mode), mode));
                return "redirect:/edit-password";
            }
        }

        JSONObject obj = Converter.convertOutputToObject(response, mode);

        JSONArray data = obj.getJSONArray("data");
        String currentPwResponse = data.getJSONObject(0).get("password").toString();

        // Checking if submitted current submitted password matched the actual current password
        if (!Hash.checkPassword(currentPwResponse, currentPw)) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "Uw huidige wachtwoord komt niet overeen.", "failed"));
            return "redirect:/edit-password";
        }

        // Checking if both submitted new passwords match
        if (!newPw.equals(newPwRepeat)) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "Uw nieuwe wachtwoord komt niet overeen met het een van de ingevulde velden.", "failed"));
            return "redirect:/edit-password";
        }

        // Checking if the new password meets the requirements
        if (!Validate.validatePassword(newPw)) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                    mode, "Het nieuwe wachtwoord komt niet overeen met de gestelde eisen", "failed"));
            return "redirect:/edit-password";
        }

        String hashedPassword = Hash.generateBcryptHash(newPw);

        try {
            RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=update-password&params=%s,%s", sessionObj.get("user_id"), hashedPassword), mode, this.getPermissions(session).get("role").toString());
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(mode, "Wachtwoord is bijgewerkt.", "success"));

            return "redirect:/profile";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    /**
     * Method that shows the page where a user can be editted
     * @param userId
     * @param model
     * @param session
     * @return
     */
    @GetMapping("/edit-user")
    public String editUserPage(
            @RequestParam("id") int userId,
            Model model,
            HttpSession session
    ) {
        String mode = UserController.getPermissions(session).get("mode").toString();
        String userResponse = RequestBuilder.buildPostRequestDbAPI(
                String.format("/api/database/procedure-parameter?name=get-user&params=%d", userId), mode, this.getPermissions(session).get("role").toString());
        String rolesResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-roles", mode, this.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String userSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/editUserPage.json"
                    : "/static/schemaModels/xml/editUserPage.xsd";
            if (!(validate.validateData(userResponse, this.getClass().getResourceAsStream(userSchema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddUserPage", mode), mode));
                return "edit-user";
            }

            String roleSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/rolesOverviewSchema.json"
                    : "/static/schemaModels/xml/rolesOverviewSchema.xsd";
            if (!(validate.validateData(rolesResponse, this.getClass().getResourceAsStream(roleSchema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddUserPage", mode), mode));
                return "edit-user";
            }

        }

        try {
            model.addAttribute("user_details", Converter.convertOutputToObject(userResponse, mode).get("data"));
            model.addAttribute("roles", Converter.convertOutputToObject(rolesResponse, mode).get("data"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        model.addAttribute("auth", getPermissions(session));

        MessageHandler.redirectMessageHandler(session, model, mode);

        return "edit-user";
    }

    /**
     * Method that updates a user.
     * @param userId
     * @param roleId
     * @param email
     * @param firstname
     * @param lastname
     * @param model
     * @param session
     * @return
     */
    @PostMapping("/edit-user/update/{id}")
    public String updateUser(
            @PathVariable("id") int userId,
            @RequestParam("rol") int roleId,
            @RequestParam("email") String email,
            @RequestParam("voornaam") String firstname,
            @RequestParam("achternaam") String lastname,
            Model model,
            HttpSession session
    ) {
        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            String updateResponse = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=update-user&params=%d,%d,%s,%s,%s", roleId, userId, email, firstname, lastname), mode, this.getPermissions(session).get("role").toString());
            System.out.println("Update" + updateResponse);
            JSONObject data = Converter.convertOutputToObject(updateResponse, mode);
            JSONArray dataArr = data.getJSONArray("data");
            System.out.println(data);
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(mode, "Gebruiker succesful geüpdate", data.get("status").toString()));
                } else {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(mode, dataArr.getJSONObject(0).get("message").toString(), data.get("status").toString()));
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        if (!session.getAttribute("user_id").equals(userId)) {
            this.SetPermissions(session, userId, roleId);
        }
        model.addAttribute("auth", getPermissions(session));
        return String.format("redirect:/edit-user?id=%s", userId);
    }

    /**
     * Method that updates the role from the user overview page.
     * @param user_id
     * @param role_id
     * @param session
     * @return
     */
    @PostMapping("/edit-user/update-role/{user_id}")
    public String editUserPage(
            @PathVariable("user_id") int user_id,
            @RequestParam("role_id") int role_id,
            HttpSession session
    ) {
        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            String response = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=update-user-role&params=%d,%d", user_id, role_id), mode, this.getPermissions(session).get("role").toString());

            JSONObject data = Converter.convertOutputToObject(response, mode);
            JSONArray dataArr = data.getJSONArray("data");
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, "Gebruiker is succesvol geüpdate", data.get("status").toString()));
                } else {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, dataArr.get(0).toString(), data.get("status").toString()));
                }
            }

            if (session.getAttribute("user_id").equals(user_id)) {
                this.SetPermissions(session, user_id, role_id);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }


        return "redirect:/user-overview";
    }

    /**
     * Method that shows the user overview page with all the user on it.
     * @param model
     * @param session
     * @return
     */
    @GetMapping("/user-overview")
    public String users(Model model, HttpSession session) {
        String mode = this.getPermissions(session).get("mode").toString();
        String usersResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-users", mode, this.getPermissions(session).get("role").toString());
        String rolesResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-roles", mode, this.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String roleschema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/rolesOverviewSchema.json"
                    : "/static/schemaModels/xml/rolesOverviewSchema.xsd";
            if (!(validate.validateData(rolesResponse, this.getClass().getResourceAsStream(roleschema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddUserPage", mode), mode));
                return "user-overview";
            }
            String userschema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/usersOverviewSchema.json"
                    : "/static/schemaModels/xml/usersOverviewSchema.xsd";
            if (!(validate.validateData(usersResponse, this.getClass().getResourceAsStream(userschema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddUserPage", mode), mode));
                return "user-overview";
            }
        }

        try {
            JSONObject users = Converter.convertOutputToObject(usersResponse, mode);
            JSONObject roles = Converter.convertOutputToObject(rolesResponse, mode);

            model.addAttribute("users", users.get("data"));
            model.addAttribute("roles", roles.get("data"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        model.addAttribute("auth", getPermissions(session));

        MessageHandler.redirectMessageHandler(session, model, mode);

        return "user-overview";
    }

    /**
     * Method that gets permissions
     * @param session
     * @return
     */
    public static JSONObject getPermissions(HttpSession session) {
        JSONObject obj = new JSONObject();
        obj.put("role", session.getAttribute("role").toString())
                .put("user_id", session.getAttribute("user_id").toString())
                .put("mode", session.getAttribute("mode").toString().toLowerCase());
        return obj;
    }

    /**
     * Method that sets permissions
     * @param session
     * @param user_id
     * @param role_id
     */
    public void SetPermissions(HttpSession session, int user_id, int role_id) {
        session.setAttribute("user_id", user_id);
        session.setAttribute("role", role_id);

    }

    /***
     * Check if login user role is not the role "Gebruiker"
     * @param session HttpSession with role data
     * @return
     */
    public static String checkUserPermission(HttpSession session) {
        if (!UserController.getPermissions(session).get("role").equals("2")) {
            return "redirect:/error-forbidden";
        }

        return null;
    }
}