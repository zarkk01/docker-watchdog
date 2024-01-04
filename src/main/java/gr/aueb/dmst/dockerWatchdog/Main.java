package gr.aueb.dmst.dockerWatchdog;

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
import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ArrayList;

public class Main {

    public static ConfigurableApplicationContext springContext;

    // Initiate myInstancesList and myImagesList
    public static ArrayList<MyInstance> myInstancesList = new ArrayList<>();
    public static ArrayList<MyImage> myImagesList = new ArrayList<>();
    public static ArrayList<MyVolume> myVolumesList = new ArrayList<>();

    // Initiate dockerClient
    public static DefaultDockerClientConfig builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
            //          .withDockerHost("tcp://localhost:2375") // Use "tcp" for TCP connections
            .build();
    public static DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

    //Initiate dbThread
    public static Thread dbThread = new Thread(new DatabaseThread());

    public static void main(String[] args) {

        try {

            springContext = SpringApplication.run(WebApp.class, args);

            // Initiate and start newMonitorThread
            MonitorThread newDockerMonitor = new MonitorThread();
            Thread newMonitorThread = new Thread(newDockerMonitor);
            newMonitorThread.start();

            //Process finished with exit code 1

            // Initiate and start executorThread
            ExecutorThread dockerExecutor = new ExecutorThread();
            Thread executorThread = new Thread(dockerExecutor);
            executorThread.start();

            // start dbThread
            dbThread.start();

            new Thread(DatabaseThread::updateLiveMetcrics).start();

            Application.launch(DesktopApp.class, args);

        } catch (Exception e) {
            // Handle exceptions here
            e.printStackTrace();
        }
    }
}
