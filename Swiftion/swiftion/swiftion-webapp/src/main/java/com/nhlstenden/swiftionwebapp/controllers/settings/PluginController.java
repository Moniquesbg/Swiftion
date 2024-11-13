package com.nhlstenden.swiftionwebapp.controllers.settings;

import com.nhlstenden.swiftionwebapp.controllers.user.UserController;
import com.nhlstenden.swiftionwebapp.utility_classes.Converter;
import com.nhlstenden.swiftionwebapp.utility_classes.RequestBuilder;
import com.nhlstenden.swiftionwebapp.utility_classes.Validate;
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

/*****************************************************************
 * The PluginController class is responsible for everything that
 * is related to the plugin page
 ****************************************************************/
@Controller
public class PluginController {

    private Validate validate;

    @Autowired
    public PluginController() {
        this.validate = new Validate();
    }

    /**
     * Method that shows the plugin overview
     * @param model model of the request
     * @param session HttpSession of the request
     * @return plugin overview page
     */
    @GetMapping("/plugin")
    public String getPluginOverview(Model model, HttpSession session) {
        // Check if user is allowed to access this path
        String userAllowed = UserController.checkUserPermission(session);

        if (userAllowed != null) {
            return userAllowed;
        }

        try {
            String mode = UserController.getPermissions(session).get("mode").toString();
            String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure?name=get-plugins"), mode, UserController.getPermissions(session).get("role").toString());

            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String schema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/pluginSchema.json"
                        : "/static/schemaModels/xml/pluginSchema.xsd";
                if(!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode)))
                {
                    System.out.println("Error: validatie met schema plugin pagina ging fout");
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateProfileSchema", mode) , mode).get("data"));
                    return "plugin";
                }
            }

            JSONObject obj = Converter.convertOutputToObject(response,mode);
            model.addAttribute("data", obj.get("data"));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }
        return "plugin";
    }

    /**
     * Method that updates the status of a plugin to on or off.
     * @param pluginId id of plugin getting updated
     * @param online status if it is online or offline
     * @param session HttpSession of the request
     * @return toggle of status for plugin
     */
    @PostMapping("/plugin/update/{plugin_id}")
    public String updatePlugin(@PathVariable("plugin_id") int pluginId, @RequestParam("submit") Boolean online, HttpSession session) {
        // Check if user is allowed to access this path
        String userAllowed = UserController.checkUserPermission(session);

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            int state = online ? 1 : 0;
            String response = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=update-plugin&params=%d,%d", pluginId, state), mode, UserController.getPermissions(session).get("role").toString());
            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String schema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/pluginSchema.json"
                        : "/static/schemaModels/xml/pluginSchema.xsd";
                if(!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode)))
                {
                    System.out.println("Error: validatie met schema plugin pagina ging fout");
                    return "plugin";
                }
            }

            String pluginresponse = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure?name=get-plugins"), mode, UserController.getPermissions(session).get("role").toString());
            String pluginSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/pluginSchema.json"
                    : "/static/schemaModels/xml/pluginSchema.xsd";

            if(!(validate.validateData(pluginresponse, this.getClass().getResourceAsStream(pluginSchema), mode)))
            {
                System.out.println("Error: validatie met schema plugin pagina fout");
                return "redirect:/plugin";
            }

            setPlugins(session, pluginresponse, mode);
            return "redirect:/plugin";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    /**
     * Method that gets specific plugin.
     * @param session HttpSession of the request
     * @param pluginpos wanted plugin
     * @return the plugin in JSONObject
     */
    public static JSONObject getPlugin(HttpSession session, int pluginpos){
        JSONArray plugins = getPlugins(session);
        plugins.get(pluginpos);
        return (JSONObject) plugins.get(pluginpos);
    }

    /**
     * Method that gets the active plugins from the session.
     * @param session HttpSession of the request
     * @return active plugins
     */
    public static JSONArray getPlugins(HttpSession session){
        return new JSONArray(session.getAttribute("plugins").toString());
    }

    /**
     * Method that sets the plugins in the sesssion.
     * @param session HttpSession of the request
     * @param pluginresponse the plugin response
     * @param mode mode of the request
     */
    public static void setPlugins(HttpSession session, String pluginresponse, String mode){
        session.setAttribute("plugins", Converter.convertOutputToObject(pluginresponse, mode).get("data").toString());
    }

    /**
     * Method that checks what the status is of the plugins.
     * @param session HttpSession of the request
     * @param pluginPosition plugin
     * @return when plugin is inactive you get a forbidden page
     */
    public static String checkPluginPathState(HttpSession session , int pluginPosition){
        if (PluginController.getPlugin(session, pluginPosition).get("online").equals(Boolean.FALSE)
                || PluginController.getPlugin(session, pluginPosition).get("online").equals(0)) {
            return "redirect:/error-forbidden";
        }

        return null;
    }
}