package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MonitorThread implements Runnable {

    //Initiate running variable true
    private volatile boolean running = true;

    // Config builder and create dockerClient
    DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
    DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

    @Override
    public void run() {
        while (running) {

            // Calling monitorCluster so to show the current cluster situation
            monitorCluster();

            // Sleep for a specified interval (e.g., 20 sec) and then repeat if running
            try {
                Thread.sleep(20000); // 20 sec
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void monitorCluster() {

        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        // Initiate containers list with existing containers.
        List<Container> containers;
        containers = dockerClient.listContainersCmd().withShowAll(true).exec();

        // Declaring the variables
        int totalContainers = 0;
        Set<String> uniqueImages = new HashSet<>();
        int runningContainers = 0;
        int stoppedContainers = 0;

        // Print container information
        for (Container container : containers) {
            System.out.println("Container ID: " + container.getId());
            System.out.println("Container Name: " + container.getNames()[0]);
            System.out.println("Container Image: " + container.getImage());
            System.out.println("Container Status: " + container.getStatus());
            System.out.println("---------------");

            // Incrementing totalContainers
            totalContainers++;

            // Adding unique images to the hash set
            String containerImageId = container.getImageId();
            uniqueImages.add(containerImageId);

            // Check if the current container is running or not
            boolean isRunning = Boolean.TRUE.equals(dockerClient.inspectContainerCmd(container.getId()).exec().getState().getRunning());
            if (isRunning) {
                // Incrementing runningContainers
                runningContainers++;
            } else {
                // Incrementing stoppedContainers
                stoppedContainers++;
            }
        }

        // Showing what we found
        System.out.println("We have " + totalContainers + " containers and from them, " +
                runningContainers + " running, while " +
                stoppedContainers + " stopped." +
                "The total unique images count is " + uniqueImages.size() + ".");

        // Letting user waiting
        System.out.println("Next update in about 20 seconds...");

    }

    public void stopMonitoring() {

        // Thread is about to stop, so while loop in run method should stop
        running = false;

        // Closing dockerClient to prevent resource leaks
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
