package gr.aueb.dmst.dockerWatchdog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The main entry point for the Spring Boot application.
 * From here, the application will search for the other
 * Spring Boot components like Entities, Services and Controllers
 * and use them so to launch. This will start when Main.java is executed
 * so user can use the application with one click.
 */
@SpringBootApplication
public class WebApp {

    /**
     * The main method which will be used to launch the Spring Boot application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }
}
