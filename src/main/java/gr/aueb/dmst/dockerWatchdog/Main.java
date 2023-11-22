package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.ArrayList;

public class Main {

    public static ArrayList<Instance> instancesList = new ArrayList<>();
    public static ArrayList<MyImage> myimagesList = new ArrayList<>();

    public static DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
    public static DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

    public static void main(String[] args) {

//      Initiate and start monitorThread
        MonitorThread dockerMonitor = new MonitorThread();
        Thread monitorThread = new Thread(dockerMonitor);
        monitorThread.start();

        ExecutorThread dockerExecutor = new ExecutorThread();
        Thread executorThread = new Thread(dockerExecutor);
        executorThread.start();

    }
}