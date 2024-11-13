package com.nhlstenden.swiftionparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = {"classpath:application.properties"})
@SpringBootApplication
public class SwiftionParserApplication {

    /**
     * Main method to start the application.
     * @param args arguments to start the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(SwiftionParserApplication.class, args);
    }

}
