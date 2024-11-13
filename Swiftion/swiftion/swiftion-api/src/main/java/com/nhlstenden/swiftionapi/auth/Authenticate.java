package com.nhlstenden.swiftionapi.auth;

import io.github.cdimascio.dotenv.Dotenv;

/*****************************************************************
 * Authenticate class is the class that makes sure a request holds
 * a valid API key before access to the queries is given
 ****************************************************************/
public class Authenticate {
    private final Dotenv env;
    private final String WEB_APP_APIKEY;

    public Authenticate() {
        this.env = Dotenv.load();
        this.WEB_APP_APIKEY = env.get("API_TOKEN");
    }

    /**
     * checks if the api token that was send in a request, equals the apikey that is saved in the .env file
     * @param apiKey key to be checked
     * @return true if it is a match, otherwise false
     */
    public boolean checkToken(String apiKey) {
        return apiKey.equals(this.WEB_APP_APIKEY);
    }

}
