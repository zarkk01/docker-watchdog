package gr.aueb.dmst.dockerWatchdog;

import java.util.ArrayList;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

public class Main
{
    public static ArrayList<Instance> instancesList = new ArrayList<>();
    public static ArrayList<Img> imagesList = new ArrayList<>();

    // Config builder and create dockerClient
    public static DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
    public static DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

    public static void main( String[] args )
    {
        // Initiate and start monitorThread
        MonitorThread dockerMonitor = new MonitorThread();
        Thread monitorThread = new Thread(dockerMonitor);
        monitorThread.start();

        // Allow the monitoring to run for a while
//        try {
//            Thread.sleep(30000); // 0.5 minute
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        // Stop the monitoring thread - stop updates
        //dockerMonitor.stopMonitoring();
    }
}