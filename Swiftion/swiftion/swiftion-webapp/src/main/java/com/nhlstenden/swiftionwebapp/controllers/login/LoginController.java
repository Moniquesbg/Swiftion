package com.nhlstenden.swiftionwebapp.controllers.login;

import com.nhlstenden.swiftionwebapp.utility_classes.Converter;
import com.nhlstenden.swiftionwebapp.utility_classes.Hash;
import com.nhlstenden.swiftionwebapp.utility_classes.RequestBuilder;
import com.nhlstenden.swiftionwebapp.utility_classes.Validate;
import jakarta.servlet.http.HttpSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*****************************************************************
 * The LoginController class is responsible for everything that
 * is related to the login page
 ****************************************************************/
@Controller
public class LoginController {

    private Validate validate;

    @Autowired
    public LoginController() {
        this.validate = new Validate();
    }

    /**
     * Method that shows the login page.
     * @return the login page view
     */
    @GetMapping("/")
    public String login() {

        return "login";
    }

    /**
     * Method that handles the login submissions.
     * @param username username of the user
     * @param password password of the user
     * @param mode mode of the request
     * @param model model of the request
     * @param session HttpSession of the request
     * @return the login page if the user is not logged in, otherwise the home page
     */
    @PostMapping("/")
    public String submitLogin(
            @RequestParam("username") String username,
            @RequestParam("psw") String password,
            @RequestParam("comMode") String mode,
            Model model,
            HttpSession session
    ){
        if (username.isEmpty() || password.isEmpty()) {
            model.addAttribute("messageHandling", Converter.convertOutputToObject(
                    Validate.exception("errorLogin", mode) , mode).get("data"));
            return "login";
        }

        JSONObject msg = null;
        String response = RequestBuilder.buildPostRequestDbAPI(
                String.format("/api/database/procedure-parameter?name=get-user-login&params=%s", username), mode, "1");


        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {

            String loginUserSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/loginSchema.json"
                    : "/static/schemaModels/xml/loginSchema.xsd";
            if(!(validate.validateData(response, this.getClass().getResourceAsStream(loginUserSchema), mode)))
            {
                System.out.println("Error: validatie met schema login pagina ging fout");
                msg = Converter.convertOutputToObject(Validate.exception("errorValidateLoginSchema", mode) , mode);
                model.addAttribute("messageHandling", msg.get("data"));
                return "login";
            }
        }

        JSONObject obj = Converter.convertOutputToObject(response, mode);

        if (!Validate.validateDbApiObject(obj)) {
            msg = Converter.convertOutputToObject(Validate.exception("errorLogin", mode) , mode);
            model.addAttribute("messageHandling", msg.get("data"));
            return "login";
        }

        JSONArray data = obj.getJSONArray("data");
        System.out.println(data);
        if (data.length() == 0) {
            msg = Converter.convertOutputToObject(Validate.exception("errorLogin", mode), mode);
            model.addAttribute("messageHandling", msg.get("data"));
            return "login";
        }

        String hashedPw = data.getJSONObject(0).get("password").toString();
        if (Hash.checkPassword(hashedPw, password)) {
            String pluginresponse = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure?name=get-plugins"), mode, data.getJSONObject(0).get("roleId").toString()
            );
            String pluginSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/pluginSchema.json"
                    : "/static/schemaModels/xml/pluginSchema.xsd";

            if(!(validate.validateData(pluginresponse, this.getClass().getResourceAsStream(pluginSchema), mode)))
            {
                System.out.println("Error: validatie met schema plugin bij het inloggen");
                msg = Converter.convertOutputToObject(Validate.exception("errorValidateLoginSchema", mode) , mode);
                model.addAttribute("messageHandling", msg.get("data"));
                return "login";
            }

            session.setAttribute("plugins", Converter.convertOutputToObject(pluginresponse, mode).get("data").toString());

            session.setAttribute("role", data.getJSONObject(0).get("roleId").toString());
            session.setAttribute("user_id", data.getJSONObject(0).get("userId").toString());
            session.setAttribute("mode", mode);

            return "redirect:/swift-copy-overview";
        } else {
            msg = Converter.convertOutputToObject(Validate.exception("errorPasswordLogin", mode), mode);
            model.addAttribute("messageHandling", msg.get("data"));
            System.out.println("Wrong password hashing went wrong");
            return "login";
        }
    }

    /**
     * Method that logs a user out.
     * @param session HttpSession of the request
     * @return the login page view
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}