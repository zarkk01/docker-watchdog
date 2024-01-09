package gr.aueb.dmst.dockerWatchdog;

import java.util.ArrayList;
import javafx.application.Application;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import org.springframework.boot.SpringApplication;

import gr.aueb.dmst.dockerWatchdog.Application.DesktopApp;
import gr.aueb.dmst.dockerWatchdog.Models.MyImage;
import gr.aueb.dmst.dockerWatchdog.Models.MyInstance;
import gr.aueb.dmst.dockerWatchdog.Models.MyVolume;
import gr.aueb.dmst.dockerWatchdog.Threads.DatabaseThread;
import gr.aueb.dmst.dockerWatchdog.Threads.ExecutorThread;
import gr.aueb.dmst.dockerWatchdog.Threads.MonitorThread;

public class Main {
    private static final DefaultDockerClientConfig builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .build();
    public final static DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();
    public static ArrayList<MyInstance> myInstances = new ArrayList<>();
    public static ArrayList<MyImage> myImages = new ArrayList<>();
    public static ArrayList<MyVolume> myVolumes = new ArrayList<>();
    public static Thread dbThread = new Thread(new DatabaseThread());

    public static void main(String[] args) {

        try {

            SpringApplication.run(WebApp.class, args);

            MonitorThread newDockerMonitor = new MonitorThread();
            Thread newMonitorThread = new Thread(newDockerMonitor);
            newMonitorThread.start();

            ExecutorThread dockerExecutor = new ExecutorThread();
            Thread executorThread = new Thread(dockerExecutor);
            executorThread.start();

            dbThread.start();

            new Thread(DatabaseThread::updateLiveMetcrics).start();

            Application.launch(DesktopApp.class, args);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
