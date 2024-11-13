package com.nhlstenden.swiftionwebapp.controllers.transaction;

import com.nhlstenden.swiftionwebapp.controllers.settings.PluginController;
import com.nhlstenden.swiftionwebapp.controllers.user.UserController;
import com.nhlstenden.swiftionwebapp.utility_classes.Converter;
import com.nhlstenden.swiftionwebapp.utility_classes.MessageHandler;
import com.nhlstenden.swiftionwebapp.utility_classes.RequestBuilder;
import com.nhlstenden.swiftionwebapp.utility_classes.Validate;
import jakarta.servlet.http.HttpSession;
import org.apache.catalina.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

/*****************************************************************
 * The TransactionController class is responsible for everything that
 * is related to the transactions of the application.
 ****************************************************************/
@Controller
@Validated
public class TransactionController {
    private Validate validate;

    @Autowired
    public TransactionController() {
        this.validate = new Validate();
    }

    /**
     * Shows the overview of all transactions.
     *
     * @param model model of request
     * @param session HttpSession of the request
     * @return overview of all transactions
     */
    @GetMapping("/transaction-overview")
    public String transactionOverview(Model model, HttpSession session) {
        getTransactionOverview(model, session);
        return "transaction-overview";
    }

    /**
     * Method that adds a new cost center.
     *
     * @param costCenter cost center
     * @param session HttpSession of the request
     * @return succes message if added successfully
     */
    @PostMapping("/transaction-overview/insert/cost-center")
    public String addCostCenter(@RequestParam("cost-center") String costCenter, HttpSession session) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 1);
        String userAllowed = UserController.checkUserPermission(session);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();
        if (costCenter.isEmpty()) {
            session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(mode,
                    "U mag geen lege kostenplaats toevoegen.", "failed"));
            return "redirect:/transaction-overview";
        }

        try {
            String response = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=insert-cost-center&params=%s", costCenter), mode, UserController.getPermissions(session).get("role").toString());
            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String currencySchemaSchema = mode.equalsIgnoreCase("json") ?
                        "/static/schemaModels/json/webappMessage.json"
                        : "/static/schemaModels/xml/webappMessage.xsd";
                if (!(validate.validateData(response, this.getClass().getResourceAsStream(currencySchemaSchema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, "Data validatie bij de voeg transactoe toe paging ging fout: costcenter", "failed"));
                    return "redirect:/transaction-overview";
                }
            }

            if (response.contains("Cost center already exists in the database.")) {
                session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                        mode, costCenter + " bestaat al", "failed"));
            } else {
                session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                        mode, costCenter + " is toegevoegd", "success"));
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return null;
        }

        return "redirect:/transaction-overview";
    }

    /**
     * Deletes a cost center
     *
     * @param costCenterId id of the cost center
     * @param name name of the cost center
     * @param session HttpSession of the request
     * @return success message if successfully deleted
     */
    @PostMapping("/transaction-overview/delete/cost-center/{id}")
    public String deleteCostCenter(@PathVariable("id") int costCenterId,
                                   @RequestParam("name") String name,
                                   HttpSession session
    ) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 0);
        String userAllowed = UserController.checkUserPermission(session);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            String response = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=delete-cost-center&params=%s", costCenterId), mode, UserController.getPermissions(session).get("role").toString());
            JSONObject obj = Converter.convertOutputToObject(response, mode);

            if (obj.get("status").equals("success")) {
                session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                        mode, name + " is succesvol verwijderd!", "success"));
            } else {
                session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                        mode, "Kan " + name + " niet verwijderen, er zijn gekoppelde transacties aanwezig.", "failed"));
            }

            return "redirect:/transaction-overview";
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return "redirect:/transaction-overview";
        }
    }

    /**
     * Shows the page where you can edit a custom transaction.
     * @param id id of custom transaction
     * @param model model of request
     * @param session HttpSession of the request
     * @return edit custom transaction page
     */
    @GetMapping("/edit-custom-transaction")
    public String editCustomTransaction(
            @RequestParam(value = "id", required = true, defaultValue = "0") int id,
            Model model,
            HttpSession session
    ) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 1);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        try {
            String mode = UserController.getPermissions(session).get("mode").toString();

            // Check if id is valid
            if (id == 0) {
                session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(
                        Validate.exception("errorGetIdCustomTransactions", mode), mode));
                return "redirect:/transaction-overview";
            }

            String customTransactionResponse = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=get-custom-transaction&params=%d", id), mode, UserController.getPermissions(session).get("role").toString());
            String costCenterResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-cost-centers", mode, UserController.getPermissions(session).get("role").toString());

            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String customTransactionSchema = mode.equalsIgnoreCase("json") ?
                        "/static/schemaModels/json/customTransactionSchema.json" :
                        "/static/schemaModels/xml/customTransactionSchema.xsd";

                if (!(validate.validateData(customTransactionResponse, this.getClass().getResourceAsStream(customTransactionSchema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateGetTransactions", mode), mode));
                    return "redirect:/transaction-overview";
                }

                String costCenterSchema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/costCenterSchema.json"
                        : "/static/schemaModels/xml/costCenterSchema.xsd";
                if (!(validate.validateData(costCenterResponse, this.getClass().getResourceAsStream(costCenterSchema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorCostCenterTransaction", mode), mode));
                    return "redirect:/transaction-overview";
                }
            }

            model.addAttribute("data", Converter.convertOutputToObject(customTransactionResponse, mode).get("data"));
            model.addAttribute("cost_center", Converter.convertOutputToObject(costCenterResponse, mode).get("data"));
            model.addAttribute("auth", UserController.getPermissions(session));

            MessageHandler.redirectMessageHandler(session, model, mode);
        } catch (Exception e) {
            System.err.printf("[*] select-custom-transaction GET handler failed to retrieve data from database : %s", e.getMessage());
            return null;
        }
        model.addAttribute("plugins", PluginController.getPlugins(session));
        return "edit-custom-transaction";
    }

    /**
     * Method that updates the custom transaction.
     * @param id id of custom transaction
     * @param date date of custom transaction
     * @param amount amount of custom transaction
     * @param costCenterId id of cost center of custom transaction
     * @param description description of custom transaction
     * @param model model of request
     * @param session HttpSession of the request
     * @return success message if successfully updated
     */
    @PostMapping("/edit-custom-transaction/update/{id}")
    public String editCustomTransaction(
            @PathVariable("id") int id,
            @RequestParam("payment-date") String date,
            @RequestParam("amount") double amount,
            @RequestParam("cost-center") int costCenterId,
            @RequestParam("description") String description,
            Model model,
            HttpSession session
    ) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 1);
        String userAllowed = UserController.checkUserPermission(session);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();
        try {
            description = description.replaceAll("\\s\\s", "").replaceAll(",", ";");
            String amountToString = String.format("%.2f", amount).replaceAll(",", ".");
            String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=update-custom-transaction&params=%d,%d,%s,%s,%s", id, costCenterId, date, amountToString, description), mode, UserController.getPermissions(session).get("role").toString());

            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String schema = mode.equalsIgnoreCase("json") ? "/static/schemaModels/json/webappMessage.json"
                        : "/static/schemaModels/xml/webappMessage.xsd";

                if (!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(
                            Validate.exception("errorValidateUpdateTransaction", mode), mode));

                    return "redirect:/transaction-overview";
                }
            }

            JSONObject data = Converter.convertOutputToObject(response, mode);
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, "Kasgeld transactie is gewijzigd", data.get("status").toString()));
                }
            }
        } catch (Exception e) {
            System.err.printf("[*] update-custom-transaction POST handler failed to update data in database : %s", e);
            return null;
        }

        model.addAttribute("auth", UserController.getPermissions(session));
        return String.format("redirect:/edit-custom-transaction?id=%d", id);
    }

    /**
     * Method that deletes a custom transaction.
     * @param id id of custom transaction
     * @param session HttpSession of the request
     * @param model model of request
     * @return success message if successfully deleted
     */
    @PostMapping("/edit-custom-transaction/delete/{id}")
    public String deleteCustomTransaction(@PathVariable("id") int id, HttpSession session, Model model) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 1);
        String userAllowed = UserController.checkUserPermission(session);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();

        try {
            String response = RequestBuilder.buildPostRequestDbAPI(
                    String.format("/api/database/procedure-parameter?name=delete-custom-transaction&params=%d", id), mode, UserController.getPermissions(session).get("role").toString());

            System.out.println(response);
            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String schema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/webappMessage.json"
                        : "/static/schemaModels/xml/webappMessage.xsd";
                System.out.println(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode));
                if (!(validate.validateData(response, this.getClass().getResourceAsStream(schema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateDeleteTransactions", mode), mode));
                    return "redirect:/transaction-overview";
                }
            }

            JSONObject data = Converter.convertOutputToObject(response, mode);
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, "Kasgeld transactie is verwijderd", data.get("status").toString()));
                }
            }
        } catch (Exception e) {
            System.err.printf("[*] delete-custom-transaction POST handler failed to update data in database : %s", e);
            return null;
        }

        return "redirect:/transaction-overview";
    }

    /**
     * Method that displays the add custom transaction page.
     * @param model model of request
     * @param session HttpSession of the request
     * @return the add custom transaction page
     */
    @GetMapping("/add-custom-transaction")
    public String addCustomTransaction(Model model, HttpSession session) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 1);
        String userAllowed = UserController.checkUserPermission(session);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();
        String costCenterResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-cost-centers", mode, UserController.getPermissions(session).get("role").toString());
        String currencyResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-currencies", mode, UserController.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
            String currencySchemaSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/currencySchema.json"
                    : "/static/schemaModels/xml/currencySchema.xsd";
            if (!(validate.validateData(currencyResponse, this.getClass().getResourceAsStream(currencySchemaSchema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddCustomTransaction", mode), mode));
                return "transaction-overview";
            }

            String costCenterSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/costCenterSchema.json"
                    : "/static/schemaModels/xml/costCenterSchema.xsd";
            if (!(validate.validateData(costCenterResponse, this.getClass().getResourceAsStream(costCenterSchema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddCustomTransaction", mode), mode));
                return "transaction-overview";
            }
        }


        try {
            model.addAttribute("cost_center", Converter.convertOutputToObject(costCenterResponse, mode).get("data"));
            model.addAttribute("currency", Converter.convertOutputToObject(currencyResponse, mode).get("data"));
            model.addAttribute("plugins", PluginController.getPlugins(session));
            model.addAttribute("auth", UserController.getPermissions(session));

            MessageHandler.redirectMessageHandler(session, model, mode);
            return "add-custom-transaction";
        } catch (Exception e) {
            System.err.printf("[*] add-custom-transaction GET handler failed to retrieve data from database : %s", e.getMessage());
            return null;
        }
    }

    /**
     * Method that adds a custom transaction to the application.
     * @param session HttpSession of the request
     * @param date date of custom transaction
     * @param currencyId currency id of custom transaction
     * @param amount amount of custom transaction
     * @param costCenterId cost center id of custom transaction
     * @param description description of custom transaction
     * @return success message if successfully added
     */
    @PostMapping("/add-custom-transaction/insert")
    public String insertCustomTransaction(HttpSession session,
                                          @RequestParam("payment-date") String date,
                                          @RequestParam("currency") int currencyId,
                                          @RequestParam(value = "amount", defaultValue = "0.00") double amount,
                                          @RequestParam(value = "cost-center",required = false, defaultValue = "1") int costCenterId,
                                          @RequestParam("description") String description) {
        // Check if plugin is allowed to be used and if user is allowed to use the path
        String pluginAllowed = PluginController.checkPluginPathState(session, 1);
        String userAllowed = UserController.checkUserPermission(session);

        if (pluginAllowed != null) {
            return pluginAllowed;
        }

        if (userAllowed != null) {
            return userAllowed;
        }

        String mode = UserController.getPermissions(session).get("mode").toString();

        if (description.isEmpty() || date.isEmpty() || amount < 0) {
            session.setAttribute("SESSION-MESSAGE", Validate.exception("errorEmptyFieldsAddTransaction", mode));
            return String.format("redirect:/add-custom-transaction");
        }

        try {
            description = description.replaceAll("\\s\\s", "").replaceAll(",", ";");
            String amountToString = String.format("%.2f", amount).replaceAll(",", ".");
            String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=insert-custom-transaction&params=%d,%d,%s,%s,%s", costCenterId, currencyId, date, amountToString, description), mode, UserController.getPermissions(session).get("role").toString());

            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                String currencySchemaSchema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/currencySchema.json"
                        : "/static/schemaModels/xml/currencySchema.xsd";
                if (!(validate.validateData(response, this.getClass().getResourceAsStream(currencySchemaSchema), mode))) {
                    session.setAttribute("SESSION-MESSAGE", Converter.convertOutputToObject(
                            Validate.exception("errorValidateAddTransaction", mode), mode));
                    return String.format("redirect:/transaction-overview");
                }
            }

            JSONObject data = Converter.convertOutputToObject(response, mode);
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, "Kasgeld transactie is toegevoegd!", data.get("status").toString()));
                    return String.format("redirect:/transaction-overview");
                }
            }
        } catch (Exception e) {
            System.err.printf("[*] add-custom-transaction POST handler failed to retrieve data from database : %s", e.getMessage());
            return null;
        }

        return "redirect:/transaction-overview";
    }

    /**
     * Method that shows a page where you can view a specific transaction.
     * @param id id of transaction
     * @param model model of request
     * @param session HttpSession of the request
     * @return view specific transaction
     * @throws IOException exception
     */
    @GetMapping("/view-transaction")
    public String viewTransaction(@RequestParam String id, Model model, HttpSession session) throws IOException {
        String mode = UserController.getPermissions(session).get("mode").toString();
        String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=get-transaction&params=%s", id), mode, UserController.getPermissions(session).get("role").toString());
        String costCenterResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-cost-centers", mode, UserController.getPermissions(session).get("role").toString());
        String creditDebitResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-card-types", mode, UserController.getPermissions(session).get("role").toString());

        if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {

            String creditDebitSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/creditDebitTransactionSchema.json"
                    : "/static/schemaModels/xml/creditDebitTransactionSchema.xsd";
            if (!(validate.validateData(creditDebitResponse, this.getClass().getResourceAsStream(creditDebitSchema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateGetTransactions", mode), mode));
                return "transaction-overview";
            }

            String costCenterSchema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/costCenterSchema.json"
                    : "/static/schemaModels/xml/costCenterSchema.xsd";
            if (!(validate.validateData(costCenterResponse, this.getClass().getResourceAsStream(costCenterSchema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateAddTransaction", mode), mode));
                return "transaction-overview";
            }

            String transactionschema = mode.equalsIgnoreCase("json")
                    ? "/static/schemaModels/json/viewTransactionSchema.json"
                    : "/static/schemaModels/xml/viewTransactionSchema.xsd";
            if (!(validate.validateData(response, this.getClass().getResourceAsStream(transactionschema), mode))) {
                model.addAttribute("messageHandling", Converter.convertOutputToObject(
                        Validate.exception("errorValidateGetTransactions", mode), mode));
                return "transaction-overview";
            }

        }

        try {
            model.addAttribute("data", Converter.convertOutputToObject(response, mode).getJSONArray("data"));
            model.addAttribute("cost_center", Converter.convertOutputToObject(costCenterResponse, mode).get("data"));
            model.addAttribute("transaction_types", Converter.convertOutputToObject(creditDebitResponse, mode).getJSONArray("data"));
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

        model.addAttribute("auth", UserController.getPermissions(session));
        model.addAttribute("plugins", PluginController.getPlugins(session));

        MessageHandler.redirectMessageHandler(session, model, mode);
        return "view-transaction";
    }

    /**
     * Method that updates the cost center and/or custom description of a transaction.
     * @param id id of transaction
     * @param costCenter cost center of transaction
     * @param customDescription custom description of transaction
     * @param model model of request
     * @param session HttpSession of the request
     * @return success message if successfully updated
     */
    @PostMapping("/view-transaction/update/{id}")
    public String updateTransaction(
            @PathVariable("id") int id,
            @RequestParam(value = "cost-center", defaultValue = "1", required = false) int costCenter,
            @RequestParam("own-description") String customDescription,
            Model model,
            HttpSession session
    ) {
        String mode = UserController.getPermissions(session).get("mode").toString();

        try {
            String response = RequestBuilder.buildPostRequestDbAPI(String.format("/api/database/procedure-parameter?name=update-transaction&params=%d,%d,%s", id, costCenter, customDescription), mode, UserController.getPermissions(session).get("role").toString());
            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {
                System.out.println("update" + response);
                String transactionschema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/webappMessage.json"
                        : "/static/schemaModels/xml/webappMessage.xsd";
                if (!(validate.validateData(response, this.getClass().getResourceAsStream(transactionschema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateGetTransactions", mode), mode));
                    return String.format("redirect:/view-transaction?id=%d", id);
                }
            }
            JSONObject obj = null;
            //messageHandling
            JSONObject data = Converter.convertOutputToObject(response, mode);
            if (data.has("status")) {
                if (data.get("status").equals("success")) {
                    session.setAttribute("SESSION-MESSAGE", Converter.buildReturnMessage(
                            mode, "De transactie is succescol geüpdatet", data.get("status").toString()));
                    return String.format("redirect:/view-transaction?id=%d", id);
                }
            }
        } catch (Exception e) {
            System.err.println(e);
            return null;
        }

        model.addAttribute("auth", UserController.getPermissions(session));
        return "redirect:/view-transaction?id=" + id;
    }

    /**
     * Method that gets all the MT940 & custom transactions.
     * @param model model of request
     * @param session HttpSession of the request
     * @return all MT940 & custom transactions
     */
    private Model getTransactionOverview(Model model, HttpSession session) {
        String mode = UserController.getPermissions(session).get("mode").toString();

        try {
            String customTransactionsResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-custom-transactions", mode, UserController.getPermissions(session).get("role").toString());
            String mt940TransactionsResponse = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-mt940-transactions", mode, UserController.getPermissions(session).get("role").toString());
            String costCenterMt940Transactions = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-cost-center-mt940-transaction", mode, UserController.getPermissions(session).get("role").toString());
            String costCenterCustomTransaction = RequestBuilder.buildPostRequestDbAPI("/api/database/procedure?name=get-cost-center-custom-transaction", mode, UserController.getPermissions(session).get("role").toString());

            if (mode.equalsIgnoreCase("json") || mode.equalsIgnoreCase("xml")) {

                String customtransactionschema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/getCustomTransactionSchema.json"
                        : "/static/schemaModels/xml/getCustomTransactionsSchema.xsd";
                if (!(validate.validateData(customTransactionsResponse, this.getClass().getResourceAsStream(customtransactionschema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateGetCustomTransactions", mode), mode));
                    return model;
                }

                String costCenterMt940TransactionsSchema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/getTransactionsSchema.json"
                        : "/static/schemaModels/xml/getTransactionsSchema.xsd";
                if (!(validate.validateData(mt940TransactionsResponse, this.getClass().getResourceAsStream(costCenterMt940TransactionsSchema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateGetTransactions", mode), mode));
                    return model;
                }

                String mt940TransactionsSchema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/getTransactionsSchema.json"
                        : "/static/schemaModels/xml/costCenterMT940Transactions.xsd";
                if (!(validate.validateData(costCenterMt940Transactions, this.getClass().getResourceAsStream(mt940TransactionsSchema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateGetTransactions", mode), mode));
                    return model;
                }

                String costCenterCustomTransactionSchema = mode.equalsIgnoreCase("json")
                        ? "/static/schemaModels/json/getTransactionsSchema.json"
                        : "/static/schemaModels/xml/costCentercustomTransactions.xsd";
                if (!(validate.validateData(costCenterCustomTransaction, this.getClass().getResourceAsStream(costCenterCustomTransactionSchema), mode))) {
                    model.addAttribute("messageHandling", Converter.convertOutputToObject(
                            Validate.exception("errorValidateGetTransactions", mode), mode));
                    return model;
                }
            }
            model.addAttribute("custom_transaction", Converter.convertOutputToObject(customTransactionsResponse, mode).get("data"));
            model.addAttribute("mt940_transaction", Converter.convertOutputToObject(mt940TransactionsResponse, mode).get("data"));
            model.addAttribute("cost_center_mt940", Converter.convertOutputToObject(costCenterMt940Transactions, mode).get("data"));
            model.addAttribute("cost_center_custom_transaction", Converter.convertOutputToObject(costCenterCustomTransaction, mode).get("data"));
        } catch (Exception e) {
            System.err.printf("[*] GET transaction-overiew handler failed to retrieve data from database : %s", e.getMessage());
        }

        MessageHandler.redirectMessageHandler(session, model, mode);

        model.addAttribute("auth", UserController.getPermissions(session));
        model.addAttribute("plugins", PluginController.getPlugins(session));
        return model;
    }
}