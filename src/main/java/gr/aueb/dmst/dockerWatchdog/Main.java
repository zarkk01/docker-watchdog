package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.ArrayList;

public class Main {

    public static ArrayList<MyInstance> myInstancesList = new ArrayList<>();
    public static ArrayList<MyImage> myImagesList = new ArrayList<>();

    public static DefaultDockerClientConfig builder = DefaultDockerClientConfig.createDefaultConfigBuilder()
//          .withDockerHost("tcp://localhost:2375") // Use "tcp" for TCP connections
            .build();
    public static DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();


    public static void main(String[] args) {

        // Initiate and start monitorThread
        MonitorThread dockerMonitor = new MonitorThread();
        Thread monitorThread = new Thread(dockerMonitor);
        monitorThread.start();

        DockerLiveMetrics.liveMeasure();

        // Initiate and start executorThread
        ExecutorThread dockerExecutor = new ExecutorThread();
        Thread executorThread = new Thread(dockerExecutor);
        executorThread.start();

    }
}