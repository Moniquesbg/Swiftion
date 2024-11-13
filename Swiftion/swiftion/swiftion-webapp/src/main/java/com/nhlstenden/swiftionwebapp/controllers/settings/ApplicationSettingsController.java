package com.nhlstenden.swiftionwebapp.controllers.settings;

import com.nhlstenden.swiftionwebapp.controllers.user.UserController;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*****************************************************************
 * The ApplicationSettingsController class is responsible for everything that
 * is related to the settings page
 ****************************************************************/
@Controller
public class ApplicationSettingsController {

    /**
     * Method that shows the settings page
     * @param model model of the request
     * @param session HttpSession of the request
     * @return settings page
     */
    @GetMapping("/settings")
    public String getPluginOverview(Model model, HttpSession session) {
        //get permissions
        JSONObject mode = UserController.getPermissions(session);

        //add mode and role to model
        model.addAttribute("mode", mode.get("mode"));
        model.addAttribute("role", mode.get("role"));

        //return settings page view
        return "settings";
    }

    /**
     * Method that updates the mode of the application to either JSON or XML
     * @param comMode communication mode
     * @param model model of the request
     * @param session HttpSession of the request
     * @return update of the mode setting
     */
    @PostMapping("/settings")
    public String getPluginOverview(@RequestParam String comMode,  Model model, HttpSession session) {
        //set mode
        session.setAttribute("mode", comMode);

        //get permissions
        JSONObject mode = UserController.getPermissions(session);

        //add mode and role to model
        model.addAttribute("mode", mode.get("mode"));
        model.addAttribute("role", mode.get("role"));

        //return settings page view
        return "settings";
    }
}