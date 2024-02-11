package gr.aueb.dmst.dockerWatchdog;

import java.util.ArrayList;

import javafx.application.Application;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import gr.aueb.dmst.dockerWatchdog.gui.GUIApplication;
import gr.aueb.dmst.dockerWatchdog.models.MyImage;
import gr.aueb.dmst.dockerWatchdog.models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.models.MyVolume;
import gr.aueb.dmst.dockerWatchdog.threads.DatabaseThread;
import gr.aueb.dmst.dockerWatchdog.threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.threads.MonitorThread;


/**
 * The main class of the application.
 * It initializes the Docker client and lists for instances, images, and volumes,
 * Also, starts the necessary threads for monitoring Docker events,
 * executing actions on Docker Components, and database operations.
 * Lastly, launches the JavaFX application and the Spring Boot application
 * which supports our REST API.
 */
public class Main {

    // Logger instance used mainly for errors
    private static final Logger logger = LogManager.getLogger(Main.class);

    /**
     * Configuration for the Docker client
     */
    private static final DefaultDockerClientConfig builder =
            DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build();

    /**
     * Instance of the Docker client which is used to
     * communicate with the Docker daemon.
     */
    public static final DockerClient dockerClient =
            DockerClientBuilder.getInstance(builder).build();

    /**
     * List for Docker containers so that we can keep track of them
     * and then store them in the database.
     */
    public static ArrayList<MyInstance> myInstances = new ArrayList<>();

    /**
     * List for Docker images so that we can keep track of them
     * and then store them in the database.
     */
    public static ArrayList<MyImage> myImages = new ArrayList<>();

    /**
     * List for Docker volumes so that we can keep track of them
     * and then store them in the database.
     */
    public static ArrayList<MyVolume> myVolumes = new ArrayList<>();

    /**
     * The main entry point of the application and responsible for initializing and starting all the necessary components
     * It starts the threads for monitoring Docker events, executing actions on Docker components, and performing database operations.
     * It also starts the Spring Boot application to make the REST API available before the launch of the JavaFX application for the GUI.
     * If any exception occurs during the startup process, it is caught and logged.
     *
     * @param args command line arguments passed to the application
     */
    public static void main(String[] args) {

        try {
            // Start all three Threads for monitoring, executing, and database operations
            Thread monitorThread = new Thread(new MonitorThread());
            monitorThread.start();
            Thread executorThread = new Thread(new ExecutorThread());
            executorThread.start();
            Thread databaseThread = new Thread(new DatabaseThread());
            databaseThread.start();

            // Start Spring Boot application and WATCHDOG REST API
            SpringApplication.run(WebApp.class, args);

            // Start JavaFX application and GUI displays
            Application.launch(GUIApplication.class, args);
        } catch (Exception e) {
            // Log any exception that occurs during the startup process
            logger.error("An error occurred during the startup process: " + e.getMessage());
        }
    }
}
