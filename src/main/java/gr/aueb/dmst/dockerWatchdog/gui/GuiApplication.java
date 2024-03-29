package gr.aueb.dmst.dockerWatchdog.gui;

import java.io.IOException;
import java.net.http.HttpClient;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;


/**
 * This class represents the main application window for the Docker Watchdog application.
 * It initializes an HTTP client and sets up the primary stage of the JavaFX application.
 */
public class GuiApplication extends Application {
    // HttpClient instance used for making HTTP requests to our REST API.
    public static HttpClient client;

    /**
     * Constructor for the GuiApplication class.
     * Initializes a new HttpClient.
     */
    public GuiApplication() {
        client = HttpClient.newHttpClient();
    }

    /**
     * The main entry point for our JavaFX application.
     * The start method is called after the SpringBoot app has completed the initialization
     * and after the system is ready for the application to begin running.
     *
     * @param stage the primary stage for this application, onto which
     *              the containers scene is set.
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Load the FXML file for the containers panel which is the main panel
        FXMLLoader fxmlLoader = new FXMLLoader(GuiApplication.class.getResource("/containersScene.fxml"));

        // Create the containers' scene
        Scene scene = new Scene(fxmlLoader.load(),1100,700);

        //Set the icon of the application to watchdogRed logo
        Image image = new Image("/images/watchdogRed.png");
        stage.getIcons().add(image);

        // Set the scene on the stage
        stage.setScene(scene);

        // Set the title of the window
        stage.setTitle("Docker Watchdog");

        // Prevent the window from being resized so to maintain full control of how the GUI looks
        stage.setResizable(false);

        // Show the stage
        stage.show();

        // When the window is closed, exit the application
        stage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });
    }
}