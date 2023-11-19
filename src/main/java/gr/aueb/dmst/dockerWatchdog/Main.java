package gr.aueb.dmst.dockerWatchdog;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.io.IOException;
import java.util. List;
public class Main
{
    public static void main( String[] args )
    {
        // Set the root logger level to INFO to reduce the amount of logging output
        ((ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.INFO);

        // Just for debugging reason.
        System.out.println( "Starting " );

        //Config builder and create dockerClient
        DefaultDockerClientConfig.Builder builder = DefaultDockerClientConfig.createDefaultConfigBuilder();
        DockerClient dockerClient = DockerClientBuilder.getInstance(builder).build();

        //No need for them.
        //builder.withDockerHost("tcp://Localhost:2375");
        //builder.withDockerCertPath("/Users/zark/.docker");

        //dockerClient.versionCmd().exec();

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

            //Adding unique images to the hash set
            String containerImageId = container.getImageId();
            uniqueImages.add(containerImageId);

            // Check if the current container is running or not
            boolean isRunning = Boolean.TRUE.equals(dockerClient.inspectContainerCmd(container.getId()).exec().getState().getRunning());
            if(isRunning){
                // Incrementing runningContainers
                runningContainers++;
            } else {
                // Incrementing stoppedContainers
                stoppedContainers++;
            }
        }

        //Closing dockerClient to prevent resource leaks
        try {
            dockerClient.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Showing what we found
        System.out.println("We have " + totalContainers + " containers and from them, " +
                runningContainers + " running, while " +
                stoppedContainers + " stopped." +
                "The total unique images count to " + uniqueImages.size() + ".");
    }
}
