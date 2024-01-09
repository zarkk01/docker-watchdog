package gr.aueb.dmst.dockerWatchdog;

import java.util.ArrayList;
import javafx.application.Application;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;
import gr.aueb.dmst.dockerWatchdog.Threads.DatabaseThread;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Threads.MonitorThread;

import org.springframework.boot.SpringApplication;

/**
 * The main class of the application.
 * It initializes the Docker client and lists for instances, images, and volumes,
 * Also, starts the necessary threads for monitoring Docker events,
 * executing actions on Docker Components, and database operations.
 * Lastly, launches the JavaFX application and the Spring Boot application
 * which supports our REST API.
 */
public class Main {

    /**
     * Configuration for the Docker client.
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
     * Lists for Docker containers so that we can keep track of them
     * and then store them in the database.
     */
    public static ArrayList<MyInstance> myInstances = new ArrayList<>();

    /**
     * Lists for Docker images so that we can keep track of them
     * and then store them in the database.
     */
    public static ArrayList<MyImage> myImages = new ArrayList<>();

    /**
     * Lists for Docker volumes so that we can keep track of them
     * and then store them in the database.
     */
    public static ArrayList<MyVolume> myVolumes = new ArrayList<>();

    /**
     * Main method of the application.
     * It starts the Spring Boot application, the JavaFX application,
     * and the threads for monitoring Docker events, executing actions on Docker Components,
     * and database operations. It first starts the Spring Boot application so that
     * the REST API is available before the JavaFX application starts.
     * @param args command line arguments
     */
    public static void main(String[] args) {

        try {

            // Start Spring Boot application
            SpringApplication.run(WebApp.class, args);

            // Start threads in a specific order so to prevent errors
            Thread newMonitorThread = new Thread(new MonitorThread());
            newMonitorThread.start();

            Thread executorThread = new Thread(new ExecutorThread());
            executorThread.start();

            Thread dbThread = new Thread(new DatabaseThread());
            dbThread.start();

            // Start JavaFX application and GUI displays
            Application.launch(DesktopApp.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
