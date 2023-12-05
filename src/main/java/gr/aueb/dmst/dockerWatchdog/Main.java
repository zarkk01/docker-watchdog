package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.ArrayList;

public class Main {

    // Initiate myInstancesList and myImagesList
    public static ArrayList<MyInstance> myInstancesList = new ArrayList<>();
    public static ArrayList<MyImage> myImagesList = new ArrayList<>();

    // Initiate dockerClient
    static String dockerHost = "tcp://localhost:2375"; // Replace with your Docker daemon's host and port

    // Build the Docker client configuration for TCP
    static DefaultDockerClientConfig.Builder configBuilder = DefaultDockerClientConfig.createDefaultConfigBuilder()
            .withDockerHost(dockerHost);

    // Use configBuilder to build the Docker client
    public static DockerClient dockerClient = DockerClientBuilder.getInstance(configBuilder.build()).build();

    public static void main(String[] args) {

        try {
            // Calling liveMeasure so to keep track of CPU Usage, Memory Usage, Block I/O, and PIDs
            DockerLiveMetrics.liveMeasure();

            // Initiate and start monitorThread
            MonitorThread dockerMonitor = new MonitorThread();
            Thread monitorThread = new Thread(dockerMonitor);
            monitorThread.start();

            // Initiate and start executorThread
            ExecutorThread dockerExecutor = new ExecutorThread();
            Thread executorThread = new Thread(dockerExecutor);
            executorThread.start();
        } catch (Exception e) {
            // Handle exceptions here
            e.printStackTrace();
        }
    }
}
